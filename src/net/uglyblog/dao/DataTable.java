package net.uglyblog.dao;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

import net.uglyblog.db.*;
import net.uglyblog.meta.TableExplorer;
import net.uglyblog.meta.TableExplorer.ColumnType;
import net.uglyblog.session.RequestSession;


public class DataTable
{
	private String tableName;
	private Map<String, ColumnType> tableSchema;
	private List<Map<String, Object>> records;
	
	
	public static DataTable Create(String tableName)
	{
		if (null != TableExplorer.getTableSchema(tableName))
			return new DataTable(tableName, false);
		else if (null != TableExplorer.getSheetSchema(tableName))
			return new DataTable(tableName, true);
		else
			return null;
	}
	
	
	private DataTable(String tableName, boolean sheet)
	{
		this.tableName = new String(tableName);
		Map<String, ColumnType> schema;
		if (false == sheet)
			schema = TableExplorer.getTableSchema(tableName);
		else
			schema = TableExplorer.getSheetSchema(tableName);
		tableSchema = new TreeMap<String, ColumnType>(schema);
		records = new ArrayList<Map<String, Object>>();
	}
	
	
	public int getSize()
	{
		return records.size();
	}
	
	
	public void append()
	{
		Map<String, Object> record = new TreeMap<String, Object>();
		for (Entry<String, ColumnType> column : tableSchema.entrySet())
		{
			record.put(column.getKey(), null);
		}
		records.add(record);
	}
	
	
	public void clear()
	{
		records.clear();
	}
	
	
	public boolean setData(int value, String col)
	{
		return setData(value, col, 0);
	}
	
	
	public boolean setData(int value, String col, int row)
	{
		if (0 > row || records.size() <= row || false == tableSchema.containsKey(col) || ColumnType.INTEGER != tableSchema.get(col))
			return false;
		else
		{
			records.get(row).put(col, new Integer(value));
			return true;
		}
	}
	
	
	public boolean setData(String value, String col)
	{
		return setData(value, col, 0);
	}
	
	
	public boolean setData(String value, String col, int row)
	{
		if (0 > row || records.size() <= row || false == tableSchema.containsKey(col) || ColumnType.STRING != tableSchema.get(col))
			return false;
		else
		{
			records.get(row).put(col, new String(value));
			return true;
		}
	}
	
	
	public boolean setData(java.sql.Date value, String col)
	{
		return setData(value, col, 0);
	}
	
	
	public boolean setData(java.sql.Date value, String col, int row)
	{
		if (0 > row || records.size() <= row || false == tableSchema.containsKey(col) || ColumnType.DATE != tableSchema.get(col))
			return false;
		else
		{
			records.get(row).put(col, value.clone());
			return true;
		}
	}
	
	
	public boolean setData(Time value, String col)
	{
		return setData(value, col, 0);
	}
	
	
	public boolean setData(Time value, String col, int row)
	{
		if (0 > row || records.size() <= row || false == tableSchema.containsKey(col) || ColumnType.TIME != tableSchema.get(col))
			return false;
		else
		{
			records.get(row).put(col, value.clone());
			return true;
		}
	}
	
	
	public boolean setData(ResultSet queryResult, int row) throws SQLException
	{
		if (0 > row || records.size() <= row || null == queryResult)
			return false;
		
		for (Entry<String, ColumnType> column : tableSchema.entrySet())
		{
			switch (column.getValue())
			{
				case INTEGER:
					setData(queryResult.getInt(column.getKey()), column.getKey(), row);
					break;
				case STRING:
					setData(queryResult.getString(column.getKey()), column.getKey(), row);
					break;
				case DATE:
					setData(queryResult.getDate(column.getKey()), column.getKey(), row);
					break;
				case TIME:
					setData(queryResult.getTime(column.getKey()), column.getKey(), row);
					break;
				default:
					break;
			}
		}
		return true;
	}
	
	
	public boolean setAllData(ResultSet queryResult) throws SQLException
	{
		if (null == queryResult)
			return false;
		
		clear();
		if (queryResult.first())
		{
			queryResult.beforeFirst();
			int row = 0;
			while (queryResult.next())
			{
				append();
				setData(queryResult, row);
				++row;
			}
		}
		return true;
	}
	
	
	public Object getData(String col, int row)
	{
		try
		{
			return records.get(row).get(col);
		}
		catch (Exception ex)
		{
			return null;
		}
	}
	
	
	public Object getData(String col)
	{
		return getData(col, 0);
	}
	
	
	public static DataTable getByIdFromDb(RequestSession requestSession, String tableName, int id) throws SQLException
	{
		Map<String, Object> conditionFields = new TreeMap<String, Object>();
		conditionFields.put("Id", id);
		return getFromDb(requestSession, tableName, conditionFields);
	}
	
	
	public static DataTable getFromDb(RequestSession requestSession, String tableName, Map<String, Object> conditionFields) throws SQLException
	{		
		Map<String, ColumnType> tableSchema = TableExplorer.getTableSchema(tableName);
		if (null == tableSchema)
			return null;
		
		StringBuilder statementStrBuilder = new StringBuilder("select * from " + tableName);
		if (null != conditionFields && false == conditionFields.isEmpty())
		{
			String whereClause = DatabaseUtil.buildWhereEqualClause(conditionFields.keySet());
			statementStrBuilder.append(" " + whereClause);
		}
		
		DataTable result = DataTable.Create(tableName);
		DbConnection connection = null;
		try
		{
			connection = requestSession.getDbConnection();
			String statementStr = statementStrBuilder.toString();
			if (null != connection)
			{
				Database.logQuery(statementStr);
				
				PreparedStatement statement = connection.prepareRetrieveStatement(statementStr);
				
				int conditionIndex = 1;
				for (Entry<String, Object> conditionField : conditionFields.entrySet())
				{
					ColumnType columnType = tableSchema.get(conditionField.getKey());
					DatabaseUtil.setStatementParameter(statement, conditionIndex, columnType, conditionField.getValue());
					++conditionIndex;
				}
					
				ResultSet queryResult = statement.executeQuery();
				if (queryResult.first())
					result.setAllData(queryResult);
			}
		}
		catch (SQLException ex)
		{
			throw ex;
		}
		
		return result;
	}
	
	
	public static int insertIntoDb(RequestSession requestSession, String tableName, Map<String, Object> recordFields) throws SQLException
	{
		int newId = -1;
		
		Map<String, ColumnType> tableSchema = TableExplorer.getTableSchema(tableName);
		if (null == tableSchema || null == recordFields || recordFields.isEmpty())
			return newId;
		
		StringBuilder statementStrBuilder = new StringBuilder("insert into " + tableName + " ");
		String valuesClause = DatabaseUtil.buildValuesClause(recordFields.keySet());
		statementStrBuilder.append(valuesClause);
		
		try
		{
			DbConnection connection = requestSession.getDbConnection();
			if (null != connection)
			{
				Database.logQuery(statementStrBuilder.toString());
				
				PreparedStatement statement = connection.prepareInsertStatement(statementStrBuilder.toString());
				int fieldIndex = 1;
				for (Entry<String, Object> recordField : recordFields.entrySet())
				{
					ColumnType columnType = tableSchema.get(recordField.getKey());
					DatabaseUtil.setStatementParameter(statement, fieldIndex, columnType, recordField.getValue());
					++fieldIndex;
				}
				statement.executeUpdate();

				ResultSet generatedKeys = statement.getGeneratedKeys();
				if (generatedKeys.next())
				{
					newId = generatedKeys.getInt(1);
				}
			}
		}
		catch (SQLException ex)
		{
			throw ex;
		}
		
		return newId;
	}
	
	
	public static int countInDb(RequestSession requestSession, String tableName, Map<String, Object> conditionFields) throws SQLException
	{
		Map<String, ColumnType> tableSchema = TableExplorer.getTableSchema(tableName);
		if (null == tableSchema)
			return -1;
		
		StringBuilder statementStrBuilder = new StringBuilder("select count(*) from " + tableName);
		if (null != conditionFields && false == conditionFields.isEmpty())
		{
			String whereClause = DatabaseUtil.buildWhereEqualClause(conditionFields.keySet());
			statementStrBuilder.append(" " + whereClause);
		}
		
		int result = -1;
		DbConnection connection = null;
		try
		{
			connection = requestSession.getDbConnection();
			String statementStr = statementStrBuilder.toString();
			if (null != connection)
			{
				Database.logQuery(statementStr);
				
				PreparedStatement statement = connection.prepareRetrieveStatement(statementStr);
				
				int conditionIndex = 1;
				for (Entry<String, Object> conditionField : conditionFields.entrySet())
				{
					ColumnType columnType = tableSchema.get(conditionField.getKey());
					DatabaseUtil.setStatementParameter(statement, conditionIndex, columnType, conditionField.getValue());
					++conditionIndex;
				}
					
				ResultSet queryResult = statement.executeQuery();
				if (queryResult.first())
					result = queryResult.getInt(1);
				else
					result = -1;
			}
		}
		catch (SQLException ex)
		{
			throw ex;
		}
		
		return result;
	}
	
	
	public static int updateIntoDb(RequestSession requestSession, String tableName, Map<String, Object> recordFields, Map<String, Object> conditionFields) throws SQLException
	{
		Map<String, ColumnType> tableSchema = TableExplorer.getTableSchema(tableName);
		if (null == tableSchema)
			throw new SQLException("Invalid table name");
		
		StringBuilder statementStrBuilder = new StringBuilder("update " + tableName);
		if (null != recordFields && false == recordFields.isEmpty())
		{
			String setClause = DatabaseUtil.buildSetClause(recordFields.keySet());
			statementStrBuilder.append(" " + setClause);
		}
		else
			throw new SQLException("Invalid fields");
		
		if (null != conditionFields && false == conditionFields.isEmpty())
		{
			String whereClause = DatabaseUtil.buildWhereEqualClause(conditionFields.keySet());
			statementStrBuilder.append(" " + whereClause);
		}
		
		DbConnection connection = null;
		int result = 0;
		try
		{
			connection = requestSession.getDbConnection();
			String statementStr = statementStrBuilder.toString();
			if (null != connection)
			{
				Database.logQuery(statementStr);
				
				PreparedStatement statement = connection.prepareUpdateStatement(statementStr);
				int parameterIndex = 1;
				for (Entry<String, Object> recordField : recordFields.entrySet())
				{
					ColumnType columnType = tableSchema.get(recordField.getKey());
					DatabaseUtil.setStatementParameter(statement, parameterIndex, columnType, recordField.getValue());
					++parameterIndex;
				}
				for (Entry<String, Object> conditionField : conditionFields.entrySet())
				{
					ColumnType columnType = tableSchema.get(conditionField.getKey());
					DatabaseUtil.setStatementParameter(statement, parameterIndex, columnType, conditionField.getValue());
					++parameterIndex;
				}
					
				result = statement.executeUpdate();
			}
		}
		catch (SQLException ex)
		{
			throw ex;
		}
		return result;
	}
	
	
	public static int deleteFromDb(RequestSession requestSession, String tableName, Map<String, Object> conditionFields) throws SQLException
	{
		Map<String, ColumnType> tableSchema = TableExplorer.getTableSchema(tableName);
		if (null == tableSchema)
			throw new SQLException("Invalid table name");
		
		StringBuilder statementStrBuilder = new StringBuilder("delete from " + tableName);
		
		if (null != conditionFields && false == conditionFields.isEmpty())
		{
			String whereClause = DatabaseUtil.buildWhereEqualClause(conditionFields.keySet());
			statementStrBuilder.append(" " + whereClause);
		}
		
		int result = 0;
		try
		{
			DbConnection connection = requestSession.getDbConnection();
			String statementStr = statementStrBuilder.toString();
			if (null != connection)
			{
				Database.logQuery(statementStr);
				
				PreparedStatement statement = connection.prepareDeleteStatement(statementStr);
				int parameterIndex = 1;
				for (Entry<String, Object> conditionField : conditionFields.entrySet())
				{
					ColumnType columnType = tableSchema.get(conditionField.getKey());
					DatabaseUtil.setStatementParameter(statement, parameterIndex, columnType, conditionField.getValue());
					++parameterIndex;
				}
					
				result = statement.executeUpdate();
			}
		}
		catch (SQLException ex)
		{
			throw ex;
		}
		
		return result;
	}
	
	
	public List<Integer> insertIntoDb(RequestSession requestSession) throws SQLException
	{
		List<Integer> result = new LinkedList<Integer>();
		for (Map<String, Object> record : records)
		{
			Map<String, Object> recordNoId = new TreeMap<String, Object>(record);
			recordNoId.remove("Id");
			int newId = insertIntoDb(requestSession, tableName, recordNoId);
			result.add(newId);
		}
		return result;
	}
	
	
	public int insertIntoDb(RequestSession requestSession, int row) throws SQLException
	{
		Map<String, Object> record = records.get(row);
		Map<String, Object> recordNoId = new TreeMap<String, Object>(record);
		recordNoId.remove("Id");
		int newId = insertIntoDb(requestSession, tableName, recordNoId);
		return newId;
	}
	
	
	public int updateIntoDb(RequestSession requestSession) throws SQLException
	{
		int countUpdatedRows = 0;
		int countRecords = records.size();
		for (int i = 0; i < countRecords; ++i)
		{
			countUpdatedRows += updateIntoDb(requestSession, i);
		}
		return countUpdatedRows;
	}
	
	
	public int updateIntoDb(RequestSession requestSession, int row) throws SQLException
	{
		Map<String, Object> record = records.get(row);
		Integer id = (Integer) record.get("Id");
		Map<String, Object> conditionFields = new TreeMap<String, Object>();
		conditionFields.put("Id", id);
		Map<String, Object> recordNoId = new TreeMap<String, Object>(record);
		recordNoId.remove("Id");
		return updateIntoDb(requestSession, tableName, recordNoId, conditionFields);
	}
	
	
	public int deleteFromDb(RequestSession requestSession) throws SQLException
	{
		int countDeletedRows = 0;
		int countRecords = records.size();
		for (int i = 0; i < countRecords; ++i)
		{
			countDeletedRows += deleteFromDb(requestSession, i);
		}
		return countDeletedRows;
	}
	
	
	public int deleteFromDb(RequestSession requestSession, int row) throws SQLException
	{
		Map<String, Object> record = records.get(row);
		Integer id = (Integer) record.get("Id");
		Map<String, Object> conditionFields = new TreeMap<String, Object>();
		conditionFields.put("Id", id);
		return deleteFromDb(requestSession, tableName, conditionFields);
	}
}
