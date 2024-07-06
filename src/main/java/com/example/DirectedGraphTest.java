package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.DirectedGraph;

public class DirectedGraphTest {

    private DirectedGraph graph;

    @BeforeEach
    public void setUp() {
        graph = new DirectedGraph();
        graph.buildGraphFromFile("/Volumes/[C] Windows 11/Users/osheenconstable/directed-graph/directed-graph/text1.txt");
    }

    @Test
    public void testQueryBridgeWordsValid() {
        assertEquals("The bridge words from build to castles are: grand", graph.queryBridgeWords("build", "castles"));
    }

    @Test
    public void testQueryBridgeWordsInvalid() {
        assertEquals("No to or nonexistent in the graph!", graph.queryBridgeWords("to", "nonexistent"));
    }

    @Test
    public void testQueryBridgeWordsNoBridge() {
        assertEquals("No bridge words from walls to and!", graph.queryBridgeWords("walls", "and"));
    }
}

