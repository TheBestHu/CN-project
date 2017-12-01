import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ActualMessage extends Message
{	/*
	Constructor for Message without payload
	*/
	public ActualMessage(int MsgType) throws InterruptedException, IOException
	{
		if(MsgType == 0)
		{
			this.MsgType = "ChokeMessage";
			this.MsgTypeValue = 0;
			this.MsgLength = 1;

			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
		else if(MsgType ==1)
		{
			this.MsgType = "UnchokedMessage";
			this.MsgTypeValue = 1;
			this.MsgLength = 1;

			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
		else if(MsgType == 2)
		{
			this.MsgType = "InterestedMessage";
			this.MsgTypeValue = 2;
			this.MsgLength = 1;

			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
		else if(MsgType == 3)
		{
			this.MsgType = "NotInterestedMessage";
			this.MsgTypeValue = 3;
			this.MsgLength = 1;

			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
	}

	public ActualMessage(int MsgType, int index) throws InterruptedException, IOException
	{
		if(MsgType == 4)
		{
			this.MsgType = "HaveMessage";
			this.MsgTypeValue = 4;
			this.MsgLength = (getBytes(index)).length+1;
		
		//utilities.getBytes(pieceIndex) is payload for this message
			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			baos.write(getBytes(index));
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
		else if(MsgType == 6)
		{
			this.MsgType = "RequestMessage";
			this.MsgTypeValue = 6;
			this.MsgLength = (getBytes(index)).length+1;
		
			//utilities.getBytes(pieceIndex) is payload for this message
		
			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			baos.write(getBytes(index));
			FullMessage = baos.toByteArray();
			returnStreamHandle();
		}
	}

	public ActualMessage(int MsgType, byte[] bitfield) throws InterruptedException, IOException
	{
		this.MsgType = "Bitfield Message";
		this.MsgTypeValue = 5;
		this.MsgLength = bitfield.length+1;

		ByteArrayOutputStream baos = getStreamHandle();
		baos.write(getBytes(this.MsgLength));
		baos.write((byte)this.MsgTypeValue);
		baos.write(bitfield);
		FullMessage = baos.toByteArray();
		returnStreamHandle();
	}

	public ActualMessage(int MsgType, int index, byte[] data) throws InterruptedException, IOException
	{
		if(MsgType == 7){
			this.MsgType = "PieceMessage";
			this.MsgTypeValue = 7;
			this.MsgLength = data.length+4+1;

			ByteArrayOutputStream baos = getStreamHandle();
			baos.write(getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
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














