package networkConnectivity;

import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;

import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

public class Project2 {

	public static final int NUMBER_OF_VERTICES = 22;
	public static final int NUMBER_OF_EXPERIMENT = 50;
	public static final int LOWER_BOUND_EDGES = 40;
	public static final int UPPER_BOUND_EDGES = 400;
	public static final int INCREMENT_EDGES = 5;

	public static void main(String[] args) throws IOException {

		// output to csv file
		String csv = "data/project2_result.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(csv));
		String[] header = "Number of Vertices, Number of Edges, Average Degree, Average Connectivity, Average Critical Edges Count"
				.split(",");
		writer.writeNext(header);

		for (int edgeCount = LOWER_BOUND_EDGES; edgeCount <= UPPER_BOUND_EDGES; edgeCount += INCREMENT_EDGES) {
			// repeat each experiment and calculate average
			int minCutTotal = 0;
			float minCutAvg = 0;
			int criticalEdgeTotal = 0;
			float criticalEdgeAvg = 0;

			for (int experimentCount = 0; experimentCount < NUMBER_OF_EXPERIMENT; experimentCount++) {
				int[][] edgeMatrix = NagamochiIbaraki.genEdgeMatrix(
						NUMBER_OF_VERTICES, edgeCount);
				UndirectedSparseMultigraph<Integer, UndirectedEdge> graph = NagamochiIbaraki
						.createGraph(edgeMatrix);
				int minCut = NagamochiIbaraki.minCutNIAlgorithm(graph);
				minCutTotal += minCut;

				int criticalEdge = 0;

				if (minCut != 0) {
					criticalEdge = NagamochiIbaraki.findCriticalEdges(
							edgeMatrix, minCut);
					criticalEdgeTotal += criticalEdge;
				}
			}

			minCutAvg = (float) minCutTotal / NUMBER_OF_EXPERIMENT;
			criticalEdgeAvg = (float) criticalEdgeTotal / NUMBER_OF_EXPERIMENT;

			// output to csv file
			String record = String.valueOf(NUMBER_OF_VERTICES)
					+ ","
					+ String.valueOf(edgeCount)
					+ ","
					+ String.valueOf((float) 2 * edgeCount / NUMBER_OF_VERTICES)
					+ "," + String.valueOf(minCutAvg) + ","
					+ String.valueOf(criticalEdgeAvg);
			String[] recordArray = record.split(",");
			writer.writeNext(recordArray);

			// output to console
			System.out.println("Number of Vertices: " + NUMBER_OF_VERTICES
					+ "; Number of Edges: " + edgeCount + "; Average degree: "
					+ (float) 2 * edgeCount / NUMBER_OF_VERTICES
					+ "; Average connectivity: " + minCutAvg
					+ "; Average number of critical edges: " + criticalEdgeAvg);
		}
		writer.close();
	}
}
