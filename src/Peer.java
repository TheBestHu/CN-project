/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.HashMap;
/**
 *
 * @author Huanwen Xu,Yebowen Hu and Yajing Fang
 */
public class Peer implements Runnable{
    int targetID;
    private int id;
    private BitSet targetBitField;
    private int bitSetLength;
    private peerProcess peerProcess;
    InputStream inputstream;
    private ArrayList<Integer> haveList = new ArrayList<>();
    int[] stateList; // state list for statemachine to control the operations of peer.
    StateMachine stateMachine;
    /**
     *
     * @param id
     * @param targetid
     * @param socket
     * @param fileSize
     * @param chunkSize
     * @param peerprocess
     * @throws IOException
     */
    Peer(int id, int targetid, Socket socket, int fileSize, int chunkSize, peerProcess peerprocess) throws IOException{
        this.id =id;
        this.targetID = targetid;
        this.peerProcess =peerprocess;
        bitSetLength = Operation.upperDiv(fileSize,chunkSize);
        targetBitField = new BitSet(bitSetLength);
        inputstream = socket.getInputStream ();
        OutputStream outputstream = socket.getOutputStream();
        stateList = new int[]{0,0,0,0,-1,0,-1};
        /**
         * build socket operate each peer file.
         * @parameter file
         */
    }
    
    @Override
    public void run(){
    }

    void have(int i) throws IOException{
        haveList.add(i);
        stateList[5] = 1;
    }

    void choke(boolean b) {
        if(b){
            stateList[0]=1;
            stateList[4]=1; //change statemachine status for peer operation to file
        }
    }

    int getSpeed() {
        int periodSpeed = stateMachine.speed;
        stateMachine.speed = 0;
        return periodSpeed;
    }
}
