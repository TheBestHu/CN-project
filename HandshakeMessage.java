import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Used for handshaking
 *
 */
public class HandshakeMessage extends Message 
{
	private static final long serialVersionUID = 2L;
	
	final String HANDSHAKE_MSG_HEADER = "P2PFILESHARINGPROJ"; 
	int peerID;
	
	public int getPeerID()
	{
		return peerID;
	}
	
	/**
	 * @param peerid
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public HandshakeMessage(int peerid) throws InterruptedException, IOException
	{		
		ByteArrayOutputStream baos = getStreamHandle();
        baos.write(HANDSHAKE_MSG_HEADER.getBytes());
        baos.write(new byte[10]);  //10 bytes zero bits
        this.peerID = peerid;
        baos.write(getBytes(peerID));
        this.FullMessage = baos.toByteArray();
        returnStreamHandle();
    }
	
	/**
	 * Create handshake message when received.
	 * @param HandShakeMsg
	 */
	public HandshakeMessage(byte[] HandShakeMsg)
	{
		this.FullMessage = HandShakeMsg;
		ByteBuffer buffer = ByteBuffer.wrap(HandShakeMsg,28,4);
		this.peerID  = buffer.getInt();
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

	private static ByteArrayOutputStream streamHandle = new ByteArrayOutputStream();
	private static ReentrantLock lock = new ReentrantLock();
	private static Condition borrowedStream = lock.newCondition();
	private static boolean isStreamInUse = false;

	private static synchronized ByteArrayOutputStream getStreamHandle() throws InterruptedException
	{
		lock.lock();
		try
		{
			if(isStreamInUse)
			{
				borrowedStream.await();
			}
			isStreamInUse = true;
			streamHandle.reset();
			return streamHandle;
		}
		finally
		{
			lock.unlock();
		}
	}
	private static void returnStreamHandle()
	{
		lock.lock();
		try
		{
			borrowedStream.signal();
			isStreamInUse = false;
		}
		finally
		{
			lock.unlock();
		}
	}
}
