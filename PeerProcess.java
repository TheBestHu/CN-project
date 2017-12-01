import java.io.IOException;
import java.util.*;
import java.io.*;


/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */

public class PeerProcess
{

	public ComCon readfile(String fname)
	{
		ComCon x = new ComCon(fname);
		return x;

	}

	public Map<Integer,PeerCon> getPeerInfo(String fname) throws FileNotFoundException
	{
		BufferedReader br = new BufferedReader(new FileReader(fname));
		String st; Boolean b;
		Map<Integer,PeerCon> map = new HashMap<Integer,PeerCon>();
		try {
			while ((st = br.readLine()) != null){
				b = false;
				String[] tokens = st.split(" ");
				if(tokens[3].equals("1")) b= true;
				map.put(Integer.parseInt(tokens[0]), new PeerCon(tokens[1], Integer.parseInt(tokens[2]), b)); // <index, (hostname,port,hasfile)>
			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	public void startAllPeers(ComCon com, Map<Integer,PeerCon> map, Integer myPeerID){
		Set<Integer> s = map.keySet();	// all peer indexes
		ArrayList<Integer> myList = new ArrayList<Integer>(s);
		int num = myList.size();
		for(int n = 0 ; n < num ; n++){
			try{
				int i = myList.get(n);
				if(myPeerID == i)
					new connect(com, map,i);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}


	}
	public static void main(String []args) throws IOException 
	{
		Integer myPeerID = Integer.parseInt(args[0]);	// read in my peerID
		PeerProcess p = new PeerProcess();
		ComCon config = p.readfile("Common.cfg");
		Map<Integer,PeerCon> map = p.getPeerInfo("PeerInfo.cfg");
		System.out.println("Starting peers");
		p.startAllPeers(config,map,myPeerID);
	}
}
