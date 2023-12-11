package src;

import java.io.*;
import java.util.Scanner;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.Random;

public class AntColonyOptimiser {
    ArrayList<Double> x = new ArrayList<Double>();
    ArrayList<Double> y = new ArrayList<Double>();

    public ArrayList<Double> getY() {
        return y;
    }

    public ArrayList<Double> getX() {
        return x;
    }

    public void clearXandY() {
        x.clear();
        y.clear();
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
            double localBestFitness = Double.MAX_VALUE;
            for (int i = 0; i < antColony.size(); i++) {
                Ant a = antColony.get(i);
                // Calculate that Ant's path and fitness
                a.calculatePath(graph.getCities(), alpha, beta);
                double antFitness = a.calculateOverallFitness();
                // This y is used for graphing testing results
                // sneaky way to double - saves a headache when graphing
                // x.add(fitnessEvals * 1.0);
                // y.add(bestFitness);
                fitnessEvals++;
                if (antFitness < localBestFitness) {
                    localBestFitness = antFitness;
                }
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
                        for (int j = 0; j < rankedAnts.size(); j++) {
                            if (rankedAnts.get(j).calculateOverallFitness() > bestFitness) {
                                rankedAnts.add(j, bestAnt);
                                break;
                            }
                        }
                        // If the list size has gone over the rank, remove the worst Ant
                        if (rankedAnts.size() > rank) {
                            rankedAnts.remove(rankedAnts.size() - 1);
                        }

                    }

                }

                if (fitnessEvals >= terminationCount) {
                    break;
                }

            }
            if (localBestFitness != Integer.MAX_VALUE) {
                x.add(fitnessEvals * 1.0);
                y.add(localBestFitness);
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

    /**
     * This method runs the tests and generates graphs out of the data derived
     */
    public void runTests() {
        // Run all the tests

        double[][] numberOfFitnessEvals = new double[4][10000];
        double[][] fitnessOverTime = new double[4][10000];
        File burmaFile = new File("burma14.xml");
        File brazilFile = new File("brazil58.xml");
        String[] datasetNames = new String[5];
        try {
            Document burmaDocument = parseXML(burmaFile);
            Document brazilDocument = parseXML(brazilFile);
            Graph gBurma = setUpGraph(burmaDocument);
            Graph gBurma2 = setUpGraph(burmaDocument);
            Graph gBurma3 = setUpGraph(burmaDocument);
            Graph gBurma4 = setUpGraph(burmaDocument);
            Graph gBrazil = setUpGraph(brazilDocument);
            // Base variables:
            // numAnts = 100, evaporation rate = 0.7, alpha = 0.5, beta =0.5, q = 1,
            // termination count = 10,000, elitism = false, rank = 0, view best path =
            // false, view best edge = false;
            // Number of ant experiments: 1, 10, 50, 100, 1,000, 10,000
            ArrayList<Double> antTestFitnesses = new ArrayList<Double>();
            // Ant population = 1
            initialisePheromone(gBurma);
            double fitness1 = runAntColonySim(gBurma, 10, 0.7, 0.5, 0.5, 1, 10000, false, 0, false, false);
            antTestFitnesses.add(fitness1);
            ArrayList<Double> xVals = getX();
            ArrayList<Double> yVals = getY();
            for (int i = 0; i < xVals.size(); i++) {
                if (xVals.get(i) != null) {
                    numberOfFitnessEvals[0][i] = Double.valueOf(xVals.get(i));
                    fitnessOverTime[0][i] = Double.valueOf(yVals.get(i));
                }
            }
            datasetNames[0] = "Ant Population 10: Burma";
            clearXandY();

            // Ant population = 10
            initialisePheromone(gBurma2);
            double fitness2 = runAntColonySim(gBurma2, 100, 0.7, 0.5, 0.5, 1, 10000, false, 0, false, false);
            antTestFitnesses.add(fitness2);
            ArrayList<Double> xVals2 = getX();
            ArrayList<Double> yVals2 = getY();
            for (int i = 0; i < xVals2.size(); i++) {
                if (xVals2.get(i) != null) {

                    numberOfFitnessEvals[1][i] = Double.valueOf(xVals2.get(i));
                    fitnessOverTime[1][i] = Double.valueOf(yVals2.get(i));
                }
            }
            datasetNames[1] = "Ant Population 100: Burma";
            clearXandY();
            // Ant population = 100
            initialisePheromone(gBurma3);
            double fitness3 = runAntColonySim(gBurma3, 1000, 0.7, 0.5, 0.5, 1, 10000, false, 0, false, false);
            antTestFitnesses.add(fitness3);
            ArrayList<Double> xVals3 = getX();
            ArrayList<Double> yVals3 = getY();
            for (int i = 0; i < xVals3.size(); i++) {
                if (xVals3.get(i) != null) {

                    numberOfFitnessEvals[2][i] = Double.valueOf(xVals3.get(i));
                    fitnessOverTime[2][i] = Double.valueOf(yVals3.get(i));
                }
            }
            datasetNames[2] = "Ant Population 1,000: Burma";
            clearXandY();
            // Ant population = 1,000
            initialisePheromone(gBurma4);
            double fitness4 = runAntColonySim(gBurma4, 10000, 0.7, 0.5, 0.5, 1, 10000, false, 0, false, false);
            antTestFitnesses.add(fitness4);
            ArrayList<Double> xVals4 = getX();
            ArrayList<Double> yVals4 = getY();
            for (int i = 0; i < xVals4.size(); i++) {
                if (xVals4.get(i) != null) {

                    numberOfFitnessEvals[3][i] = Double.valueOf(xVals4.get(i));
                    fitnessOverTime[3][i] = Double.valueOf(yVals4.get(i));
                }
            }
            datasetNames[3] = "Ant Population 10,000: Burma";

            clearXandY();
            // Display the ant test results [4274.0, 3731.0, 4410.0, 4797.0]
            displayResults("Ant Population Experimentation - Local Best - Burma",
                    "Ant Population Experimentation - Local Best - Burma", numberOfFitnessEvals,
                    fitnessOverTime, datasetNames, 3000);

            // Evaporation rate experiment: 0.1,0.2,0.3 etc
            double pheromone = 0.1;

            double[][] numberOfFitnessEvalsPheromone = new double[10][10000];
            double[][] fitnessOverTimePheromone = new double[10][10000];
            String[] pheromoneLevels = new String[10];
            Graph gBurmaP = setUpGraph(burmaDocument);
            initialisePheromone(gBurmaP);

            for (int i = 0; i < 10; i++) {
                runAntColonySim(gBurmaP, 100, pheromone, 0.5, 0.5, 1, 10000, false, 0, false, false);
                ArrayList<Double> xValsPheromone = getX();
                ArrayList<Double> yValsPheromone = getY();
                for (int j = 0; j < xValsPheromone.size(); j++) {
                    if (xValsPheromone.get(j) != null) {
                        numberOfFitnessEvalsPheromone[i][j] = Double.valueOf(xValsPheromone.get(j));
                        fitnessOverTimePheromone[i][j] = Double.valueOf(yValsPheromone.get(j));
                    }
                }
                pheromoneLevels[i] = "Evaporation Rate " + String.format("%.2f", pheromone) + ": Burma";
                clearXandY();
                pheromone += 0.1;
                gBurmaP = null;
                gBurmaP = setUpGraph(burmaDocument);
                initialisePheromone(gBurmaP);

            }
            displayResults("Evaporation Rate Experimentation - Local Best - Burma",
                    "Evaporation Rate Experimentation - Local Best - Burma", numberOfFitnessEvalsPheromone,
                    fitnessOverTimePheromone, pheromoneLevels, 3000);

            // Alpha/Beta experiment: 0.1/0.9, 0.2/0.8 etc
            double alpha = 0.1;
            double beta = 0.9;

            double[][] numberOfFitnessEvalsAB = new double[10][10000];
            double[][] fitnessOverTimeAB = new double[10][10000];
            String[] ABRatios = new String[10];
            Graph gBurmaAB = setUpGraph(burmaDocument);
            initialisePheromone(gBurmaAB);

            for (int i = 0; i < 10; i++) {
                runAntColonySim(gBurmaAB, 100, 0.7, alpha, beta, 1, 10000, false, 0, false, false);
                ArrayList<Double> xValsAB = getX();
                ArrayList<Double> yValsAB = getY();
                for (int j = 0; j < xValsAB.size(); j++) {
                    if (xValsAB.get(j) != null) {
                        numberOfFitnessEvalsAB[i][j] = Double.valueOf(xValsAB.get(j));
                        fitnessOverTimeAB[i][j] = Double.valueOf(yValsAB.get(j));
                    }
                }
                ABRatios[i] = "Alpha to Beta " + String.format("%.2f", alpha) + "/" + String.format("%.2f", beta)
                        + ": Burma";
                clearXandY();
                alpha += 0.1;
                beta -= 0.1;
                gBurmaAB = null;
                gBurmaAB = setUpGraph(burmaDocument);
                initialisePheromone(gBurmaAB);

            }
            displayResults("Alpha-Beta Ratio - Local Best - Burma", "Alpha-Beta Ratio - Local Best - Burma",
                    numberOfFitnessEvalsAB,
                    fitnessOverTimeAB, ABRatios, 3000);
            // Elitism vs rank (5,10,20,50) vs nothing

            double[][] numberOfFitnessEvalsElite = new double[6][10000];
            double[][] fitnessOverTimeElite = new double[6][10000];
            String[] eliteVRankVNormal = new String[6];
            Graph gBurmaElite = setUpGraph(burmaDocument);
            initialisePheromone(gBurmaElite);
            runAntColonySim(gBurmaElite, 100, 0.7, 0.5, 0.5, 1, 10000, true, 0, false, false);

            ArrayList<Double> xValsElite = getX();
            ArrayList<Double> yValsElite = getY();
            for (int i = 0; i < xValsElite.size(); i++) {
                if (xValsElite.get(i) != null) {

                    numberOfFitnessEvalsElite[0][i] = Double.valueOf(xValsElite.get(i));
                    fitnessOverTimeElite[0][i] = Double.valueOf(yValsElite.get(i));
                }
            }
            eliteVRankVNormal[0] = "Elitism Approach: Burma";
            clearXandY();
            gBurmaElite = null;
            gBurmaElite = setUpGraph(burmaDocument);
            initialisePheromone(gBurmaElite);
            // Rank 5
            runAntColonySim(gBurmaElite, 100, 0.7, 0.5, 0.5, 1, 10000, false, 5, false, false);

            ArrayList<Double> xValsRank5 = getX();
            ArrayList<Double> yValsRank5 = getY();
            for (int i = 0; i < xValsRank5.size(); i++) {
                if (xValsRank5.get(i) != null) {

                    numberOfFitnessEvalsElite[1][i] = Double.valueOf(xValsRank5.get(i));
                    fitnessOverTimeElite[1][i] = Double.valueOf(yValsRank5.get(i));
                }
            }
            eliteVRankVNormal[1] = "Rank Approach - 5: Burma";
            clearXandY();
            gBurmaElite = null;
            gBurmaElite = setUpGraph(burmaDocument);
            initialisePheromone(gBurmaElite);
            // Rank 10
            runAntColonySim(gBurmaElite, 100, 0.7, 0.5, 0.5, 1, 10000, false, 10, false, false);

            ArrayList<Double> xValsRank10 = getX();
            ArrayList<Double> yValsRank10 = getY();
            for (int i = 0; i < xValsRank10.size(); i++) {
                if (xValsRank10.get(i) != null) {

                    numberOfFitnessEvalsElite[2][i] = Double.valueOf(xValsRank10.get(i));
                    fitnessOverTimeElite[2][i] = Double.valueOf(yValsRank10.get(i));
                }
            }
            eliteVRankVNormal[2] = "Rank Approach - 10: Burma";
            clearXandY();
            gBurmaElite = null;
            gBurmaElite = setUpGraph(burmaDocument);
            initialisePheromone(gBurmaElite);

            // Rank 20
            runAntColonySim(gBurmaElite, 100, 0.7, 0.5, 0.5, 1, 10000, false, 20, false, false);

            ArrayList<Double> xValsRank20 = getX();
            ArrayList<Double> yValsRank20 = getY();
            for (int i = 0; i < xValsRank20.size(); i++) {
                if (xValsRank20.get(i) != null) {

                    numberOfFitnessEvalsElite[3][i] = Double.valueOf(xValsRank20.get(i));
                    fitnessOverTimeElite[3][i] = Double.valueOf(yValsRank20.get(i));
                }
            }
            eliteVRankVNormal[3] = "Rank Approach - 20: Burma";
            clearXandY();
            gBurmaElite = null;
            gBurmaElite = setUpGraph(burmaDocument);
            initialisePheromone(gBurmaElite);
            // Rank 50
            runAntColonySim(gBurmaElite, 100, 0.7, 0.5, 0.5, 1, 10000, false, 50, false, false);

            ArrayList<Double> xValsRank50 = getX();
            ArrayList<Double> yValsRank50 = getY();
            for (int i = 0; i < xValsRank50.size(); i++) {
                if (xValsRank50.get(i) != null) {

                    numberOfFitnessEvalsElite[4][i] = Double.valueOf(xValsRank50.get(i));
                    fitnessOverTimeElite[4][i] = Double.valueOf(yValsRank50.get(i));
                }
            }
            eliteVRankVNormal[4] = "Rank Approach - 50: Burma";
            clearXandY();
            gBurmaElite = null;
            gBurmaElite = setUpGraph(burmaDocument);
            initialisePheromone(gBurmaElite);
            runAntColonySim(gBurmaElite, 100, 0.7, 0.5, 0.5, 1, 10000, false, 0, false, false);

            ArrayList<Double> xValsNorm = getX();
            ArrayList<Double> yValsNorm = getY();
            for (int i = 0; i < xValsNorm.size(); i++) {
                if (xValsNorm.get(i) != null) {

                    numberOfFitnessEvalsElite[5][i] = Double.valueOf(xValsNorm.get(i));
                    fitnessOverTimeElite[5][i] = Double.valueOf(yValsNorm.get(i));
                }
            }
            eliteVRankVNormal[5] = "Basic Approach: Burma";
            clearXandY();
            gBurmaElite = null;
            gBurmaElite = setUpGraph(burmaDocument);
            initialisePheromone(gBurmaElite);
            displayResults("Elitism vs Rank vs Basic - Local Best - Burma",
                    "Elitism vs Rank vs Basic - Local Best - Burma", numberOfFitnessEvalsElite,
                    fitnessOverTimeElite, eliteVRankVNormal, 3000);

            // Brazil DS now
            double[][] numberOfFitnessEvalsBr = new double[4][10000];
            double[][] fitnessOverTimeBr = new double[4][10000];
            String[] datasetNamesBr = new String[5];
            ArrayList<Double> antTestFitnessesBrazil = new ArrayList<Double>();
            // Ant population = 1
            initialisePheromone(gBrazil);
            double fitness1Br = runAntColonySim(gBrazil, 10, 0.7, 0.5, 0.5, 1, 10000, false, 0, false, false);
            antTestFitnesses.add(fitness1Br);
            ArrayList<Double> xValsbr = getX();
            ArrayList<Double> yValsbr = getY();
            for (int i = 0; i < xValsbr.size(); i++) {
                if (xValsbr.get(i) != null) {
                    numberOfFitnessEvalsBr[0][i] = Double.valueOf(xValsbr.get(i));
                    fitnessOverTimeBr[0][i] = Double.valueOf(yValsbr.get(i));
                }
            }
            datasetNamesBr[0] = "Ant Population 10: Brazil";
            clearXandY();

            // Ant population = 10
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);
            double fitness2Br = runAntColonySim(gBrazil, 100, 0.7, 0.5, 0.5, 1, 10000, false, 0, false, false);
            antTestFitnessesBrazil.add(fitness2Br);
            ArrayList<Double> xVals2br = getX();
            ArrayList<Double> yVals2br = getY();
            for (int i = 0; i < xVals2br.size(); i++) {
                if (xVals2br.get(i) != null) {

                    numberOfFitnessEvalsBr[1][i] = Double.valueOf(xVals2br.get(i));
                    fitnessOverTimeBr[1][i] = Double.valueOf(yVals2br.get(i));
                }
            }
            datasetNamesBr[1] = "Ant Population 100: Brazil";
            clearXandY();
            // Ant population = 100
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBurma3);
            double fitness3br = runAntColonySim(gBrazil, 1000, 0.7, 0.5, 0.5, 1, 10000, false, 0, false, false);
            antTestFitnessesBrazil.add(fitness3br);
            ArrayList<Double> xVals3br = getX();
            ArrayList<Double> yVals3br = getY();
            for (int i = 0; i < xVals3br.size(); i++) {
                if (xVals3br.get(i) != null) {

                    numberOfFitnessEvalsBr[2][i] = Double.valueOf(xVals3br.get(i));
                    fitnessOverTimeBr[2][i] = Double.valueOf(yVals3br.get(i));
                }
            }
            datasetNamesBr[2] = "Ant Population 1,000: Brazil";
            clearXandY();
            // Ant population = 1,000
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);
            double fitness4br = runAntColonySim(gBrazil, 10000, 0.7, 0.5, 0.5, 1, 10000, false, 0, false, false);
            antTestFitnessesBrazil.add(fitness4br);
            ArrayList<Double> xVals4br = getX();
            ArrayList<Double> yVals4br = getY();
            for (int i = 0; i < xVals4br.size(); i++) {
                if (xVals4br.get(i) != null) {

                    numberOfFitnessEvalsBr[3][i] = Double.valueOf(xVals4br.get(i));
                    fitnessOverTimeBr[3][i] = Double.valueOf(yVals4br.get(i));
                }
            }
            datasetNamesBr[3] = "Ant Population 10,000: Brazil";

            clearXandY();
            // Display the ant test results [4274.0, 3731.0, 4410.0, 4797.0]
            displayResults("Ant Population Experimentation - Local Best - Brazil",
                    "Ant Population Experimentation - Local Best - Brazil", numberOfFitnessEvalsBr,
                    fitnessOverTimeBr, datasetNamesBr, 3000);

            // Evaporation rate experiment: 0.1,0.2,0.3 etc
            double pheromonebr = 0.1;

            double[][] numberOfFitnessEvalsPheromonebr = new double[10][10000];
            double[][] fitnessOverTimePheromonebr = new double[10][10000];
            String[] pheromoneLevelsbr = new String[10];
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);

            for (int i = 0; i < 10; i++) {
                runAntColonySim(gBrazil, 100, pheromonebr, 0.5, 0.5, 1, 10000, false, 0, false, false);
                ArrayList<Double> xValsPheromonebr = getX();
                ArrayList<Double> yValsPheromonebr = getY();
                for (int j = 0; j < xValsPheromonebr.size(); j++) {
                    if (xValsPheromonebr.get(j) != null) {
                        numberOfFitnessEvalsPheromonebr[i][j] = Double.valueOf(xValsPheromonebr.get(j));
                        fitnessOverTimePheromonebr[i][j] = Double.valueOf(yValsPheromonebr.get(j));
                    }
                }
                pheromoneLevelsbr[i] = "Evaporation Rate " + String.format("%.2f", pheromonebr) + ": Brazil";
                clearXandY();
                pheromonebr += 0.1;
                gBrazil = null;
                gBrazil = setUpGraph(brazilDocument);
                initialisePheromone(gBrazil);

            }
            displayResults("Evaporation Rate Experimentation - Local Best - Brazil",
                    "Evaporation Rate Experimentation - Local Best - Brazil", numberOfFitnessEvalsPheromonebr,
                    fitnessOverTimePheromonebr, pheromoneLevelsbr, 3000);

            // Alpha/Beta experiment: 0.1/0.9, 0.2/0.8 etc
            double alphabr = 0.1;
            double betabr = 0.9;
            double[][] numberOfFitnessEvalsABbr = new double[10][10000];
            double[][] fitnessOverTimeABbr = new double[10][10000];
            String[] ABRatiosbr = new String[10];
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);

            for (int i = 0; i < 10; i++) {
                runAntColonySim(gBrazil, 100, 0.7, alphabr, betabr, 1, 10000, false, 0, false, false);
                ArrayList<Double> xValsABbr = getX();
                ArrayList<Double> yValsABbr = getY();
                for (int j = 0; j < xValsABbr.size(); j++) {
                    if (xValsABbr.get(j) != null) {
                        numberOfFitnessEvalsABbr[i][j] = Double.valueOf(xValsABbr.get(j));
                        fitnessOverTimeABbr[i][j] = Double.valueOf(yValsABbr.get(j));
                    }
                }
                ABRatiosbr[i] = "Alpha to Beta " + String.format("%.2f", alphabr) + "/" + String.format("%.2f", betabr)
                        + ": Burma";
                clearXandY();
                alphabr += 0.1;
                betabr -= 0.1;
                gBrazil = null;
                gBrazil = setUpGraph(brazilDocument);
                initialisePheromone(gBrazil);

            }
            displayResults("Alpha-Beta Ratio - Local Best - Brazil", "Alpha-Beta Ratio - Local Best - Brazil",
                    numberOfFitnessEvalsABbr,
                    fitnessOverTimeABbr, ABRatiosbr, 3000);
            // Elitism vs rank (5,10,20,50) vs nothing

            double[][] numberOfFitnessEvalsElitebr = new double[6][10000];
            double[][] fitnessOverTimeElitebr = new double[6][10000];
            String[] eliteVRankVNormalbr = new String[6];
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);
            runAntColonySim(gBrazil, 100, 0.7, 0.5, 0.5, 1, 10000, true, 0, false, false);

            ArrayList<Double> xValsElitebr = getX();
            ArrayList<Double> yValsElitebr = getY();
            for (int i = 0; i < xValsElitebr.size(); i++) {
                if (xValsElitebr.get(i) != null) {

                    numberOfFitnessEvalsElitebr[0][i] = Double.valueOf(xValsElitebr.get(i));
                    fitnessOverTimeElitebr[0][i] = Double.valueOf(yValsElitebr.get(i));
                }
            }
            eliteVRankVNormalbr[0] = "Elitism Approach: Brazil";
            clearXandY();
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);
            // Rank 5
            runAntColonySim(gBrazil, 100, 0.7, 0.5, 0.5, 1, 10000, false, 5, false, false);

            ArrayList<Double> xValsRank5br = getX();
            ArrayList<Double> yValsRank5br = getY();
            for (int i = 0; i < xValsRank5br.size(); i++) {
                if (xValsRank5br.get(i) != null) {

                    numberOfFitnessEvalsElitebr[1][i] = Double.valueOf(xValsRank5br.get(i));
                    fitnessOverTimeElitebr[1][i] = Double.valueOf(yValsRank5br.get(i));
                }
            }
            eliteVRankVNormalbr[1] = "Rank Approach - 5: Brazil";
            clearXandY();
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);
            // Rank 10
            runAntColonySim(gBrazil, 100, 0.7, 0.5, 0.5, 1, 10000, false, 10, false, false);

            ArrayList<Double> xValsRank10br = getX();
            ArrayList<Double> yValsRank10br = getY();
            for (int i = 0; i < xValsRank10br.size(); i++) {
                if (xValsRank10br.get(i) != null) {

                    numberOfFitnessEvalsElitebr[2][i] = Double.valueOf(xValsRank10br.get(i));
                    fitnessOverTimeElitebr[2][i] = Double.valueOf(yValsRank10br.get(i));
                }
            }
            eliteVRankVNormalbr[2] = "Rank Approach - 10: Brazil";
            clearXandY();
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);

            // Rank 20
            runAntColonySim(gBrazil, 100, 0.7, 0.5, 0.5, 1, 10000, false, 20, false, false);

            ArrayList<Double> xValsRank20br = getX();
            ArrayList<Double> yValsRank20br = getY();
            for (int i = 0; i < xValsRank20br.size(); i++) {
                if (xValsRank20br.get(i) != null) {

                    numberOfFitnessEvalsElitebr[3][i] = Double.valueOf(xValsRank20br.get(i));
                    fitnessOverTimeElitebr[3][i] = Double.valueOf(yValsRank20br.get(i));
                }
            }
            eliteVRankVNormalbr[3] = "Rank Approach - 20: Brazil";
            clearXandY();
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);
            // Rank 50
            runAntColonySim(gBrazil, 100, 0.7, 0.5, 0.5, 1, 10000, false, 50, false, false);

            ArrayList<Double> xValsRank50br = getX();
            ArrayList<Double> yValsRank50br = getY();
            for (int i = 0; i < xValsRank50br.size(); i++) {
                if (xValsRank50br.get(i) != null) {

                    numberOfFitnessEvalsElitebr[4][i] = Double.valueOf(xValsRank50br.get(i));
                    fitnessOverTimeElitebr[4][i] = Double.valueOf(yValsRank50br.get(i));
                }
            }
            eliteVRankVNormalbr[4] = "Rank Approach - 50: Brazil";
            clearXandY();
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);
            runAntColonySim(gBrazil, 100, 0.7, 0.5, 0.5, 1, 10000, false, 0, false, false);

            ArrayList<Double> xValsNormbr = getX();
            ArrayList<Double> yValsNormbr = getY();
            for (int i = 0; i < xValsNormbr.size(); i++) {
                if (xValsNormbr.get(i) != null) {

                    numberOfFitnessEvalsElitebr[5][i] = Double.valueOf(xValsNormbr.get(i));
                    fitnessOverTimeElitebr[5][i] = Double.valueOf(yValsNormbr.get(i));
                }
            }
            eliteVRankVNormalbr[5] = "Basic Approach: Brazil";
            clearXandY();
            gBrazil = null;
            gBrazil = setUpGraph(brazilDocument);
            initialisePheromone(gBrazil);
            displayResults("Elitism vs Rank vs Basic - Local Best - Brazil",
                    "Elitism vs Rank vs Basic - Local Best - Brazil", numberOfFitnessEvalsElitebr,
                    fitnessOverTimeElitebr, eliteVRankVNormalbr, 3000);
        } catch (Exception e) {
            System.out.println("Error reading the files: ");
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param appName     - The name of the application window of the graph
     * @param plotTitle   - The title of the graph
     * @param xCoor       - The list of X coordinates
     * @param yCoor       - The list of Y coordinates
     * @param datasetName - The name of each of the lines on the graph
     * @param lowerYBound - If needed, adjusts the lower Y bound to give better
     *                    visibility of the data
     */
    public void displayResults(String appName, String plotTitle, double[][] xCoor, double[][] yCoor,
            String[] datasetName, int lowerYBound) {
        XYLinePlot chart = new XYLinePlot(appName, plotTitle, xCoor, yCoor, datasetName, lowerYBound);
        chart.savePlot("./images/", plotTitle + ".jpeg");
    }

    /**
     * Main method, take in user input and initialise Graph before running the ACO
     * 
     * @param args - String[] - any additional command line args
     */
    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        boolean continuing = false;
        if(args.length == 11){
            AntColonyOptimiser aco1 = new AntColonyOptimiser();
            aco1.clearXandY();
            File acoFile = new File(args[0]);
            Document doc1 = null;
            try{
                doc1 = aco1.parseXML(acoFile);
                Graph runGraph = null;
                runGraph = aco1.setUpGraph(doc1);
                aco1.initialisePheromone(runGraph);
                double fitnessTest = aco1.runAntColonySim(runGraph,Integer.parseInt(args[1]),Double.parseDouble(args[2]), Double.parseDouble(args[3]),Double.parseDouble(args[4]),Double.parseDouble(args[5]),Integer.parseInt(args[6]),Boolean.parseBoolean(args[7]),Integer.parseInt(args[8]),Boolean.parseBoolean(args[9]),Boolean.parseBoolean(args[10]));
                System.out.println(fitnessTest);
            } catch (Exception e){
                System.out.print(e.toString());
            }
        }else{
            continuing = true;
        }
        while (continuing) {
            AntColonyOptimiser aco = new AntColonyOptimiser();
            aco.clearXandY();
            System.out.println("Welcome to the Ant Colony Optimiser!");
            System.out.println("Please enter the file name you wish to use:\n");
            String input = reader.nextLine();
            if (input.equals("-1")) {
                aco.runTests();
                break;
            }
            File acoFile = new File(input);
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
                System.out.println("Please restart and try again.");
            } catch (ParserConfigurationException e) {
                System.out.println("There has been an issue in the configuration build. Please restart and try again.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
                System.out.println("Please restart and try again.");

            } catch (IOException e) {
                System.out.println("An IO exception has been encountered reading this file.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
                System.out.println("Please restart and try again.");

            } catch (SAXException e) {
                System.out.println(
                        "There has been a parse error with the file. Please make sure it's formatted correctly.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
                System.out.println("Please restart and try again.");

            } catch (IllegalArgumentException e) {
                System.out.println("The file cannot be null.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
                System.out.println("Please restart and try again.");

            } catch (Exception e) {
                System.out.println("An unexpected error has occurred.");
                System.out.println("See full exception? Y/N");
                if (reader.nextLine().equalsIgnoreCase("Y")) {
                    System.out.println(e.toString());
                }
                System.out.println("Please restart and try again.");

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