package h4e.cifs4rse.model;

import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.core.subsystems.ISubSystem;

public class CIFSResource extends AbstractResource {
	
	private String name;
	private String id;
	private String deptNbr;	

	/**
	 * Default constructor for DeveloperResource.
	 */
	public CIFSResource()
	{
		super();
	}
	
	/**
	 * Constructor for DeveloperResource when given parent subsystem.
	 */
	public CIFSResource(ISubSystem parentSubSystem)
	{
		super(parentSubSystem);
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Returns the id.
	 * @return String
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Sets the id.
	 * @param id The id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Returns the deptNbr.
	 * @return String
	 */
	public String getDeptNbr()
	{
		return deptNbr;
	}

	/**
	 * Sets the deptNbr.
	 * @param deptNbr The deptNbr to set
	 */
	public void setDeptNbr(String deptNbr)
	{
		this.deptNbr = deptNbr;
	}

}
