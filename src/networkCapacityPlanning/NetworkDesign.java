package networkCapacityPlanning;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.HashSet;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.princeton.cs.algs4.DijkstraSP;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class NetworkDesign {

	private static final int DEFAULT_NUMBER_OF_NODES = 30;
	private static final int DEFAULT_NUMBER_OF_LOW_COST_EDGE = 5;
	private static final int MAX_DEMAND = 4;
	private static final int LOW_COST = 1;
	private static final int HIGH_COST = 300;

	private int numberOfNodes;
	private int numberOfLowCostEdge;
	private int[][] trafficDemand;
	private int[][] unitCost;
	private int[][] flow;
	private EdgeWeightedDigraph costGraph;
	private EdgeWeightedDigraph flowGraph;
	private int totalCost;
	private float density;

	public NetworkDesign(int numberOfNodes, int numberOfLowCostEdge) {
		this.numberOfNodes = numberOfNodes;
		this.numberOfLowCostEdge = numberOfLowCostEdge;
	}

	public NetworkDesign() {
		this.numberOfNodes = DEFAULT_NUMBER_OF_NODES;
		this.numberOfLowCostEdge = DEFAULT_NUMBER_OF_LOW_COST_EDGE;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public int getNumberOfLowCostEdge() {
		return numberOfLowCostEdge;
	}

	public int[][] getTrafficeDemand() {
		return trafficDemand;
	}

	public int[][] getUnitCost() {
		return unitCost;
	}

	public int[][] getFlow() {
		return flow;
	}

	public EdgeWeightedDigraph getCostGraph() {
		return costGraph;
	}

	public EdgeWeightedDigraph getFlowGraph() {
		return flowGraph;
	}

	public int getTotalCost() {
		return totalCost;
	}

	public float getDensity() {
		return density;
	}

	public void setNumberOfNodes(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException(
					"number of nodes must be a positive number");
		} else {
			numberOfNodes = n;
		}
	}

	public void setNumberOfLowCostEdge(int n) {
		if ((n <= 0) || (n >= numberOfNodes)) {
			throw new IllegalArgumentException(
					"number of low cost edege must be a positive number, and must be less than number of nodes");
		} else {
			numberOfLowCostEdge = n;
		}
	}

	public void setTrafficDemand() {
		trafficDemand = new int[numberOfNodes][numberOfNodes];

		for (int i = 0; i < trafficDemand.length; i++) {
			for (int j = 0; j < trafficDemand[i].length; j++) {
				if (i == j) {
					trafficDemand[i][j] = 0;
				} else {
					trafficDemand[i][j] = randomGen(MAX_DEMAND);
				}
			}
		}
	}

	public void setUnitCost() {
		unitCost = new int[numberOfNodes][numberOfNodes];

		// initialize the all values of the cost matrix to HIGH_COST
		for (int i = 0; i < unitCost.length; i++) {
			for (int j = 0; j < unitCost[i].length; j++) {
				unitCost[i][j] = HIGH_COST;
			}
		}

		// randomly select k different edges that has cost LOW_COST
		for (int i = 0; i < unitCost.length; i++) {
			HashSet<Integer> indices = new HashSet<Integer>();
			while (indices.size() < numberOfLowCostEdge) {
				int index = randomGen(numberOfNodes - 1);
				if (!(index == i) && !indices.contains(index)) {
					indices.add(index);
				}
			}
			for (Integer k : indices) {
				unitCost[i][k] = LOW_COST;
			}
		}
	}

	public void setFlow() {
		flow = new int[numberOfNodes][numberOfNodes];
		for (int i = 0; i < flow.length; i++) {
			for (int j = 0; j < flow[i].length; j++) {
				flow[i][j] = 0;
			}
		}

		// use Dijkstra single source shortest path algorithm
		// calculate shortest path to all other nodes from source node
		for (int s = 0; s < costGraph.V(); s++) {
			DijkstraSP sp = new DijkstraSP(costGraph, s);

			for (int t = 0; t < costGraph.V(); t++) {
				if (sp.hasPathTo(t)) {
					for (DirectedEdge e : sp.pathTo(t)) {
						flow[e.from()][e.to()] += trafficDemand[s][t];
					}
				}
			}
		}
	}

	public void setCostGraph() {
		costGraph = createGraph(unitCost);
	}

	public void setFlowGraph() {
		flowGraph = createGraph(flow);
	}

	public void setTotalCost() {
		totalCost = 0;
		for (int i = 0; i < flow.length; i++) {
			for (int j = 0; j < flow[i].length; j++) {
				totalCost += flow[i][j] * unitCost[i][j];
			}
		}
	}

	public void setDensity() {
		int countEdge = 0;
		for (int i = 0; i < flow.length; i++) {
			for (int j = 0; j < flow[i].length; j++) {
				if (flow[i][j] > 0) {
					countEdge++;
				}
			}
		}

		density = (float) countEdge / (numberOfNodes * (numberOfNodes - 1));

	}

	public void printInput() {
		for (int i = 0; i < trafficDemand.length; i++) {
			for (int j = 0; j < trafficDemand[i].length; j++) {
				System.out.format("%3d" + " ", trafficDemand[i][j]);
			}
			System.out.println();
		}

		for (int i = 0; i < unitCost.length; i++) {
			for (int j = 0; j < unitCost[i].length; j++) {
				System.out.format("%3d" + " ", unitCost[i][j]);
			}
			System.out.println();
		}
		System.out.println(costGraph);
	}

	public void printOutput() {
		for (int i = 0; i < flow.length; i++) {
			for (int j = 0; j < flow[i].length; j++) {
				System.out.format("%3d" + " ", flow[i][j]);
			}
			System.out.println();
		}
		System.out.println(flowGraph);
		System.out.println("Total Cost is: " + totalCost);
		System.out.println("Network density is: " + density);
	}

	// method for flow graph visualization
	public static void visualizeGraph(int[][] matrix, int N, int k) {
		Graph<Integer, String> graph = new DirectedSparseGraph<Integer, String>();

		for (int i = 0; i < matrix.length; i++) {
			graph.addVertex((Integer) i);
		}

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				// only select edge that has flow bigger than 0
				if (matrix[i][j] > 0) {
					graph.addEdge(i + "->" + j, i, j, EdgeType.DIRECTED);
				}
			}
		}

		Layout<Integer, String> layout = new CircleLayout<Integer, String>(
				graph);
		layout.setSize(new Dimension(800, 800));

		BasicVisualizationServer<Integer, String> vv = new BasicVisualizationServer<Integer, String>(
				layout);
		Transformer<Integer, Paint> vertexPaint = new Transformer<Integer, Paint>() {
			public Paint transform(Integer i) {
				return Color.YELLOW;
			}
		};

		vv.setPreferredSize(new Dimension(800, 800));
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<Integer>());
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		JFrame frame = new JFrame("Network Visualization - N = " + N + ", k = "
				+ k);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}

	// generate a random integer from [0, 1, ..., range]
	private static int randomGen(int range) {
		return (int) ((range + 1) * Math.random());
	}

	private static EdgeWeightedDigraph createGraph(int[][] edgeMatrix) {
		EdgeWeightedDigraph output = new EdgeWeightedDigraph(edgeMatrix.length);
		for (int i = 0; i < edgeMatrix.length; i++) {
			for (int j = 0; j < edgeMatrix[i].length; j++) {
				DirectedEdge edge = new DirectedEdge(i, j, edgeMatrix[i][j]);
				output.addEdge(edge);
			}
		}
		return output;
	}
}
