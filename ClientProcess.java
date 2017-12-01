
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;

/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */

public class ClientProcess implements Runnable
{
	private DataOutputStream DataOutS;
	private DataInputStream DataInS;
	private String AddrOfServer;
	private static final int TIMEOUT = 5000;    //5 seconds
	private static final int RECEIVE_TIMEOUT = 1000;    //1 second
	private Socket cSocket;
	private static final String CONNECTION_NAME = "ClientConnection";
	private int sPortNum;
	private PipedOutputStream pipedOPS = new PipedOutputStream();
	private connect MConnect;
	WriteLog w = new WriteLog();

	public ClientProcess(String AddrOfServer, int sPortNum, connect MConnect) //throws SocketTimeoutException, IOException
	{
		this.cSocket = new Socket();
		//while(true)
		//{
			try
			{	this.sPortNum = sPortNum;
                this.AddrOfServer = AddrOfServer;
                this.cSocket.connect(new InetSocketAddress(this.AddrOfServer, this.sPortNum), TIMEOUT);
				this.DataOutS = new DataOutputStream(cSocket.getOutputStream());
				this.DataInS = new DataInputStream(cSocket.getInputStream());
				//break;
			}
			catch (IOException e)
			{
				try{Thread.sleep(500);} catch (InterruptedException e1){/*ignore*/}
			}

		this.MConnect = MConnect;
		System.out.println("Client: connected to server now...");


		try{
			w.TcpConnectionOutgoing(MConnect.getID(), this.AddrOfServer);
		}
		catch(Exception e){
		}

	} 


	public ClientProcess(Socket theSocket, connect MConnect) throws IOException
	{
		this.cSocket = theSocket;
		this.DataOutS = new DataOutputStream(cSocket.getOutputStream());
		this.DataInS = new DataInputStream(cSocket.getInputStream());
		this.MConnect = MConnect;
		System.out.println("Client: connected to server now...");
	}

	public void send(byte[] data) throws IOException
	{
		DataOutS.write(data);
		DataOutS.flush();
	}
// xhw
	public void send(ActMessage ActM) throws IOException
	{
		byte[] data = ActM.getFullMessage();
		DataOutS.write(data);
		DataOutS.flush();
	}

	private void receive() throws IOException
	{
		//always read first 4 bytes, then read equivalent to the length indicated by those 4 bytes
		byte[] lengthOfB = new byte[4];
		DataInS.readFully(lengthOfB);
		ByteBuffer buffer1 = ByteBuffer.wrap(lengthOfB,0,4);
		int length = buffer1.getInt();
		pipedOPS.write(getBytes(length));

		//now read the data indicated by length and write it to buffer
		byte[] buffer = new byte[length];
		DataInS.readFully(buffer);
		pipedOPS.write(buffer);
		pipedOPS.flush();
		clientBlocker();
	}


	synchronized void receive(int preknownDataLength) throws EOFException, IOException
	{
		byte[] buffer = new byte[preknownDataLength];
		//using read fully here to completely download the data before placing it in buffer
		DataInS.readFully(buffer);
		pipedOPS.write(buffer);
	}

	@Override
	public void run()
	{
		//keep reading until client dies
		while(true)
		{
			try
			{
				this.receive();
			}
			catch (InterruptedIOException iioex)
			{
				if(MConnect.getBM().canIQuit())
				{
					try
					{
						System.out.println("close connections called");
						Thread.yield();
						this.closeConnections();
						
					}
					catch (Exception e){/*quit silently*/}
					break;
				}
			}
			catch (IOException e)
			{
				break;
			}
		}
	}

	protected void clientBlocker(){
		ArrayList<Integer> one = new ArrayList<Integer>();
		for(int i=0;i<10;i++)
			one.add(i);
		if(CONNECTION_NAME== "ClientConnection"){
			Collections.sort(one);
		}
	}

	/**called at end to close up all the connections
	 * @throws IOException
	 */
	private void closeConnections() throws IOException
	{
		if(this.pipedOPS != null)
		{	
            System.out.println("close piped OS");
			this.pipedOPS.close();
		}
		if(this.DataInS != null)
		{						
            System.out.println("close DataInputStream");
			this.DataInS.close();
		}
		if(this.DataOutS != null)
		{						
            System.out.println("close DataOutputStream");
			this.DataOutS.close();
		}
		if(this.cSocket != null)
		{						
            System.out.println("close client socket");
			this.cSocket.close();
		}
	}

	public PipedOutputStream getPipedOPS()
	{
		return this.pipedOPS;
	}

	public void setTimeout() throws SocketException
	{
		this.cSocket.setSoTimeout(RECEIVE_TIMEOUT);
	}
	private static byte[] getBytes(int number){
		byte[] result = new byte[4];
		int shift = 0;
		for (int i = 0; i < result.length; i++) {

			shift = (result.length - 1 - i) * 8; // 24, 16, 8, 0

			result[i] = (byte) (number >> shift);
		}
		return result;
	}
}

