package byecycle.views;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.IPackageFragment;
import byecycle.views.layout.GraphLayout;

public class PackageLayoutMap {

	private final Map<IPackageFragment, GraphLayout> _contents = new HashMap<IPackageFragment, GraphLayout>();

	public GraphLayout getLayoutFor(IPackageFragment aPackage) {
		return _contents.get(aPackage);
	}
}
