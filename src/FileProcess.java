import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.stream.IntStream;

class FileProcess {
    private InputStream inputStream;
    private OutputStream outputStream;
    private int chunkSize;
    private int fileSize;
    private RandomAccessFile randomAccessFile;
    private BitSet bitField;
    int[] requestField;
    private int bitSetLength;
    int count = 0;

    FileProcess(int peerID, String fileName, int exist, int fileSize, int chunkSize, Socket socket) throws IOException {
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        String fileName1 = "/cise/homes/chilee/Desktop/Network/peer_" + peerID + "/" + fileName;
        this.fileSize = fileSize;
        this.chunkSize = chunkSize;
        bitSetLength = (int) Math.ceil((double)fileSize/(double)chunkSize);
        File place = new File("/cise/homes/chilee/Desktop/Network/peer_"+ peerID +"/");
        if (! place.exists()) place.mkdirs();

        randomAccessFile = new RandomAccessFile(fileName1, "rw");
        bitField = new BitSet(bitSetLength);
        requestField = new int[bitSetLength];
       
        if(exist == 1){
            bitField.set(0,bitSetLength);
            count = bitSetLength;
        }
        else{
            /*
            for(int j = 0; j<=bitSetLength;j++){
                bitField.set(j,false);
            }
            */
            bitField.set(0,bitSetLength, false);    // xhw
            count = 0;
        }

        int i = 0;
        while(i<bitSetLength){
            requestField[i]=0;
            i++;
        }
    }
    synchronized void sendFile(ActMsg actMsg) throws IOException {
        byte[] msg = actMsg.msgPack();
        outputStream.write(msg);
        outputStream.flush();
    }


    synchronized void sendFile(int code) throws IOException{
        ActMsg actMsg = new ActMsg(code);
        byte[] msg = actMsg.msgPack();
        outputStream.write(msg);
        outputStream.flush();
    }
// xhw
    synchronized void sendHS(int sourceid, Log log, int targetId) throws IOException{
        HandShakeMessage HSMessage = new HandShakeMessage (sourceid);
        byte[] byte_msg = HSMessage.handShakeMessageToByte();
        outputStream.write(byte_msg);
        outputStream.flush();
        byte[] rev = new byte[32];
        inputStream.read(rev, 0, 32);
        HandShakeMessage revMsg = new HandShakeMessage (rev);
        if (revMsg.handShake_Header.equals ("P2PFILESHARINGPROJ"))
            if (revMsg.peerId == targetId) {
                log.cnted (targetId);
            }
    }

    synchronized byte[] receiveFile() throws IOException{
        byte[] length = new byte[4];
        byte[] outA;
        int rev, total=0;
        while(true)
        {
            if ((total < 4)) {
                rev = inputStream.read (length, total, 4 - total);
                total = total + rev;
            } else {
                break;
            }
        }
        int fileLength = Byte2Int(length);
        outA = new byte[fileLength];
        total=0;

        while(total<fileLength)
        {
            rev = inputStream.read(outA, total, fileLength-total);
            total = total + rev;
        }
        ///hybw
        byte[] out = new byte[4+fileLength];
        for(int i=0;i<4+fileLength;i++){
            out[i] = i<4 ? length[i] : outA[i-4];
        }
        return out;
    }

    private int Byte2Int(byte[] bytes) {
        int result = 0;
        for (int i=0; i<4; i++) {
            result = ( result << 8 ) - Byte.MIN_VALUE + (int) bytes[i];
        }
        return result;
    }

    byte[] bitfdToByte() {
        return bitField.toByteArray();
    }

    boolean checkBitFd() { 
        return IntStream.range(0, bitSetLength).allMatch(i -> bitField.get(i)); // all true return true
    }

    void wrtPiece(byte[] content, int index) throws IOException {
        randomAccessFile.seek((long) index * chunkSize);
        randomAccessFile.write(content);
        count++;
    }


    byte[] getPiece(int index) throws IOException {
        int n=4;
        int Size = index == bitSetLength - 1 ? fileSize - index * chunkSize : chunkSize;
        byte[] payload = new byte[Size + n];
        byte[] chunkN = new byte[Size];
        byte[] indexToByte;
        indexToByte = new byte[n];
        int number = 16777216;
        for(int i =n;i>0;i--){
            indexToByte[n-i] = (byte) (index/number);
            number = number/256;
        }
        long readPlace = (long) index*chunkSize;
        randomAccessFile.seek(readPlace);
        randomAccessFile.read(chunkN);
        System.arraycopy(indexToByte, 0, payload, 0, n);
        System.arraycopy(chunkN, 0, payload, n, payload.length - n);
        return payload;
    }


    boolean getBitFd(int i) {
        return bitField.get(i);
    }

    void setBitFd(int i) {
        bitField.set(i, true);
    }

}
