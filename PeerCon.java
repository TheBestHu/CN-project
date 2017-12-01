
/**
 * @author Huanwen Xu,Yajing Fang and Yebowen Hu
 */
public class PeerCon {

	private String hostName;
	private int listeningPort;
	private boolean hasCompleteFile;

	public PeerCon(String hostName, int listeningPort, boolean hasFile)
	{
		this.hostName = hostName;
		this.listeningPort = listeningPort;
		this.hasCompleteFile = hasFile;
	}
	public String FindHostInfo()
	{
		return hostName;
	}

	public int FindPortNumber()
	{
		return listeningPort;
	}

	public boolean hasCompleteFile()
	{
		return hasCompleteFile;
	}

}

