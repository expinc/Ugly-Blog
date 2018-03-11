package net.uglyblog.db;

import java.sql.*;
import java.util.Collection;
import net.uglyblog.meta.TableExplorer.ColumnType;


public class DatabaseUtil
{
	public static String buildWhereEqualClause(Collection<String> fields)
	{
		if (null != fields && !fields.isEmpty())
		{
			StringBuilder clauseBuilder = new StringBuilder("where");
			boolean firstField = true;
			for (String field : fields)
			{
				if (firstField)
					clauseBuilder.append(" " + field + " = ?");
				else
					clauseBuilder.append(" and " + field + " = ?");
				firstField = false;
			}
			return clauseBuilder.toString();
		}
		else
			return null;
	}
	
	
	public static String buildValuesClause(Collection<String> fields)
	{
		if (null != fields && !fields.isEmpty())
		{
			StringBuilder clauseBuilder = new StringBuilder("(");
			
			boolean firstField = true;
			for (String field : fields)
			{
				if (firstField)
					clauseBuilder.append(field);
				else
					clauseBuilder.append(", " + field);
				firstField = false;
			}
			clauseBuilder.append(") values (");
			
			for (int i = 0; i < fields.size(); ++i)
			{
				if (0 == i)
					clauseBuilder.append("?");
				else
					clauseBuilder.append(", ?");
			}
			clauseBuilder.append(")");
			
			return clauseBuilder.toString();
		}
		else
			return null;
	}
	
	
	public static String buildSetClause(Collection<String> fields)
	{
		if (null != fields && !fields.isEmpty())
		{
			StringBuilder clauseBuilder = new StringBuilder("set ");
			boolean firstField = true;
			for (String field : fields)
			{
				if (firstField)
					clauseBuilder.append(field + " = ?");
				else
					clauseBuilder.append(", " + field + " = ?");
				firstField = false;
			}
			
			return clauseBuilder.toString();
		}
		else
			return null;
	}


	public static void setStatementParameter(PreparedStatement statement, int index, ColumnType columnType, Object value) throws SQLException
	{
		switch (columnType)
		{
			case STRING:
				statement.setString(index, (String) value);
				break;
			case INTEGER:
				statement.setInt(index, (int) value);
				break;
			case DATE:
				statement.setDate(index, (Date) value);
				break;
			case TIME:
				statement.setTime(index, (Time) value);
				break;
			default:
				break;
		}
	}
}
