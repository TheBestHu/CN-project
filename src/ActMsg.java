public class ActMsg {

    int messageType;
    byte[] messagePayload;
    private int messageLength;

    ActMsg(int messageType)
    {
        this.messageType = messageType;
        this.messagePayload = null;
        messageLength = 5;
    }

    ActMsg(int messageType, byte[] messagePayload)
    {
        this.messageType = messageType;
        this.messagePayload = messagePayload;
        messageLength = 1 + messagePayload.length;
    }

    void plusPayload(int piece)
    {
///////////////////// change helper byte[] intToByte(int number)  fang 25-36
        byte[] message;
        byte[] result;
        result = new byte[4];
        result[0] = (byte) (piece / 16777216);
        result[1] = (byte) (piece / 65536);
        result[2] = (byte) (piece / 256);
        result[3] = (byte) piece;
        //message = Helper.intToByte(piece);
///////////////////////////////////////////////////////////        
        message = result;
        messagePayload = message;
        messageLength = 1 + messagePayload.length;
        /*
        byte[] message;
        message = Helper.intToByte(piece);
        messagePayload = message;
        messageLength = 1 + messagePayload.length;
        */
    }

    void plusPayload(byte[] messagePayload)
    {
        this.messagePayload = messagePayload;
        messageLength =1 + messagePayload.length;
    }

    byte[] msgPack()
    {
        byte[] message = new byte[messageLength+4];
        message[0] = (byte)(messageLength>>24);
        message[1] = (byte)(messageLength>>16);
        message[2] = (byte)(messageLength>>8);
        message[3] = (byte)(messageLength);
        message[4] = (byte)messageType;
        int i;
        int j = 0;
        if(messagePayload != null)
        {
            i = 5;
            while (i < messageLength + 4) {
                message[i] = messagePayload[j++];
                i++;
            }
        }
        return message;
    }

}

	
