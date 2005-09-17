package byecycle.views.layout;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.eclipse.draw2d.geometry.Point;

public class GraphLayoutMemento implements Serializable {
	
	private static final long serialVersionUID = 1L;

	static private final Random _random = new Random();

	private final Map<String, Point> _positionsByName = new HashMap<String, Point>();

	public <T> GraphLayoutMemento(List<NodeFigure<T>> nodeFigures) {
		for (NodeFigure<T> nodeFigure : nodeFigures) {
			_positionsByName.put(nodeFigure.name(), nodeFigure.targetPosition());
		}
	}

	public GraphLayoutMemento() {}

	<T> void layout(List<NodeFigure<T>> nodeFigures) {
		for (NodeFigure<T> nodeFigure : nodeFigures) {
			nodeFigure.position(producePosition(nodeFigure));
		}
    }

	private <T> Point producePosition(NodeFigure<T> nodeFigure) {
		Point result = _positionsByName.get(nodeFigure.name());
		return result == null ? randomPosition() : result;
	}

	private Point randomPosition() {
		int x = 30 + _random.nextInt(41);
		int y = 30 + _random.nextInt(41);
		return new Point(x, y);
	}

	public static GraphLayoutMemento random() {
		return new GraphLayoutMemento();
	}

}
