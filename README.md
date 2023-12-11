# nature_inspired_CA
This repo contains all the relevant files for the NIC coursework, 2023.

Javadocs for this project can be found in the javadoc folder.

# Getting Started

To run this project, either open in an IDE of your choosing and run the main function located in AntColonyOptimiser.java, or use the following command line commands:

`java -jar nature_inspired_CA.jar`

If you wish to run the simulation without navigating through the menus, you may use command line arguments in the format:

`java -jar nature_inspired_CA.jar <filename> <number of ants> <evaporation rate> <alpha> <beta> <q> <termination count> <elitism> <rank> <viewing best path> <viewing best path with weights>`

For example, to run the Burma dataset, with 100 ants, an evaporation rate of 0.7, an alpha/beta of 0.5/0.5, a q of 1, a termination count of 10,000, without elitism, with 20 ranked ants, and to not see the best path, with or without weights, the command would be:

`java -jar nature_inspired_CA.jar burma14.xml 100 0.7 0.5 0.5 1 10000 false 20 false false`

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

# Running other graphs

If you wish to run the ACO over another file, it must be in the TSPLIB format, and an XML file. Place it in the root folder, along with brazil58.xml and burma14.xml

# Running tests and generate graphs
If desired, when prompted for a file name, you can enter "-1" to run result data generators. This was to add graphs to the associated report, but the code is not optimised, takes long to run, and is not advised to run. The resulting graphs can be found in /images/. 

# Known error
If you try to run the optimiser and get the error: 
`Receiver class gnu.xml.dom.DomElement does not define or inherit an implementation of the resolved method 'abstract java.lang.String getTextContent()' of interface org.w3c.dom.Node.`

This is an error from the JFreeChart library - simply delete the gnujaxp.jar file from the class path.