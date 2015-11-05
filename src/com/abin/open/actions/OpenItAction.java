package com.abin.open.actions;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.abin.open.util.OperatingSystem;

public class OpenItAction implements IObjectActionDelegate {

	protected IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

	protected Shell shell;

	protected ISelection currentSelection;

	protected String systemBrowser;

	public OpenItAction() {
		this.systemBrowser = OperatingSystem.INSTANCE.getSystemBrowser();
	}

	@Override
	public void run(IAction arg0) {
		if (this.currentSelection == null || this.currentSelection.isEmpty()) {
			return;
		}

		if (this.currentSelection instanceof ITreeSelection) {
			ITreeSelection treeSelection = (ITreeSelection) this.currentSelection;

			TreePath[] paths = treeSelection.getPaths();

			for (int i = 0; i < paths.length; i++) {
				TreePath path = paths[i];
				Object segment = path.getLastSegment();

				IResource resource = null;

				if ((segment instanceof IResource))
					resource = (IResource) segment;
				else if ((segment instanceof IJavaElement)) {
					resource = ((IJavaElement) segment).getResource();
				}
				if (resource == null) {
					continue;
				}
				String browser = this.systemBrowser;
				String location = resource.getLocation().toOSString();
				if ((resource instanceof IFile)) {
					location = ((IFile) resource).getParent().getLocation().toOSString();
					if (OperatingSystem.INSTANCE.isWindows()) {
						browser = this.systemBrowser + " /select,";
						location = ((IFile) resource).getLocation().toOSString();
					}
				}
				gotoHere(browser, location);
			}
		} else if (this.currentSelection instanceof ITextSelection || this.currentSelection instanceof IStructuredSelection) {
			// open current editing file
			IEditorPart editor = window.getActivePage().getActiveEditor();
			if (editor != null) {
				IFile current_editing_file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
				String browser = this.systemBrowser;
				String location = current_editing_file.getParent().getLocation().toOSString();
				if (OperatingSystem.INSTANCE.isWindows()) {
					browser = this.systemBrowser + " /select,";
					location = current_editing_file.getLocation().toOSString();
				}
				gotoHere(browser, location);
			}
		}
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		this.currentSelection = arg1;
	}

	@Override
	public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
		this.window = arg1.getSite().getWorkbenchWindow();
        this.shell = arg1.getSite().getShell();
	}

	protected void gotoHere(String browser, String location) {
		try {
			if (OperatingSystem.INSTANCE.isWindows()) {
				Runtime.getRuntime().exec(browser + " \"" + location + "\"");
			} else {
				Runtime.getRuntime().exec(new String[] { browser, location });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
