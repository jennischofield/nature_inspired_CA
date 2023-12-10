package src;

import java.util.ArrayList;

public class City {
    private String cityName;
    private ArrayList<Edge> paths;

    /**
     * Constructor - takes in the name and initialises path list
     * 
     * @param name     - String - City name
     * @param pathList - ArrayList<Edge> - Associated edges list
     */
    public City(String name, ArrayList<Edge> pathList) {
        cityName = name;
        paths = pathList;
    }

    /**
     * Constructor - takes in name only, and creates an empty pathList
     * 
     * @param name - String - City name
     */
    public City(String name) {
        cityName = name;
        paths = new ArrayList<Edge>();
    }

    /**
     * Setter - sets name of city
     * 
     * @param name - String - new city name
     */
    public void setName(String name) {
        cityName = name;
    }

    /**
     * Adds new paths to the city
     * 
     * @param newPaths - ArrayList<Edge> - list of paths to be added
     */
    public void addPaths(ArrayList<Edge> newPaths) {
        paths.addAll(newPaths);
    }

    /**
     * Adds a single Edge to the path list
     * 
     * @param e - Edge - The edge to be added
     */
    public void addPath(Edge e) {
        paths.add(e);
    }

    /**
     * Getter - gets the list of paths
     * 
     * @return ArrayList<Edge> - the paths of the city
     */
    public ArrayList<Edge> getPaths() {
        return paths;
    }

    /**
     * Checks if a path exists between this city and another
     * 
     * @param name - String - The city to check
     * @return int - The index in the list
     */
    public int containsCity(String name) {
        for (int i = 0; i < paths.size(); i++) {
            if (paths.get(i).getDestination().getName() == name) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the weight for a specific city to city connection
     * 
     * @param name - String - The city to check
     * @return double - The weight of an edge
     */
    public double getWeightForEdge(String name) {

        if (name == null) {
            System.out.println("Destination Name Null.");
            return -1;
        } else if (containsCity(name) == -1) {
            System.out.println("There is no path connecting city " + this.cityName + " with " + name);
            return -1;
        } else {
            int edgeIndex = containsCity(name);
            Edge e = paths.get(edgeIndex);
            double returnWeight = e.getWeight();
            return returnWeight;
        }
    }

    /**
     * Getter - Gets the name of the city
     * 
     * @return String - The name of the city
     */
    public String getName() {
        return cityName;
    }

    /**
     * toString - Returns the city name and number of paths nicely formatted
     * 
     * @return String - The name + paths nicely formatted
     */
    public String toString() {
        return cityName + " is connected to " + paths.size() + " other cities.";
    }

    /**
     * The equals override - If a city's name matches, then it's also a match.
     * 
     * @param o - Object - The object to compare this City to
     * @return boolean - Whether or not the two objects are matches
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        return ((City) o).getName().equals(this.cityName);
    }
}
