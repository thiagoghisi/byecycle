package byecycle.views.layout;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.draw2d.geometry.Point;
import byecycle.views.layout.algorithm.Coordinates;
import byecycle.views.layout.algorithm.GraphLayoutMemento;

public class GraphMorpher {

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

	
	private static class NodeMorpher {

		private static final int MAX_ANIMATION_STEP_PIXELS = 3;
		private final NodeFigure<?> _figure;
		private final int _targetX;
		private final int _targetY;

		NodeMorpher(NodeFigure<?> figure, Coordinates target) {
			_figure = figure;
			_targetX = Math.round(target._x);
			_targetY = Math.round(target._y);
		}

		void morphingStep() {
			int newX = currentX();
			int newY = currentY();
			
			int step = MAX_ANIMATION_STEP_PIXELS;
			int dX = Math.max(Math.min(_targetX - newX, step), -step);
			int dY = Math.max(Math.min(_targetY - newY, step), -step);
			
			newX += dX;
			newY += dY;
			
			_figure.position(new Point(newX, newY));
		}

		private int currentX() { return _figure.position().x; }
		private int currentY() { return _figure.position().y; }

		public boolean onTarget() {
			return currentX() == _targetX && currentY() == _targetY;
		}
	}

	
}
