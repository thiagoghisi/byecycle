// Copyright (C) 2005 Klaus Wuestefeld and Rodrigo B. de Oliveira.
// This is free software. See the license distributed along with this file.

package byecycle.views.layout.algorithm;


class StressMeter {

	float _reading;

	void addStress(float stress) {
		_reading += stress;
	}
	
	void reset() {
		_reading = 0;
	}
	
}
