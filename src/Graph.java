package src;

import java.util.ArrayList;

public class Graph {
    private ArrayList<City> cities;
    // this needs to be an arraylist bc i need to get values from it. implement
    // duplicate checking
    private Edge[][] adjacencyMatrix;

    /**
     * Constructor - initialises the cities list to an empty ArrayList
     */
    public Graph() {
        cities = new ArrayList<City>();
    }

    /**
     * Constructor - initialises the cities list to the input
     * 
     * @param c - ArrayList<City> - the list of cities
     */
    public Graph(ArrayList<City> c) {
        cities = c;
    }

    /**
     * Getter - gets the list of cities
     * 
     * @return ArrayList<City> - the list of cities to return
     */
    public ArrayList<City> getCities() {
        return cities;

    }

    /**
     * Adds a city to the Graph
     * 
     * @param c - City - the city to add to the Graph
     */
    public void addCity(City c) {
        cities.add(c);
    }

    /**
     * Creates a new square adjacency matrix with dimension i
     * 
     * @param i - int - the dimension of the matrix
     */
    public void setAdjacencyMatrix(int i) {
        adjacencyMatrix = new Edge[i][i];
    }

    /**
     * Getter - gets the adjacency matrix
     * 
     * @return Edge[][] - the adjacency matrix
     */
    public Edge[][] getAdjacencyMatrix() {
        return adjacencyMatrix;
    }

    /**
     * Redirects the pointer in the adjacency list's Edge's to reference the correct
     * destination
     * As the corresponding city cannot be confirmed to be created until after the
     * full parsing of the input XML
     */
    public void repointEdges() {
        for (int i = 0; i < adjacencyMatrix[0].length; i++) {
            for (int j = 0; j < adjacencyMatrix.length; j++) {
                if (adjacencyMatrix[i][j] != null) {
                    // null check to ignore where the destination is also the current city
                    adjacencyMatrix[i][j].setDestination(cities.get(j));
                }
            }
        }
    }

    /**
     * Setter - sets values in the adjacency matrix
     * 
     * @param i - int - the row index
     * @param j - int - the column index
     * @param e - Edge - the Edge to be added at that index, then repointed later.
     */
    public void setAdjacencyMatrixValue(int i, int j, Edge e) {
        if (adjacencyMatrix[i][j] == null) {
            adjacencyMatrix[i][j] = e;
        }
    }

    /**
     * Evaporates the pheromone throughout the adjacency matrix
     * 
     * @param evaporation - double - the evaporation rate
     */
    public void evaporatePaths(double evaporation) {
        for (int i = 0; i < adjacencyMatrix[0].length; i++) {
            for (int j = 0; j < adjacencyMatrix.length; j++) {
                if (adjacencyMatrix[i][j] != null) {
                    // Can't evaporate the connection between the city and itself
                    adjacencyMatrix[i][j].evaporatePheromone(evaporation);
                }
            }
        }
    }

    /**
     * toString - Displays all the cities, the adjacency matrix, and pheromone
     * matrix of this Graph
     * 
     * @return String - the formatted toString
     */
    public String toString() {
        String retString = "";
        for (City c : cities) {
            retString += c.toString();
            retString += "\n";
        }
        retString += "\nAdjacency Matrix:\n";
        for (int i = 0; i < adjacencyMatrix[0].length; i++) {
            for (int j = 0; j < adjacencyMatrix.length; j++) {

                if (adjacencyMatrix[i][j] == null) {
                    retString += "Self";
                } else {
                    retString += adjacencyMatrix[i][j].getWeight();
                }
                retString += " ";
            }
            retString += "\n";
        }
        retString += "\nPheromoneMatrix:\n";
        for (int i = 0; i < adjacencyMatrix[0].length; i++) {
            for (int j = 0; j < adjacencyMatrix.length; j++) {

                if (adjacencyMatrix[i][j] == null) {
                    retString += "None";
                } else {
                    retString += adjacencyMatrix[i][j].getPheromone();
                }
                retString += " ";
            }
            retString += "\n";
        }
        return retString;
    }
}
