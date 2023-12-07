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
        public String toString(){
            String retString = "";
            for(City c:cities){
                retString += c.toString();
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
                    Edge newEdge = new Edge(new City(elem.getNodeValue()),Double.parseDouble(elem.getAttribute("cost")) );
                    newCity.addPath(newEdge);
                    //This iterates through all the edges for one 'vertex' in the file
                    // After it's done, it moves onto the next vertex in the list.
                }
            }
            retGraph.addCity(newCity);
            //This iterates through all the cities and adds them to the graph
        }
        return retGraph;
    }
    public void initialisePheromone(Graph g){
        
    }
    public static void main(String [] args){
        AntColonyOptimiser aco = new AntColonyOptimiser();
        File testFile = new File("Test.xml");
        try{
        Document testDoc = aco.parseXML(testFile);
        Graph testGraph = aco.setUpGraph(testDoc);
        System.out.println(testGraph);
        }catch(Exception e){
            System.out.print("Error:" + e.toString());
        }
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