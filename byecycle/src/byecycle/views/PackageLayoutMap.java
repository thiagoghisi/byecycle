package byecycle.views;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.IPackageFragment;
import byecycle.views.layout.GraphLayoutMemento;

public class PackageLayoutMap {

	private final Map<IPackageFragment, GraphLayoutMemento> _contents = new HashMap<IPackageFragment, GraphLayoutMemento>();

	public GraphLayoutMemento getLayoutFor(IPackageFragment aPackage) {
		GraphLayoutMemento result = _contents.get(aPackage);
		return result == null ? new GraphLayoutMemento() : result;
	}

	public void keep(IPackageFragment aPackage, GraphLayoutMemento memento) {
		_contents.put(aPackage, memento);
	}
}
