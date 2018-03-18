package de.jcm.home3d.task;

import java.util.ArrayList;

import de.jcm.home3d.Home3D;
import de.jcm.home3d.packet.out.PacketOutStatusUpdate;

public abstract class Task extends Thread
{
	private int id;
	private String name;
	private String argument;
	private String status;
	private int code;
	private boolean active;
	
	private boolean changed=false;
	
	/**
	 * @param id
	 * @param name
	 * @param argmuent
	 */
	public Task(int id, String name, String argument)
	{
		super(name);
		this.id = id;
		this.name = name;
		this.argument = argument;
	}
	
	/**
	 * @return the id
	 */
	public int getTaskId()
	{
		return id;
	}
	
	/**
	 * Does NOT set <code>changed</codes>
	 * @param id
	 *            the id to set
	 */
	public void setTaskId(int id)
	{
		this.id = id;
	}
	
	/**
	 * @return the name
	 */
	public String getTaskName()
	{
		return name;
	}
	
	/**
	 * Does NOT set <code>changed</codes>
	 * @param name
	 *            the name to set
	 */
	public void setTaskName(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}
	
	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status)
	{
		if(!status.equals(this.status))
			this.changed=true;
		this.status = status;
	}
	
	/**
	 * @return the code
	 */
	public int getCode()
	{
		return code;
	}
	
	/**
	 * @param done
	 *            the code to set
	 */
	public void setCode(int code)
	{
		if(this.code != code)
			this.changed=true;
		this.code = code;
		if(this.code==2)
			this.active=true;
	}
	
	/**
	 * <b>Do NOT call</b><br>
	 * Sets <code>code</codes> to <code>2</code>
	 */
	public void run()
	{
		setCode(2);
	}
	
	/**
	 * @return the active
	 */
	public boolean isActive()
	{
		return active;
	}
	
	/**
	 * Does NOT set <code>changed</codes>
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}

	/**
	 * @return the argument
	 */
	public String getArgument()
	{
		return argument;
	}

	/**
	 * Does NOT set <code>changed</codes>
	 * @param argument the argument to set
	 */
	public void setArgument(String argument)
	{
		this.argument = argument;
	}	
	
	public void update()
	{
		changed=false;
		
		//Debug
		//System.out.println(status);
		
		ArrayList<Task> list=new ArrayList<>();
		list.add(this);
		Home3D.sendPacket(new PacketOutStatusUpdate(list));
	}

	/**
	 * @return whether some values have changed
	 */
	public boolean hasChanged()
	{
		return changed;
	}
	
	public void setChanged(boolean changed)
	{
		this.changed=changed;
	}
}
