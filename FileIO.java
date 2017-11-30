import java.io.*;

/**
 * 
 * @author Huanwen by 11/30/2017
 *
 */
public class FileIO
{
    private File myFile;
    private int myFileSize;    
    private int myPieceSize;
    private final int SIZEOFEACHCHUNK = 1 << 26; 

    public FileIO(String PathofFile, int SizeofFile, int SizeofPiece) throws FileNotFoundException
    {
        this.myFile = new File(PathofFile);
        this.myFileSize = SizeofFile;
        this.myPieceSize = SizeofPiece;
    }
   
    /**
     * if file is not present then a dummy file is created 
     * size of dummy file is same as size of file to be sent
     * @return T/F depending on whether file is created or not
     * @throws IOException
     */
    
    public boolean createDummyFile() throws IOException{
        if(myFile.exists()){
            System.out.println("File already exists, not able to create dummyfile");
            return false;
        }
        if(myFile.createNewFile()){
            FileOutputStream FO = new FileOutputStream(myFile);
            byte[] temp = new byte[SIZEOFEACHCHUNK];
            int i = SIZEOFEACHCHUNK;
            while(i < myFileSize){
                FO.write(temp);
                i += SIZEOFEACHCHUNK;
            }
            i -= SIZEOFEACHCHUNK;
            int remain = myFileSize - i;
            temp = new byte[remain];
            FO.write(temp);
            FO.close();
            return true;
        }
        else{
            return false;
        }
    }
   
    /**
     * given a pieceID returns the corresponding chunk
     * data should be already present in file else request message wouldnot come
     * @param pieceID
     * @return
     * @throws IOException
     */

    public byte[] getChunk(int pieceID) throws IOException{
        RandomAccessFile file =new RandomAccessFile(myFile, "r");
        long start = (long)pieceID*myPieceSize;
        file.seek(Math.max(start,0));
        long trueSize;
        if((file.length()-start)< myPieceSize){
            trueSize = file.length()-start;
        }
        else{
            trueSize = myPieceSize;
        }
        byte[] temp = new byte[(int)trueSize];
        file.read(temp);
        file.close();
        return temp;
    }


    public void writeFilePiece(int pieceID, byte[] pieceData) throws IOException
    {
        RandomAccessFile ran = new RandomAccessFile(this.myFile, "rw");
        ran.seek(Math.max(pieceID *myPieceSize, 0));
        ran.write(pieceData);
        ran.close();
    }    
}

