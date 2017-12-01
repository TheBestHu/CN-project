import java.io.*;
import java.nio.ByteBuffer;

/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */

public class MsgOperation implements Runnable
{
	private int connectedToID = -1;
	private boolean isChoked = true;
	private ClientProcess myClient = null;
	private DataInputStream dis = null;
	private connect myConnection;
	private BM myBitMap;
	private volatile boolean requestSenderStarted = false;
	WriteLog w = new WriteLog();
	private int myID;
	private long start_Download;
	private long stop_Download;

    /**
     *
     * @param aClient
     * @param myConnection
     * @throws IOException
     */
	public MsgOperation(ClientProcess aClient, connect myConnection) throws IOException
	{
		this.myConnection = myConnection;
		this.myBitMap = myConnection.getBM();
		this.myClient = aClient;
		this.dis = new DataInputStream(new PipedInputStream(aClient.getPipedOPS()));
		this.myID = Integer.parseInt(myConnection.getID());
	}

	@Override
	public void run()
	{
		try
		{
			this.sendHandshake(myID);
			myClient.setTimeout();
			(new Thread(myClient)).start();
			this.processData();
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		} 
	}

    /**
     *
     * @param myPeerID
     * @throws IOException
     * @throws InterruptedException
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
		w.GetHandshakeMessage(myID, connectedToID);
		if(myBitMap == null){
			System.out.println("My file is NULL"); 
		}
		if(myBitMap.doIHaveAnyPiece())
		{
			myClient.send(new ActMessage(5,myBitMap.getMyFileBitMap()).getFullMessage());
		}
	}

	private void processData() throws IOException, InterruptedException
	{        
		//now always receive bytes and take action
		while(true)
		{
			if(myBitMap.canIQuit()){
				myConnection.Quit();
				break;
			}
			Message msg = getNextMessage();
			//now interpret the message and take action

			int payloadLength = msg.getTheLengthOfMessages() - 1;  //removing  the size of message type
			// System.out.println("msg.getTheTypeValueOfMessages()" + msg.getTheTypeValueOfMessages());
			if(msg.getTheTypeOfMessages().equals("ChokeMessage"))
				processChokeMessage();

			else if(msg.getTheTypeOfMessages().equals("UnchokedMessage"))
				processUnchokeMessage();

			else if(msg.getTheTypeOfMessages().equals("InterestedMessage"))
				processInterestedMessage();

			else if(msg.getTheTypeOfMessages().equals("NotInterestedMessage"))
				processNotInterestedMessage();

			else if(msg.getTheTypeOfMessages().equals("HaveMessage"))
				processHaveMessage();

			else if(msg.getTheTypeOfMessages().equals("BitfieldMessage"))
				processBitfieldMessage(payloadLength);

			else if(msg.getTheTypeOfMessages().equals("RequestMessage"))
				processRequestMessage();

			else if(msg.getTheTypeOfMessages().equals("PieceMessage"))
				processPieceMessage(payloadLength);

			else
				System.out.println("No such type of Message");

		}
	}

    /**
     *
     * @return
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
			ActMessage i = new ActMessage(2);

			myClient.send(i);
		}
		else
		{
			myClient.send((new ActMessage(3)));
		}
	}


	private void processPieceMessage(int msgLength) throws IOException, InterruptedException
	{

		int pieceIndex = readIndex();
		byte[] pieceData = readPayload(msgLength-4);

		stop_Download = System.currentTimeMillis();
		myConnection.addOrUpdatedownloadratePeer(connectedToID, (stop_Download - start_Download));
		myBitMap.reportPieceReceived(pieceIndex, pieceData);
		w.PD(Integer.toString(myID), Integer.toString(connectedToID), pieceIndex, myBitMap.getDownloadedPieceCount(myID));

		if (myBitMap.getDownloadedPieceCount(myID) == myBitMap.getTotalPieceCount())
		{
			w.DC(Integer.toString(myID));
			myConnection.sendGM(myConnection.reportconnectedlist(), new ActMessage(3).getFullMessage());
		}

		if(!isChoked)
		{
			int desiredPiece = myBitMap.getPeerPieceIndex(connectedToID);
			if(desiredPiece != -1)
			{
				myClient.send((new ActMessage(6,desiredPiece)).getFullMessage());
			}
		}

		myConnection.sendGM(myConnection.reportconnectedlist(), (new ActMessage(4,pieceIndex)).getFullMessage());
		myConnection.sendGM(myConnection.computeAndGetWastePeersList(), new ActMessage(3).getFullMessage());
	}


	private void processRequestMessage() throws IOException, InterruptedException
	{        

		int pieceIndex = readIndex();
		byte[] dataForPiece = myBitMap.getPieceData(pieceIndex);
		myClient.send((new ActMessage(7, pieceIndex, dataForPiece)).getFullMessage());
	}

	private void processHaveMessage() throws IOException, InterruptedException
	{

		int pieceIndex = readIndex();
		w.Have(Integer.toString(myID), Integer.toString(connectedToID),pieceIndex );

		myBitMap.reportPeerPieceAvailablity(connectedToID, pieceIndex);
		if(!myBitMap.doIHavePiece(pieceIndex))
		{
			myConnection.findIPeer(connectedToID);
			myClient.send((new ActMessage(2)).getFullMessage());
		}
	}

	private void processNotInterestedMessage()
	{
		w.NotInterested(Integer.toString(myID), Integer.toString(connectedToID));
		myConnection.findNIPeer(connectedToID);
	}

	private void processInterestedMessage()
	{
		w.Interested(Integer.toString(myID), Integer.toString(connectedToID));
		myConnection.findIPeer(connectedToID);
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
	{
		w.Choked(Integer.toString(myID), Integer.toString(connectedToID));
		this.isChoked = true;
		myConnection.resetdownloadrate_peer(connectedToID);
	}

	private Message getNextMessage() throws IOException, InterruptedException
	{

		byte[] lengthBuffer = new byte[4];
		try{
            dis.readFully(lengthBuffer);
		}
		catch(Exception e){
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
				myClient.send((new ActMessage(6,desiredPiece)));
			}
		}

		@Override
		public void run()
		{
			while(true){
				if(myBitMap.canIQuit()){
					try{
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

		}
	}
}

