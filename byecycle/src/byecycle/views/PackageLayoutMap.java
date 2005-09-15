package byecycle.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import byecycle.views.layout.GraphLayoutMemento;

public class PackageLayoutMap {

	private final WorkspaceJob _saveJob = createSaveJob();
	private Map<IPackageFragment, GraphLayoutMemento> _scheduledSaves;
	private final Object _scheduledSavesMonitor = new Object();

	public GraphLayoutMemento getLayoutFor(IPackageFragment aPackage) {
		GraphLayoutMemento result = read(aPackage);
		return result == null ? new GraphLayoutMemento() : result;
		//TODO: get from an LRU cache.
	}

	private GraphLayoutMemento read(IPackageFragment aPackage) {
		try {
			IFile file = produceFileFor(aPackage);
			if (!file.exists()) return null;
			
			InputStream contents = file.getContents();
			try {
				return (GraphLayoutMemento)new ObjectInputStream(contents).readObject();
			} finally {
				contents.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void keep(IPackageFragment aPackage, GraphLayoutMemento memento) {
		scheduleSave(aPackage, memento);
		//TODO: add to an LRU cache.
	}
	
	private void scheduleSave(IPackageFragment aPackage, GraphLayoutMemento memento) {
		synchronized (_scheduledSavesMonitor) {
			scheduledSaves().put(aPackage, memento);
		}
		_saveJob.schedule(1000 * 30);
	}

	private void save(IPackageFragment aPackage, GraphLayoutMemento memento) {
		try {
			IFile file = produceFileFor(aPackage);
			if (file.exists()) file.delete(false, null);

			ByteArrayOutputStream serialization = new ByteArrayOutputStream();
			new ObjectOutputStream(serialization).writeObject(memento); //TODO: Use readable format (properties file) instead of serialization.
			file.create(new ByteArrayInputStream(serialization.toByteArray()), false, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void performScheduledSaves() {
		Map<IPackageFragment, GraphLayoutMemento> mySaves;
		synchronized (_scheduledSavesMonitor) {
			mySaves = scheduledSaves();
			_scheduledSaves = null;
		}				

		for (IPackageFragment aPackage : mySaves.keySet())
			save(aPackage, mySaves.get(aPackage));
	}

	static private IFile produceFileFor(IPackageFragment aPackage) throws CoreException, JavaModelException {
		IProject project = aPackage.getJavaProject().getProject();
		IFolder cacheFolder = produceCacheFolder(project);

		String rootName = rootNameFor(aPackage).replaceAll("/", "__");

		String packageName = aPackage.isDefaultPackage()
			? "(default package)"
			: aPackage.getElementName();

		IFile file = cacheFolder.getFile(rootName + "__" + packageName + ".serialized"); //TODO: + "_" + System.currentTimeMillis()); //To avoid SCM conflicts.
		return file;
	}

	static private String rootNameFor(IPackageFragment aPackage) throws JavaModelException {
		IPackageFragmentRoot root = getPackageFragmentRoot(aPackage);
		if (root == null) return "";
		
		IResource correspondingResource;
		try {
			correspondingResource = root.getCorrespondingResource();
		} catch (JavaModelException ignored) {
			return "";
		}
		if (correspondingResource == null) return "";
		
		return correspondingResource.getProjectRelativePath().toString();
	}

	static private IFolder produceCacheFolder(IProject project) throws CoreException {
		IFolder byecycleFolder = project.getFolder(".byecycle");
		if (!byecycleFolder.exists()) byecycleFolder.create(false, false, null);
		
		IFolder result = byecycleFolder.getFolder("layoutcache");
		if (!result.exists()) result.create(false, false, null);
		
		return result;
	}

	/**
	 * @return a IPackageFragmentRoot representing a source folder, jar file, zip file or null if the package is directly in the root of an Eclipse project.
	 */
	static private IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
		if (element == null) return null;
		return element instanceof IPackageFragmentRoot
			? (IPackageFragmentRoot) element
			: getPackageFragmentRoot(element.getParent());
	}

	private WorkspaceJob createSaveJob() {
		WorkspaceJob job = new WorkspaceJob("Writing Byecycle layout cache") {
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				performScheduledSaves();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(WorkspaceJob.DECORATE); //Low priority.
		return job;
	}

	private Map<IPackageFragment, GraphLayoutMemento> scheduledSaves() {
		synchronized (_scheduledSavesMonitor) {
			if (_scheduledSaves == null)
				_scheduledSaves = new HashMap<IPackageFragment, GraphLayoutMemento>();
			return _scheduledSaves;
		}
	}
	
}
