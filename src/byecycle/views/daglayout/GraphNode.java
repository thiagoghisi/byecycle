package byecycle.views.daglayout;

public class GraphNode {

    public static GraphNode[] create(String[] names) {
        GraphNode[] result = new GraphNode[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = new GraphNode(names[i]);
        }
        return result;
    }

    private final String _name;

    public GraphNode(String name) {
        _name = name;
    }

    public String name() {
        return _name;
    }


}
