import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by xing he on 2017/3/8.Project for Computer Networks Course.
 */
public class peerProcess {
    private int numOfPN;
    private int UnChokingInterval;
    private int opUnChokingInterval;
    //private FileChunk fileChunk;
    private FileProcess fp;
    public Log log; // xhw
    private int fileExist;
    public LinkedList <Peer> fileShareChannelList = new LinkedList <> ();
    public Peer op = null;
    private int nTime;
    private int opnTime;
    private boolean aBoolean = false;
    public LinkedList <Peer> tempList = new LinkedList <> ();
    public static void main (String[] args) throws Exception {
        new peerProcess (args[0]);
    }

    private peerProcess(String ID) throws Exception {

        readFile(ID);
        startThread();

        while ( true ) {
            Thread.sleep (cycleTimer());
            if (checkList()) {
                Thread.sleep (1000); 
                log.completeLog(); // xhw
                break;
            }

            if (nTime < 0.1) {
                if (fileShareChannelList.size () != 0) {
                    LinkedList <Integer> l = new LinkedList <> ();
                    l = chokeJudge();
                    if (l.size () != 0) {
                        log.changeOfPreNbLog (l); // xhw
                    }
                }
                nTime = UnChokingInterval;
                sortList(fileShareChannelList);
            }
            if (opnTime < 0.1) {
                chooseOp();
                opnTime = opUnChokingInterval;
            }
        }
    }


    private void sortList(LinkedList<Peer> fileShareChannelList) {
        Comparator <Peer> comparator = new Comparator <Peer> () {
            public int compare (Peer p1, Peer p2) {
                if (p1.getSpeed() > p2.getSpeed())
                    return 1;
                else if (p1.getSpeed() < p2.getSpeed())
                    return -1;
                else
                    return 0;
            }
        };
        fileShareChannelList.sort (comparator);
    }

    private synchronized void chooseOp() throws IOException {  // xhw
        if(op != null){
            op.choke();
            fileShareChannelList.add(op);
            op = null;
        }
        for(Peer temp : fileShareChannelList){
            if(temp.StateMap.get(0) != -1){
                op = temp;
                op.unchoke();
                log.changeOfOpUnchokeNbLog (op.targetId);
                log.unchokingLog (op.targetId);
                fileShareChannelList.remove(temp);
                return;
            }
        }
    }

    private synchronized boolean checkList() {
        return fileShareChannelList.size () == 0 && op == null;
    }

    private int cycleTimer() {
        int time;
        time = Math.min(opnTime, nTime); 
        nTime = nTime - time;
        opnTime = opnTime - time;
        return time * 1000;
    }

    synchronized void quit(Peer p) throws IOException {
        if (!aBoolean) {
            aBoolean = true;
        }
        if (op == p) {
            op = null;
            opnTime = 0;
        } else {
            fileShareChannelList.remove (p);
            nTime = 0;
        }
    }

   private void readFile(String ID) throws Exception{
        //Read file ****************************************************************************
        log = new Log(ID);
        int peerId = Integer.parseInt(ID);
        String[] arr;
        BufferedReader CommonRead = new BufferedReader(new FileReader (new File("Common.cfg")));
        arr = CommonRead.readLine().split(" ");
        numOfPN = Integer.parseInt(arr[1]);
        arr = CommonRead.readLine().split(" ");
        UnChokingInterval = Integer.parseInt(arr[1]);
        arr = CommonRead.readLine().split(" ");
        opUnChokingInterval = Integer.parseInt(arr[1]);
        arr = CommonRead.readLine().split(" ");
        String fileName = arr[1];
        arr = CommonRead.readLine().split(" ");
        int fileSize = Integer.parseInt(arr[1]);
        arr = CommonRead.readLine().split(" ");
        int pieceSize = Integer.parseInt(arr[1]);
        nTime = UnChokingInterval;
        opnTime = opUnChokingInterval;
        Path wiki_path = Paths.get("/cise/homes/chilee/Desktop/Network/", "PeerInfo.cfg");
        Charset charset = Charset.forName("ISO-8859-1");
        List<String> lines = Files.readAllLines(wiki_path, charset);
        String[] stringArray;

        int id;
        for(int i = 0; i < lines.size() && lines.get(i) != null && !Objects.equals("",lines.get(i)); i++)
        {
            stringArray = lines.get(i).split(" ");
            id = Integer.parseInt(stringArray[0]);
            if(id == peerId) {
                fileExist = Integer.parseInt(stringArray[3]);
            }
        }
        //fileChunk = new FileChunk(peerId, fileName, fileExist, fileSize, pieceSize);
        //Set the values readed from peerInfo.cfg and create socket(XH)
        CreateSocket(lines, peerId, fileSize, pieceSize, fileName);
        //Read file over ****************************************************************************
    }
    // xhw
    private void CreateSocket(List<String> lines, int peerId, int fileSize, int pieceSize, String fileName) throws Exception {
        String[] stringArray;
        int id;
        ServerSocket serverSocket = null;
        for (String line : lines) {
            stringArray = line.split(" ");
            id = Integer.parseInt(stringArray[0]);
            String ipAddress = stringArray[1];
            int port = Integer.parseInt(stringArray[2]);
            //Get the portNumber of this peerId
            Peer fileShareChannel;
            //xhw
            if(id < peerId){
                Socket socket = new Socket(ipAddress,port);
                fp = new FileProcess(peerId, fileName, fileExist, fileSize, pieceSize, socket); // xhw
                fileShareChannel = new Peer(peerId,id,socket,fileSize,pieceSize,this, fp);
                fileShareChannelList.add(fileShareChannel);
                tempList.add(fileShareChannel);
                log.cnted(id);
            }
            else if(id > peerId){
                Socket socket = null;
                if (serverSocket != null) {
                    socket = serverSocket.accept();
                }
                fp = new FileProcess(peerId, fileName, fileExist, fileSize, pieceSize, socket); // xhw
                fileShareChannel = new Peer(peerId, id, socket, fileSize, pieceSize, this, fp);
                fileShareChannelList.add(fileShareChannel);
                tempList.add(fileShareChannel);
                log.cnct(id);
            }
            else {
                serverSocket = new ServerSocket(port);
            }
        }
    }
    // xhw

    private LinkedList<Integer> chokeJudge() throws IOException {
        LinkedList<Integer> list = new LinkedList<>();
        if (fileShareChannelList.size () <= numOfPN) {
            for (Peer aPeerlist : fileShareChannelList) {
                if (aPeerlist.StateMap.get(0) != -1) {
                    aPeerlist.unchoke();    // xhw
                    log.unchokingLog(aPeerlist.targetId); // xhw
                    list.add(aPeerlist.targetId);
                }
            }
        }
        else {
            int i = 0;
            while ( i < fileShareChannelList.size () ) {
                if (i < numOfPN) {
                    if (fileShareChannelList.get (i).StateMap.get(0) != -1) {
                        fileShareChannelList.get (i).unchoke ();  // xhw
                        log.unchokingLog (fileShareChannelList.get (i).targetId);   // xhw
                        list.add (fileShareChannelList.get (i).targetId);
                    }
                } 
                else {
                    if (fileShareChannelList.get (i).StateMap.get(0) != 1) {
                        fileShareChannelList.get (i).choke ();   //xhw
                        log.chokingLog (fileShareChannelList.get (i).targetId); // xhw
                    }
                }
                i++;
            }
        }
        return list;
    }


    private void startThread() {
        for ( Peer peers : fileShareChannelList ) {
            new Thread (peers).start ();
        }
    }

}


