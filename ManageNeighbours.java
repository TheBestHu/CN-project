import java.io.IOException;
import java.util.*;

public class ManageNeighbours implements Runnable
{
	private Connection myConnection;
	private Set<Integer> PrepreferredPeerIDSet;
	WriteLog w = new WriteLog();

	public ManageNeighbours(Connection myConnection)
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
			//e.printStackTrace();
		} 
	}
	/**
	 * calculate download rate for all peers that are receiving data at the moment
	 * retain two best uploaders
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void findNeighbours() throws IOException, InterruptedException
	{
		List<double[]> peerDownloadRatesList = new ArrayList<double[]>();
		Set<Integer> interestedNeighborsList = myConnection.getmyInterestedNeighbours();
		for(Integer peerID : interestedNeighborsList)
		{
			double[] peerIDAndRatePair = new double[2];
			peerIDAndRatePair[0] = (double)peerID;
			peerIDAndRatePair[1] = myConnection.getDownloadRate(peerID);
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
		//int count = peerDownloadRatesList.size() < myConnection.NUM_PREFERRED_NEIGHBORS ?
		//		peerDownloadRatesList.size() : myConnection.NUM_PREFERRED_NEIGHBORS;

		int count = Math.min(peerDownloadRatesList.size(), myConnection.NUM_PREFERRED_NEIGHBORS);
				for(int i = 0; i < count; i++)
				{
					int peerID = (int)peerDownloadRatesList.get(i)[0];
					CurrentPreferred.add(peerID);
				}

				for(Integer peerID : CurrentPreferred){
					if(!PrepreferredPeerIDSet.contains(peerID))
						myConnection.reportUnchokedPeer(peerID);
				}

				for(Integer peerID : this.PrepreferredPeerIDSet)
				{
					if(!CurrentPreferred.contains(peerID))
						myConnection.reportChokedPeer(peerID);
					
				}

				//this.PrepreferredPeerIDSet = CurrentPreferred;

				if(CurrentPreferred.size() > 0)
				{
					StringBuilder sb = new StringBuilder();
					for(Integer peerID : CurrentPreferred)
					{
						sb.append(peerID);
						sb.append(",");
					}
					sb.deleteCharAt(sb.length()-1);
					w.PrefNeighbours(Integer.toString(myConnection.getMyPeerID()), sb.toString());
				}
				this.PrepreferredPeerIDSet = CurrentPreferred;
	}
}


