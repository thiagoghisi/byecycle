package byecycle.views.daglayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * @author rodrigob
 */
public class GraphCanvas extends Canvas {

	private GraphNode[] _graph;
	
	private IFigure _graphFigure;

	private final XYLayout _contentsLayout = new XYLayout();

	private final Map _nodeFiguresByNode = new HashMap();
	
	private final LightweightSystem _lws;
	
	private final Random _random = new Random();

	public GraphCanvas(Composite parent) {
		super(parent, SWT.FILL | SWT.NO_BACKGROUND);
		_lws = new LightweightSystem(this);
	}

	public void setGraph(GraphNode[] graph) {
		_graph = graph;
		
		initGraphFigure();	
		improveLayout();
	}
	

	public void improveLayout() {
		NodeFigure figure1 = randomNodeFigure();
		NodeFigure figure2 = randomNodeFigure();
		if (figure1 == figure2)
			return;
		move(figure1, figure2);

		makeInvertedDependenciesRed();
		_graphFigure.repaint();
	}

	private NodeFigure randomNodeFigure() {
		GraphNode node = _graph[_random.nextInt(_graph.length)];
		return produceNodeFigureFor(node);
	}

	private void move(NodeFigure figure1, NodeFigure figure2) {
		if (figure1.node().dependsOn(figure2.node())
				|| figure2.node().dependsOn(figure1.node()))
			attract(figure1, figure2);
		else
			repel(figure1, figure2);
		_graphFigure.setLayoutManager(_contentsLayout);
		_graphFigure.revalidate();
	}

	private void repel(NodeFigure figure1, NodeFigure figure2) {
		Point location1 = figure1.getBounds().getLocation();
		Point location2 = figure2.getBounds().getLocation();

		Dimension xyDifference = location1.getDifference(location2);

		double force = 1 / Math.min(location1.getDistance2(location2), 0.001);

		// TODO Auto-generated method stub

		_contentsLayout.setConstraint(figure1, new Rectangle(_random
				.nextInt(300), _random.nextInt(300), -1, -1));
	}

	private void attract(NodeFigure figure1, NodeFigure figure2) {
		// TODO Auto-generated method stub
	}

	private void makeInvertedDependenciesRed() {
		Iterator children = _graphFigure.getChildren().iterator();
		while (children.hasNext()) {
			IFigure child = (IFigure) children.next();

			if (child instanceof PolylineConnection) {
				PolylineConnection dependency = (PolylineConnection) child;
				dependency
						.setForegroundColor(dependency.getStart().y > dependency
								.getEnd().y ? ColorConstants.red
								: ColorConstants.black);
			}
		}
		_graphFigure.revalidate();
	}
	
	private void initGraphFigure() {
		_graphFigure = new Figure();
		
		for (int i = 0; i < _graph.length; i++) {
			GraphNode node = _graph[i];
			IFigure dependentFigure = produceNodeFigureFor(node);
			Iterator providers = node.providers();
			while (providers.hasNext()) {
				IFigure providerFigure = produceNodeFigureFor((GraphNode) providers
						.next());
				addDependencyFigure(dependentFigure, providerFigure);
			}
		}

		_lws.setContents(_graphFigure);
	}

	private void addDependencyFigure(IFigure dependentFigure,
			IFigure providerFigure) {
		PolylineConnection dependency = new PolylineConnection();
		dependency.setSourceAnchor(new ChopboxAnchor(dependentFigure));
		dependency.setTargetAnchor(new ChopboxAnchor(providerFigure));

		PolygonDecoration arrowHead = new PolygonDecoration();
		PointList decorationPointList = new PointList();
		decorationPointList.addPoint(0, 0);
		decorationPointList.addPoint(-1, 1);
		decorationPointList.addPoint(-1, 0);
		decorationPointList.addPoint(-1, -1);
		arrowHead.setTemplate(decorationPointList);
		dependency.setTargetDecoration(arrowHead);

		_graphFigure.add(dependency);
	}

	private NodeFigure produceNodeFigureFor(GraphNode node) {
		NodeFigure result = (NodeFigure) _nodeFiguresByNode.get(node);
		if (result != null)
			return result;

		result = new NodeFigure(node);
		_nodeFiguresByNode.put(node, result);
		_graphFigure.add(result);
		return result;
	}


}
