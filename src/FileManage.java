import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class FileManage {
	InputStream inputstream;
	OutputStream outputstream;
	FileManage(InputStream inputstream, OutputStream outputstream) throws IOException{
		
		
	}
	
	synchronized void Send(HandShakeMessage HandShake_message) throws IOException{
		byte[] message = HandShake_message.toByte();
		outputstream.write(message);
		outputstream.flush();
	}
	
	synchronized void Send(ActualMessage Acutal_message)  throws IOException{
		byte[] message = Acutal_message.PackMessage();
		outputstream.write(message);
		outputstream.flush();
	}
	
	synchronized void Send(int Message_type) throws IOException{
		ActualMessage Actual_message = new ActualMessage(Message_type);
		byte[] message = Actual_message.PackMessage();
		outputstream.write(message);
		outputstream.flush();
	}
	
	synchronized byte[] receive() throws IOException{
		byte[] length = new byte[4];
		byte[] pureinfo;
		int temp=0;
		int get=0;
		while(true) {
			if (get<4) {
				temp = inputstream.read (length, get, 4-get);
				get = get +temp;
			}
			else break;
			}
		int Msglength = Operation.toInt(length);
		pureinfo = new byte[Msglength];
		get=0;
		
		while(get<Msglength) {
			temp = inputstream.read(pureinfo, get, Msglength-get);
			get = get + temp;
		}
		
		byte[] output = new byte[length.length + pureinfo.length];
		System.arraycopy(length, 0, output, 0 , 4);
		System.arraycopy(pureinfo, 0, output, 4, 4 + pureinfo.length-4);
		return output;
	}
	
	synchronized void Handshake(int PeerID, /*FileChunk FileChunk,*/ int TargetId) {
		HandShakeMessage Handshake = new HandShakeMessage (PeerID);
        //Send(Handshake);
        //fileChunk.log.cnct (TargetId);
        byte[] receive = new byte[32];
       // inputstream.read(receive, 0, 32);
        HandShakeMessage RcvMsg = new HandShakeMessage (receive);
        if (RcvMsg.HandshakeHeader.equals ("P2PFILESHARINGPROJ"))
            if (RcvMsg.peerId == TargetId) {
                //fileChunk.log.cnted (TargetId);
            }
	}
	

}
