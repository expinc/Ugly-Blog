package net.uglyblog.bo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import net.uglyblog.dao.DataTable;
import net.uglyblog.db.*;
import net.uglyblog.session.RequestSession;


public class Board extends BusinessObject
{
	public enum AuthorityType {READ, REPLY, POST, MANAGE, OWN}
	
	
	protected static Map<String, AuthorityType> str2AuthorityTypes;
	
	
	static
	{
		str2AuthorityTypes = new TreeMap<String, AuthorityType>();
		str2AuthorityTypes.put("Read", AuthorityType.READ);
		str2AuthorityTypes.put("Reply", AuthorityType.REPLY);
		str2AuthorityTypes.put("Post", AuthorityType.POST);
		str2AuthorityTypes.put("Manage", AuthorityType.MANAGE);
		str2AuthorityTypes.put("Own", AuthorityType.OWN);
	}
	
	
	public static AuthorityType str2AuthorityType(String str)
	{
		return str2AuthorityTypes.get(str);
	}
	
	
	public static String AuthorityType2Str(AuthorityType authorityType)
	{
		switch (authorityType)
		{
			case READ:
				return "Read";
			case REPLY:
				return "Reply";
			case POST:
				return "Post";
			case MANAGE:
				return "Manage";
			case OWN:
				return "Own";
			default:
				return "Read";
		}
	}
	
	
	public static DataTable getBoardHeaders(RequestSession requestSession)
	{
		DataTable result = null;
		
		try
		{
			requestSession.startTransaction(Connection.TRANSACTION_READ_COMMITTED);
			try
			{
				String statementStr = new String(
						"select BOARDS.Id as Id, BOARDS.Name as Name, ACCOUNTS.Nickname as Moderator, count(POSTS.Id) as CountPosts " +
						"from BOARDS " +
						"inner join ACCOUNT_BOARD_AUTHORITIES on BOARDS.Id = ACCOUNT_BOARD_AUTHORITIES.BoardId " +
						"inner join ACCOUNTS on ACCOUNT_BOARD_AUTHORITIES.AccountId = ACCOUNTS.Id " +
						"left outer join POSTS on BOARDS.Id = POSTS.BoardId " +
						"where ACCOUNT_BOARD_AUTHORITIES.Authority = 'Manage' " +
						"group by BOARDS.Id, BOARDS.Name, ACCOUNTS.Nickname");
				
				DbConnection connection = requestSession.getDbConnection();
				if (null != connection)
				{
					Database.logQuery(statementStr);
					
					PreparedStatement statement = connection.prepareRetrieveStatement(statementStr);
						
					ResultSet queryResult = statement.executeQuery();
					result = DataTable.Create("BoardHeaders");
					if (queryResult.first())
					{
						result.setAllData(queryResult);
					}
				}
				requestSession.makeCommit(true);
			}
			finally
			{
				requestSession.endTransaction();
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return result;
	}


	public Board(RequestSession requestSession)
	{
		super(requestSession);
		DataTable tableBoards = DataTable.Create("BOARDS");
		dataTables.put("BOARDS", tableBoards);
	}

	
	@Override
	public boolean getById(int id) throws SQLException
	{
		DataTable tableBoards = DataTable.getByIdFromDb(requestSession, "BOARDS", id);
		if (null != tableBoards)
		{
			dataTables.put("BOARDS", tableBoards);
			return true;
		}
		else
			return false;
		
	}
	
	
	@Override
	public List<BusinessObject> getAll(RequestSession requestSession) throws SQLException
	{
		return getAllInternal(requestSession);
	}
	
	
	@Override
	protected String getMainTableName()
	{
		return "BOARDS";
	}
	
	
	public BusinessObject getModerator()
	{
		BusinessObject result = null;
		DataTable tableBoards = dataTables.get("BOARDS");
		Integer boardId = (Integer)tableBoards.getData("Id");
		
		try
		{
			requestSession.startTransaction(Connection.TRANSACTION_REPEATABLE_READ);
			try
			{
				String statementStr = new String(
						"select ACCOUNT_BOARD_AUTHORITIES.AccountId " +
						"from BOARDS " +
						"inner join ACCOUNT_BOARD_AUTHORITIES on BOARDS.Id = ACCOUNT_BOARD_AUTHORITIES.BoardId " +
						"where BOARDS.Id = " + boardId + " " +
							"and ACCOUNT_BOARD_AUTHORITIES.Authority = \'Manage\'");
				
				DbConnection connection = requestSession.getDbConnection();
				if (null != connection)
				{
					Database.logQuery(statementStr);
					
					PreparedStatement statement = connection.prepareRetrieveStatement(statementStr);
						
					ResultSet queryResult = statement.executeQuery();
					if (queryResult.first())
					{
						int moderatorId = queryResult.getInt(1);
						BusinessObject account = new Account(requestSession);
						if (true == account.getById(moderatorId))
							result = account;
					}
				}
				requestSession.makeCommit(true);
			}
			finally
			{
				requestSession.endTransaction();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	public DataTable getPostHeaders()
	{
		DataTable result = null;
		DataTable tableBoards = dataTables.get("BOARDS");
		Integer boardId = (Integer)tableBoards.getData("Id");
		
		try
		{
			requestSession.startTransaction(Connection.TRANSACTION_READ_COMMITTED);
			try
			{
				String statementStr = new String(
						"select POSTS.Id, POSTS.Title, ACCOUNTS.Id as OwnerId, ACCOUNTS.Nickname as Owner, POSTS.CreateDate, POSTS.CreateTime, POSTS.UpdateDate, POSTS.UpdateTime " + 
						"from BOARDS " + 
						"inner join POSTS on BOARDS.Id = POSTS.BoardId " + 
						"inner join ACCOUNTS on POSTS.OwnerId = ACCOUNTS.Id " + 
						"where BOARDS.Id = " + boardId);
				
				DbConnection connection = requestSession.getDbConnection();
				if (null != connection)
				{
					Database.logQuery(statementStr);
					
					PreparedStatement statement = connection.prepareRetrieveStatement(statementStr);
						
					ResultSet queryResult = statement.executeQuery();
					result = DataTable.Create("PostHeaders");
					if (queryResult.first())
					{						
						result.setAllData(queryResult);
					}
				}
				requestSession.makeCommit(true);
			}
			finally
			{
				requestSession.endTransaction();
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
}
