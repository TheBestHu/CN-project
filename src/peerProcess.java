
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import tool.readFile;

public class peerProcess{
	private int numOfPN;
	private int unChokeInterval;
	private int optimalInterval;
	private int fileExist;
	private LinkedList<Peer> fileShareList = new LinkedList<>();
	private Peer optimal = null;
	private int nTime;
	private int opnTime;
	private String filename;
	private int pieceSize;
	private int filesize;
	public LinkedList <Peer> tempList = new LinkedList <> ();

    public static void main (String[] args) throws Exception {
        new peerProcess (args[ 0 ]);
    }

    private int Timer(){
    	int time;
    	if(opnTime<=nTime) time = opnTime;
    	else time = nTime;
    	nTime = nTime - time;
    	opnTime = opnTime - time;
    	return time*1000;
    }

    synchronized void shareInfo(int i) throws IOException{
    	int j =0;
    	while(j<fileShareList.size()){
    		fileShareList.get(j).have(i);
    		j++;
    	}
    	if( optimal!=null) optimal.have(i);
    }

    private void readFile(String peerID) throws IOException{
		/**
		 * need to be modified
		 */
		readFile rd=new readFile();
		rd.readcommon();
    	rd.readpeer();
		numOfPN = rd.NumberOfPreferredNeighbors;
		optimalInterval = rd.OptimisticUnchockingInterval;
		fileExist = rd.fileD[rd.findID(peerID)];
		filename = rd.FileName;
		unChokeInterval = rd.UnchokingInterval;
		pieceSize =rd.PieceSize;
		filesize = rd.FileSize;


    	// read in Common.cfg and PeerInfo.cfg
    }

    //change optimal unchoking peer
    private synchronized void changeOptimal() {
    	if(optimal !=null){
    		optimal.choke(true);
    		fileShareList.add(optimal);
    		optimal = null;
    	}
    	int i=0;
    	// pick a randomm number i which is less than size of fileShareList
    	optimal = fileShareList.get(i);
    	optimal.choke(false);
    }

    // sort List based on speed
    private void sortList(LinkedList<Peer> fileShareList){
    	Comparator <Peer> comparator = new Comparator<Peer>(){
    		int speed1, speed2;
    		public int compare(Peer p1, Peer p2){
    			speed1 = p1.getSpeed();
    			speed2 = p2.getSpeed();
    			if(speed1>speed2) return 1;
    			else if(speed1<speed2) return -1;
    			else return 0;
    		}
    	};
    	fileShareList.sort(comparator);
    }

    private void startThread(){
    	for(Peer channelList : fileShareList){
    		new Thread(channelList).start();
    	}
    }
    private peerProcess(String peerID) throws IOException{
    	readFile(peerID);
    	startThread();

    	while(true){
    		if(optimal == null && fileShareList.size()==0) break;

    		if(nTime < 0.1){
    			// change neighbour
    			nTime = unChokeInterval;
    			sortList(fileShareList);
    		}
    		if(opnTime<0.1){
    			changeOptimal();
    			opnTime = optimalInterval;
    		}

    	}
    }
}
