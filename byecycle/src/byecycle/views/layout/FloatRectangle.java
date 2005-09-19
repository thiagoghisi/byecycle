package byecycle.views.layout;


public class FloatRectangle {

	public float _x;  //Left
	public float _y;  //Top (NOT BOTTOM!).
	public float _width;
	public float _height;

	public float areaOfIntersection(FloatRectangle other) {
		float left = Math.max(this.left(), other.left());
		float right = Math.min(this.right(), other.right());
		float top = Math.max(this.top(), other.top());
		float bottom = Math.min(this.bottom(), other.bottom());

		if (left >= right || top >= bottom) return 0;

		return (right - left) * (bottom - top);
	}

	private float left() { return _x; }

	private float right() {	return _x + _width; }

	private float top() { return _y; }

	private float bottom() { return _y + _height; }
}
