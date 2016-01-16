package networkCapacityPlanning;

public class Project1 {

	private static final int NUMBER_OF_ITERATIONS = 50;

	public static void main(String[] args) {
		NetworkDesign network = new NetworkDesign();

		float[] avgTotalCost = new float[16];
		float[] avgDensity = new float[16];

		for (int i = 0; i < 16; i++) {
			avgTotalCost[i] = 0;
			avgDensity[i] = 0;
		}

		for (int k = 3; k <= 15; k++) {
			// run the test NUMBER_OF_ITERATIONS times for every k from 3 to 15
			// calculate the average value of total cost and network density
			network.setNumberOfLowCostEdge(k);
			float costSum = 0;
			float densitySum = 0;
			for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {

				network.setTrafficDemand();
				network.setUnitCost();
				network.setCostGraph();
				network.setFlow();
				network.setFlowGraph();
				network.setTotalCost();
				network.setDensity();
				costSum += network.getTotalCost();
				densitySum += network.getDensity();

				if ((k + 1) % 4 == 0 && i == 8) {
					NetworkDesign.visualizeGraph(network.getFlow(),
							network.getNumberOfNodes(),
							network.getNumberOfLowCostEdge());
				}
			}
			avgTotalCost[k] = costSum / NUMBER_OF_ITERATIONS;
			avgDensity[k] = densitySum / NUMBER_OF_ITERATIONS;
		}

		for (int i = 3; i < avgTotalCost.length; i++) {
			System.out.format("%6.2f  ", avgTotalCost[i]);
		}

		System.out.println();

		for (int i = 3; i < avgDensity.length; i++) {
			System.out.format("%6.4f  ", avgDensity[i]);
		}

	}
}
