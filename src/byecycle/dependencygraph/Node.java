//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.dependencygraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class Node {
    
    private final static Random RANDOM = new Random();

    public static Node[] createGraph(String[] names) {
        Node[] result = new Node[names.length];
        
        Node previous = null;
        for (int i = 0; i < names.length; i++) {
            result[i] = new Node(names[i]);
        }

        produceRandomDependencies(result);
        
        return result;
    }

    private static void produceRandomDependencies(Node[] graph) {
        int dependenciesToCreate = (int)(graph.length * 1.5);
        
    	while (dependenciesToCreate-- > 0) {
    	    Node node1 = drawOneFrom(graph);
    	    Node node2 = drawOneFrom(graph);
    	    if (node1 == node2) continue;
    	    
    	    node1.addProvider(node2);
    	}
    }

    public static Node drawOneFrom(Node[] hat) {
        return hat[RANDOM.nextInt(hat.length)];
    }

    public Node(String name) {
        this(name, "class");
    }

    public Node(String name, String kind) {
        _name = name;
        _kind = kind;
    }

    private final String _name;
    private final String _kind;
    private final Set _providers = new HashSet();

    public String name() {
        return _name;
    }

    public String kind() {
        return _kind;
    }

    public Iterator providers() {
        return _providers.iterator();
    }

    public void addProvider(Node provider) {
        _providers.add(provider);
    }

    public boolean dependsOn(Node other) {
        return _providers.contains(other);
    }

    public void clearProviders() {
        _providers.clear();
    }

}
