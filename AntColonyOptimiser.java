import java.io.*;
import java.util.Scanner;

import javax.print.attribute.standard.JobStateReasons;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.HashSet;
public class AntColonyOptimiser {

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
        public City getNode(){
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
          */

    }
}