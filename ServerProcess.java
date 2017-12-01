import java.io.*;
import java.net.*;
/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */

public class ServerProcess implements Runnable
{
	private ServerSocket serSocket;
	private connect thisMyConnection;

	private static final int ACCEPT_TIMEOUT = 100000;
	WriteLog w = new WriteLog();

	public ServerProcess(String host, int port, connect mConnection) throws UnknownHostException, IOException
	{
		System.out.println("IM IN THE SERVER CONNECTION CONSTRUCTOR :: hostname = " +host+ " port = " + port);
		this.serSocket = new ServerSocket(port, 0, InetAddress.getByName(host.trim()));
		this.serSocket.setSoTimeout(ACCEPT_TIMEOUT);
		this.thisMyConnection = mConnection;
		w.ServerConnectionIncoming(mConnection.getID(), this.serSocket.getLocalSocketAddress().toString(), Integer.toString(this.serSocket.getLocalPort()));

		try{
			Thread.sleep(50);
		}
		catch(Exception e){
			//e.printStackTrace();
		}
		System.out.println("Server socket ready to run");
	}

	@Override
	public void run()
	{

		while(true)
		{
			Socket CSocket = null;
			// wait for connections forever
			try
			{
				CSocket = this.serSocket.accept();
				w.TcpConnectionIncoming(thisMyConnection.getID(), CSocket.getInetAddress().getHostName());

				(new Thread(new MsgOperation(new ClientProcess(CSocket, thisMyConnection), thisMyConnection))).start();
				System.out.println("Server has been requested by a client");
			}
			catch(InterruptedIOException iioex)
			{
				if(thisMyConnection.getBM().canIQuit())
				{
					try
					{
						this.serSocket.close();
						break;
					} catch (IOException e){
					}
				}
			}
			catch (IOException e)
			{
				//e.printStackTrace();
			}
		}
	}
}

