import java.io.*;
import java.nio.ByteBuffer;

/**
 * used to handle messages that are received
 *
 */
public class MessageHandler implements Runnable
{
	private int connectedToID = -1;
	private boolean isChoked = true;
	private ClientConnection myClient = null;
	private DataInputStream dis = null;
	private Connection myConnection;
	private BitMap myBitMap;
	private volatile boolean requestSenderStarted = false;
	WriteLog w = new WriteLog();
	private int myID;
	private long start_Download;
	private long stop_Download;

	/**
	 * messageHandler called from client using client and connection as parameters
	 * @param aClient
	 * @param myConnection
	 * @throws IOException
	 */
	public MessageHandler(ClientConnection aClient, Connection myConnection) throws IOException
	{
		this.myConnection = myConnection;
		this.myBitMap = myConnection.getBitMap();
		this.myClient = aClient;
		this.dis = new DataInputStream(new PipedInputStream(aClient.getPipedOutputStream()));
		this.myID = myConnection.getMyPeerID();      
	}

	@Override
	public void run()
	{
		try
		{
			this.sendHandshake(myID);
			//this.processHandshake(this.receiveHandshake());
			myClient.setSoTimeout();            
			(new Thread(myClient)).start();
			this.processData();
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		} 
	}
/*
	private HandshakeMessage receiveHandshake() throws IOException
	{
		myClient.receive(32);
		byte[] handshakeMsg = new byte[32];
		dis.readFully(handshakeMsg);
		return new HandshakeMessage(handshakeMsg);
	}
*/

	private void sendHandshake(int myPeerID) throws IOException, InterruptedException
	{
		Message msg = new HandshakeMessage(myPeerID);
		myClient.send(msg.getFullMessage());
		myClient.receive(32);
		byte[] handshakeMsg = new byte[32];
		dis.readFully(handshakeMsg);
		HandshakeMessage HSmessage = new HandshakeMessage(handshakeMsg);
		byte [] msgBytes = HSmessage.getFullMessage();
		byte [] msgHeader = new byte[18];
		System.arraycopy(msgBytes, 0, msgHeader, 0, 18);
		this.connectedToID = HSmessage.getPeerID();
		this.myConnection.reportNewClientConnection(this.connectedToID, myClient);
		w.ReceivedHandshake(myID, connectedToID);
		if(myBitMap == null){
			System.out.println("My file is NULL"); 
		}
		if(myBitMap.doIHaveAnyPiece())
		{
			myClient.send(new ActualMessage(5,myBitMap.getMyFileBitMap()).getFullMessage());
		}
	}

	private void processData() throws IOException, InterruptedException
	{        
		//now always receive bytes and take action
		while(true)
		{
			if(myBitMap.canIQuit()){
				myConnection.QuitProcess();
				break;
			}
			Message msg = getNextMessage();
			//now interpret the message and take action

			int payloadLength = msg.getMsgLength() - 1;  //removing  the size of message type
			// System.out.println("msg.getMsgTypeValue()" + msg.getMsgTypeValue());
			if(msg.getMsgType().equals("ChokeMessage"))
				processChokeMessage();

			else if(msg.getMsgType().equals("UnchokedMessage"))
				processUnchokeMessage();

			else if(msg.getMsgType().equals("InterestedMessage"))
				processInterestedMessage();

			else if(msg.getMsgType().equals("NotInterestedMessage"))
				processNotInterestedMessage();

			else if(msg.getMsgType().equals("HaveMessage"))
				processHaveMessage();

			else if(msg.getMsgType().equals("BitfieldMessage"))
				processBitfieldMessage(payloadLength);

			else if(msg.getMsgType().equals("RequestMessage"))
				processRequestMessage();

			else if(msg.getMsgType().equals("PieceMessage"))
				processPieceMessage(payloadLength);

			else
				System.out.println("No such type of Message");

		}
	}

	/**
	 * receives message and processes it if msg type value is 5
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private int readIndex() throws IOException, InterruptedException
	{
		byte[] index = new byte[4];
		dis.readFully(index);
		ByteBuffer buffer = ByteBuffer.wrap(index,0,4);
		return buffer.getInt();
	}
	private byte[] readPayload(int length) throws IOException, InterruptedException
	{
		byte[] data = new byte[length];
		dis.readFully(data);
		return data;
	}

	private void processBitfieldMessage(int msgLength) throws IOException, InterruptedException
	{

		byte[] BitMap = readPayload(msgLength);
		myBitMap.setPeerBitMap(connectedToID, BitMap);
		if(myBitMap.hasInterestingPiece(connectedToID))
		{
			ActualMessage i = new ActualMessage(2);

			myClient.send(i);
		}
		else
		{
			myClient.send((new ActualMessage(3)));
		}
	}

	/**
	 * process a piece message
	 * checks if choked or unchoked and responds appropriately
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void processPieceMessage(int msgLength) throws IOException, InterruptedException
	{
		/*
		byte[] pieceBuffer = new byte[4];
		dis.readFully(pieceBuffer);
		int pieceIndex = Utilities.getIntFromByte(pieceBuffer, 0);
		byte[] pieceData = new byte[msgLength - 4];  
		dis.readFully(pieceData);
		*/
		int pieceIndex = readIndex();
		byte[] pieceData = readPayload(msgLength-4);

		stop_Download = System.currentTimeMillis();
		myConnection.addOrUpdatedownloadrate_peer(connectedToID, (stop_Download - start_Download));
		myBitMap.reportPieceReceived(pieceIndex, pieceData);
		w.PieceDownload(Integer.toString(myID), Integer.toString(connectedToID), pieceIndex, myBitMap.getDownloadedPieceCount(myID));

		if (myBitMap.getDownloadedPieceCount(myID) == myBitMap.getTotalPieceCount())
		{
			w.DownloadComplete(Integer.toString(myID));
			myConnection.sendGroupMessage(myConnection.getconnectedPeersList(), new ActualMessage(3).getFullMessage());
		}

		if(!isChoked)
		{
			int desiredPiece = myBitMap.getPeerPieceIndex(connectedToID);
			if(desiredPiece != -1)
			{
				myClient.send((new ActualMessage(6,desiredPiece)).getFullMessage());
			}
		}

		myConnection.sendGroupMessage(myConnection.getconnectedPeersList(), (new ActualMessage(4,pieceIndex)).getFullMessage());
		myConnection.sendGroupMessage(myConnection.computeAndGetWastePeersList(), new ActualMessage(3).getFullMessage());
	}

	/**
	 * request message sent for the required pirece index
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void processRequestMessage() throws IOException, InterruptedException
	{        
		//byte[] indexBuffer = new byte[4];
		//dis.readFully(indexBuffer);
		int pieceIndex = readIndex();
		byte[] dataForPiece = myBitMap.getPieceData(pieceIndex);
		// myClient.send((new PieceMessage(pieceIndex, dataForPiece)).getFullMessage());
		myClient.send((new ActualMessage(7, pieceIndex, dataForPiece)).getFullMessage());
	}

	private void processHaveMessage() throws IOException, InterruptedException
	{
		/*
		byte[] payload = new byte[msgLength];
		dis.readFully(payload);
		int pieceIndex = Utilities.getIntFromByte(payload, 0);
		*/
		int pieceIndex = readIndex();
		w.Have(Integer.toString(myID), Integer.toString(connectedToID),pieceIndex );

		myBitMap.reportPeerPieceAvailablity(connectedToID, pieceIndex);
		if(!myBitMap.doIHavePiece(pieceIndex))
		{
			myConnection.reportInterestedPeer(connectedToID);
			myClient.send((new ActualMessage(2)).getFullMessage());
		}
	}

	private void processNotInterestedMessage()
	{
		w.NotInterested(Integer.toString(myID), Integer.toString(connectedToID));
		myConnection.reportNotInterestedPeer(connectedToID);
	}

	private void processInterestedMessage()
	{
		w.Interested(Integer.toString(myID), Integer.toString(connectedToID));
		myConnection.reportInterestedPeer(connectedToID);
	}

	private void processUnchokeMessage() throws IOException, InterruptedException
	{
		w.Unchoked(Integer.toString(myID), Integer.toString(connectedToID));
		this.isChoked = false;

		if(!requestSenderStarted)
		{
			(new Thread(new RequestMessageProcessor())).start();
			this.requestSenderStarted  = true;
		}
	}

	private void processChokeMessage()
	{	/*
		if(myBitMap.canIQuit()){
			System.out.println("quit from message handler");
			this.myConnection.QuitProcess();
		}
		*/
		w.Choked(Integer.toString(myID), Integer.toString(connectedToID));
		this.isChoked = true;
		myConnection.resetdownloadrate_peer(connectedToID);
	}
/*
	private void processHandshake(HandshakeMessage handshakeMsg) throws IOException, InterruptedException
	{
		byte [] msgBytes = handshakeMsg.getFullMessage();
		byte [] msgHeader = new byte[18];
		System.arraycopy(msgBytes, 0, msgHeader, 0, 18);
		this.connectedToID = handshakeMsg.getPeerID();
		this.myConnection.reportNewClientConnection(this.connectedToID, myClient);
		w.ReceivedHandshake(myID, connectedToID);
		if(myBitMap == null){
			System.out.println("My file is NULL"); 
		}
		if(myBitMap.doIHaveAnyPiece())
		{
			myClient.send(new ActualMessage(5,myBitMap.getMyFileBitMap()).getFullMessage());
		}
	}
*/
	private Message getNextMessage() throws IOException, InterruptedException
	{
		/*
		if(myBitMap.canIQuit())
			this.myConnection.QuitProcess();
		*/
		byte[] lengthBuffer = new byte[4];
		try{
            dis.readFully(lengthBuffer);
		}
		catch(Exception e){
                // System.out.println("Connection closed");
        }

        ByteBuffer bf = ByteBuffer.wrap(lengthBuffer,0,4);
        int msgLength = bf.getInt();
        
        
		byte[] msgType = new byte[1];
		
        try{
            dis.readFully(msgType);
		}
        catch(Exception e){}
        Message m = new Message(msgType[0],msgLength);
		return m;
	}

	class RequestMessageProcessor implements Runnable
	{
		private void sendRequestMessage() throws IOException, InterruptedException
		{
			int desiredPiece = myBitMap.getPeerPieceIndex(connectedToID);
			if(desiredPiece != -1)
			{
				myClient.send((new ActualMessage(6,desiredPiece)));
			}
		}

		@Override
		public void run()
		{
			while(true){
				if(myBitMap.canIQuit()){
					try{
						// Thread.sleep(1000);
						break;
					}
					catch (Exception e)
					{
						break;//e.printStackTrace();
					}
				}
				try
				{
					this.sendRequestMessage();
					Thread.sleep(5);
				} 
				catch (Exception e)
				{
					break;//e.printStackTrace();
				} 

			}
			/*
			while(! myBitMap.canIQuit())
			{
				try
				{
					this.sendRequestMessage();
					Thread.sleep(5);
				} catch (Exception e)
				{
					e.printStackTrace();
				} 
			}
			*/
		}
	}
}

