package byecycle.views.layout;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CartesianLayout implements Serializable {
	
	private static final long serialVersionUID = 1L;

	static private final Random _random = new Random();

	private final Map<String, Coordinates> _coordinatesByName = new HashMap<String, Coordinates>();

	public void keep(String name, Coordinates coordinates) {
		_coordinatesByName.put(name, coordinates);
	}

	public Coordinates coordinatesFor(String name) {
		Coordinates result = _coordinatesByName.get(name);
		return result == null ? randomCoordinates() : result;
	}
	
	private Coordinates randomCoordinates() {
		float x = _random.nextFloat() * 600;
		float y = _random.nextFloat() * 600;
		return new Coordinates(x, y);
	}

	public static CartesianLayout random() {
		return new CartesianLayout();
	}

	public Iterable<String> nodeNames() {
		return _coordinatesByName.keySet();
	}


}
