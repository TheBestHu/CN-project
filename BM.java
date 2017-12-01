import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */
public class BM
{
	private final String fN;

	private Map<Integer, byte[]> IDtoPiece;
	private Map<Integer, AtomicInteger> IDtoCountdown;
	private Set<Integer> AskedForPeer;
	private fileinout theFileHand;
	private final String myDirectory;
	private final int myPeerID;
	private final int EntireRPiece;
	private byte[] EntireBM;
	private connect thisMConnect;

	public BM(int myPeerID, ComCon myCommonConfig, Set<Integer> peerConfigIDs,
			  boolean doIHaveFile, connect thisMConnect, Map<Integer,PeerCon> map) throws IOException
	{

		this.thisMConnect = thisMConnect;
		this.myPeerID = myPeerID;
		this.fN = myCommonConfig.getfName();
		this.IDtoPiece = new ConcurrentHashMap<Integer, byte[]>();
		this.IDtoCountdown = new ConcurrentHashMap<Integer, AtomicInteger>();
		this.AskedForPeer = new ConcurrentSkipListSet<Integer>();
		this.EntireRPiece = Divide(myCommonConfig.getSizeOfFile(), myCommonConfig.getSizeForEachPiece()); //xhw
		//(int)Math.ceil((double)myCommonConfig.getSizeOfFile() / myCommonConfig.getSizeForEachPiece());

		int totalBytesRequiredForPieces = Divide(EntireRPiece,8); //xhw
		//(int)Math.ceil((double)EntireRPiece / 8);

		//initialize maps with all peerIDs (including mine) and 0s in value field
		for(Integer PeerID : peerConfigIDs)
		{
			this.IDtoPiece.put(PeerID, new byte[totalBytesRequiredForPieces]);
			this.IDtoCountdown.put(PeerID, new AtomicInteger(0));
		}
		this.myDirectory = System.getProperty("user.dir") + "/peer_" + myPeerID;

		this.theFileHand = new fileinout(myDirectory + "/" + myCommonConfig.getfName(),
				myCommonConfig.getSizeOfFile(), myCommonConfig.getSizeForEachPiece());

		//create the BitMap which will be finally required

		this.EntireBM = getEntireBM(EntireRPiece);
		
		//create a dummy file on disk for storage if I don't have a complete file
		if(!doIHaveFile)
		{
			System.out.println("create dummy file created");
			this.theFileHand.createFile();
		}
		else
		{
			this.IDtoPiece.put(myPeerID, this.EntireBM);

		}
		System.out.println("BitMap created");
	}

	private static int Divide(int dividend, int divisor)
	{
		int result;
		result = dividend / divisor;
		return result * divisor == dividend ? result : result + 1;
	}

	private byte[] getEntireBM(int totalPiecesRequired)
	{
		//add 1 to all of bits in EntireBM
		int len;
		int temp = totalPiecesRequired/8;
		if(temp*8==totalPiecesRequired){
			len = temp;
		}
		else{
			len = temp+1;
		}
		//(int)Math.ceil((double)EntireRPiece/8);

		byte[] fullBitMap = new byte[len];
		for(int i = 0; i < len; i++)
		{
			fullBitMap[i] = (byte)0xFF;
		}
		int lastBytePieces = totalPiecesRequired & 7;   //EntireRPiece & 7 = EntireRPiece % 8
		if(lastBytePieces > 0)  //then zero-filling is required
		{
			fullBitMap[len - 1] = (byte)(fullBitMap[len - 1]&0xFF >>> (8 - lastBytePieces));
		}
		return fullBitMap;
	}


	public void reportPieceReceived(int pieceID, byte[] pieceData) throws IOException {
		theFileHand.writeFilePiece(pieceID, pieceData);
		synchronized (this) {
			byte[] myFileBitMap = this.IDtoPiece.get(myPeerID);
			this.updateBitMapWithPiece(myFileBitMap, pieceID);
			this.IDtoCountdown.get(myPeerID).addAndGet(1);
			// findBits(myPeerID);
		}

		if (canIQuit()) {
			System.out.println("quit from reportPieceReceived");

		}

	}

	public byte[] getPieceData(int pieceIndex) throws IOException
	{
		return theFileHand.getChunk(pieceIndex);
	}
	
	public byte[] getMyFileBitMap()
	{
		return this.IDtoPiece.get(myPeerID);
	}

	public int getTotalPieceCount()
	{
		return EntireRPiece;
	}
	

	public int getDownloadedPieceCount(int peerID)
	{
		return this.IDtoCountdown.get(peerID).get();
	}
	
	public byte[] getPeerBitMap(int peerID)
	{
		return this.IDtoPiece.get(peerID);
	}

	public synchronized void setPeerBitMap(int peerID, byte[] BitMap)
	{
		this.IDtoPiece.put(peerID, BitMap);
	}

	public String getfN()
	{
		return fN;
	}


	public void reportPeerPieceAvailablity(int peerID, int pieceIndex)
	{
		synchronized(this)
		{
			byte[] peerFileBitMap = this.IDtoPiece.get(peerID);
			updateBitMapWithPiece(peerFileBitMap, pieceIndex);
			this.IDtoCountdown.get(peerID).addAndGet(1);
		}
	
		if(canIQuit())
		{
			System.out.println("quit from reportPeerPieceAvailablity");

		}
	}

	public boolean canIQuit()
	{
		for(byte[] aBitMap : this.IDtoPiece.values())
		{
			if(!isBitMapFinal(aBitMap))
			{
				return false;
			}
		}
		return true;
	
	}

	private boolean isBitMapFinal(byte[] BitMap)
	{
		int len = BitMap.length;
		for(int i=0;i<len;i++){
			if(BitMap[i] != EntireBM[i])
				return false;
		}
		return true;
	}


	public boolean doIHavePiece(int pieceIndex)
	{
		byte[] myFileBitMap = this.IDtoPiece.get(myPeerID);
		int pieceLocation = pieceIndex / 8;
		int bitLocation = pieceIndex & 7;   // = pieceIndex % 8
		if((myFileBitMap[pieceLocation] & (1 << bitLocation)) != 0)  // == 0 means we don't have that piece
		{
			return true;
		}
		return false;
	}

	private void updateBitMapWithPiece(byte[] peerFileBitMap, int pieceIndex)
	{
		int pieceLocation = pieceIndex / 8;
		int bitLocation = pieceIndex & 7;   // = pieceIndex % 8
		peerFileBitMap[pieceLocation] |= (1 << bitLocation);
	}

	
	public boolean doIHaveAnyPiece()
	{
		byte[] myFileBitMap = this.IDtoPiece.get(myPeerID);
		final int mask = 0x000000FF;
		final int len = myFileBitMap.length;
		for(int i = 0; i < len; i++)
		{
			if((myFileBitMap[i] & mask) != 0)
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasInterestingPiece(int anotherPeerID)
	{
		// showBitMap();
		byte[] myFileBitMap = this.IDtoPiece.get(myPeerID);
		final int len = myFileBitMap.length;
		byte[] peerFileBitMap = this.IDtoPiece.get(anotherPeerID);
		// findBits(anotherPeerID);
		for(int i = 0; i < len; i++)
		{
			if(myFileBitMap[i] != peerFileBitMap[i])
				return true;
		}
		return false;
	}

	public int getPeerPieceIndex(int peerID)
	{
		// showBitMap();
		byte[] myFileBitMap = this.IDtoPiece.get(myPeerID);
		int desiredPieceID = -1;        
		//check the first available piece which is not requested, if found add to requested piece list
		byte[] peerBitMap = this.IDtoPiece.get(peerID);
		final int len = myFileBitMap.length;
		List<Integer> possiblePieces = new ArrayList<Integer>();
		
		for(int i = 0; i < len; i++)
		{
			if(myFileBitMap[i] != peerBitMap[i])
			{
				for(int j = 0; j < 8; j++)
				{
					//if peer has the piece and I don't have it, request it
					if((myFileBitMap[i] & (1 << j)) == 0 && (peerBitMap[i] & (1 << j)) != 0)
					{
						int attemptedPieceIndex = i*8 + j;
						if(this.AskedForPeer.contains(attemptedPieceIndex))
							desiredPieceID = -1;
						else
							desiredPieceID = attemptedPieceIndex;

						if (desiredPieceID != -1)
							possiblePieces.add(desiredPieceID);
					}
				}
			}
		}
		if(possiblePieces.size() ==0){
			return -1;
		}
		else{
			Random rand = new Random();
			int idx = rand.nextInt(possiblePieces.size());
			// access that element from the possiblePieces list and return
			int pieceIndex = possiblePieces.get(idx);
			this.AskedForPeer.add(pieceIndex);
			return pieceIndex;
		}
	}
}
