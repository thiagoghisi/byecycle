package byecycle;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;

import byecycle.views.daglayout.GraphNode;

public class PackageDependencyAnalysis {

	private final Map _nodes = new HashMap();
	
	private final GraphNode _root;

	public PackageDependencyAnalysis(IPackageFragment pkg, IProgressMonitor monitor) throws JavaModelException {
		
		_root = getNode(pkg.getElementName());

		ASTParser parser = ASTParser.newParser(AST.JLS2);

		DependencyVisitor visitor = new DependencyVisitor();
		ICompilationUnit[] units = pkg.getCompilationUnits();
		for (int i = 0; i < units.length; i++) {
			ICompilationUnit each = units[i];
			parser.setResolveBindings(true);
			parser.setSource(each);
			
			CompilationUnit node = (CompilationUnit) parser.createAST(monitor); 
			node.accept(visitor);
		}
	}
	
	public GraphNode root() {
		return _root;
	}
	
	public Map nodes() {
		return _nodes;
	}

	private GraphNode getNode(String packageName) {
		GraphNode node = (GraphNode)_nodes.get(packageName);
		if (null == node) {
			node = new GraphNode(packageName);
			_nodes.put(packageName, node);
		}
		return node;
	}

	class DependencyVisitor extends ASTVisitor {
		
		public boolean visit(SimpleType node) {
			addProvider(node.resolveBinding());
			return true;
		}

		public boolean visit(Expression node) {
			addProvider(node.resolveTypeBinding());
			return true;
		}

		public boolean visit(MethodInvocation node) {
			addProvider(node.resolveTypeBinding());
			return true;
		}

		public boolean visit(ClassInstanceCreation node) {
			addProvider(node.resolveTypeBinding());
			return true;
		}

		private void addProvider(ITypeBinding type) {
			if (null == type)
				return;
			if (type.isArray())
				type = type.getElementType();
			if (type.isPrimitive())
				return;
			if (type.getQualifiedName().equals(""))
				return; //TODO: Check why this happens.
			_root.addProvider(getNode(type.getPackage().getName()));
		}

	}
}