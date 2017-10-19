package h4e.cifs4rse.model;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.dstore.internal.extra.IPropertySource;
import org.eclipse.rse.ui.view.AbstractSystemRemoteAdapterFactory;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;

import h4e.cifs4rse.model.CIFSResource;
import h4e.cifs4rse.model.CIFSResourceAdapter;

public class CIFSResourceAdapterFactory extends AbstractSystemRemoteAdapterFactory implements IAdapterFactory {

	private CIFSResourceAdapter _adapter = new CIFSResourceAdapter();
	
	/**
	 * Constructor for CIFSResourceAdapterFactory.
	 */
	public CIFSResourceAdapterFactory() {
		super();
	}

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(Object, Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType)
	{
		ISystemViewElementAdapter adapter = null;
		if (adaptableObject instanceof CIFSResource) {
		  adapter = (ISystemViewElementAdapter) _adapter;
		}

		// these lines are very important! 
		if ((adapter != null) && (adapterType == IPropertySource.class)) {
		  adapter.setPropertySourceInput(adaptableObject);
		}
		return adapter;
	}

}
