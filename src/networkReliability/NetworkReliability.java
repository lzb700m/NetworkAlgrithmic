package networkReliability;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * Used to be instantiated to perform required network reliability experiments
 * 
 * @author LiP
 *
 */
public class NetworkReliability {

	// network representation (a complete undirected graph)
	private UndirectedSparseGraph<Integer, NetworkLink> network;
	// network states representation (each entry in the map is a single state of
	// the network, key is the network representation, value indicates the
	// network status (UP or DOWN))
	private Map<UndirectedSparseGraph<Integer, NetworkLink>, Boolean> networkStates;

	/**
	 * constructor
	 * 
	 * @param numOfNodes
	 *            : number of nodes in the network
	 * @param reliability
	 *            : reliability of each link assumed that all links have
	 *            identical reliability
	 */
	public NetworkReliability(int numOfNodes, double reliability) {
		this.network = constructCompleteNetwork(numOfNodes, reliability);

		List<NetworkLink> allLinks = new ArrayList<NetworkLink>(
				network.getEdges());
		List<List<NetworkLink>> allLinksSubSet = linksSubSet(allLinks);

		networkStates = new HashMap<UndirectedSparseGraph<Integer, NetworkLink>, Boolean>();

		for (List<NetworkLink> links : allLinksSubSet) {
			UndirectedSparseGraph<Integer, NetworkLink> event = constructCompleteNetwork(
					numOfNodes, reliability);
			for (NetworkLink link : links) {
				Pair<Integer> nodes = network.getEndpoints(link);
				event.findEdge(nodes.getFirst(), nodes.getSecond())
						.setLinkDown();
			}

			networkStates.put(event, isConnected(event));
		}
	}

	/**
	 * calculate network reliability
	 * 
	 * @return
	 */
	public double networkReliability() {
		double result = 0;
		for (UndirectedSparseGraph<Integer, NetworkLink> graph : networkStates
				.keySet()) {
			if (networkStates.get(graph)) {
				result += reliability(graph);
			}
		}
		return result;
	}

	/**
	 * flip network states and calculate reliability of flipped network states
	 * 
	 * @param k
	 * @return
	 */
	public double networkReliabilityFlipped(int k) {

		Set<UndirectedSparseGraph<Integer, NetworkLink>> flippedStates = new HashSet<UndirectedSparseGraph<Integer, NetworkLink>>();

		Random random = new Random();
		List<UndirectedSparseGraph<Integer, NetworkLink>> keys = new ArrayList<UndirectedSparseGraph<Integer, NetworkLink>>(
				networkStates.keySet());

		while (flippedStates.size() < k) {
			UndirectedSparseGraph<Integer, NetworkLink> randomKey = keys
					.get(random.nextInt(keys.size()));

			if (!flippedStates.contains(randomKey)) {
				flippedStates.add(randomKey);
				networkStates.put(randomKey, !networkStates.get(randomKey)); // flip
			}
		}

		double result = networkReliability();

		// flip it back
		for (UndirectedSparseGraph<Integer, NetworkLink> graph : flippedStates) {
			networkStates.put(graph, !networkStates.get(graph)); // flip back
		}

		return result;
	}

	/**
	 * Construct a complete graph represent the network
	 * 
	 * @param numOfNodes
	 *            - number of nodes in the graph
	 * @param reliability
	 *            - reliability of network links
	 * @return the constructed complete graph
	 */
	private UndirectedSparseGraph<Integer, NetworkLink> constructCompleteNetwork(
			int numOfNodes, double reliability) {

		UndirectedSparseGraph<Integer, NetworkLink> result = new UndirectedSparseGraph<Integer, NetworkLink>();

		for (int i = 1; i <= numOfNodes; i++) {
			result.addVertex(i);
		}

		for (int i = 1; i <= numOfNodes; i++) {
			for (int j = 1; j <= numOfNodes; j++) {
				if (i != j && !result.isNeighbor(i, j)) {
					result.addEdge(new NetworkLink(reliability), i, j);
				}
			}
		}

		return result;
	}

	/**
	 * find out all states
	 * 
	 * @param links
	 * @return
	 */
	private List<List<NetworkLink>> linksSubSet(List<NetworkLink> links) {
		List<List<NetworkLink>> result = new ArrayList<List<NetworkLink>>();
		List<NetworkLink> list = new ArrayList<NetworkLink>();

		if (links == null || links.size() == 0) {
			return result;
		}

		subsetHelper(links, result, list, 0);
		return result;
	}

	/*
	 * helper function to find all possible states
	 */
	private void subsetHelper(List<NetworkLink> links,
			List<List<NetworkLink>> result, List<NetworkLink> list, int index) {
		result.add(new ArrayList<NetworkLink>(list));

		for (int i = index; i < links.size(); i++) {
			list.add(links.get(i));
			subsetHelper(links, result, list, i + 1);
			list.remove(list.size() - 1);
		}
	}

	/**
	 * use DFS to tell if the network is connected
	 * 
	 * @param graph
	 * @return
	 */
	private boolean isConnected(
			UndirectedSparseGraph<Integer, NetworkLink> graph) {
		List<Integer> allNodes = new ArrayList<Integer>(graph.getVertices());
		Integer startNode = allNodes.get(0);

		Set<Integer> visited = new HashSet<Integer>();
		Stack<Integer> stack = new Stack<Integer>();
		stack.push(startNode);

		while (!stack.empty()) {
			Integer current = stack.pop();
			if (!visited.contains(current)) {
				visited.add(current);
			}

			List<Integer> neighbors = new ArrayList<Integer>(
					graph.getNeighbors(current));

			for (Integer neighbor : neighbors) {
				if (!visited.contains(neighbor)
						&& graph.findEdge(current, neighbor).isUp()) {
					stack.push(neighbor);
				}
			}
		}

		return (visited.size() == graph.getVertexCount());

	}

	/*
	 * calculate reliability of a single network state
	 */
	private static double reliability(
			UndirectedSparseGraph<Integer, NetworkLink> graph) {
		double result = 1;
		List<NetworkLink> allLinks = new ArrayList<NetworkLink>(
				graph.getEdges());
		for (NetworkLink link : allLinks) {
			if (link.isUp()) {
				result *= link.getReliability();
			} else {
				result *= (1 - link.getReliability());
			}
		}

		return result;
	}

	/**
	 * network reliability instance reporting
	 */
	public void print() {
		int upCount = 0;
		int downCount = 0;

		System.out.println("Number of nodes: " + network.getVertexCount()
				+ "; Number of links: " + network.getEdgeCount()
				+ "; Number of network states: " + networkStates.size() + ".");

		for (UndirectedSparseGraph<Integer, NetworkLink> graph : networkStates
				.keySet()) {
			if (networkStates.get(graph)) {
				upCount++;
				// System.out.println(graph);
				// System.out.println(networkStates.get(graph));
				// visualizeGraph(graph);
			} else {
				downCount++;
			}
		}

		System.out.println("Network up states count: " + upCount
				+ ", network down states count: " + downCount + ".");
	}

	/*
	 * graph visualization
	 */
	@SuppressWarnings("unused")
	private void visualizeGraph(
			UndirectedSparseGraph<Integer, NetworkLink> graph) {
		Layout<Integer, NetworkLink> layout = new CircleLayout<Integer, NetworkLink>(
				graph);
		layout.setSize(new Dimension(200, 200));

		BasicVisualizationServer<Integer, NetworkLink> vv = new BasicVisualizationServer<Integer, NetworkLink>(
				layout);
		Transformer<Integer, Paint> vertexPaint = new Transformer<Integer, Paint>() {
			public Paint transform(Integer i) {
				return Color.YELLOW;
			}
		};

		vv.setPreferredSize(new Dimension(200, 200));
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<Integer>());
		vv.getRenderContext().setEdgeLabelTransformer(
				new ToStringLabeller<NetworkLink>());
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		JFrame frame = new JFrame("Network Reliability Experiment");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}
}
