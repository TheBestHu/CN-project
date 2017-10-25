/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author huanwenxu
 */
public class ActualMessage {
    int Message_length;
    int Message_type;
    byte[] Payload;
    ActualMessage(int message_type){
        Message_type = message_type;
        Message_length = 5;
        Payload = null;
    }
    
    ActualMessage(int message_type,byte[] payload){
        Message_type = message_type;
        Payload = payload;
        Message_length = 1+Payload.length;
    }
    
    void addPiece(int piece){
        byte[] temp;
        temp = Operation.toByte(piece);
        Payload = temp;
        Message_length = 1+Payload.length;
    }
    
    void addPiece(byte[] piece){
        Payload = piece;
        Message_length = 1+Payload.length;
    }
    
    byte[] PackMessage(){
        byte[] message = new byte[Message_length+4];
        message[0] = (byte)(Message_length>>(32-8));
        message[1] = (byte)(Message_length>>(32-8-8));
        message[2] = (byte)(Message_length>>(32-8-8-8));
        message[3] = (byte)(Message_length>>(32-8-8-8-8));
        message[4] = (byte)Message_type;
        int i;
        int j =0;
        if(Payload!=null){
            i=5;
            while(i<Message_length+4){
                message[i] = Payload[j];
                j++;
                i++;
            }
        }
        return message;
    }
}
