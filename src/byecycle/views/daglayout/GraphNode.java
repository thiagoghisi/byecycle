package byecycle.views.daglayout;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GraphNode {

    public static GraphNode[] create(String[] names) {
        GraphNode[] result = new GraphNode[names.length];
        
        GraphNode previous = null;
        for (int i = 0; i < names.length; i++) {
            GraphNode current = new GraphNode(names[i]); 
            result[i] = current;
            if (previous != null) previous.addProvider(current);
            previous = current;
        }
        
        return result;
    }

    public GraphNode(String name) {
        _name = name;
    }

    private final String _name;
    private final Set _providers = new HashSet();

    public String name() {
        return _name;
    }

    public Iterator providers() {
        return _providers.iterator();
    }

    public void addProvider(GraphNode provider) {
        _providers.add(provider);
    }

    public boolean dependsOn(GraphNode other) {
        return _providers.contains(other);
    }


}
