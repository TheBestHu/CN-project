import java.io.Serializable;

/**
 * Message should be serialized
 * has message type value and full payload message
 *
 */
public class Message implements Serializable 
{
	protected static final long serialVersionUID = 1L;
	String MsgType;
	int MsgLength;
	byte[] FullMessage;
	int MsgTypeValue;
	Message()
	{
	}
	Message(int type, int length)
	{
		this.MsgTypeValue = type;
		this.MsgLength = length;
		switch(type)
		{
		case 0:
			//System.out.println("CHoke Message Received");
			this.MsgType="ChokeMessage";
			break;
		case 1:
			//System.out.println("UnCHoke Message Received");
			this.MsgType="UnchokedMessage";
			break;
		case 2:
			//System.out.println("Interested Message Received");
			this.MsgType="InterestedMessage";
			break;
		case 3:
			//System.out.println("Not Interested Message");
			this.MsgType="NotInterestedMessage";
			break;
		case 4:
			//System.out.println("Have Message Received");
			this.MsgType="HaveMessage";
			break;
		case 5:

			//System.out.println("Bitfield message received");
			this.MsgType="BitfieldMessage";
			break;
		case 6:
			//System.out.println("request Message Received");
			this.MsgType="RequestMessage";
			break;
		case 7:
			//System.out.println("Piece Message Received");
			this.MsgType="PieceMessage";
			break;
		default:
			System.out.println("Undefined Message!!!");
		}
	}

	public int getMsgTypeValue()
	{
		return MsgTypeValue;
	}

	public void setMsgTypeValue(int msgVal)
	{
		this.MsgTypeValue = msgVal;
	}

	public String getMsgType()
	{
		return MsgType;
	}

	public void setMsgType(String msg)
	{
		this.MsgType = msg;
	}

	public int getMsgLength()
	{
		return MsgLength;
	}

	public void setMsgLength(int msglen)
	{
		this.MsgLength = msglen;
	}

	public byte[] getFullMessage()
	{
		return FullMessage;
	}

	public void setFullMessage(byte[] pack)
	{
		this.FullMessage = pack;
	}
}
