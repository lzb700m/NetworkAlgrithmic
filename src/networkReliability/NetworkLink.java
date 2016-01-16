package networkReliability;

/**
 * Class to represent network links for the given task
 * 
 * @author LiP
 *
 */
public class NetworkLink {

	// link reliability of a NetworkLink instance
	private double reliability;
	// indicates if the link instance is up or down
	private boolean status;

	/*
	 * constructor, all NetworkLinks are up by default
	 */
	public NetworkLink(double d) {
		this.reliability = d;
		this.status = true;
	}

	/*
	 * Deep copy of a NetworkLink instance
	 */
	public NetworkLink(NetworkLink o) {
		this.reliability = o.reliability;
		this.status = o.status;
	}

	/*
	 * getters and setters
	 */
	public double getReliability() {
		return reliability;
	}

	public boolean isUp() {
		return status;
	}

	public void setReliability(double d) {
		this.reliability = d;
	}

	public void setLinkUp() {
		this.status = true;
	}

	public void setLinkDown() {
		this.status = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString() used for reporting and visualization
	 * purposes
	 */
	@Override
	public String toString() {
		if (status) {
			return "UP";
		} else {
			return "DOWN";
		}
	}
}
