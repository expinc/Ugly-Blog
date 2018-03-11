package net.uglyblog.bo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import net.uglyblog.dao.DataTable;
import net.uglyblog.db.Database;
import net.uglyblog.db.DbConnection;
import net.uglyblog.session.RequestSession;


public abstract class BusinessObject
{	
	protected Map<String, DataTable> dataTables = null;
	protected RequestSession requestSession = null;
	
	
	public BusinessObject(RequestSession requestSession)
	{
		dataTables = new TreeMap<String, DataTable>();
		this.requestSession = requestSession;
	}
	
	
	public DataTable getDataTable(String tableName)
	{
		return dataTables.get(tableName);
	}
	
	
	public void setDataTable(String tableName, DataTable dataTable)
	{
		dataTables.put(tableName, dataTable);
	}
	
	
	public boolean getById(int id) throws SQLException
	{
		return false;
	}
	
	
	public boolean isValid(boolean forUpdate) throws SQLException
	{
		return false;
	}
	
	
	public int create(boolean doAutoComplete) throws SQLException
	{
		return -1;
	}
	
	
	public boolean update(boolean doAutoComplete) throws SQLException
	{
		return false;
	}
	
		
	public boolean delete() throws SQLException
	{
		return false;
	}
	
	
	public boolean autoComplete() throws SQLException
	{
		return false;
	}
	
	
	protected void clear()
	{
		for (Map.Entry<String, DataTable> entry : dataTables.entrySet())
		{
			entry.getValue().clear();
		}
	}
	
	
	public List<BusinessObject> getAll(RequestSession requestSession) throws SQLException
	{
		return new LinkedList<BusinessObject>();
	}
	
	
	public int getId()
	{
		DataTable tableMain = getDataTable(getMainTableName());
		if (1 == tableMain.getSize())
			return (Integer) tableMain.getData("Id");
		else
			return 0;
	}
	
	
	protected List<BusinessObject> getAllInternal(RequestSession requestSession) throws SQLException
	{
		List<BusinessObject> result = new LinkedList<BusinessObject>();
		String statementStr = new String("select Id from " + getMainTableName());
		
		requestSession.startTransaction(Connection.TRANSACTION_READ_COMMITTED);
		try
		{
			DbConnection connection = requestSession.getDbConnection();
			if (null != connection)
			{
				Database.logQuery(statementStr);
				PreparedStatement statement = connection.prepareRetrieveStatement(statementStr);
					
				ResultSet queryResult = statement.executeQuery();
				while (queryResult.next())
				{
					BusinessObject board = new Board(requestSession);
					board.getById(queryResult.getInt(0));
					result.add(board);
				}
			}
			requestSession.makeCommit(true);
		}
		finally
		{
			requestSession.endTransaction();
		}
		
		return result;
	}
	
	
	abstract protected String getMainTableName();
}
