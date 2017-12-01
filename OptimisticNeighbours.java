import java.io.IOException;
import java.util.*;
/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */
public class OptimisticNeighbours implements Runnable
{
	private connect myConnection;
	WriteLog w = new WriteLog();

	public OptimisticNeighbours(connect Connection)
	{
		this.myConnection = Connection;
	}

	@Override
	public void run()
	{
		try
		{
			this.optUnchokedPeer();
		} catch (Exception e)
		{
		}
	}

	private void optUnchokedPeer() throws IOException, InterruptedException
	{
		Integer prevPeer = myConnection.getUnchockedP();
		if (prevPeer != -1)
			myConnection.findChokedPeer(myConnection.getUnchockedP());

		Set<Integer> chokedPeersSet = myConnection.getChokedPeers();
		List<Integer> interestedAndChoked = new LinkedList<Integer>();
		interestedAndChoked.addAll(myConnection.getmyIN());
		interestedAndChoked.retainAll(chokedPeersSet);
		if(interestedAndChoked.size() > 0)
		{
			Random rand = new Random();
			int selectedPeer = interestedAndChoked.get(rand.nextInt(interestedAndChoked.size()));
			myConnection.findUnchokedPeer(selectedPeer);
			myConnection.setUnchockedP(selectedPeer);
			w.OptUnchokedNeighbours(myConnection.getID(), Integer.toString(selectedPeer));
		}
	}
}
