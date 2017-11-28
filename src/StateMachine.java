import java.io.IOException;
import java.util.BitSet;
import java.util.Map;
import java.util.Random;

/**
 * Created by xinghe on 2017/3/8.Project for Computer Networks Course.
 */
public class StateMachine {
    int speed = 0;
    //int[] stateArray;
    Map<Integer, Integer> StateMap;
    //FileChunk fileChunk;
    FileProcess fp;
    private int targetId;
    // private FileOperation file;
    private int bitSetLength;
    private peerProcess peerProcess;
    public BitSet targetBitField;
    private Log log;

    StateMachine(Map<Integer,Integer> StateMap, int targetId, int bitSetLength, BitSet targetBitField, peerProcess peerProcess, Log log, FileProcess fp) {
        this.StateMap = StateMap;
        //this.fileChunk = fileChunk;
        this.targetId = targetId;
        // this.file = file;
        this.bitSetLength = bitSetLength;
        this.targetBitField = targetBitField;
        this.peerProcess = peerProcess;
        this.log = log;
        this.fp = fp;
    }

    void operationChoosing(ActMsg msg) throws IOException {
        MsgProcess mp = new MsgProcess(targetId,bitSetLength,targetBitField, peerProcess, fp);
        if (msg.messageType == 0) {
            log.chokingLog(targetId);
            StateMap.put(1, 1);
        } else if (msg.messageType == 1) {
            mp.unchokeProcess(StateMap);

        } else if (msg.messageType == 2) {
            mp.interestProcess(StateMap);
            
        } else if (msg.messageType == 3) {
            mp.uninterestProcess(StateMap);
            
        } else if (msg.messageType == 4) {
            mp.haveProcess(msg);

        } else if (msg.messageType == 5) {
            mp.bitFieldProcess(msg);

        } else if (msg.messageType == 6) {
            mp.requestProcess(msg, StateMap);

        } else if (msg.messageType == 7) {
            speed = mp.pieceProcess(msg, StateMap, speed);
        }
    }
}
