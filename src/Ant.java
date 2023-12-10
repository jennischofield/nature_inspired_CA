package src;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class Ant {
    ArrayList<City> tourMemory = new ArrayList<City>();
    ArrayList<Edge> edgePath = new ArrayList<Edge>();
    City currentCity;
    City startingCity;

    /**
     * Constructor
     * 
     * @param c - The starting City of the Ant
     */
    public Ant(City c) {
        // This sets the starting city
        currentCity = c;
        startingCity = c;
        tourMemory.add(c);
    }

    /**
     * Setter - sets the current city of the ant
     * 
     * @param c - The City to set the current city to
     */
    public void setCurrentCity(City c) {
        currentCity = c;
    }

    /**
     * Getter - gets the current city of the ant
     * 
     * @return City - the current city
     */
    public City getCurrentCity() {
        return currentCity;
    }

    /**
     * Getter - gets the initial city of the ant
     * 
     * @return City - the initial city of the ant
     */
    public City getStartingCity() {
        return startingCity;
    }

    /**
     * Getter - gets the tour memory (list of all cities visited)
     * 
     * @return ArrayList<City> - The list of all the cities visited
     */
    public ArrayList<City> getTourMemory() {
        return tourMemory;
    }

    /**
     * Setter - sets the tour memory (list of all cities visited)
     * 
     * @param c - The ArrayList of all the cities visited
     */
    public void setTourMemory(ArrayList<City> c) {
        tourMemory = c;
    }

    /**
     * Adds a stop on the ant's tour of all the cities
     * 
     * @param c - The city to be added to the tour
     */
    public void addCityToTour(City c) {
        tourMemory.add(c);
    }

    /**
     * Calculates the overall fitness of an ant's route
     * The weight of each edge added together
     * 
     * @return double - The fitness of this ant's solution
     * @throws InvalidParameterException - If a city in the tour memory doesn't have
     *                                   a edge between it and the current city
     */
    public double calculateOverallFitness() throws InvalidParameterException {
        double retValue = 0;
        for (int i = 0; i < tourMemory.size() - 1; i++) {

            // We terminate at size -1 one, as in the case of A->B->C, you only need the
            // measurements for A->B and B->C. There is
            // no C->null value
            // For whatever the next node in the list is...
            String nextCityName = tourMemory.get(i + 1).getName();
            // ...get the name and add the weight from current city to that city
            double valueToAdd = tourMemory.get(i).getWeightForEdge(nextCityName);
            if (valueToAdd != -1) {
                retValue += tourMemory.get(i).getWeightForEdge(nextCityName);
            } else {
                throw new InvalidParameterException(
                        "A city is in the tour memory that a connection doesn't exist for. City " + currentCity + " to "
                                + nextCityName);
            }
        }
        return retValue;
    }

    /**
     * Checks to see if a city has already been visited
     * 
     * @param e - Edge - The edge being explored
     * @return boolean - Whether or not a city has been visited before
     */
    public boolean checkIfVisited(Edge e) {
        for (City c : tourMemory) {
            // This is name comparison, rather than memory address comparison
            if (c.getName() == e.getDestination().getName()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This determines out of the possible paths, which one an ant should take
     * This does it by using the equation:
     * weight * beta / pheromone *alpha
     * We are trying to maximise the pheromone while minimising the weight (or cost)
     * of a path
     * 
     * @param edges           - ArrayList<Edge> - All the possible edges for an ant
     *                        to go down
     * @param pheromoneWeight - The alpha value, how much the pheromone value should
     *                        be taken into account
     * @param weightWeight    - The beta value, how much the weight of an edge
     *                        should be taken into account
     * @return Edge - The best Edge out of the list
     */
    public Edge decidePath(ArrayList<Edge> edges, double pheromoneWeight, double weightWeight) {
        Edge bestEdge = edges.get(0);
        for (Edge e : edges) {
            if ((e.getWeight() * weightWeight)
                    / (e.getPheromone() * pheromoneWeight) < (bestEdge.getWeight() * weightWeight)
                            / (bestEdge.getPheromone() * pheromoneWeight)) {
                bestEdge = e;
            }
        }
        return bestEdge;
    }

    /**
     * This calculates the return edge of the journey. Split out to make the code
     * more legible
     */
    public void returnJourney() {
        ArrayList<Edge> paths = currentCity.getPaths();
        for (Edge e : paths) {
            if (e.getDestination().getName() == startingCity.getName()) {
                edgePath.add(e);
                currentCity = startingCity;
                tourMemory.add(startingCity);
                break;
            }
        }
    }

    /**
     * This calculates the path an ant takes
     * 
     * @param allCities - All the possible cities in the graph
     * @param alpha     - How much the pheromone value impacts the path chosen
     * @param beta      - How much the weight of an edge impacts the path chosen
     */
    public void calculatePath(ArrayList<City> allCities, double alpha, double beta) {
        while (tourMemory.size() < allCities.size()) {
            // Get all the possible paths for the current city,
            // Then remove the cities already visited
            ArrayList<Edge> possiblePaths = new ArrayList<Edge>(currentCity.getPaths());
            ArrayList<Edge> pathsToRemove = new ArrayList<Edge>();
            for (Edge p : possiblePaths) {
                if (checkIfVisited(p)) {
                    pathsToRemove.add(p);
                }
            }
            possiblePaths.removeAll(pathsToRemove);

            // If there are no possible paths, break out of the loop
            if (possiblePaths.size() == 0) {
                break;
            }
            // Pass the possible paths to the decidePath method to get the best path
            Edge bestEdge = decidePath(possiblePaths, alpha, beta);

            // Add the new edge and city to the respective lists, and update the current
            // city to the new city
            edgePath.add(bestEdge);
            currentCity = bestEdge.getDestination();
            tourMemory.add(currentCity);

        }
        // Once the path is found, return to the first node
        returnJourney();
    }

    /**
     * This increases the pheromone value on the edges used
     * 
     * @param d - the increase rate of pheromone
     */
    public void increasePheromoneOnPath(double d) {
        // This gets called after the path has been decided - it iterates through this
        // ant's paths and increases the pheromone by d
        for (Edge e : edgePath) {
            e.updatePheromone(d);
        }
    }

    /**
     * The toString method - displays the current city and overall path fitness
     * 
     * @return String - The String representation
     */
    public String toString() {
        return "My current city is: " + currentCity + " and my overall fitness right now is: "
                + calculateOverallFitness();
    }

    /**
     * This prints the path of an ant with the weights in between each node.
     * This helps for verification of correct fitness evaluation.
     * 
     * @return String - Concatenated list of cities + variables
     */
    public String printEdgePath() {
        String s = currentCity.getName();
        for (Edge e : edgePath) {
            s += "-[" + e.getWeight() + "]>" + e.getDestination().getName();
        }
        return s;
    }
}
