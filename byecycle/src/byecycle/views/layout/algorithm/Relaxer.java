package byecycle.views.layout.algorithm;

import java.util.ArrayList;
import java.util.List;
import byecycle.views.layout.algorithm.forces.DependencySpring;
import byecycle.views.layout.algorithm.forces.Force;
import byecycle.views.layout.algorithm.forces.Gravity;
import byecycle.views.layout.algorithm.forces.MutualExclusion;
import byecycle.views.layout.algorithm.forces.StaticElectricity;
import byecycle.views.layout.algorithm.forces.SuperiorityComplex;

class Relaxer {

	private static final Force STATIC_ELECTRICITY = new StaticElectricity();
	private static final Force GRAVITY = new Gravity();
	private static final Force DEPENDENCY_SPRING = new DependencySpring();
	private static final Force SUPERIORITY_COMPLEX = new SuperiorityComplex();
	private static final Force MUTUAL_EXCLUSION = new MutualExclusion();
	
	private final StressMeter _stressMeter;

	private final List<GraphElement> _graphElements;
	private final List<NodeElement> _nodeElements;

	private boolean _firstTime = true;
	private boolean _hasConverged = false;
	
	Relaxer(List<NodeElement> nodeElements, List<DependencyElement> dependencyElements, StressMeter stressMeter) {
		_graphElements = new ArrayList<GraphElement>();
		_graphElements.addAll(nodeElements);
		_graphElements.addAll(dependencyElements);
		
		_nodeElements = nodeElements;
		
		_stressMeter = stressMeter;
	}
	
	boolean hasConverged() {
		return _hasConverged;
	}

	void step() {
		giveToForces(); //This call will actually make the graph elements move, responding to the forces applied in the previous step.

		_stressMeter.reset();
		measureForces();
	}
	
	private void measureForces() {
		for (int i = 0; i < _graphElements.size(); i++) {
		    GraphElement element1 = _graphElements.get(i);
		    
		    for (int j = i + 1; j < _graphElements.size(); j++) {
		    	GraphElement element2 = _graphElements.get(j);
		
		    	STATIC_ELECTRICITY.applyTo(element1, element2);
		    	GRAVITY.applyTo(element1, element2);
				DEPENDENCY_SPRING.applyTo(element1, element2);
				SUPERIORITY_COMPLEX.applyTo(element1, element2);
				//MUTUAL_EXCLUSION.applyTo(element1, element2);
		    }
		}
	}

	/** "Give: To yield to physical force." Dictionary.com */
	private void giveToForces() {
		float greatestForce = 0;
		for (NodeElement node : _nodeElements)
			if(node.pendingForceMagnitude() > greatestForce) greatestForce = node.pendingForceMagnitude();

		if (_firstTime) {
			_firstTime = false;
			return;
		}

		_hasConverged = greatestForce < 0.00003;
		if (_hasConverged) return;
		
		float timeFrame = 0.3f / greatestForce;
		if (timeFrame > 100) timeFrame = 100; 
		System.out.println(timeFrame);
		for (NodeElement node : _nodeElements)
			node.give(timeFrame);
		
	}
	
}
