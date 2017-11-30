import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

/**
 * @author Yebowen Hu
 */

public class CommonConfig
{
    private int numPreferredNeighbours;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;
    public CommonConfig (String filename){
        String st;
        String apps[];
        Vector<String> lines = new Vector<String>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            while ((st = in.readLine()) != null) {
                lines.addElement(st);
            }
            in.close();
            apps = new String[lines.size()];
            String common[][] = new String[apps.length][2];
            for (int i = 0; i < apps.length; i++) {
                apps[i] = (String) lines.elementAt(i);
                String[] tokens = apps[i].split("\\s+");
                common[i][0] = tokens[0];
                common[i][1] = tokens[1];
            }
            this.numPreferredNeighbours = Integer.parseInt(common[0][1]);
            this.unchokingInterval = Integer.parseInt(common[1][1]);
            this.optimisticUnchokingInterval = Integer.parseInt(common[2][1]);
            this.fileName = common[3][1];
            this.fileSize = Integer.parseInt(common[4][1]);
            this.pieceSize = Integer.parseInt(common[5][1]);
        }catch (Exception e){
            System.out.println(e.toString());
        }

    }
    public int getNumPreferredNeighbours()
    {
        return numPreferredNeighbours;
    }

    public int getUnchokingInterval()
    {
        return unchokingInterval;
    }

    public int getOptimisticUnchokingInterval()
    {
        return optimisticUnchokingInterval;
    }

    public String getFileName()
    {
        return fileName;
    }

    public int getFileSize()
    {
        return fileSize;
    }

    public int getPieceSize()
    {
        return pieceSize;
    }
}

