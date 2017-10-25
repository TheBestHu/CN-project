/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author huanwenxu
 */
public class HandShakeMessage {
    String HandshakeHeader;
    int peerId;
    private String Handshake_message;
    HandShakeMessage(int peerId){
        HandshakeHeader = "P2PFILESHARINGPROJ";
        this.peerId = peerId;
        Handshake_message = HandshakeHeader+"0000000000"+this.peerId;
    }
    
    HandShakeMessage(byte[] rcvdHSM) {
        String stringMessage = new String (rcvdHSM);
        StringBuffer stringBuffer = new StringBuffer (stringMessage);
        this.HandshakeHeader = stringBuffer.substring (0, 18);
        this.peerId = Integer.parseInt (stringBuffer.substring (28, 32));
    }
    
    byte[] toByte(){
        byte[] temp = this.Handshake_message.getBytes();
        return temp;
    }
}
