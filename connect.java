import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */

public class connect
{

	private int waitingPort;
	private final int theSizeOfP;
	private AtomicInteger UnchockedP;
	private ConcurrentHashMap<Integer, ClientProcess> peerConnectionMap;
	private Map<Integer, Double> downloadrate_peer; // peer id --> download rate
	private Set<Integer> setofChokedPeers = new ConcurrentSkipListSet<Integer>();
	private ComCon myCommonConfig;
	WriteLog w = new WriteLog();
	final int NUM_PREFERRED_NEIGHBORS;
	private final int UNCHOKING_INTERVAL;
	private BM myBitMap;
	private Set<Integer> myInterestedNeighbours;
	private List<Integer> connectedPeersList;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final int OPTIMISTIC_UNCHOKING_INTERVAL;
	private Map<Integer, PeerCon> peerInfoMap;
	private int myPeerID;
	private String myHostName;

	public connect(ComCon myCommonConfig, Map<Integer, PeerCon> peerMap, int myPeerID) throws IOException, InterruptedException
	{
		this.myCommonConfig = myCommonConfig;
		PeerCon myPeerConfig = peerMap.get(myPeerID);
		this.myHostName = myPeerConfig.FindHostInfo();
		this.waitingPort = myPeerConfig.FindPortNumber();
		System.out.println("My host name = " + myHostName + " and my listening port is " + waitingPort);

		this.theSizeOfP = myCommonConfig.getSizeForEachPiece();
		this.peerInfoMap = peerMap;
		this.myPeerID = myPeerID;
		this.downloadrate_peer = new HashMap<Integer, Double>();
		this.myInterestedNeighbours = new ConcurrentSkipListSet<Integer>();
		connectedPeersList = new ArrayList<Integer>();
		this.NUM_PREFERRED_NEIGHBORS = myCommonConfig.getNumberOfPN();
		this.UNCHOKING_INTERVAL = myCommonConfig.getIntOfUnchocking();
		this.OPTIMISTIC_UNCHOKING_INTERVAL = myCommonConfig.getChooseTheONInt();
		this.peerConnectionMap = new ConcurrentHashMap<Integer, ClientProcess>();
		connectedPeersList.addAll(this.peerInfoMap.keySet());
		UnchockedP = new AtomicInteger(-1);

		//begin connection
		this.begin();
	}
	public void begin() throws IOException, InterruptedException
	{

		Thread serverThread = new Thread(new ServerProcess(myHostName, waitingPort, this));
		serverThread.start();
		this.myBitMap = new BM(myPeerID, myCommonConfig, peerInfoMap.keySet(), this.peerInfoMap.get(myPeerID).hasCompleteFile(), this,this.peerInfoMap);
		this.processPeerInfoMap();
		scheduler.scheduleAtFixedRate(new Neighbours(this), 0, UNCHOKING_INTERVAL, TimeUnit.SECONDS);
		scheduler.scheduleAtFixedRate(new OptimisticNeighbours(this), 0, OPTIMISTIC_UNCHOKING_INTERVAL, TimeUnit.SECONDS);
	}

	private void processPeerInfoMap() throws  IOException ///huye
	{
		Set<Integer> set = this.peerInfoMap.keySet();
		ArrayList<Integer> setList = new ArrayList<>(set);
		int n = setList.size();
		for (int i = 0 ; i<n;i++){
			Integer a = setList.get(i);
			if(a<myPeerID){
				PeerCon peerinfo = this.peerInfoMap.get(a);
				System.out.println("Creating a client for " + myPeerID);
				ClientProcess newClient = new ClientProcess(peerinfo.FindHostInfo(),
						peerinfo.FindPortNumber(), this);
				MsgOperation aMessageHandler = new MsgOperation(newClient, this);
				(new Thread(aMessageHandler)).start();
				//findConnectionStatus();
				this.peerConnectionMap.put(a, newClient);
			}
		}
	}


	public void sendGM(List<Integer> peerIDList, byte[] data) throws IOException
	{
		int i = peerIDList.size(); //huye
		for(int n = 0; n< i;n++){
			Integer a = peerIDList.get(n);
			if(this.peerConnectionMap.containsKey(a)){
				this.peerConnectionMap.get(a).send(data);
			}
		}
	}


	
	public void sendPM(int peerID, byte[] data) throws IOException
	{
		if(this.peerConnectionMap.containsKey(peerID))
		{
			this.peerConnectionMap.get(peerID).send(data);
		}

	}

	public BM getBM()
	{
		return this.myBitMap;
	}

	public String getID()
	{
		return Integer.toString(this.myPeerID);
	}

	public void findIPeer(int peerID)
	{
		this.myInterestedNeighbours.add(new Integer(peerID));
	}

	public void findNIPeer(int peerID)
	{
		this.myInterestedNeighbours.remove(new Integer(peerID));
	}


	/**
	 * returns list of all the peers participating in file transfer
	 * @return List of peer IDs
	 */
	public List<Integer> reportconnectedlist()
	{
		return connectedPeersList;
	}

	public List<Integer> computeAndGetWastePeersList()
	{
		List<Integer> wastePeersList = new ArrayList<Integer>();
		for(Integer peerID : connectedPeersList)
		{
			if(!myBitMap.hasInterestingPiece(peerID))
			{
				wastePeersList.add(peerID);
			}
		}
		return wastePeersList;
	}
	public synchronized void addOrUpdatedownloadratePeer(Integer peerId, long elapsedTime)
	{              
		double downloadRate = (double)this.theSizeOfP /elapsedTime;
		downloadrate_peer.put(peerId, downloadRate);
	}

	public synchronized void resetdownloadrate_peer(Integer peerId)
	{          
		downloadrate_peer.put(peerId, 0.0);
	}

	public double detectTheRateOfDownload(Integer peerId)
	{
		if(this.downloadrate_peer.containsKey(peerId))
			return downloadrate_peer.get(peerId);
		else
			return -1;
	}

	public Set<Integer> getmyIN()
	{
		return this.myInterestedNeighbours;
	}

	public void findChokedPeer(Integer peerID) throws IOException, InterruptedException
	{
		this.sendPM(peerID, new ActMessage(0).getFullMessage());
		this.setofChokedPeers.add(peerID);
	}

	public void findUnchokedPeer(int peerID) throws IOException, InterruptedException
	{
		this.sendPM(peerID, new ActMessage(1).getFullMessage());
		this.setofChokedPeers.remove(peerID);
	}

	public Set<Integer> getChokedPeers()
	{
		return this.setofChokedPeers;
	}

	public int getUnchockedP()
	{
		return UnchockedP.get();
	}

	public void setUnchockedP(int peerId)
	{
		this.UnchockedP.set(peerId);
	}

	public void reportNewClientConnection(int clientID, ClientProcess aClient)
	{
		this.peerConnectionMap.put(clientID, aClient);
	}

	public void Quit()
	{
		try{

			Thread.yield();
 		}
		catch(Exception e){
		}
		scheduler.shutdown();
	}
}

