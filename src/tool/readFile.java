package tool;

import java.io.*;
import java.io.BufferedReader;
import java.util.*;
import java.util.HashMap;
import java.util.Map;

/**
 * read each file (common.cfg and peerInfo.cfg) return each in subclass.
 */

public class readFile {
    public int NumberOfPreferredNeighbors;
    public int UnchokingInterval;
    public int OptimisticUnchockingInterval;
    public String FileName;
    public int FileSize;
    public int PieceSize;

    public void readcommon() throws IOException{
        String filename = "common.cfg";
        FileReader FR = new FileReader(filename);
        BufferedReader common = new BufferedReader(FR);
        String content = common.readLine();
        HashMap<String,String> commonInfo = new HashMap<>();
        /*
        read common.cfg line by line
         */
        while(content!=null){
            String[] split = content.split(" ");
            commonInfo.put(split[0],split[1]);
        }
        /**
         * commone parameter
         *  @NumberOfPreferredNeighbors,UnchokingInterval,OtimisticUnchockingInterval
         *  @FileName,FileSize and @PieceSize
         */
        NumberOfPreferredNeighbors = Integer.parseInt(commonInfo.get("NumberOfPreferredNeighbors"));
        UnchokingInterval = Integer.parseInt(commonInfo.get("UnchokingInterval"));
        OptimisticUnchockingInterval = Integer.parseInt(commonInfo.get("OptimisticUnchockingInterval"));
        FileName = commonInfo.get("FileName");
        FileSize = Integer.parseInt(commonInfo.get("FileSize"));
        PieceSize = Integer.parseInt(commonInfo.get("PieceSize"));
        common.close();
    }
    /**
     * this part content should be modified after know how to connect peers and that sort of things
     * @param filepath
     * @throws IOException
     */
    public int[] peerID;
    public String[] address;
    public int[] portNo;
    /**
     * file determine parameters fileD
     */
    public int[] fileD;
    public void readpeer(String filepath) throws IOException{
        String filename = "PeerInfo.cfg";
        FileReader FR = new FileReader(filename);
        BufferedReader peerInfo = new BufferedReader(FR);
        String content = peerInfo.readLine();
        while(content!=null){
            int num = 0;
            String[] info = content.split(" ");
            peerID[num] = Integer.parseInt(info[0]);
            address[num] = info[1];
            portNo[num] = Integer.parseInt(info[2]);
            fileD[num] = Integer.parseInt(info[3]);
        }
        peerInfo.close();
    }

}
