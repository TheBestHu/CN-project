import java.io.*;
import java.util.*;

public class WriteLog
{
  //Creating a Log file named log_peer_[peerID].log if one doesnt exist
  public void CreateLog(String peer) {
      String filename = "peer_"+peer+"/log_peer_" + peer + ".log";
      File directory = new File("peer_"+peer);
      directory.mkdir();
      File file = new File(filename);
      try {
          if (file.exists() == false) {
              file.createNewFile();
          }
          FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);  //file.getAbsoluteFile()  get the absolute filepath.
          BufferedWriter bw = new BufferedWriter(fw);
          bw.write(" Log File for Peer " + peer + ".");
          bw.newLine();
          bw.close();
      }catch (IOException a) {
          a.printStackTrace();
      }
  }

  //Writing Log for Incoming TCP connection from Peer 2 to peer 1
  public void TcpConnectionIncoming(String peer1, String peer2)
  {        
	  String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
			File file = new File(filename);
            if (!file.exists()) {
                CreateLog(peer1);
            }
            String operation = " is connected from Peer ";
            WriteLine(filename,peer1,operation,peer2);
 }
  
  
  public void ServerConnectionIncoming(String peer1, String address, String port)
  {        
	  String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
			File file = new File(filename);
            if (!file.exists()) 
            {
		CreateLog(peer1);
            }
            String operation = "ready to listen";
            WriteLine(filename,peer1,operation,address);
  }
    // Writing Log for making the TCP connection from Peer1 to Peer2
  public void TcpConnectionOutgoing(String peer1, String peer2)
  {        String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
			File file = new File(filename);
            if (!file.exists()) {
				CreateLog(peer1);
			}
			String operation = " makes connection to Peer ";
            WriteLine(filename, peer1,operation, peer2);
  }

  
  public void PrefNeighbours(String peer1, String prefPeerList)
  {
	  		String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
            File file = new File(filename);
            if (!file.exists()) 
            {
		CreateLog(peer1);
            }
            String operation  = " has the preferred neighbours ";
            WriteLine(filename,peer1,operation,prefPeerList);
}
  
  public void OptUnchokedNeighbours(String peer1, String peer2)
  {
	  		String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
            File file = new File(filename);
            if (!file.exists()) 
            {
		CreateLog(peer1);
            }
            String operation = " has optimistically unchocked neighbour ";
            WriteLine(filename,peer1, operation,peer2);
  }
  
  
  public void Unchoked(String peer1, String peer2)
  {
	  		String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
            File file = new File(filename);
            if (!file.exists()) 
            {
		CreateLog(peer1);
            }
            String operation = " is unchocked by peer ";
            WriteLine(filename,peer1,operation,peer2);
  }
  
  public void Choked(String peer1, String peer2)
  {
	  		String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
            File file = new File(filename);
            if (!file.exists()) 
            {
		CreateLog(peer1);
            }
            String operation = " is chocked by Peer ";
            WriteLine(filename, peer1, operation,peer2);
  }
  
  public void Have(String peer1, String peer2, int index)
  {
	  		String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
            File file = new File(filename);
            if (!file.exists()) 
            {
		CreateLog(peer1);
            }
            String operation = " receive 'Have' message from peer "+peer2+" for the piece "+index;
            WriteLine(filename,peer1,operation,"");
  }
  
  public void Interested(String peer1, String peer2)
  {
	  String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
      File file = new File(filename);
      if (!file.exists())
      {
		CreateLog(peer1);
      }
      String operation = " received 'interested' message from Peer ";
      WriteLine(filename, peer1, operation,peer2);
  }
  
  public void NotInterested(String peer1, String peer2) {
      String filename = "peer_" + peer1 + "/log_peer_" + peer1 + ".log";
      File file = new File(filename);
      if (!file.exists()) {
          CreateLog(peer1);
      }
      String operation = " received 'Not interested' message from Peer ";
      WriteLine(filename, peer1, operation, peer2);
  }
  
  public void PieceDownload(String peer1, String peer2, int index, int pieces)
  {
	  String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
            File file = new File(filename);
            if (!file.exists()) 
            {
		CreateLog(peer1);
            }
      String num = Integer.toString(pieces);
      String operation = " has downloaded piece "+index+ "from "+peer2 + ". Now the number of pieces it has is ";
      WriteLine(filename, peer1, operation,num);
  }
  
  public void DownloadComplete(String peer1)
  {
	  String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
	  String operation = " has downloaded the complete file";
	  WriteLine(filename,peer1,operation,"");
  }

  
  ///*****************************log handshake message begin*****************************************
  
  public void ReceivedHandshake(int peer1, int peer2)
  {
      String filename = "peer_" + peer1+"/log_peer_"+peer1+".log";
      File file = new File(filename);
      if (!file.exists())
      {
		CreateLog(Integer.toString(peer1));
      }
      String n1 = Integer.toString(peer1);
      String n2 = Integer.toString(peer2);
      String operation = " received handshake message from Peer  ";
      WriteLine(filename,n1,operation,n2);
  }
  ///***************************log handshake message ends here*****************************************
  //***************************   write log line by line    *****************************************
    public void WriteLine(String filename,String peer1, String operation, String peer2){
        try{
            Date date = new Date();
            FileWriter fw = new FileWriter(filename,true);
            BufferedWriter bw = new BufferedWriter(fw);
            String msg = date.toString()+": Peer "+peer1+operation+peer2+".";
            bw.write(msg);
            bw.newLine();
            bw.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

}

