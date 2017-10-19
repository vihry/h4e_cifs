package h4e.cifs4rse.actions;

import java.util.regex.Pattern;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
//import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import h4e.cifs4rse.model.CIFSResource;

/**
 * 
 * 
 * http://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.rse.doc.isv%2Fguide%2Ftutorials.html
 * 
 *
 */
public class CopyAction implements IObjectActionDelegate {

	private Shell shell;
	private IStructuredSelection selection;
	
	public CopyAction() {
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	@Override
	public void run(IAction action) {
		//IRemoteFile f = (IRemoteFile) selection.getFirstElement();
		CIFSResource f = (CIFSResource) selection.getFirstElement();
		String source_file_name = f.getName();
		// Backup files should end with ".yyyyMMddHHmm".
		boolean is_backup_file = Pattern.matches(".+\\.\\d{12}", source_file_name);
		if (is_backup_file) {
			String target_file_name = source_file_name.substring(0, source_file_name.length() - 13);
			if (MessageDialog.openConfirm(shell, "Restore from Backup", String.format("Restore '%s' from '%s'?", target_file_name, source_file_name))) {
				//try {
					// Restore from the backup file.
					//f.getParentRemoteFileSubSystem().copy(f, f.getParentRemoteFile(), target_file_name, null);
					// Refresh the parent folder, keep to select the backup file.
					//RSECorePlugin.getTheSystemRegistry().fireEvent(new SystemResourceChangeEvent(f.getParentRemoteFile(), ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, f));
				//} catch (SystemMessageException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				//}
				System.out.println( "do nothing" );
			}
		}
		else {
			MessageDialog.openError(shell, "Restore from Backup", "Backup files should end with '.yyyyMMddHHmm'.");
		}
	}
	
//	@Override
//	public void run(IAction action) {
//		IRemoteFile f = (IRemoteFile) selection.getFirstElement();
//		// Prompt for the file to open.
//		String working_directory = (f.isDirectory() ? f.getAbsolutePath() : f.getParentPath());
//		InputDialog input = new InputDialog(shell, "Open by Path", "Absolute/Relative Path:", working_directory, null);
//		if (input.open() == InputDialog.OK && input.getValue().trim().length() > 0) {
//			String path = input.getValue().trim();
//			// Relative path?
//			// TODO Improve the logic for checking whether a path is absolute or relative.
//			if (!(path.startsWith("/") || (path.length() >= 2 && path.charAt(1) == ':'))) {
//				path = working_directory + f.getSeparator() + path;
//			}
//			try {
//				// Try to open the file.
//				new SystemEditableRemoteFile(f.getParentRemoteFileSubSystem().getRemoteFileObject(path, null)).open(shell);
//			} catch (SystemMessageException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
//	@Override
//	public void run(IAction action) {
//		// Get the absolute paths of the selected files.
//		StringBuilder sb = new StringBuilder();
//		for (Object o: selection.toArray()) {
//			if (o instanceof IRemoteFile) {
//				if (sb.length() > 0) sb.append('\n');
//				sb.append(((IRemoteFile)o).getAbsolutePath());
//			}
//		}
//		// Save the paths into the clipboard.
//		RSEUIPlugin.getTheSystemRegistryUI().getSystemClipboard().setContents(
//			new Object[] { sb.toString() },
//			new Transfer[] { TextTransfer.getInstance() }
//		);
//	}
	
//	@Override
//	public void run(IAction action) {
//		IRemoteFile f = (IRemoteFile) selection.getFirstElement();
//		try {
//			// Backup the first selected file with the timestamp appended.
//			f.getParentRemoteFileSubSystem().copy(f, f.getParentRemoteFile(), f.getName() + "." + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()), null);
//			// Refresh the parent folder, keep to select the selected file.
//			RSECorePlugin.getTheSystemRegistry().fireEvent(new SystemResourceChangeEvent(f.getParentRemoteFile(), ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, f));
//		} catch (SystemMessageException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = (IStructuredSelection) selection;
	}
	
	protected Shell getShell() {
		return SystemBasePlugin.getActiveWorkbenchShell();
	}

	protected ISubSystem getSubSystem() {
		return null;//getFirstSelectedRemoteFile().getParentRemoteFileSubSystem();
	}

}
