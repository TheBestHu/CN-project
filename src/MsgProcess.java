import java.io.IOException;
import java.util.BitSet;
import java.util.Map;
import java.util.Random;

public class MsgProcess{
    //FileChunk fileChunk;
    FileProcess fp;
    private int targetId;
    //private FileOperation file;
    private int bitSetLength;
    private peerProcess peerProcess;
    public BitSet targetBitField;
    MsgProcess(int targetId,  int bitSetLength, BitSet targetBitField, peerProcess peerProcess, FileProcess fp){
    	//this.fileChunk = fileChunk;
    	this.targetId = targetId;
    	//this.file = file;
    	this.bitSetLength = bitSetLength;
    	this.targetBitField = targetBitField;
        this.peerProcess = peerProcess;
        this.fp = fp;
    }

    public void unchokeProcess(Map<Integer,Integer> StateMap) throws IOException
    {
    	int rpiece;
        while(true)
        {
            rpiece = getPiece(targetBitField);
            if(rpiece == -1)
                break;
            else if(fp.requestField[rpiece] == 0)
                break;
        }
        if (rpiece == -1)
        {
            fp.sendFile(3);
        }
        else
        {
            ActMsg m=new ActMsg(6);
            m.plusPayload(rpiece);
            StateMap.put(6,rpiece);
            fp.sendFile(m);
            StateMap.put(2, 1);
            StateMap.put(1, -1);
            fp.requestField[rpiece]=1;
        }
        peerProcess.log.unchokingLog(targetId);
    }
    public void interestProcess(Map<Integer,Integer> StateMap) throws IOException
    {
    			StateMap.put(3, 1);
            peerProcess.log.interestedReiceived(targetId);
    }

    public void uninterestProcess(Map<Integer,Integer> StateMap) throws IOException
    {
    			StateMap.put(3, -1);
            peerProcess.log.notInterestedReceived(targetId);
    }

    public void haveProcess(ActMsg m) throws IOException
    {
        int pieceNum=Byte2Int(m.messagePayload);
        peerProcess.log.haveReceived(targetId, pieceNum);
        targetBitField.flip(pieceNum);
        if (fp.getBitFd (pieceNum)) {
            fp.sendFile(3);
        } else {
            fp.sendFile(2);
        }
    }

    public void bitFieldProcess(ActMsg m) throws IOException
    {
        targetBitField = rdBitfd(m.messagePayload);
        int index = getPiece(targetBitField);
        if (index < 0) {
            fp.sendFile(3);
        } else {
            fp.sendFile(2);
        }
    }

    public void requestProcess(ActMsg m, Map<Integer,Integer> StateMap)throws IOException
    {
        int pieceNumber = Byte2Int(m.messagePayload);
        if(StateMap.get(0) !=1 && !targetBitField.get (pieceNumber))
        {
            ActMsg ms = new ActMsg(7);
            ms.plusPayload(fp.getPiece(pieceNumber));
            fp.sendFile(ms);
        }
    }

    public int pieceProcess(ActMsg m, Map<Integer,Integer> StateMap, int speed) throws IOException
    {
        byte[] fileIndex= new byte[4];
        byte[] fileContent= new byte[m.messagePayload.length-4];
        int i,index;
        i = 0;
        while (i < 4) {
            fileIndex[i]=m.messagePayload[i];
            i++;
        }
        index = Byte2Int(fileIndex);
        if(!fp.getBitFd (index))
        {
            for(i=4;i<m.messagePayload.length;i++)
                fileContent[i-4]=m.messagePayload[i];
            fp.wrtPiece(fileContent, index);
            fp.setBitFd(index);
            peerProcess.log.pieceDownloadLog(targetId, index, fp.count);
            shareInfo(index);
            StateMap.put(6, -1);
            StateMap.put(2, -1);
            fp.requestField[index]=2;
        }
        if(StateMap.get(2)!=1)
        {
            int piece;
            while(true)
            {
                piece = getPiece(targetBitField);
                if (piece == -1 || fp.requestField[piece] == 0) break;
            }
            StateMap.put(6, piece);
            if (piece == -1) {
                fp.sendFile(m);
                StateMap.put(2, -1);
            } else {
                ActMsg ms = new ActMsg(6);
                ms.plusPayload(piece);
                fp.sendFile(ms);
                StateMap.put(2, 1);
                fp.requestField[piece]=1;
            }
        }
        return (speed+1);
    }

    private BitSet rdBitfd(byte[] payload)
    {
        return BitSet.valueOf(payload);
    }

    private int getPiece(BitSet bitField)
    {
        int base = new Random().nextInt(bitSetLength);
        int i;
        for(i = 0; i < bitSetLength; i++)
            if(bitField.get ((base + i) % bitSetLength) && !fp.getBitFd ((base + i) % bitSetLength))
                return (base+i)% bitSetLength;
        return -1;
    }

    private int Byte2Int(byte[] bytes) {
        int result = 0;
        for (int i=0; i<4; i++) {
            result = ( result << 8 ) - Byte.MIN_VALUE + (int) bytes[i];
        }
        return result;
    }

    synchronized void shareInfo(int i) throws IOException {
        int j = 0;
        while ( j < peerProcess.fileShareChannelList.size () ) {
            peerProcess.fileShareChannelList.get (j).have (i);
            j++;
        }
        if (peerProcess.op != null)
            peerProcess.op.have (i);
    }
}