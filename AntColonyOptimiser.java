import java.io.*;
import java.util.Scanner;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
public class AntColonyOptimiser {
    public class Ant {
        ArrayList<City> tourMemory = new ArrayList<City>();
        ArrayList<Edge> edgePath = new ArrayList<Edge>();
        City currentCity;
        public Ant(City c){
            //This sets the starting city
            currentCity = c;
            tourMemory.add(c);
        }
        public void setCurrentCity(City c){
            currentCity = c;
        }
        public City getCurrentCity(){
            return currentCity;
        }
        public ArrayList<City> getTourMemory(){
            return tourMemory;
        }
        public void setTourMemory(ArrayList<City> c){
            tourMemory = c;
        }
        public void addCityToTour(City c){
            tourMemory.add(c);
        }
        public double calculateOverallFitness(){
            double retValue = 0;
            for(int i =0; i<tourMemory.size()-1;i++){

                // We terminate at size -1 one, as in the case of A->B->C, you only need the measurements for A->B and B->C. There is
                // no C->null value
                //For whatever the next node in the list is...
                String nextCityName =  tourMemory.get(i+1).getName();
                //...get the name and add the weight from current city to that city
                retValue += tourMemory.get(i).getWeightForEdge(nextCityName);
            }
            return retValue;
        }
        public boolean checkIfVisited(Edge e){
            for(City c:tourMemory){
                //Safer to do name comparison rather than direct equality - may avoid memory issues down the line
                if(c.getName() == e.getDestination().getName()){
                    return true;
                }
            }
            return false;
        }
        public Edge decidePath(ArrayList<Edge> edges, double pheromoneWeight, double weightWeight){
            Edge bestEdge = edges.get(0);
            for(Edge e:edges){
                //this is a STAND IN HEURISTIC 
                //this is the top half of the equation - by comparing this against all the other best weights, it's effectively 
                //dividing it by the summation of all the other paths.
                if((e.getWeight()*weightWeight)/ (e.getPheromone()*pheromoneWeight) < (bestEdge.getWeight()*weightWeight) / (bestEdge.getPheromone()*pheromoneWeight)){
                    bestEdge = e;
                }
            }
            return bestEdge;
        }
        public void calculatePath(ArrayList<City> allCities, double alpha, double beta){
            //this takes in a hashset (ensures no duplicates on input), but the ant handles using an arraylist (as insertion order 
            //matters)

            while(tourMemory.size() < allCities.size()){
                //get all possible nodes from the current node
                //remove those that already are in tour memory
                // Find the best possible edge from the list
                //add to tourmemory
                ArrayList<Edge>possiblePaths = (ArrayList<Edge>)(currentCity.getPaths()).clone();
                ArrayList<Edge>pathsToRemove = new ArrayList<Edge>();
                for(Edge p:possiblePaths){
                    if(checkIfVisited(p)){
                       pathsToRemove.add(p);
                    }
                }
                possiblePaths.removeAll(pathsToRemove);
                //Now we have a list of actual possible paths
                // Pass that to our ant
                if(possiblePaths.size() == 0){
                    break;
                }
                Edge bestEdge = decidePath(possiblePaths, alpha, beta);
                //Now we've found the best edge, add that edge's destination to the tour memory and set current node to it
                //We also add the best edge to the edge list
                edgePath.add(bestEdge);
                currentCity = bestEdge.getDestination();
                tourMemory.add(currentCity);
                //tour memory has now been increased, continue iterating
            
            }
            //Once this is done, you can then check the overall fitness of the path.
        }
        public void increasePheromoneOnPath(double d){
            //This gets called after the path has been decided - it iterates through this ant's paths and increases the pheromone by d
            for(Edge e:edgePath){
                e.updatePheromone(d);
            }
        }
        public String toString(){
            return "My current city is: " + currentCity + " and my overall fitness right now is: " + calculateOverallFitness(); 
        }
    }    
    public class City {
        private String cityName;
        private ArrayList<Edge> paths;
        public City(String name, ArrayList<Edge> pathList){
            cityName = name;
            paths = pathList;
        }
        public City(String name){
            cityName = name;
            paths = new ArrayList<Edge>();
        }
        public void setName(String name){
            cityName = name;
        }
        public void addPaths(ArrayList<Edge> newPaths){
            paths.addAll(newPaths);
        }
        public void addPath(Edge e){
            paths.add(e);
        }
        public ArrayList<Edge> getPaths(){
            return paths;
        }
        public int containsCity(String name){
            for(int i =0; i<paths.size();i++){
                if(paths.get(i).destination.getName() == name){
                    return i;
                }
            }
            return -1;
        }
        public double getWeightForEdge(String name){

            if(name == null){
                System.out.println("Destination Name Null.");
                return -1;
            }else if(containsCity(name) == -1){
                //We've overridden the equals in the Edge class to check if the name matches, then it exists.
                //This way, we can use arraylist's contains
                System.out.println("There is no path connecting city " + this.cityName + " with " + name);
                return -1;
            }else{
                int edgeIndex = containsCity(name);
                Edge e = paths.get(edgeIndex);
                //This is roundabout, and if there's time, it should be reworked into a hashset or something.
                double returnWeight = e.getWeight();
                return returnWeight;
            }
        }
        public String getName(){
            return cityName;
        }
        public String toString(){
            return cityName + " is connected to " + paths.size() + " other cities.";
        }
        @Override
        public boolean equals(Object o){
            if(this == o){
                return true;
            }
            if(o == null ||o.getClass() != this.getClass()){
                return false;
            }
            return ((City)o).getName().equals(this.cityName);
        }
    }
    public class Edge {
        private City destination;
        private double weight;
        private double pheromone;
        public Edge(City d, double w){
            destination = d;
            weight = w;
            pheromone = -1;
        }
        public void setDestination(City d){
            destination = d;
        }
        public void setWeight(double w){
            weight = w;
        }
        public void setPheromone(double p){
            pheromone = p;
        }
        public City getDestination(){
            return destination;
        }
        public double getWeight(){
            return weight;
        }
        public double getPheromone(){
            return pheromone;
        }
        public void evaporatePheromone(double e){
            pheromone *= e;
        }
        public void updatePheromone(double p){
            //This is until I set up the update function of Q/fitness
            pheromone  *= p;
        }
        @Override
        public boolean equals(Object o){
            if(this == o){
                return true;
            }
            
            if((o.getClass()) == String.class){
                //This checks to see if the edge is the same - a workaround for getting the weights from the ant (which only has cities)
                return (o.toString() == this.destination.toString());
            }
            if(o == null ||o.getClass() != this.getClass()){
                return false;
            }
            return ((Edge)o).getDestination().equals(this.destination);
        }
    }
    public class Graph {
        private ArrayList<City> cities;
        //this needs to be an arraylist bc i need to get values from it. implement duplicate checking
        private Edge[][] adjacencyMatrix;
        public Graph(){
            cities = new ArrayList<City>();
        }
        public Graph(ArrayList<City> c){
            cities = c;
        }
        public ArrayList<City> getCities(){
            return cities;
        
        }
        public void addCity(City c){
            cities.add(c);
        }
        public void setAdjacencyMatrix(int i){
            //We know that the adjacency matrix will always be square
            adjacencyMatrix = new Edge[i][i];
        }
        public Edge[][] getAdjacencyMatrix(){
            return adjacencyMatrix;
        }
        public void repointEdges(){
            for(int i =0; i < adjacencyMatrix[0].length;i++){
                for(int j=0; j <adjacencyMatrix.length; j++){
                    if(adjacencyMatrix[i][j] != null){
                        //null check to ignore the self case
                        adjacencyMatrix[i][j].setDestination(cities.get(j));
                    }
                }
            }
        }
        public void setAdjacencyMatrixValue(int i, int j, Edge e){
            if(adjacencyMatrix[i][j] == null){
                adjacencyMatrix[i][j] = e;
                //Symmetry! they're weighted, not-directional graphs. [1][2] is the same weight as [2][1]
                //No, actually. The destination needs to point at the correct value.
            }
        }
        public void evaporatePaths(double evaporation){
            for(int i = 0; i <adjacencyMatrix[0].length; i++){
                for(int j =0; j<adjacencyMatrix.length; j++){
                    if(adjacencyMatrix[i][j] != null){
                        //Can't evaporate the city self connection
                        adjacencyMatrix[i][j].evaporatePheromone(evaporation);
                    }
                }
            }
        }
        public String toString(){
            String retString = "";
            for(City c:cities){
                retString += c.toString();
                retString += "\n";
            }
            retString += "\nAdjacency Matrix:\n";
            for(int i = 0; i < adjacencyMatrix[0].length; i++){
                for(int j =0; j<adjacencyMatrix.length; j++){
                    
                    if(adjacencyMatrix[i][j]==null){
                        retString += "Self";
                    }else{
                        retString += adjacencyMatrix[i][j].getWeight();
                    }
                    retString += " ";
                }
                retString += "\n";
            }
            retString += "\nPheromoneMatrix:\n";
            for(int i = 0; i < adjacencyMatrix[0].length; i++){
                for(int j =0; j<adjacencyMatrix.length; j++){
                    
                    if(adjacencyMatrix[i][j]==null){
                        retString += "None";
                    }else{
                        retString += adjacencyMatrix[i][j].getPheromone();
                    }
                    retString += " ";
                }
                retString += "\n";
            }
            return retString;
        }
    }
    public AntColonyOptimiser(){

    }
    public Document parseXML(File f) throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {
       
        DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(f);
        doc.getDocumentElement().normalize();
        return doc;
    }
    public Graph setUpGraph(Document doc){
        //Add error catching.
        Graph retGraph = new Graph();
        NodeList nl = doc.getElementsByTagName("vertex");
        retGraph.setAdjacencyMatrix(nl.getLength());
        for(int i = 0; i < nl.getLength(); i++){
            Node n = nl.item(i); //This gets the entire 'vertex', which is a City
           
            City newCity = new City(Integer.toString(i));
            
            //Now get that city's weights
            NodeList edgeList = n.getChildNodes();
            for(int j = 0; j < edgeList.getLength(); j++){
                Node newEdgeNode = edgeList.item(j);
                
                if (newEdgeNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element elem = (Element)newEdgeNode;
                    //This creates a new edge, with the destination's value & cost
                    //It's okay to create a new city, as the equals method has been overridden
                    //just kidding! it isn't. You need to figure out a way to get the edge assigned to the relevant city
                    //Right now, it's just getting a blank city with no connections. 
                    //Something like after interpreting all this, iterate over the entire adjacency matrix, and point the city of
                    //that edge towards the column's city.
                    System.err.println(elem.getTextContent());
                    Edge newEdge = new Edge(new City(elem.getTextContent()),Double.parseDouble(elem.getAttribute("cost")) );
                    newCity.addPath(newEdge);
                    System.out.println(newCity);
                    retGraph.setAdjacencyMatrixValue(i ,Integer.parseInt(elem.getTextContent()), newEdge);
                    //This iterates through all the edges for one 'vertex' in the file
                    //This assigns a vertex its paths, and also inputs it the graph's own adjacency matrix
                    // After it's done, it moves onto the next vertex in the list.
                }
            }
            retGraph.addCity(newCity);
            //This iterates through all the cities and adds them to the graph
        }
        //test check
        retGraph.repointEdges();
        //The above makes it so the edge's destinations are actually pointing to the City, not a placeholder.
        return retGraph;
    }
    public void initialisePheromone(Graph g){
        for(int i = 0; i < g.adjacencyMatrix[0].length; i++){
            for(int j = 0; j <g.adjacencyMatrix.length;j++){
                if(g.adjacencyMatrix[i][j] != null){
                    if(g.adjacencyMatrix[i][j].getPheromone() == -1){
                        //There's a possibility that the pheromone would be 0, so to represent the uninitalised pheromone, it has to be -1
                        double ph = Math.random();
                        g.adjacencyMatrix[i][j].setPheromone(ph);
                        g.adjacencyMatrix[j][i].setPheromone(ph);
                        //symmetry again

                    }
                }
            }
        }
    }
    public City getRandomCity(Graph g)throws IllegalArgumentException{
        if(g == null){
           throw new IllegalArgumentException("Graph cannot be null");
        }
        ArrayList<City> cities = g.getCities();
        if(cities == null || cities.isEmpty()){
            throw new IllegalArgumentException("City list can't be null nor empty");
        }
        int randomIndex = new Random().nextInt(cities.size());
        int i =0;
        for (City c : cities) {
            if (i == randomIndex) {
                return c;
            }
            i++;
        }
        throw new IllegalStateException("Something's gone wrong picking a random starting city");
    }
    public String printPath(ArrayList<City> cl){
        String retString = "Path: ";
        for(int i =0; i<cl.size()-1; i++){
            retString += cl.get(i).getName() +" -> ";
        }
        retString += cl.get(cl.size()-1).getName();
        return retString;
    }
    public double runAntColonySim(Graph graph, int numAnts, double evaporationRate, double alpha, double beta, int terminationCount){
        //Intitalise the ants
        ArrayList<Ant> antColony = new ArrayList<Ant>();
        
        //Ants initialised
        //Create the condition - after 10,000 fitness evaluations, terminate and return the best fitness.
        int fitnessEvals = 0;
        double bestFitness = Double.MAX_VALUE;
        Ant bestAnt = new Ant(null);
        ArrayList<City> bestPath = new ArrayList<City>();
        while(fitnessEvals < terminationCount){
            //run each ant's path
            //evaluate the fitness of those paths
            //then update all the paths - this needs to be updated to reflect the weights but w/e
            //evaporate the paths
            //The check happens after all this, and since we need to re-randomise the ants, we create them here. 
            // We clear the list at the end - the best path/ant/fitness is saved
            for(int i =0; i<numAnts; i++){
                City startCity = getRandomCity(graph);
                Ant newAnt = new Ant(startCity);
                antColony.add(newAnt);
            }
            System.out.println(antColony.size());
            int count = 0;
            for(Ant a:antColony){
                
                a.calculatePath(graph.getCities(), alpha, beta);
                double antFitness = a.calculateOverallFitness();
                fitnessEvals++;
                if(antFitness < bestFitness){
                    bestFitness = antFitness;
                    bestPath = a.getTourMemory();
                    bestAnt = a;
                    System.out.println("Best Fitness is now:" + bestFitness +"\nBest Ant: " + bestAnt + " # "+count);
                    
                }
                count++;
            }
            //Updating the path
            for(Ant a: antColony){
                //Inverse of the best fitness
                a.increasePheromoneOnPath(1/bestFitness);
            }
            
            //Evaporate all the paths
            graph.evaporatePaths(evaporationRate);
            //Reset the antcolony
            antColony.clear();

        }
        System.out.println(printPath(bestPath));
        System.out.println(bestAnt);
        return bestFitness;
    } 
    
    public static void main(String [] args){
        AntColonyOptimiser aco = new AntColonyOptimiser();
        File testFile = new File("Test.xml");
        Document testDoc = null;
        try{
            testDoc = aco.parseXML(testFile);
        }catch(Exception e){
            System.out.print("Error:" + e.toString());
        }

        Graph testGraph = aco.setUpGraph(testDoc);
        aco.initialisePheromone(testGraph);
        aco.runAntColonySim(testGraph, 10, 0.7, 0.5, 0.5, 100);
        
        //Okay, so the file reading and graph configuration is set up, 
        //Albeit with some formatting errors. Next steps is to set up the Ants and the other
         /*
          * 1) Set up the random pheromone distribution
          2)Generate x amount of ants and have them navigate the graph
          3) Making them bias, naturally,
          4)Phermone update + reward based on improvement to fitness
            i) Fitness is probably something abt shortest distance
            5) Evaporation RAte
            6)Termination condition

            So now we have a rough ACO - we haven't generated the ants but that would go in the "initialise aco methods"
          
          Okay, so the core of it's working - now to fix a weird bug. 
          Somehow, it's getting impossible fitnesses. IF i had to guess, there's something not letting the ants run all the way through. 
          A 1 and 0 fitness is impossible...so somewhere it's not adding the paths correctly to each ant's move
            */

    }
}