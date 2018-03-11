package net.uglyblog.db;

import java.sql.Savepoint;


public class SavepointAdaptor
{
	private Savepoint savepoint;
	private boolean toCommit;
	
	
	SavepointAdaptor(Savepoint savepoint)
	{
		this.savepoint = savepoint;
		toCommit = false;
	}
	
	
	void setToCommit(boolean value)
	{
		toCommit = value;
	}
	
	
	boolean getToCommit()
	{
		return toCommit;
	}
	
	
	Savepoint getAdaptee()
	{
		return savepoint;
	}
}
