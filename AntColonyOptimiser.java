import java.io.*;
import java.util.Scanner;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.HashSet;
public class AntColonyOptimiser {
    public class Ant {
        ArrayList<City> tourMemory;
        ArrayList<Edge> edgePath;
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
        public Edge decidePath(ArrayList<Edge> edges){
            Edge bestEdge = edges.get(0);
            for(Edge e:edges){
                //this is a STAND IN HEURISTIC 
                if(e.getPheromone()* e.getWeight() < bestEdge.getPheromone() * bestEdge.getWeight()){
                    bestEdge = e;
                }
            }
            return bestEdge;
        }
        public void calculatePath(HashSet<City> allCities){
            //this takes in a hashset (ensures no duplicates on input), but the ant handles using an arraylist (as insertion order 
            //matters)

            while(tourMemory.size() < allCities.size()){
                //get all possible nodes from the current node
                //remove those that already are in tour memory
                // Find the best possible edge from the list
                //add to tourmemory
                ArrayList<Edge>possiblePaths = currentCity.getPaths();
                for(Edge p:possiblePaths){
                    if(checkIfVisited(p)){
                        possiblePaths.remove(p);
                    }
                }
                //Now we have a list of actual possible paths
                // Pass that to our ant
                Edge bestEdge = decidePath(possiblePaths);
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
        public double getWeightForEdge(String name){

            if(name == null){
                System.out.println("Destination Name Null.");
                return -1;
            }else if(!paths.contains(name)){
                //We've overridden the equals in the Edge class to check if the name matches, then it exists.
                //This way, we can use arraylist's contains
                System.out.println("There is no path connecting city " + this.cityName + " with " + name);
                return -1;
            }else{
                int edgeIndex = paths.indexOf(name);
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
            return cityName + "is connected to " + paths.size() + " other cities.";
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
        private HashSet<City> cities;
        private Edge[][] adjacencyMatrix;
        public Graph(){
            cities = new HashSet<City>();
        }
        public Graph(HashSet<City> c){
            cities = c;
        }
        public HashSet<City> getCities(){
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
        public void setAdjacencyMatrixValue(int i, int j, Edge e){
            if(adjacencyMatrix[i][j] == null){
                adjacencyMatrix[i][j] = e;
                adjacencyMatrix[j][i] = e;
                //Symmetry! they're weighted, not-directional graphs. [1][2] is the same weight as [2][1]
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
        System.out.println(testGraph);
        aco.initialisePheromone(testGraph);
        System.out.println(testGraph);
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
          */

    }
}