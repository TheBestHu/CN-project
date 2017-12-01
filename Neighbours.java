import java.io.IOException;
import java.util.*;
/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */
public class Neighbours implements Runnable
{
	private connect myConnection;
	private Set<Integer> PrepreferredPeerIDSet;
	WriteLog w = new WriteLog();

	public Neighbours(connect myConnection)
	{
		this.myConnection = myConnection;
		this.PrepreferredPeerIDSet= new TreeSet<Integer>();
	}

	public void run()
	{
		try
		{
			System.out.println("Changing Neighbours");
			findNeighbours();
		} catch (Exception e)
		{
		}
	}

	private void findNeighbours() throws IOException, InterruptedException
	{
		List<double[]> peerDownloadRatesList = new ArrayList<double[]>();
		Set<Integer> interestedNeighborsList = myConnection.getmyIN();
		for(Integer peerID : interestedNeighborsList)
		{
			double[] peerIDAndRatePair = new double[2];
			peerIDAndRatePair[0] = (double)peerID;
			peerIDAndRatePair[1] = myConnection.detectTheRateOfDownload(peerID);
			peerDownloadRatesList.add(peerIDAndRatePair);
		}
		Collections.sort(peerDownloadRatesList, new Comparator<double[]>()
		{
			@Override
			public int compare(double[] rate1, double[] rate2)
			{
				if(rate1[1] > rate2[1])
					return 1;
				else if(rate1[1] < rate2[1])
					return -1;
				else
					return 0;
			}
		}
		);

		Set<Integer> CurrentPreferred = new TreeSet<Integer>();

		int count = Math.min(peerDownloadRatesList.size(), myConnection.NUM_PREFERRED_NEIGHBORS);
				for(int i = 0; i < count; i++)
				{
					int peerID = (int)peerDownloadRatesList.get(i)[0];
					CurrentPreferred.add(peerID);
				}

				for(Integer peerID : CurrentPreferred){
					if(!PrepreferredPeerIDSet.contains(peerID))
						myConnection.findUnchokedPeer(peerID);
				}

				for(Integer peerID : this.PrepreferredPeerIDSet)
				{
					if(!CurrentPreferred.contains(peerID))
						myConnection.findChokedPeer(peerID);
					
				}


				if(CurrentPreferred.size() > 0)
				{
					StringBuilder sb = new StringBuilder();
					for(Integer peerID : CurrentPreferred)
					{
						sb.append(peerID);
						sb.append(",");
					}
					sb.deleteCharAt(sb.length()-1);
					w.PrefNeighbours(myConnection.getID(), sb.toString());
				}
				this.PrepreferredPeerIDSet = CurrentPreferred;
	}
}


