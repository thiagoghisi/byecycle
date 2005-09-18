package byecycle.views.layout;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import byecycle.views.layout.algorithm.Coordinates;
import byecycle.views.layout.algorithm.GraphLayoutMemento;

public class GraphMorpher {

	private static class NodeMorpher {

		NodeMorpher(NodeFigure<?> figure, Coordinates target) {
			// TODO Auto-generated constructor stub
		}

		void morphingStep() {
			// TODO Auto-generated method stub
		}

		public boolean onTarget() {
			// TODO Auto-generated method stub
			return false;
		}
	}

	private final List<NodeMorpher> _nodeMorphers = new LinkedList<NodeMorpher>();

	public <T> GraphMorpher(Collection<NodeFigure<T>> nodes, GraphLayoutMemento targets) {
		for (NodeFigure<T> node : nodes)
			addNodeMorpherFor(node, targets);
	}

	private void addNodeMorpherFor(NodeFigure<?> node, GraphLayoutMemento targets) {
		Coordinates myTarget = targets.getCoordinatesFor(node.name());
		_nodeMorphers.add(new NodeMorpher(node, myTarget));
	}

	void morphingStep() {
		Iterator<NodeMorpher> it = _nodeMorphers.iterator();
		while (it.hasNext()) {
			NodeMorpher morpher = it.next();
			morpher.morphingStep();
	 		if (morpher.onTarget()) it.remove();
		}
	}

	public boolean done() {
		return _nodeMorphers.isEmpty();
	}

}
