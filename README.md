# nature_inspired_CA
This repo contains all the relevant files for the NIC coursework, 2023.

Javadocs for this project can be found in the javadoc folder.

# Getting Started

To run this project, either open in an IDE of your choosing and run the main function located in AntColonyOptimiser.java, or use the following command line commands:

`java -jar nature_inspired_CA.jar`

# Dependencies

To generate the testing graphs, the JFreeChart library was used. A normal run of the optimiser has no other dependencies.

# Parameters

The ACO takes in 11 parameters - 
1. File name - the name of the XML file to read in the Graph structure.
1. Number of Ants - the population size of the colony.
1. Evaporation Rate - the rate of which the pheromone will evaporate from an edge.
1. Alpha - the effect the pheromone has on the path calculation.
1. Beta -the effect the weight of an edge has on the path calculation.
1. Q - the pheromone update rate, used in the equation Q/fitness.
1. Termination Count - the number of fitness evaluations until termination. The specification of the coursework sets this at 10,000.
1. Elitism - whether or not to use the elitism variation of ACO.
1. Rank - whether or not to use the ranked ants variation of ACO. If so, it then will ask for how many elite ants
1. Viewing Best Path - whether or not the user wants to see a visual representation of the best path
1. Viewing Best Path with Weights - whether or not the user wants to see a visual represenation of the best path with the edge weights.

The ACO will return the best fitness found overall.
