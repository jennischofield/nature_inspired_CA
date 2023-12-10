package src;

import java.io.*;
import java.util.Scanner;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.Random;

public class AntColonyOptimiser {
    ArrayList<Double> y = new ArrayList<Double>();

    public ArrayList<Double> getY() {
        return y;
    }

    /**
     * Parses the input XML file and accesses the DOM structure
     * 
     * @param f - File - the file to be parsed
     * @return Document - the DOM structure extracted from the file
     * @throws ParserConfigurationException - thrown if there's a configuration
     *                                      issue with the document build
     * @throws IOException                  - thrown if an IO error occurs when
     *                                      parsing
     * @throws SAXException                 - thrown if any parse errors occur
     * @throws IllegalArgumentException     - thrown when the file is null
     */
    public Document parseXML(File f)
            throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(f);
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * This sets up the Graph with the input from the XML file's DOM
     * 
     * @param doc - Document - the DOM model that the Graph is built from
     * @return Graph - the constructed graph
     * @throws IllegalArgumentException - if the DOM model does not contain "vertex"
     */
    public Graph setUpGraph(Document doc) {
        Graph retGraph = new Graph();
        NodeList nl = doc.getElementsByTagName("vertex");
        if (nl.getLength() == 0) {
            throw new IllegalArgumentException(
                    "The input XML must contain at least one \"vertex\" node. Please check your spelling");
        }

        retGraph.setAdjacencyMatrix(nl.getLength());

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            // The Node is the "vertex" element, which is a City

            City newCity = new City(Integer.toString(i));

            // Now get that city's edges
            NodeList edgeList = n.getChildNodes();

            for (int j = 0; j < edgeList.getLength(); j++) {

                Node newEdgeNode = edgeList.item(j);

                if (newEdgeNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element elem = (Element) newEdgeNode;
                    // This creates a new edge, and after the entire matrix is populated, the edges
                    // are correctly repointed at the relevant destination City
                    Edge newEdge = new Edge(new City(elem.getTextContent()),
                            Double.parseDouble(elem.getAttribute("cost")), newCity);
                    newCity.addPath(newEdge);
                    retGraph.setAdjacencyMatrixValue(i, Integer.parseInt(elem.getTextContent()), newEdge);

                    // This iterates through all the edges for one 'vertex' in the file
                    // This assigns a vertex its paths, and also inputs it in the graph's own
                    // adjacency matrix
                    // After it's done, it moves onto the next vertex in the list.
                }
            }
            retGraph.addCity(newCity);
            // This iterates through all the cities and adds them to the graph
        }
        retGraph.repointEdges();
        // The above makes it so the edge's destinations are actually pointing to the
        // City, not a placeholder.
        return retGraph;
    }

    /**
     * This initialises all the random pheromone amounts in the graph
     * 
     * @param g - Graph - the Graph for pheromone initialisation
     */
    public void initialisePheromone(Graph g) {
        for (int i = 0; i < g.getAdjacencyMatrix()[0].length; i++) {
            for (int j = 0; j < g.getAdjacencyMatrix().length; j++) {
                if (g.getAdjacencyMatrix()[i][j] != null) {
                    if (g.getAdjacencyMatrix()[i][j].getPheromone() == -1) {
                        // There's a possibility that the pheromone would be 0, so to represent the
                        // uninitalised pheromone, it has to be -1
                        double ph = Math.random();
                        g.getAdjacencyMatrix()[i][j].setPheromone(ph);
                        g.getAdjacencyMatrix()[j][i].setPheromone(ph);
                        // Since the graph is undirected, it is symmetrical - [i][j]'s pheromone is the
                        // same as [j][i]'s

                    }
                }
            }
        }
    }

    /**
     * This gets a random city for the Ant to start on
     * 
     * @param g - Graph - The graph with all the possible cities in
     * @return City - The random city
     * @throws IllegalArgumentException - If the graph is null, or the city list is
     *                                  null/empty
     * @throws IllegalStateException    - If something has gone catastrophically
     *                                  wrong with picking a random city
     */
    public City getRandomCity(Graph g) throws IllegalArgumentException, IllegalStateException {
        if (g == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        ArrayList<City> cities = g.getCities();
        if (cities == null || cities.isEmpty()) {
            throw new IllegalArgumentException("City list can't be null nor empty");
        }
        int randomIndex = new Random().nextInt(cities.size());
        int i = 0;
        for (City c : cities) {
            if (i == randomIndex) {
                return c;
            }
            i++;
        }
        throw new IllegalStateException("Something's gone wrong picking a random starting city");
    }

    /**
     * Returns a nicely formatted string containing all the cities visited in order
     * 
     * @param cl - ArrayList<City> - The list of cities, in order
     * @return String - The formatted string
     */
    public String printPath(ArrayList<City> cl) {
        String retString = "Path: ";
        for (int i = 0; i < cl.size() - 1; i++) {
            retString += cl.get(i).getName() + " -> ";
        }
        retString += cl.get(cl.size() - 1).getName();
        return retString;
    }

    /**
     * The running method of the simulation. This takes in parameters, then
     * evaluates the graph
     * 
     * @param graph            - Graph - the graph to be evaluated
     * @param numAnts          - int - the number of Ant actors to be intialised
     * @param evaporationRate  - double - the rate of which the pheromone should be
     *                         evaporated
     * @param alpha            - double - the effect the pheromone rate has on the
     *                         path calculation
     * @param beta             - double - the effect the weight of an edge has on
     *                         the path calculation
     * @param q                - double - the pheromone update rate - q/fitness
     * @param terminationCount - int - the number of fitness evaluations until
     *                         termination
     * @param elitism          - boolean - to use the elitism adjustment of ACO
     * @param rank             - int - to use the rank adjustment of ACO, the int is
     *                         how many elite members
     * @param viewBestPath     - boolean - if the user wishes to see the best path
     *                         found
     * @param viewBestEdgePath - boolean - if the user wishes to see the best path
     *                         found with weights
     * @return double - the best fitness of the graph found.
     */
    public double runAntColonySim(Graph graph, int numAnts, double evaporationRate, double alpha, double beta, double q,
            int terminationCount, boolean elitism, int rank, boolean viewBestPath, boolean viewBestEdgePath) {

        // Intitalise the ant list
        ArrayList<Ant> antColony = new ArrayList<Ant>();

        // Create the condition - after 10,000 fitness evaluations, terminate and return
        // the best fitness.
        int fitnessEvals = 0;
        double bestFitness = Double.MAX_VALUE;
        Ant bestAnt = new Ant(null);
        // This is an array list to make it easier for adding/deleting
        ArrayList<Ant> rankedAnts = new ArrayList<Ant>();
        ArrayList<City> bestPath = new ArrayList<City>();
        while (fitnessEvals < terminationCount) {

            // Create all the ants, randomise their start location, and add to the list
            for (int i = 0; i < numAnts; i++) {
                City startCity = getRandomCity(graph);
                Ant newAnt = new Ant(startCity);
                antColony.add(newAnt);
            }
            // For every ant in the colony
            for (Ant a : antColony) {
                // Calculate that Ant's path and fitness
                a.calculatePath(graph.getCities(), alpha, beta);
                double antFitness = a.calculateOverallFitness();
                // This y is used for graphing testing results
                y.add(antFitness);
                fitnessEvals++;
                // If the ant's fitness is better than the best fitness so far - we are
                // minimising the fitness
                if (antFitness < bestFitness) {
                    // Make this ant the best ant, best path, and best fitness
                    bestFitness = antFitness;
                    bestPath = a.getTourMemory();
                    bestAnt = a;

                    // If the user has chosen to use the rank variation, continue
                    if (rank > 0) {

                        if (rankedAnts.size() == 0) {
                            rankedAnts.add(bestAnt);
                        }
                        // Iterate the list and slot in the ant in the correct slot
                        for (int i = 0; i < rankedAnts.size(); i++) {
                            if (rankedAnts.get(i).calculateOverallFitness() > bestFitness) {
                                rankedAnts.add(i, bestAnt);
                                break;
                            }
                        }
                        // If the list size has gone over the rank, remove the worst Ant
                        if (rankedAnts.size() > rank) {
                            rankedAnts.remove(rankedAnts.size() - 1);
                        }
                    }
                }
            }

            // Updating the path
            for (Ant a : antColony) {
                // Inverse of the best fitness
                a.increasePheromoneOnPath(q / bestFitness);
            }

            // Elitism gives the local best path additional pheromone to encourage using
            // those edges
            if (elitism) {
                // increases the best path pheromone again for the best ant
                bestAnt.increasePheromoneOnPath(q / bestFitness);
            }
            // The rank system gives the top x ants' paths additional pheromone
            if (rank > 0) {
                for (Ant ra : rankedAnts) {
                    ra.increasePheromoneOnPath(q / bestFitness);
                }
            }
            // Evaporate all the paths
            graph.evaporatePaths(evaporationRate);
            // Reset the antcolony - this way excessive memory isn't used.
            antColony.clear();

        }
        // If the user wants to see the path with or without the weights
        if (viewBestPath) {
            System.out.println(printPath(bestPath));
        }
        if (viewBestEdgePath) {
            System.out.println(bestAnt.printEdgePath());

        }
        return bestFitness;
    }

    public void runTests() {
        // Run all the tests

    }

    public void displayResults(String appName, String plotTitle, int[][] xCoor, int[][] yCoor, String[] datasetName) {
        XYLinePlot chart = new XYLinePlot(appName, plotTitle, xCoor, yCoor, datasetName);
        chart.savePlot("/images/" + plotTitle);
    }

    /**
     * Main method, take in user input and initialise Graph before running the ACO
     * 
     * @param args - String[] - any additional command line args
     */
    public static void main(String[] args) {
        AntColonyOptimiser aco = new AntColonyOptimiser();
        Scanner reader = new Scanner(System.in);
        boolean continuing = true;
        while (continuing) {
            System.out.println("Welcome to the Ant Colony Optimiser!");
            System.out.println("Do you wish to run the tests and generate graphs? (Y/N)");
             while (true) {
                String runTests = reader.nextLine();
                if (runTests.equalsIgnoreCase("Y")) {
                    System.out.println("Running tests now.");
                    aco.runTests(); 
                    break;
                } else if (runTests.equalsIgnoreCase("N")) {
                    System.out.println("Continuing to ACO simulator.");
                    break;
                } else {
                    System.out.println("Please enter Y or N");
                }

            }
            System.out.println("Please enter the file name you wish to use:\n");
            File acoFile = new File(reader.nextLine());
            Document doc = null;
            try {
                doc = aco.parseXML(acoFile);
            } catch (NullPointerException e) {
                System.out.println(
                        "Error reading the file. Please check your spelling - you must include the file extension.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
            } catch (ParserConfigurationException e) {
                System.out.println("There has been an issue in the configuration build. Please restart and try again.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
            } catch (IOException e) {
                System.out.println("An IO exception has been encountered reading this file.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
            } catch (SAXException e) {
                System.out.println(
                        "There has been a parse error with the file. Please make sure it's formatted correctly.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
            } catch (IllegalArgumentException e) {
                System.out.println("The file cannot be null.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
            } catch (Exception e) {
                System.out.println("An unexpected error has occurred.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
            }

            // Setting up the graph
            Graph testGraph = null;
            try {
                testGraph = aco.setUpGraph(doc);
            } catch (IllegalArgumentException e) {
                System.out.println("The XML file must contain \"vertex\" elements.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
            } catch (Exception e) {
                System.out.println("An unexpected error has occurred.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
            }

            // Initialise pheromone
            aco.initialisePheromone(testGraph);
            // Time to set up the ACO proper
            System.out.println("What would you like the population of ants to be? (Between 1-10000)");
            int numAnts = 100;
            double evaporationRate = 0.7;
            double alpha = 0.5;
            double beta = 0.5;
            double q = 1;
            boolean elitism = false;
            int rank = 0;
            boolean bestPathPrint = false;
            boolean bestPathEdgesPrint = false;
            while (true) {
                try {
                    numAnts = Integer.parseInt(reader.nextLine());
                    if (numAnts > 10000) {
                        numAnts = 10000;
                    } else if (numAnts < 1) {
                        numAnts = 1;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Please enter a number.");
                }
            }
            System.out.println("What would you like the evaporation rate to be? (Between 0 and 1)");
            while (true) {
                try {
                    evaporationRate = Double.parseDouble(reader.nextLine());
                    if (evaporationRate > 1) {
                        evaporationRate = 1;
                    } else if (evaporationRate < 0) {
                        evaporationRate = 0;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Please enter a number.");
                }
            }
            System.out.println(
                    "What would you like the alpha value to be?\nThe alpha value is how much of an effect the pheromone has on determining the best path. (Between 0 and 1)");
            while (true) {
                try {
                    alpha = Double.parseDouble(reader.nextLine());
                    if (alpha > 1) {
                        alpha = 1;
                    } else if (alpha < 0) {
                        alpha = 0;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Please enter a number.");
                }
            }
            System.out.println(
                    "What would you like the beta value to be?\nThe beta value is how much of an effect the weight of a path has on determining the best path. (Between 0 and 1)");
            while (true) {
                try {
                    beta = Double.parseDouble(reader.nextLine());
                    if (beta > 1) {
                        beta = 1;
                    } else if (beta < 0) {
                        beta = 0;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Please enter a number.");
                }
            }
            System.out.println(
                    "What would you like the q value to be?\nThe q value is used to increase the pheromone by q/fitness. (Greater than 0)");
            while (true) {
                try {
                    q = Double.parseDouble(reader.nextLine());
                    if (q < 0) {
                        q = 1;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Please enter a number.");
                }
            }
            System.out.println("Would you like to use the elitism approach? (Y/N)");
            while (true) {

                elitism = false;
                String elitismInput = reader.nextLine();
                if (elitismInput.equalsIgnoreCase("Y")) {
                    elitism = true;
                    break;
                } else if (elitismInput.equalsIgnoreCase("N")) {
                    elitism = false;
                    break;
                } else {
                    System.out.println("Please enter Y or N");
                }

            }
            System.out.println("Would you like to use the ranked elites approach? (Y/N)");
            while (true) {

                rank = 0;
                String rankInput = reader.nextLine();
                if (rankInput.equalsIgnoreCase("Y")) {
                    System.out.println("How many elites? (Between 1 and 10,000)");
                    while (true) {
                        try {
                            rank = Integer.parseInt(reader.nextLine());
                            if (rank < 1) {
                                rank = 1;
                            } else if (rank > 10000) {
                                rank = 10000;
                            }
                            break;
                        } catch (Exception e) {
                            System.out.println("Please enter a number.");
                        }
                    }
                    break;
                } else if (rankInput.equalsIgnoreCase("N")) {
                    rank = 0;
                    break;
                } else {
                    System.out.println("Please enter Y or N");
                }

            }
            System.out.println("Would you like to see the best path found? (Y/N)");
            while (true) {

                bestPathPrint = false;
                String BPPinput = reader.nextLine();
                if (BPPinput.equalsIgnoreCase("Y")) {
                    bestPathPrint = true;
                    break;
                } else if (BPPinput.equalsIgnoreCase("N")) {
                    bestPathPrint = false;
                    break;
                } else {
                    System.out.println("Please enter Y or N");
                }

            }
            System.out.println("Would you like to see the best path found with weights? (Y/N)");
            while (true) {

                bestPathEdgesPrint = false;
                String BPPEinput = reader.nextLine();
                if (BPPEinput.equalsIgnoreCase("Y")) {
                    bestPathEdgesPrint = true;
                    break;
                } else if (BPPEinput.equalsIgnoreCase("N")) {
                    bestPathEdgesPrint = false;
                    break;
                } else {
                    System.out.println("Please enter Y or N");
                }

            }

            System.out.println("Running Simulation Now.");
            double bestFitness = aco.runAntColonySim(testGraph, numAnts, evaporationRate, alpha, beta, q, 10000,
                    elitism,
                    rank, bestPathPrint, bestPathEdgesPrint);
            System.out.println("The best fitness found was " + bestFitness);

            System.out.println("Would you like to try again? (Y/N)");

            while (true) {
                String continuingInput = reader.nextLine();
                if (continuingInput.equalsIgnoreCase("Y")) {
                    continuing = true;
                    System.out.println("Resetting to the beginning...");
                    break;
                } else if (continuingInput.equalsIgnoreCase("N")) {
                    continuing = false;
                    reader.close();
                    System.out.println("Thank you! Goodbye!");
                    break;
                } else {
                    System.out.println("Please enter Y or N");
                }

            }
        }

    }
}