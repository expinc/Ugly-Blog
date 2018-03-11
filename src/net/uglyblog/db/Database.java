package net.uglyblog.db;
import java.sql.*;


public abstract class Database
{
	private static final String dbUrl = "jdbc:sqlserver://localhost\\EXPSQL;databaseName=UGLYBLOG";
	private static final String user = "sa";
	private static final String password = "Pass1234";
	private static boolean doLog = true;
	
	
	public static boolean isLogEnabled()
	{
		return doLog;
	}
	
	
	public static void enableLog(boolean value)
	{
		doLog = value;
	}
	
	
	public static Connection getConnection() throws SQLException
	{
		//System.out.println("Connecting database...");
		Connection connection = null;
		
		try
		{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			connection = DriverManager.getConnection(dbUrl, user, password);
			if (null != connection)
			{
				/*System.out.println("Database connected.");
				DatabaseMetaData meta = connection.getMetaData();
				System.out.println(
						"Driver name - " + meta.getDriverName() +
						", Driver version - " + meta.getDriverVersion() +
						", Product name - " + meta.getDatabaseProductName() +
						", Product version - " + meta.getDatabaseProductVersion());*/
			}
		}
		catch (SQLException ex)
		{
			throw ex;
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		
		return connection;
	}
	
	
	public static boolean logQuery(String query)
	{
		if (Database.isLogEnabled())
		{
			System.out.println("Executing query: " + query);
			return true;
		}
		else
			return false;
	}
}
