import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

			ByteArrayOutputStream baos = Utilities.getStreamHandle();
			baos.write(Utilities.getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			FullMessage = baos.toByteArray();
			Utilities.returnStreamHandle();
		}
		else if(MsgType ==1)
		{
			this.MsgType = "UnchokedMessage";
			this.MsgTypeValue = 1;
			this.MsgLength = 1;

			ByteArrayOutputStream baos = Utilities.getStreamHandle();
			baos.write(Utilities.getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			FullMessage = baos.toByteArray();
			Utilities.returnStreamHandle();
		}
		else if(MsgType == 2)
		{
			this.MsgType = "InterestedMessage";
			this.MsgTypeValue = 2;
			this.MsgLength = 1;

			ByteArrayOutputStream baos = Utilities.getStreamHandle();
			baos.write(Utilities.getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			FullMessage = baos.toByteArray();
			Utilities.returnStreamHandle();
		}
		else if(MsgType == 3)
		{
			this.MsgType = "NotInterestedMessage";
			this.MsgTypeValue = 3;
			this.MsgLength = 1;

			ByteArrayOutputStream baos = Utilities.getStreamHandle();
			baos.write(Utilities.getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			FullMessage = baos.toByteArray();
			Utilities.returnStreamHandle();
		}
	}

	public ActualMessage(int MsgType, int index) throws InterruptedException, IOException
	{
		if(MsgType == 4)
		{
			this.MsgType = "HaveMessage";
			this.MsgTypeValue = 4;
			this.MsgLength = (Utilities.getBytes(index)).length+1;
		
		//utilities.getBytes(pieceIndex) is payload for this message
		
			ByteArrayOutputStream baos = Utilities.getStreamHandle();
			baos.write(Utilities.getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			baos.write(Utilities.getBytes(index));
			FullMessage = baos.toByteArray();
			Utilities.returnStreamHandle();
		}
		else if(MsgType == 6)
		{
			this.MsgType = "RequestMessage";
			this.MsgTypeValue = 6;
			this.MsgLength = (Utilities.getBytes(index)).length+1;
		
			//utilities.getBytes(pieceIndex) is payload for this message
		
			ByteArrayOutputStream baos = Utilities.getStreamHandle();
			baos.write(Utilities.getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			baos.write(Utilities.getBytes(index));
			FullMessage = baos.toByteArray();
			Utilities.returnStreamHandle();
		}
	}

	public ActualMessage(int MsgType, byte[] bitfield) throws InterruptedException, IOException
	{
		this.MsgType = "Bitfield Message";
		this.MsgTypeValue = 5;
		this.MsgLength = bitfield.length+1;

		ByteArrayOutputStream baos = Utilities.getStreamHandle();
		baos.write(Utilities.getBytes(this.MsgLength));
		baos.write((byte)this.MsgTypeValue);
		baos.write(bitfield);
		FullMessage = baos.toByteArray();
		Utilities.returnStreamHandle();
	}

	public ActualMessage(int MsgType, int index, byte[] data) throws InterruptedException, IOException
	{
		if(MsgType == 7){
			this.MsgType = "PieceMessage";
			this.MsgTypeValue = 7;
			this.MsgLength = data.length+4+1;

			ByteArrayOutputStream baos = Utilities.getStreamHandle();
			baos.write(Utilities.getBytes(this.MsgLength));
			baos.write((byte)this.MsgTypeValue);
			baos.write(Utilities.getBytes(index));
			baos.write(data);
			FullMessage = baos.toByteArray();
			Utilities.returnStreamHandle();
		}
	}
}














