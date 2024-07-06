package com.example;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DirectedGraph {

    private Graph<String, DefaultWeightedEdge> graph;

    public DirectedGraph() {
        graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    public void buildGraphFromFile(String filePath) {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            scanner.useDelimiter("[\\s,.;!?]+"); // Split by whitespace and punctuation
            String previousWord = null;

            while (scanner.hasNext()) {
                String word = scanner.next().toLowerCase();

                if (!word.matches("[a-zA-Z]+")) {
                    continue; // Skip non-alphabetic strings
                }

                graph.addVertex(word);

                if (previousWord != null) {
                    graph.addVertex(previousWord);

                    DefaultWeightedEdge edge = graph.getEdge(previousWord, word);
                    if (edge == null) {
                        edge = graph.addEdge(previousWord, word);
                        graph.setEdgeWeight(edge, 1);
                    } else {
                        graph.setEdgeWeight(edge, graph.getEdgeWeight(edge) + 1);
                    }
                }

                previousWord = word;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void displayGraph() {
        System.out.println("Graph: ");
        for (String vertex : graph.vertexSet()) {
            System.out.print(vertex + ": ");
            for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(vertex)) {
                System.out.print(Graphs.getOppositeVertex(graph, edge, vertex) + "(" + graph.getEdgeWeight(edge) + ") ");
            }
            System.out.println();
        }
    }

    public void visualizeGraph() {
        mxGraph mxgraph = new mxGraph();
        Object parent = mxgraph.getDefaultParent();

        mxgraph.getModel().beginUpdate();
        try {
            Map<String, Object> cells = new HashMap<>();
            for (String vertex : graph.vertexSet()) {
                Object v = mxgraph.insertVertex(parent, null, vertex, 100, 100, 80, 30);
                cells.put(vertex, v);
            }

            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                mxgraph.insertEdge(parent, null, graph.getEdgeWeight(edge), cells.get(source), cells.get(target));
            }
        } finally {
            mxgraph.getModel().endUpdate();
        }

        mxIGraphLayout layout = new mxCircleLayout(mxgraph);
        layout.execute(mxgraph.getDefaultParent());

        mxGraphComponent graphComponent = new mxGraphComponent(mxgraph);
        JFrame frame = new JFrame();
        frame.getContentPane().add(graphComponent);
        frame.setTitle("Directed Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    // Functionality 3: Query bridge words between word1 and word2
    public String queryBridgeWords(String word1, String word2) {
        if (!graph.containsVertex(word1) || !graph.containsVertex(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        Set<String> bridgeWords = new HashSet<>();
        Set<DefaultWeightedEdge> outgoingEdges = graph.outgoingEdgesOf(word1);

        for (DefaultWeightedEdge edge : outgoingEdges) {
            String neighbor = graph.getEdgeTarget(edge);
            if (graph.containsEdge(neighbor, word2)) {
                bridgeWords.add(neighbor);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridgeWords);
        }
    }

    // Functionality 4: Generate new text with bridge words
    public String generateNewText(String inputText) {
        StringBuilder result = new StringBuilder();
        Scanner scanner = new Scanner(inputText);

        String previousWord = null;
        while (scanner.hasNext()) {
            String word = scanner.next().toLowerCase();

            if (!word.matches("[a-zA-Z]+")) {
                result.append(word).append(" ");
                continue; // Skip non-alphabetic strings
            }

            if (previousWord != null) {
                Set<String> bridgeWords = queryBridgeWordsSet(previousWord, word);
                if (!bridgeWords.isEmpty()) {
                    String bridge = getRandomElement(bridgeWords);
                    result.append(bridge).append(" ");
                }
            }

            result.append(word).append(" ");
            previousWord = word;
        }

        return result.toString().trim();
    }

    private Set<String> queryBridgeWordsSet(String word1, String word2) {
        Set<String> neighbors1 = Graphs.neighborSetOf(graph, word1);
        Set<String> bridgeWords = new HashSet<>();

        for (String neighbor : neighbors1) {
            if (graph.containsEdge(neighbor, word2)) {
                bridgeWords.add(neighbor);
            }
        }

        return bridgeWords;
    }

    // Functionality 5: Calculate shortest path between word1 and word2
    public String calcShortestPath(String word1, String word2) {
        if (!graph.containsVertex(word1) || !graph.containsVertex(word2)) {
            return "One or both of the words are not in the graph!";
        }

        DijkstraShortestPath<String, DefaultWeightedEdge> shortestPathAlgorithm =
                new DijkstraShortestPath<>(graph);

        List<String> shortestPath;
        try {
            shortestPath = shortestPathAlgorithm.getPath(word1, word2).getVertexList();
        } catch (Exception e) {
            return "No path found between " + word1 + " and " + word2 + "!";
        }

        double pathWeight = shortestPathAlgorithm.getPathWeight(word1, word2);

        return "Shortest path from " + word1 + " to " + word2 + " is: " 
        +
                String.join("->", shortestPath) + ". Path weight: " + pathWeight;
    }

    // Functionality 6: Random walk
    public String randomWalk(String filePath) {
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        String currentNode = getRandomElement(graph.vertexSet());
        result.append(currentNode).append(" ");

        while (true) {
            Set<DefaultWeightedEdge> outgoingEdges = graph.outgoingEdgesOf(currentNode);
            if (outgoingEdges.isEmpty()) {
                break;
            }

            DefaultWeightedEdge randomEdge = getRandomElement(outgoingEdges);
            String nextNode = graph.getEdgeTarget(randomEdge);

            result.append(nextNode).append(" ");

            if (graph.outDegreeOf(nextNode) == 0 || result.indexOf(nextNode) != -1) {
                break;
            }

            currentNode = nextNode;
        }

        // Write to file
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(result.toString().trim());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString().trim();
    }

    private <T> T getRandomElement(Set<T> set) {
        int randomIndex = new Random().nextInt(set.size());
        int currentIndex = 0;
        for (T element : set) {
            if (currentIndex == randomIndex) {
                return element;
            }
            currentIndex++;
        }
        throw new NoSuchElementException("Set is empty");
    }

    public static void main(String[] args) {
        DirectedGraph directedGraph = new DirectedGraph();

        // Specify the path to your file here
        String filePath = "/Volumes/[C] Windows 11/Users/osheenconstable/directed-graph/directed-graph/text1.txt";

        directedGraph.buildGraphFromFile(filePath);
        directedGraph.displayGraph();
        directedGraph.visualizeGraph();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Choose an option:");
            System.out.println("1. Query bridge words");
            System.out.println("2. Generate new text with bridge words");
            System.out.println("3. Calculate shortest path");
            System.out.println("4. Perform random walk");
            System.out.println("5. Exit");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("Enter two words to query bridge words:");
                    String input = scanner.nextLine();
                    String[] words = input.split("\\s+");
                    if (words.length != 2) {
                        System.out.println("Please enter exactly two words.");
                        continue;
                    }
                    String word1 = words[0].toLowerCase();
                    String word2 = words[1].toLowerCase();
                    System.out.println(directedGraph.queryBridgeWords(word1, word2));
                    break;

                case "2":
                    System.out.println("Enter a sentence to generate new text with bridge words:");
                    input = scanner.nextLine();
                    System.out.println(directedGraph.generateNewText(input));
                    break;

                case "3":
                    System.out.println("Enter two words to calculate the shortest path:");
                    input = scanner.nextLine();
                    words = input.split("\\s+");
                    if (words.length != 2) {
                        System.out.println("Please enter exactly two words.");
                        continue;
                    }
                    word1 = words[0].toLowerCase();
                    word2 = words[1].toLowerCase();
                    System.out.println(directedGraph.calcShortestPath(word1, word2));
                    break;

                case "4":
                    System.out.println("Performing a random walk through the graph:");
                    System.out.println("Enter the file path to save the random walk output:");
                    String saveFilePath = scanner.nextLine();
                    System.out.println(directedGraph.randomWalk(saveFilePath));
                    break;

                case "5":
                    System.out.println("Exiting...");
                    System.exit(0);

                default:
                    System.out.println("Invalid choice. Please select a valid option.");
                    break;
            }
        }
    }
}
