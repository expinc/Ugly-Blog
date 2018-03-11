package net.uglyblog.db;

import java.sql.*;
import java.util.Stack;


public class DbConnection
{
	private Connection jdbcConnection = null;
	private Stack<SavepointAdaptor> savepoints = null;
	
	
	public DbConnection() throws SQLException
	{
		jdbcConnection = Database.getConnection();
		savepoints = new Stack<SavepointAdaptor>();
	}
	
	
	public PreparedStatement prepareRetrieveStatement(String sql) throws SQLException
	{
		if (null != jdbcConnection)
			return jdbcConnection.prepareStatement(
					sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
		else
			return null;
	}
	
	
	public PreparedStatement prepareInsertStatement(String sql) throws SQLException
	{
		if (null != jdbcConnection)
			return jdbcConnection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
		else
			return null;
	}
	
	
	public PreparedStatement prepareUpdateStatement(String sql) throws SQLException
	{
		if (null != jdbcConnection)
			return jdbcConnection.prepareStatement(sql);
		else
			return null;
	}
	
	
	public PreparedStatement prepareDeleteStatement(String sql) throws SQLException
	{
		if (null != jdbcConnection)
			return jdbcConnection.prepareStatement(sql);
		else
			return null;
	}
	
	
	public void close() throws SQLException
	{
		if (null != jdbcConnection)
		{
			jdbcConnection.close();
			jdbcConnection = null;
		}
	}
	
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	
	public void startTransaction(int isolationLevel) throws SQLException
	{
		if (null == jdbcConnection)
			throw new SQLException("No connection!");
		else
		{
			if (savepoints.empty())
				jdbcConnection.setAutoCommit(false);
			setIsolationLevel(isolationLevel);
			SavepointAdaptor savepoint = new SavepointAdaptor(jdbcConnection.setSavepoint());
			savepoints.push(savepoint);
		}
	}
	
	
	public void endTransaction() throws SQLException
	{
		if (null == jdbcConnection || savepoints.empty())
			throw new SQLException("No connection for commit!");
		
		SavepointAdaptor savepoint = savepoints.pop();
		boolean toCommit = savepoint.getToCommit();
		if (toCommit)
		{
			if (savepoints.empty())
			{
				jdbcConnection.commit();
				jdbcConnection.setAutoCommit(true);
			}
		}
		else
		{
			jdbcConnection.rollback(savepoint.getAdaptee());
			if (savepoints.empty())
			{
				jdbcConnection.rollback();
				jdbcConnection.setAutoCommit(true);
			}
		}
	}
	
	
	public void makeCommit(boolean value)
	{
		SavepointAdaptor savepoint = savepoints.peek();
		savepoint.setToCommit(value);
	}
	
	
	private void setIsolationLevel(int value) throws SQLException
	{
		if (
			Connection.TRANSACTION_READ_UNCOMMITTED != value &&
			Connection.TRANSACTION_READ_COMMITTED != value &&
			Connection.TRANSACTION_REPEATABLE_READ != value &&
			Connection.TRANSACTION_SERIALIZABLE != value)
			value = Connection.TRANSACTION_READ_COMMITTED;
		
		int currentLevel = jdbcConnection.getTransactionIsolation();
		if (value > currentLevel)
			jdbcConnection.setTransactionIsolation(value);
	}
}
