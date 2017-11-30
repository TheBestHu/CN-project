
/**
 * @author Yebowen Hu
 */
public class PeerConfig {

	private String hostName;
	private int listeningPort;
	private boolean hasCompleteFile;

	public PeerConfig(String hostName, int listeningPort, boolean hasFile)
	{
		this.hostName = hostName;
		this.listeningPort = listeningPort;
		this.hasCompleteFile = hasFile;
	}
	public String getHostName()
	{
		return hostName;
	}

	public int getListeningPort()
	{
		return listeningPort;
	}

	public boolean hasCompleteFile()
	{
		return hasCompleteFile;
	}

}

