import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */

public class ComCon
{
    private int NumberOfPN;
    private int IntOfUnchocking;
    private int ChooseTheONInt;
    private String fName;
    private int SizeOfFile;
    private int SizeForEachPiece;
    public ComCon (String filename){
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
            this.NumberOfPN = Integer.parseInt(common[0][1]);
            this.IntOfUnchocking = Integer.parseInt(common[1][1]);
            this.ChooseTheONInt = Integer.parseInt(common[2][1]);
            this.fName = common[3][1];
            this.SizeOfFile = Integer.parseInt(common[4][1]);
            this.SizeForEachPiece = Integer.parseInt(common[5][1]);
        }catch (Exception e){
            System.out.println(e.toString());
        }

    }
    public int getNumberOfPN()
    {
        return NumberOfPN;
    }

    public int getIntOfUnchocking()
    {
        return IntOfUnchocking;
    }

    public int getChooseTheONInt()
    {
        return ChooseTheONInt;
    }

    public String getfName()
    {
        return fName;
    }

    public int getSizeOfFile()
    {
        return SizeOfFile;
    }

    public int getSizeForEachPiece()
    {
        return SizeForEachPiece;
    }
}

