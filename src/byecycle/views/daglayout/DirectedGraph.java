package byecycle.views.daglayout;

public class DirectedGraph {

    private final Node[] _nodes;

    static public interface NodeVisitor {

        public void visit(Node node);

    }

    public DirectedGraph(String[] nodesNames) {
        _nodes = new Node[nodesNames.length];
        for (int i = 0; i < nodesNames.length; i++) {
            _nodes[i] = new Node(nodesNames[i]);
        }
    }

    public void visitNodes(NodeVisitor visitor) {
        for (int i = 0; i < _nodes.length; i++) {
            visitor.visit(_nodes[i]);
        }
        
    }
}
