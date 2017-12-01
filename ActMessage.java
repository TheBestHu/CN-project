import java.io.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */

public class ActMessage extends Message
{
	public ActMessage(int MsgType) throws InterruptedException, IOException
	{
		if(MsgType == 0)
		{
			this.TheTypeOfMessages = "ChokeMessage";
			this.TheTypeValueOfMessages = 0;
			this.TheLengthOfMessages = 1;

			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.TheLengthOfMessages));
			baos.write((byte)this.TheTypeValueOfMessages);
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
		else if(MsgType ==1)
		{
			this.TheTypeOfMessages = "UnchokedMessage";
			this.TheTypeValueOfMessages = 1;
			this.TheLengthOfMessages = 1;

			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.TheLengthOfMessages));
			baos.write((byte)this.TheTypeValueOfMessages);
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
		else if(MsgType == 2)
		{
			this.TheTypeOfMessages = "InterestedMessage";
			this.TheTypeValueOfMessages = 2;
			this.TheLengthOfMessages = 1;

			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.TheLengthOfMessages));
			baos.write((byte)this.TheTypeValueOfMessages);
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
		else if(MsgType == 3)
		{
			this.TheTypeOfMessages = "NotInterestedMessage";
			this.TheTypeValueOfMessages = 3;
			this.TheLengthOfMessages = 1;

			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.TheLengthOfMessages));
			baos.write((byte)this.TheTypeValueOfMessages);
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
	}

	public ActMessage(int MsgType, int index) throws InterruptedException, IOException
	{
		if(MsgType == 4)
		{
			this.TheTypeOfMessages = "HaveMessage";
			this.TheTypeValueOfMessages = 4;
			this.TheLengthOfMessages = (getBytes(index)).length+1;
		
		//utilities.getBytes(pieceIndex) is payload for this message
			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.TheLengthOfMessages));
			baos.write((byte)this.TheTypeValueOfMessages);
			baos.write(getBytes(index));
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
		else if(MsgType == 6)
		{
			this.TheTypeOfMessages = "RequestMessage";
			this.TheTypeValueOfMessages = 6;
			this.TheLengthOfMessages = (getBytes(index)).length+1;
		
			//utilities.getBytes(pieceIndex) is payload for this message
		
			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.TheLengthOfMessages));
			baos.write((byte)this.TheTypeValueOfMessages);
			baos.write(getBytes(index));
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
	}

	public ActMessage(int MsgType, byte[] bitfield) throws InterruptedException, IOException
	{
		this.TheTypeOfMessages = "Bitfield Message";
		this.TheTypeValueOfMessages = 5;
		this.TheLengthOfMessages = bitfield.length+1;

		ByteArrayOutputStream baos = getStreamHandle();
		baos.write(getBytes(this.TheLengthOfMessages));
		baos.write((byte)this.TheTypeValueOfMessages);
		baos.write(bitfield);
		FullMessage = baos.toByteArray();
		returnStreamHandle();
	}

	public ActMessage(int MsgType, int index, byte[] data) throws InterruptedException, IOException
	{
		if(MsgType == 7){
			this.TheTypeOfMessages = "PieceMessage";
			this.TheTypeValueOfMessages = 7;
			this.TheLengthOfMessages = data.length+4+1;

			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.TheLengthOfMessages));
			baos.write((byte)this.TheTypeValueOfMessages);
			baos.write(getBytes(index));
			baos.write(data);
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
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
	private static boolean UsedStream = false;

	private static synchronized ByteArrayOutputStream getStreamHandle() throws InterruptedException
	{
		lock.lock();
		try
		{
			if(UsedStream)
			{
				borrowedStream.await();
			}
			UsedStream = true;
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
            UsedStream = false;
        }
        finally
        {
            lock.unlock();
        }
    }
}














