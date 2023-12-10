package src;

public class Edge {
    private City destination;
    private City origin;
    private double weight;
    private double pheromone;

    /**
     * Constructor - Takes in a destination city, the weight of the path, and the
     * origin city
     * 
     * @param d - City - The destination city
     * @param w - double - The edge's weight
     * @param o - City - The origin city
     */
    public Edge(City d, double w, City o) {
        destination = d;
        origin = o;
        weight = w;
        pheromone = -1;
    }

    /**
     * Setter - Sets the destination city
     * 
     * @param d - City - The new destination city
     */
    public void setDestination(City d) {
        destination = d;
    }

    /**
     * Setter - Sets the weight
     * 
     * @param w - double - The new weight
     */
    public void setWeight(double w) {
        weight = w;
    }

    /**
     * Setter - sets the pheromone
     * 
     * @param p - double - The new pheromone value
     */
    public void setPheromone(double p) {
        pheromone = p;
    }

    /**
     * Setter - sets the origin
     * 
     * @param o - City - the new origin
     */
    public void setOrigin(City o) {
        origin = o;
    }

    /**
     * Getter - gets the destination city
     * 
     * @return City - The destination
     */
    public City getDestination() {
        return destination;
    }

    /**
     * Getter - gets the weight of an edge
     * 
     * @return double - The weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Getter - gets the pheromone of an edge
     * 
     * @return double - The pheromone value
     */
    public double getPheromone() {
        return pheromone;
    }

    /**
     * Getter - gets the origin of an edge
     * 
     * @return City - The origin city
     */
    public City getOrigin() {
        return origin;
    }

    /**
     * This evaporates the pheromone at a rate of e
     * 
     * @param e - double - The evaporation rate
     */
    public void evaporatePheromone(double e) {
        pheromone *= (1 - e);
    }

    /**
     * This updates the pheromone on the edge
     * 
     * @param p - double - The value to update the pheromone by
     */
    public void updatePheromone(double p) {
        pheromone *= p;
    }
}