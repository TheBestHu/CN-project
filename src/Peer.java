/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;
import java.net.*;
import java.util.*;
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
    
    Peer(int id, int targetid, Socket socket, int fileSize, int chunkSize, peerProcess peerprocess) throws IOException{
        this.id =id;
        this.targetID = targetid;
        this.peerProcess =peerprocess;
        bitSetLength = Operation.upperDiv(fileSize,chunkSize);
        targetBitField = new BitSet(bitSetLength);
        inputstream = socket.getInputStream ();
    }
    
    @Override
    public void run(){
    }
}
