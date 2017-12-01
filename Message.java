import java.io.Serializable;

/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */

public class Message implements Serializable 
{
	protected static final long serialVersionUID = 1L;
	String TheTypeOfMessages;
	int TheLengthOfMessages;
	byte[] FullMessage;
	int TheTypeValueOfMessages;
	Message()
	{
	}
	Message(int type, int length)
	{
		this.TheTypeValueOfMessages = type;
		this.TheLengthOfMessages = length;
		switch(type)
		{
		case 0:
			this.TheTypeOfMessages ="ChokeMessage";
			break;
		case 1:
			this.TheTypeOfMessages ="UnchokedMessage";
			break;
		case 2:
			this.TheTypeOfMessages ="InterestedMessage";
			break;
		case 3:
			this.TheTypeOfMessages ="NotInterestedMessage";
			break;
		case 4:
			this.TheTypeOfMessages ="HaveMessage";
			break;
		case 5:

			this.TheTypeOfMessages ="BitfieldMessage";
			break;
		case 6:
			this.TheTypeOfMessages ="RequestMessage";
			break;
		case 7:
			this.TheTypeOfMessages ="PieceMessage";
			break;
		default:
			System.out.println("Undefined Message!!!");
		}
	}
    public String getTheTypeOfMessages()
    {
        return TheTypeOfMessages;
    }
    public int getTheLengthOfMessages()
    {
        return TheLengthOfMessages;
    }
    public byte[] getFullMessage()
    {
        return FullMessage;
    }

}
