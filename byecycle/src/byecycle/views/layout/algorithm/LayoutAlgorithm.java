package byecycle.views.layout.algorithm;

import java.util.Collection;
import byecycle.dependencygraph.Node;

public class LayoutAlgorithm {

	private final StressMeter _stressMeter = new StressMeter();
	private float _lowestStressEver;
	
	private Relaxer _relaxer;

	public LayoutAlgorithm(Collection<Node<?>> graph, CartesianLayout initialLayout, NodeSizeProvider sizeProvider) {
		measureInitialStress();
	}

	private void measureInitialStress() {
		relaxer().step();
		_lowestStressEver = _stressMeter._reading;
	}

	private Relaxer relaxer() {
		if (_relaxer == null) _relaxer = new Relaxer(_stressMeter);
		if (_relaxer.hasConverged()) {
			prepareToSeekNonLocalMinimum();
			_relaxer = new Relaxer(_stressMeter);
		}
		return _relaxer;
	}

	private void prepareToSeekNonLocalMinimum() {
		// TODO Auto-generated method stub
		
	}

	public boolean improveLayoutStep() {
		relaxer().step();
		
		if (_stressMeter._reading < _lowestStressEver) {
			_lowestStressEver = _stressMeter._reading;
			return true;
		}
		return false;
	}

	public CartesianLayout layoutMemento() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
