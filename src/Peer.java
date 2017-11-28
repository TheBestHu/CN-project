import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xinghe on 2017/4/8.
 */


public class Peer implements Runnable{
    int targetId;
    private int id;
    private BitSet targetBitField;
    private int bitSetLength;
    //private FileChunk fileChunk;
    private FileProcess fp;
    private peerProcess peerProcess;
    InputStream inputStream;
    OutputStream outputStream;
    Map<Integer, Integer> StateMap; 
    //int[] stateArray;
    private ArrayList<Integer> haveList = new ArrayList <>();
    //private FileOperation file;
    private StateMachine stateMachine;
    Peer(int id, int targetId, Socket socket, int fileSize, int chunkSize, peerProcess peerProcess, FileProcess fp) throws IOException
    {
        this.id = id;
        this.targetId = targetId;
        //this.fileChunk = fileChunk;
        this.fp = fp;
        this.peerProcess = peerProcess;
        bitSetLength = (int)Math.ceil((double)fileSize/(double)chunkSize);
        targetBitField = new BitSet(bitSetLength);
        inputStream = socket.getInputStream ();
        outputStream = socket.getOutputStream();
        // file = new FileOperation(inputStream, outputStream);
        this.StateMap = new HashMap<>();
        this.StateMap.put(0, 0);
        this.StateMap.put(1, 0);
        this.StateMap.put(2, 0);
        this.StateMap.put(3, 0);
        this.StateMap.put(4, -1);
        this.StateMap.put(5, 0);
        this.StateMap.put(6, -1);        
        //stateArray = new int[] {0, 0, 0, 0, -1, 0, -1};
        stateMachine = new StateMachine(StateMap, targetId, bitSetLength, targetBitField, peerProcess, peerProcess.log, fp);
    }

    @Override
    public void run() {
        try {
            fp.sendHS(id, peerProcess.log, targetId);
            sendBitField();
            do {
                stateChecking();
                if (stateMachine.fp.checkBitFd() && checkBitFd(stateMachine.targetBitField) && stateMachine.StateMap.get(5) != 1) {
                    Thread.sleep(1000);
                    peerProcess.quit(this);
                    break;
                }

                if (inputStream.available() == 0) {
                    Thread.sleep(50);
                    continue;
                }

                byte[] receiveMessage = fp.receiveFile();
                ActMsg msg = byteToMsg(receiveMessage);
                if (StateMap.get(1) == 1 && StateMap.get(6) != -1) {
                    StateMap.put(2, -1);
                    fp.requestField[StateMap.get(6)] = 0;
                    StateMap.put(6, -1);
                }

                if (StateMap.get(6) != -1 && msg.messageType == 0) {
                    StateMap.put(2, -1);
                    fp.requestField[StateMap.get(6)] = 0;
                    StateMap.put(6, -1);
                }
                stateMachine.operationChoosing(msg);
            } while (true);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized ActMsg byteToMsg(byte[] rcv){
        int messageType,i;
        byte[] payload=new byte[rcv.length-5];
        messageType=(int)rcv[4];
        i=5;
        while (i < rcv.length) {
            payload[i-5]=rcv[i];
            i++;
        } 
        ActMsg msg = new ActMsg(messageType, payload);
        return msg;
    }
    // xhw
    synchronized void choke()
    {
        StateMap.put(0, 1);
        StateMap.put(4, 1);

    }
    synchronized void unchoke()
    {
    	StateMap.put(0, -1);
    	StateMap.put(4, 2);
    }
    // xhw
    synchronized int getSpeed()
    {
        int periodSpeed = stateMachine.speed;
        stateMachine.speed = 0;
        return periodSpeed;
    }

    synchronized void have(int i) throws IOException
    {
        haveList.add(i);
        StateMap.put(5, 1);  // bitfield has changed
    }

    private synchronized void stateChecking() throws IOException
    {
        switch (StateMap.get(4)) {
            case 0:
                break;
            case 1: {
                fp.sendFile (0);
                StateMap.put(4, 0);
                break;
            }
            case 2: {
                fp.sendFile (1);
                StateMap.put(4, 0);
                break;
            }
        }
        if (StateMap.get(5) == 1) {
            if (haveList.size() != 0) {
                do {
                    ActMsg m = new ActMsg(4);
                    m.plusPayload(haveList.get(0));
                    fp.sendFile(m);
                    haveList.remove(0);
                } while (haveList.size() != 0);
            }
            StateMap.put(5, -1); // bitfields updated
        }
    }

    private synchronized boolean checkBitFd(BitSet bitField)
    {
        for(int i=0;i<bitSetLength;i++)
            if(!bitField.get(i))
                return false;
        return true;
    }

    private synchronized void sendBitField() throws IOException {
        byte[] sendBitField = fp.bitfdToByte ();
        ActMsg bitFieldMsg = new ActMsg (5, sendBitField);
        fp.sendFile (bitFieldMsg);
    }
}
