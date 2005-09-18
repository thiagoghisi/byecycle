package byecycle.views.layout.algorithm;

public class Coordinates {
	
	public final float _x;
	public final float _y;

	public Coordinates(float x, float y) {
		_x = x;
		_y = y;
	}

	public float getDistance(Coordinates other) {
		return (float)Math.hypot(_x - other._x, _y - other._y);
	}
}
