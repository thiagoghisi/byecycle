package byecycle.views.layout.algorithm;

class Relaxer {

	private float _impetus = Constants.INITIAL_IMPETUS ;

	private final StressMeter _stressMeter;
	private float _stressLocalMinimum = Float.MAX_VALUE;
	
	Relaxer(StressMeter stressMeter) {
		_stressMeter = stressMeter;
	}
	
	boolean hasConverged() {
		return _impetus < Constants.MINIMUM_IMPETUS;
	}

	void step() {
		giveToForces(); //Forces were applied in previous step.

		_stressMeter.reset();
		applyForces();
		
		if (_stressMeter._reading >= _stressLocalMinimum) {
			calmDownToConverge();
		} else {
			_stressLocalMinimum = _stressMeter._reading;
		}
		
	}
	
	private void calmDownToConverge() {
		_impetus *= 0.9;
	}
	
}
