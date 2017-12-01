import java.io.*;

/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */
public class fileinout
{
    private File LocalFile;
    private int SizeofFile;
    private int SizeOfPiece;
    private final int SIZEOFEACHCHUNK = 1 << 26; 

    public fileinout(String PathofFile, int SizeofFile, int SizeofPiece) throws FileNotFoundException
    {
        this.LocalFile = new File(PathofFile);
        this.SizeofFile = SizeofFile;
        this.SizeOfPiece = SizeofPiece;
    }
   

    public boolean createFile() throws IOException{
        if(LocalFile.exists()){
            System.out.println("File already exists, not able to create file");
            return false;
        }
        if(LocalFile.createNewFile()){
            FileOutputStream FO = new FileOutputStream(LocalFile);
            byte[] temp = new byte[SIZEOFEACHCHUNK];
            int i = SIZEOFEACHCHUNK;
            while(i < SizeofFile){
                FO.write(temp);
                i += SIZEOFEACHCHUNK;
            }
            i -= SIZEOFEACHCHUNK;
            int remain = SizeofFile - i;
            temp = new byte[remain];
            FO.write(temp);
            FO.close();
            return true;
        }
        else{
            return false;
        }
    }
   

    public byte[] getChunk(int pieceID) throws IOException{
        RandomAccessFile file =new RandomAccessFile(LocalFile, "r");
        long start = (long)pieceID* SizeOfPiece;
        file.seek(Math.max(start,0));
        long trueSize;
        if((file.length()-start)< SizeOfPiece){
            trueSize = file.length()-start;
        }
        else{
            trueSize = SizeOfPiece;
        }
        byte[] temp = new byte[(int)trueSize];
        file.read(temp);
        file.close();
        return temp;
    }


    public void writeFilePiece(int pieceID, byte[] pieceData) throws IOException
    {
        RandomAccessFile ran = new RandomAccessFile(this.LocalFile, "rw");
        ran.seek(Math.max(pieceID * SizeOfPiece, 0));
        ran.write(pieceData);
        ran.close();
    }    
}

