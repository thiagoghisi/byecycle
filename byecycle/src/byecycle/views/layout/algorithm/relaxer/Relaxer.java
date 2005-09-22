package byecycle.views.layout.algorithm.relaxer;

import java.util.ArrayList;
import java.util.List;

import byecycle.views.layout.criteria.DependencyElement;
import byecycle.views.layout.criteria.GraphElement;
import byecycle.views.layout.criteria.StressMeter;


public class Relaxer {

	private final List<GraphElement> _graphElements;
	private final List<RelaxerNode> _relaxerNodes;

	private float _previousStress;
	private final StressMeter _stressMeter;

	private float _impetus = Constants.INITIAL_IMPETUS;


	@SuppressWarnings("unchecked")
	public Relaxer(List<?> nodeElements, List<DependencyElement> dependencyElements, StressMeter stressMeter) {
		_relaxerNodes = (List<RelaxerNode>)nodeElements;

		_graphElements = new ArrayList<GraphElement>();
		_graphElements.addAll(_relaxerNodes);
		_graphElements.addAll(dependencyElements);

		_stressMeter = stressMeter;
		_previousStress = applyForces();
	}

	public boolean hasConverged() {
		return _impetus < Constants.MINIMUM_IMPETUS;
	}

	public void step() {
		_impetus = tryToMove() ? (float)(_impetus * 1.1) : (float)(_impetus / 2);

		System.out.println(_impetus);
	}

	private boolean tryToMove() {
		checkpoint();

		give();

		float stress = applyForces();
		if (stress > _previousStress) {
			rollback();
			return false;
		}

		_previousStress = stress;
		return true;
	}

	float applyForces() {
		return _stressMeter.applyForcesTo(_relaxerNodes, _graphElements);
	}

	private void checkpoint() {
		for (RelaxerNode node : _relaxerNodes)
			node.checkpoint();
	}

	private void give() {
		for (RelaxerNode candidate : _relaxerNodes)
			candidate.give(_impetus);
	}

	private void rollback() {
		for (RelaxerNode node : _relaxerNodes)
			node.rollback();
	}

}
