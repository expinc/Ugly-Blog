package net.uglyblog.session;

import java.sql.SQLException;

import net.uglyblog.db.DbConnection;


public class RequestSession
{
	private DbConnection dbConnection = null;
	
	
	public DbConnection getDbConnection() throws SQLException
	{
		if (null == dbConnection)
			dbConnection = new DbConnection();
		return dbConnection;
	}
	
	
	public void dispose() throws SQLException
	{
		if (null != dbConnection)
		{
			dbConnection.close();
			dbConnection = null;
		}
	}
	
	
	protected void finalize() throws Throwable
	{
		dispose();
	}
	
	
	public void startTransaction(int isolationLevel) throws SQLException
	{
		getDbConnection().startTransaction(isolationLevel);
	}
	
	
	public void endTransaction() throws SQLException
	{
		getDbConnection().endTransaction();
	}
	
	
	public void makeCommit(boolean value) throws SQLException
	{
		getDbConnection().makeCommit(value);
	}
}
