package networkConnectivity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.UndirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class NagamochiIbaraki {

	private static final int MAX_VERTICES_COUNT = 22;

	/**
	 * find minimum cut of a graph using Nagamochi-Ibaraki algorithm
	 * 
	 * @param graph
	 *            input graph
	 * @return minimum cut
	 */
	public static int minCutNIAlgorithm(
			UndirectedSparseMultigraph<Integer, UndirectedEdge> graph) {
		// input check
		if (graph == null || graph.getVertexCount() == 1) {
			System.out
					.println("Error: Graph does not contrain enough vertices.");
			return 0;
		}

		/*
		 * base case: if there are only 2 vertices in the graph, return the
		 * number of edges between the 2 vertices
		 */
		if (graph.getVertexCount() == 2) {
			Iterator<Integer> itVertex = graph.getVertices().iterator();
			Integer vertex = itVertex.next();
			return graph.getIncidentEdges(vertex).size();
		} else {
			// find MA ordering
			ArrayList<Integer> maOrdering = findMAOrdering(graph);
			/* if graph is disconnected, return minimum cut - zero */
			if (maOrdering == null) {
				return 0;
			}

			/*
			 * otherwise, find lambda(from, to) and compare it with the minimum
			 * cut of the contracted graph
			 */
			Integer from = maOrdering.get(maOrdering.size() - 1);
			Integer to = maOrdering.get(maOrdering.size() - 2);
			int lambdaFromTo = graph.getIncidentEdges(from).size();

			contractGraph(graph, from, to);
			// recursive call minCutNIAlgorithm()
			return Math.min(lambdaFromTo, minCutNIAlgorithm(graph));
		}
	}

	/**
	 * create a Maximum Adjacency ordering of a graph, starting point is chosen
	 * at random.
	 * 
	 * @param graph
	 *            input undirected graph
	 * @return ordered vertices if such ordering exists, or null if graph is
	 *         empty or disconnected
	 */
	public static ArrayList<Integer> findMAOrdering(
			UndirectedSparseMultigraph<Integer, UndirectedEdge> graph) {

		int nVertices = graph.getVertexCount();
		ArrayList<Integer> vertices = new ArrayList<Integer>();

		// check input if input graph has no vertices
		vertices.addAll(graph.getVertices());
		if (vertices.size() == 0) {
			return null;
		}

		ArrayList<Integer> result = new ArrayList<Integer>();
		/*
		 * connectingEdgeCount tracks the number of connecting edges with
		 * existing MA ordered vertex set
		 */
		int[] connectingEdgeCount = new int[MAX_VERTICES_COUNT];

		// start with the first nodes in the vertices list
		Integer nextVertex = vertices.get(0);

		while (result.size() < nVertices && nextVertex != null) {
			result.add(nextVertex);

			Set<UndirectedEdge> incidentEdges = new HashSet<UndirectedEdge>();
			incidentEdges.addAll(graph.getIncidentEdges(nextVertex));

			for (UndirectedEdge e : incidentEdges) {
				Integer firstEndpoint = graph.getEndpoints(e).getFirst();
				Integer secondEndpoint = graph.getEndpoints(e).getSecond();

				// update connectingEdgeCount Array
				if (result.contains(firstEndpoint)
						&& result.contains(secondEndpoint)) {
					connectingEdgeCount[nextVertex]--;
				} else {
					Integer endPoint = (!result.contains(firstEndpoint)) ? firstEndpoint
							: secondEndpoint;
					connectingEdgeCount[endPoint]++;
				}
			}

			int maxCount = 0;
			Integer maxVertex = null;
			for (int i = 0; i < connectingEdgeCount.length; i++) {
				if (connectingEdgeCount[i] > maxCount) {
					maxCount = connectingEdgeCount[i];
					maxVertex = i;
				}
			}
			nextVertex = maxVertex;
		}
		if (result.size() < nVertices) {
			return null;
		} else {
			return result;
		}
	}

	/**
	 * graph contraction given 2 vertices, edges are reserved expect self loops;
	 * output error message if one or more vertices does not exist in the given
	 * graph
	 * 
	 * @param graph
	 *            contraction to be performed on
	 * @param from
	 *            vertex to be contracted
	 * @param to
	 *            vertex to be merged to
	 */
	public static void contractGraph(
			UndirectedSparseMultigraph<Integer, UndirectedEdge> graph,
			Integer from, Integer to) {
		if (!graph.containsVertex(from) || !graph.containsVertex(to)) {
			System.out
					.println("Error: vertices to be contracted does not exist in graph.");
		} else {
			Set<UndirectedEdge> edges = new HashSet<UndirectedEdge>();
			edges.addAll(graph.getIncidentEdges(from));

			for (UndirectedEdge e : edges) {
				Integer firstEndpoint = graph.getEndpoints(e).getFirst();
				Integer secondEndpoint = graph.getEndpoints(e).getSecond();
				Integer endPoint = (firstEndpoint == from) ? secondEndpoint
						: firstEndpoint;

				if (endPoint != to) {
					graph.addEdge(new UndirectedEdge(), endPoint, to);
				}
			}
			graph.removeVertex(from);
		}
	}

	/**
	 * find number of critical edges of a given graph, remove edges one by one
	 * and calculate minimum cut of the remaining graph. Parallel edges are
	 * tested only once.
	 * 
	 * @param edgeMatrix
	 *            edge matrix of the input graph
	 * @param minCut
	 *            minimum cut of the input graph
	 * @return number of critical edges
	 */
	public static int findCriticalEdges(int[][] edgeMatrix, int minCut) {

		int result = 0;
		for (int i = 0; i < edgeMatrix.length; i++) {
			for (int j = 0; j < edgeMatrix[i].length; j++) {
				if (edgeMatrix[i][j] > 0) {
					edgeMatrix[i][j]--;
					int minCutTest = minCutNIAlgorithm(createGraph(edgeMatrix));

					if (minCutTest < minCut) {
						result = result + edgeMatrix[i][j] + 1;
					}
					edgeMatrix[i][j]++;
				}
			}
		}
		return result;
	}

	/*
	 * Utility method - create a UndirectedSparseMultigraph given n and m
	 */
	public static UndirectedSparseMultigraph<Integer, UndirectedEdge> createGraph(
			int[][] edgeMatrix) {

		UndirectedOrderedSparseMultigraph<Integer, UndirectedEdge> result = new UndirectedOrderedSparseMultigraph<Integer, UndirectedEdge>();

		// add nodes
		for (int i = 0; i < edgeMatrix.length; i++) {
			result.addVertex(i);
		}

		// add edges
		for (int i = 0; i < edgeMatrix.length; i++) {
			for (int j = 0; j < edgeMatrix[i].length; j++) {
				for (int k = 0; k < edgeMatrix[i][j]; k++) {
					result.addEdge(new UndirectedEdge(), i, j);
				}
			}
		}
		return result;
	}

	/*
	 * Utility method - given number of nodes and number of edges m, generate
	 * randomly the edges of the graph
	 */
	public static int[][] genEdgeMatrix(int n, int m) {
		int[][] result = new int[n][n];

		// initialize edge matrix with zero edges
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				result[i][j] = 0;
			}
		}
		int count = 0;
		while (count < m) {
			int startNode = randomGen(n - 1);
			int endNode = randomGen(n - 1);
			// no self-loops
			if (startNode != endNode) {
				result[startNode][endNode] += 1;
				count++;
			}
		}
		return result;
	}

	public static int findEdgeCount(int[][] m) {
		int result = 0;
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				result += m[i][j];
			}
		}
		return result;
	}

	/*
	 * Utility method - generate a random integer from [0, 1, ..., range]
	 */
	private static int randomGen(int range) {
		return (int) ((range + 1) * Math.random());
	}

	/*
	 * Utility method - print matrix
	 */
	public static void printMatrix(int m[][]) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				System.out.format("%3d" + " ", m[i][j]);
			}
			System.out.println();
		}
	}

	/*
	 * Utility method - for graph visualization
	 */
	public static void visualizeGraph(
			UndirectedSparseMultigraph<Integer, UndirectedEdge> graph,
			String text) {

		Layout<Integer, UndirectedEdge> layout = new CircleLayout<Integer, UndirectedEdge>(
				graph);
		layout.setSize(new Dimension(800, 800));

		BasicVisualizationServer<Integer, UndirectedEdge> vv = new BasicVisualizationServer<Integer, UndirectedEdge>(
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

		JFrame frame = new JFrame(text);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}

}
