package Backend.Algorithms;

import Backend.RoutingEngine;
import DataStructures.Coordinate;
import DataStructures.Node;
import DataStructures.Route;
import Database.Queries.TripIdQuery;
import Helper.Calculator.WalkingCalculator;
import com.opencsv.exceptions.CsvValidationException;
import lombok.Getter;

import java.util.*;

@Getter
public class AStar {
    private Node startingNode;
    private Node destinationNode;
    private int startTime;
    private int tripTime;
    private PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(Node::getFCost));
    private List<Node> closed = new ArrayList<>();
    private List<Node> path;
    private TripIdQuery tripIdQuery = new TripIdQuery();
    private WalkingCalculator walkingCalculator = new WalkingCalculator();
    private int threshold;
    private Route route;

    public AStar(Node startingNode, Node destinationNode, int startTime) {
        this.startingNode = startingNode;
        this.destinationNode = destinationNode;
        this.startTime = startTime;
        this.threshold = 2;

        this.startingNode.setHCost(this.destinationNode);
        this.startingNode.setGCost(0);
        this.startingNode.setFCost(this.startingNode.calculateFCost());
        open.add(this.startingNode);
        calculatePath();
        retracePath(this.startingNode, this.destinationNode);
        if (!path.contains(startingNode)) path.add(startingNode);
        if (!path.contains(destinationNode)) path.add(destinationNode);
        this.route = new Route(path, tripTime);
    }

    public static void main(String[] args) throws CsvValidationException {
        RoutingEngine engine = new RoutingEngine();
        engine.loadDatabase("src/main/resources/hsl.zip");

        Node startingNode = new Node(new Coordinate(60.1571193, 24.8578216), 600);
        Node destinationNode = new Node(new Coordinate(60.2123793, 24.9491987), 0);

        long start = System.currentTimeMillis();
        AStar a = new AStar(startingNode, destinationNode, 660);
        System.err.println("A* runtime = " + (System.currentTimeMillis() - start) + " ms");
        System.err.println("final time: " + a.getTripTime() + " minutes");
    }

    private void calculatePath() {
        Node current;
        long start = System.currentTimeMillis();
        while (!open.isEmpty()) {
            current = open.remove();
            closed.add(current);
            int walkingTimeFromCurrent = walkingCalculator.calculateTime(
                    current.getCoordinate(),
                    destinationNode.getCoordinate()
            );
            if (!"walk".equals(current.getTransportType()) && walkingTimeFromCurrent <= 10) {
                destinationNode.setGCost(current.getGCost() + walkingTimeFromCurrent);
                destinationNode.setParent(current);
                destinationNode.setTransportType("walk");
                destinationNode.setTime(startTime + destinationNode.getGCost());
                break;
            }

            current.setHCost(destinationNode);
            current.setFCost(current.calculateFCost());

            if (current.equals(destinationNode)) {
                break;
            }
            if ((System.currentTimeMillis() - start) > 5000) {
                System.err.println("runtime exceeded 5 seconds");
                destinationNode.setDateTime(walkingCalculator.calculateTime(startingNode.getCoordinate(), destinationNode.getCoordinate()));
                break;
            }
            closed.add(current);
            for (Node neighbor : current.generateNeighbors(current.getGCost() + startTime + 30)) {
                int edgeTime;
                try {
                    if ("walk".equals(neighbor.getTransportType())) {
                        edgeTime = walkingCalculator.calculateTime(current.getCoordinate(), neighbor.getCoordinate());
                    }
                    if ((!"walk".equals(neighbor.getTransportType())) && (current.getTransportType().equals("walk"))) {
                        edgeTime = walkingCalculator.calculateTime(current.getCoordinate(), neighbor.getCoordinate());
                    }
                    if ((!current.getTransportType().equals("walk")) && (neighbor.getTransportType().equals("walk"))) {
                        int walkingTime = walkingCalculator.calculateTime(current.getCoordinate(), neighbor.getCoordinate());
                        edgeTime = walkingTime;
                        neighbor.setTime(current.getTime() + walkingTime);
                    } else {
                        edgeTime = neighbor.getTime() - current.getTime();
                    }
                } catch (NullPointerException e) { // this is for the first node
                    edgeTime = neighbor.getTime() - current.getTime();
                }
                int cumulativeGCost = current.getGCost() + edgeTime;
                if ((edgeTime > 30) && (neighbor.getTripId().equals("0"))) {
                    continue;
                }
                if (open.contains(neighbor) && cumulativeGCost < neighbor.getGCost()) {
                    open.remove(neighbor);
                }
                if (closed.contains(neighbor) && cumulativeGCost < neighbor.getGCost()) {
                    closed.remove(neighbor);
                }
                if (!open.contains(neighbor) && !closed.contains(neighbor)) {
                    neighbor.setGCost(cumulativeGCost);
                    neighbor.setHCost(destinationNode);
                    neighbor.setFCost(neighbor.calculateFCost());
                    open.add(neighbor);
                    neighbor.setParent(current);
                }
                if (current.getHCost() < 5) {
                    if (current.getHCost() < threshold) {
                        open.add(destinationNode);
                        destinationNode.setGCost(cumulativeGCost);
                        destinationNode.setHCost(destinationNode);
                        destinationNode.setFCost(destinationNode.calculateFCost());
                        destinationNode.setParent(current);
                        break;
                    } else if (threshold < 10) {
                        threshold++;
                    }
                }
            }
        }
    }

    private void retracePath(Node startNode, Node destinationNode) {
        path = new ArrayList<>();
        Node currentNode = destinationNode;

        if (currentNode.getParent() == null) { // edge case when we walk from start to destination without taking public transit
            tripTime = startNode.getHCost();
            path.add(startNode);
            path.add(destinationNode);
            return;
        }

        tripTime = currentNode.getGCost();
        currentNode.setDateTime(tripTime + startTime);

        while (currentNode != startNode) {
            path.add(currentNode);
            currentNode = currentNode.getParent();
        }
        path.add(startNode);
        Collections.reverse(path);

        // Remove consecutive walking nodes
        List<Node> filteredPath = new ArrayList<>();
        Node previousNode = null;

        for (Node node : path) {
            if (previousNode != null && "walk".equals(previousNode.getTransportType()) && "walk".equals(node.getTransportType())) {
                continue;
            }
            filteredPath.add(node);
            previousNode = node;
        }

        path = filteredPath;

        int destinationTime = destinationNode.getGCost() + this.startTime;
        destinationNode.setTime(destinationTime);
    }
}