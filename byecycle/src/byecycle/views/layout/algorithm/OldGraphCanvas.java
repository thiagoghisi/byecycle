//Copyright (C) 2004 Klaus Wuestefeld and Rodrigo B de Oliveira.
//This is free software. See the license distributed along with this file.

package byecycle.views.layout.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.widgets.Composite;
import byecycle.JavaType;
import byecycle.dependencygraph.Node;
import byecycle.views.layout.algorithm.StressMeter;
import byecycle.views.layout.algorithm.forces.DependencySpring;
import byecycle.views.layout.algorithm.forces.Force;
import byecycle.views.layout.algorithm.forces.MutualExclusion;
import byecycle.views.layout.algorithm.forces.StaticElectricity;
import byecycle.views.layout.algorithm.forces.SuperiorityComplex;


public class OldGraphCanvas<T> extends FigureCanvas {

	private static final Force STATIC_ELECTRICITY = new StaticElectricity();
	private static final Force DEPENDENCY_SPRING = new DependencySpring();
	private static final Force SUPERIORITY_COMPLEX = new SuperiorityComplex();
	private static final Force MUTUAL_EXCLUSION = new MutualExclusion();

	private final List<GraphElement> _graphElements = new ArrayList<GraphElement>();
	private List<NodeFigure<T>> _nodeFigures;
	private DependencyFigure[] _dependencyFigures;
	private final Map<IFigure, Node<T>> _nodesByIFigure = new HashMap<IFigure, Node<T>>();

	private final Random _random = new Random();
	

	private void prepareToSeekAnotherMinimum() {
		randomNodeFigure().nudgeNudge();
		_previousStress = Float.MAX_VALUE;
		_impetus = INITIAL_IMPETUS;
	}


	private boolean seekLocalStressMinimumForAWhile() {
		//long start = System.nanoTime();
		//do {
			if (seekLocalStressMinimumStep()) return true;
		//} while (System.nanoTime() - start < 1000000); //One millisecond at least.
		return false;
	}

	private boolean seekLocalStressMinimumStep() {
		_stressMeter.reset();

		for (int i = 0; i < _graphElements.size(); i++) {
	        GraphElement element1 = _graphElements.get(i);
	        
            for (int j = i + 1; j < _graphElements.size(); j++) {
            	GraphElement element2 = _graphElements.get(j);

            	STATIC_ELECTRICITY.actUpon(element1, element2);
				DEPENDENCY_SPRING.actUpon(element1, element2);
				SUPERIORITY_COMPLEX.actUpon(element1, element2);
				MUTUAL_EXCLUSION.actUpon(element1, element2);
            }
        }

		boolean stable = true;
		for (NodeFigure<T> figure : _nodeFigures) {
            if (figure.give(_impetus)) stable = false;
        }
		
		translateToOrigin();

		return stable;
	}

	private void translateToOrigin() {
		float smallestX = Float.MAX_VALUE;
		float smallestY = Float.MAX_VALUE;
		for (NodeFigure<T> figure : _nodeFigures) {
			if (figure._candidateX < smallestX) smallestX = figure._candidateX;
			if (figure._candidateY < smallestY) smallestY = figure._candidateY;
		}

		float dx = -smallestX + MARGIN_PIXELS;
		float dy = -smallestY + MARGIN_PIXELS;
		for (NodeFigure<T> figure : _nodeFigures) {
			figure.translateBy(dx, dy);
		}
	}

	private NodeFigure<T> randomNodeFigure() {
		int randomIndex = _random.nextInt(_nodeFigures.size());
		return _nodeFigures.get(randomIndex);
	}
	

	private void initGraphElements(Iterable<Node<T>> nodeGraph) {
		_nodesByIFigure.clear();
		
		Map<Node, NodeFigure> nodeFiguresByNode = new HashMap<Node, NodeFigure>();
		List<DependencyFigure> dependencyFigures = new ArrayList<DependencyFigure>();
		
		for (Node<T> node : nodeGraph) {
			NodeFigure dependentFigure = produceNodeFigureFor(node, nodeFiguresByNode);
			Iterator<Node<T>> providers = node.providers();
			while (providers.hasNext()) {
				Node<T> provider = providers.next();
				NodeFigure providerFigure = produceNodeFigureFor(provider, nodeFiguresByNode);
				dependencyFigures.add(new DependencyFigure(dependentFigure, providerFigure));
			}
		}

		_graphElements.clear();
		_graphElements.addAll(nodeFiguresByNode.values());
		_graphElements.addAll(dependencyFigures);

		_dependencyFigures = new DependencyFigure[dependencyFigures.size()];
		System.arraycopy(dependencyFigures.toArray(), 0, _dependencyFigures, 0, _dependencyFigures.length);

		_nodeFigures = new ArrayList<NodeFigure<T>>(nodeFiguresByNode.size());
		_nodeFigures.addAll(nodeFiguresByNode.values());
	}


	private NodeFigure produceNodeFigureFor(Node<T> node, Map<Node, NodeFigure> nodeFiguresByNode) {
		NodeFigure result = nodeFiguresByNode.get(node);
		if (result != null)	return result;

		result = new NodeFigure<T>(node, _stressMeter);
		nodeFiguresByNode.put(node, result);
		_nodesByIFigure .put(result.figure(), node);
		if(node.kind2()==JavaType.PACKAGE)
			result.figure().addMouseListener(_mouseListener);
		return result;
	}


}
