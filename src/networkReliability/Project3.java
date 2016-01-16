package networkReliability;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.opencsv.CSVWriter;

/**
 * Driver class for Project 3 of CS6385.001 Algorithmic Aspects of
 * Telecommunication Networks
 * 
 * Experiment 1 calculate the given network reliability using exhaustive
 * enumeration for single link reliability ranging from 0 to 1.
 * 
 * Experiment 2 calculate the network reliability after flipping randomly k
 * system states, k ranges from 0 to 30. To reduce the effect of randomness, for
 * each value of k, the experiment is carried out 1000 times and the resulting
 * reliability is averaged out.
 * 
 * @author LiP
 *
 */
public class Project3 {
	/*
	 * project configuration: network is represented with a complete undirected
	 * graph with 5 nodes; In experiment 2, each link has the same reliability
	 * of 0.9, and for each k the experiment is carried out 1000 times to be
	 * average out.
	 */
	public static final int NUM_OF_NODES = 5;
	public static final double FIXED_RELIABILITY = 0.9;
	public static final int FLIP_REPEATS = 1000;

	public static void main(String[] args) throws IOException {
		// output to csv file
		String csv_experiment1 = "data/project3_experiment1_result.csv";
		String csv_experiment2 = "data/project3_experiment2_result.csv";
		NumberFormat formatter = new DecimalFormat("0.000");

		// experiment 1
		CSVWriter writer = new CSVWriter(new FileWriter(csv_experiment1));
		String[] header = "Single link reliability, Network reliability"
				.split(",");
		writer.writeNext(header);

		for (double r = 0; r <= 1.01; r = r + 0.04) {
			NetworkReliability experiment1 = new NetworkReliability(
					NUM_OF_NODES, r);

			writer.writeNext((formatter.format(r) + "," + formatter
					.format(experiment1.networkReliability())).split(","));

			System.out.println("Link reliability is: " + formatter.format(r)
					+ ", Network reliability is: "
					+ formatter.format(experiment1.networkReliability()));
		}
		writer.close();

		// experiment 2
		writer = new CSVWriter(new FileWriter(csv_experiment2));
		header = "Flip k, Single link reliability, Flipped Network reliability"
				.split(",");
		writer.writeNext(header);
		NetworkReliability experiment2 = new NetworkReliability(NUM_OF_NODES,
				FIXED_RELIABILITY);

		System.out.printf(
				"Link reliability is: %5.2f, Network reliability is: %5.3f \n",
				FIXED_RELIABILITY, experiment2.networkReliability());

		for (int k = 0; k <= 30; k++) {
			double result = 0;
			for (int i = 0; i < FLIP_REPEATS; i++) {
				result += experiment2.networkReliabilityFlipped(k);
			}
			result = result / FLIP_REPEATS;
			writer.writeNext((k + "," + FIXED_RELIABILITY + "," + formatter
					.format(result)).split(","));
			System.out
					.printf("Flip k = %3d, Network reliability is: %5.3f, Flipped Network reliability is: %5.3f\n",
							k, experiment2.networkReliability(), result);
		}
		writer.close();
		experiment2.print();
	}
}
