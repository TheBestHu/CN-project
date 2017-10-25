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

    }

    public void readpeer(String filepath) throws IOException{

    }

}
