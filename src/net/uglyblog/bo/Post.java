package net.uglyblog.bo;

import java.sql.*;
import java.util.*;

import net.uglyblog.dao.DataTable;
import net.uglyblog.session.RequestSession;


public class Post extends BusinessObject
{
	public Post(RequestSession requestSession)
	{
		super(requestSession);
		DataTable tablePosts = DataTable.Create("POSTS");
		dataTables.put("POSTS", tablePosts);
		DataTable tableReplies = DataTable.Create("REPLIES");
		dataTables.put("REPLIES", tableReplies);
	}

	
	@Override
	protected String getMainTableName()
	{
		return "POSTS";
	}
	
	
	@Override
	public boolean getById(int id) throws SQLException
	{
		if (BusinessObjectManager.isLogEnabled())
			System.out.println("Getting post #" + id + "...");
		clear();
		
		requestSession.startTransaction(Connection.TRANSACTION_READ_COMMITTED);
		try
		{
			DataTable tablePosts = DataTable.getByIdFromDb(requestSession, "POSTS", id);
			if (0 >= tablePosts.getSize())
				return false;
			dataTables.put("POSTS", tablePosts);
			
			Map<String, Object> repliesConditionFields = new TreeMap<String, Object>();
			repliesConditionFields.put("PostId", id);
			DataTable tableReplies = DataTable.getFromDb(requestSession, "REPLIES", repliesConditionFields);
			dataTables.put("REPLIES", tableReplies);
			
			requestSession.makeCommit(true);
		}
		finally
		{
			requestSession.endTransaction();
		}
		
		return true;
	}
	
	
	@Override
	public boolean isValid(boolean forUpdate) throws SQLException
	{
		return isValidPosts() && isValidReplies();
	}

	
	@Override
	public int create(boolean doAutoComplete) throws SQLException
	{
		if (BusinessObjectManager.isLogEnabled())
			System.out.println("Creating post...");
		
		int newId = -1;
		requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
		try
		{
			if (doAutoComplete && !autoComplete())
				return -1;
			if (false == isValid(false))
				return -1;
			
			DataTable tablePosts = dataTables.get("POSTS");
			List<Integer> newIds = tablePosts.insertIntoDb(requestSession);
			if (!newIds.isEmpty())
			{
				newId = newIds.get(0);
				tablePosts.setData(newId, "Id");
				
				completeReplyIds();
				DataTable tableReplies = getDataTable("REPLIES");
				int countRecords = tableReplies.getSize();
				List<Integer> newReplyIds = tableReplies.insertIntoDb(requestSession);
				int countNewReplyIds = newReplyIds.size();
				if (countRecords != countNewReplyIds)
					return -1;
				for (int i = 0; i < countRecords; ++i)
				{
					tableReplies.setData(newReplyIds.get(i), "Id", i);
				}
				
				requestSession.makeCommit(true);
			}
		}
		finally
		{
			requestSession.endTransaction();
		}
		
		return newId;
	}
	
	
	@Override
	public boolean update(boolean doAutoComplete) throws SQLException
	{
		if (BusinessObjectManager.isLogEnabled())
			System.out.println("Updating post...");
		
		requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
		try
		{
			if (doAutoComplete && !autoComplete())
				return false;			
			if (!isValid(true))
				return false;
			
			DataTable tablePosts = getDataTable("POSTS");
			if (0 >= tablePosts.updateIntoDb(requestSession))
				return false;
			
			completeReplyIds();
			DataTable tableReplies = getDataTable("REPLIES");
			int countRecords = tableReplies.getSize();
			int countUpdates = 0;
			for (int i = 0; i < countRecords; ++i)
			{
				Integer replyId = (Integer) tableReplies.getData("Id", i);
				int result;
				if (null == replyId || 0 >= replyId)
				{
					result = tableReplies.insertIntoDb(requestSession, i);
					tableReplies.setData(result, "Id");
				}
				else
					result = tableReplies.updateIntoDb(requestSession, i);
				if (0 >= result)
					return false;
				else
					++countUpdates;
			}
			
			if (countRecords != countUpdates)
				return false;
			else
				requestSession.makeCommit(true);
		}
		finally
		{
			requestSession.endTransaction();
		}
		
		return true;
	}
	
	
	@Override
	public boolean delete() throws SQLException
	{
		if (BusinessObjectManager.isLogEnabled())
			System.out.println("Deleting post...");
		
		requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
		try
		{
			DataTable tablePosts = getDataTable("POSTS");
			if (1 != tablePosts.getSize())
				return false;
			Integer id = (Integer) tablePosts.getData("Id");
			
			Map<String, Object> replyConditions = new TreeMap<String, Object>();
			replyConditions.put("PostId", id);
			DataTable.deleteFromDb(requestSession, "REPLIES", replyConditions);
			tablePosts.deleteFromDb(requestSession);
			
			requestSession.makeCommit(true);
		}
		finally
		{
			requestSession.endTransaction();
		}
		
		return true;
	}
	
	
	@Override
	public boolean autoComplete() throws SQLException
	{
		requestSession.startTransaction(Connection.TRANSACTION_REPEATABLE_READ);
		try
		{
			completePosts();
			completeReplies();
		}
		finally
		{
			requestSession.endTransaction();
		}
		return true;
	}
	
	
	private void completePosts()
	{
		DataTable tablePosts = getDataTable("POSTS");
		long currentTimeMillis = System.currentTimeMillis();
		java.sql.Date currentDate = new java.sql.Date(currentTimeMillis);
		java.sql.Time currentTime = new java.sql.Time(currentTimeMillis);
		
		java.sql.Date createDate = (java.sql.Date) tablePosts.getData("CreateDate");
		if (null == createDate)
			tablePosts.setData(currentDate, "CreateDate");
		Time createTime = (Time) tablePosts.getData("CreateTime");
		if (null == createTime)
			tablePosts.setData(currentTime, "CreateTime");
		
		java.sql.Date updateDate = (java.sql.Date) tablePosts.getData("UpdateDate");
		if (null == updateDate)
			tablePosts.setData(currentDate, "UpdateDate");
		Time updateTime = (Time) tablePosts.getData("UpdateTime");
		if (null == updateTime)
			tablePosts.setData(currentTime, "UpdateTime");
	}
	
	
	private void completeReplies()
	{	
		DataTable tableReplies = getDataTable("REPLIES");
		java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
		Time currentTime = new Time(System.currentTimeMillis());
		int countRecords = tableReplies.getSize();
		for (int i = 0; i < countRecords; ++i)
		{
			java.sql.Date createDate = (java.sql.Date) tableReplies.getData("CreateDate", i);
			if (null == createDate)
				tableReplies.setData(currentDate, "CreateDate", i);
			Time createTime = (Time) tableReplies.getData("CreateTime", i);
			if (null == createTime)
				tableReplies.setData(currentTime, "CreateTime", i);
			tableReplies.setData(currentDate, "UpdateDate", i);
			tableReplies.setData(currentTime, "UpdateTime", i);
		}
	}
	
	
	private void completeReplyIds()
	{
		DataTable tablePosts = getDataTable("POSTS");
		Integer postId = (Integer) tablePosts.getData("Id");
		
		DataTable tableReplies = getDataTable("REPLIES");
		int countRecords = tableReplies.getSize();
		for (int i = 0; i < countRecords; ++i)
		{
			tableReplies.setData(postId, "PostId", i);
		}
	}
	
	
	private boolean isValidPosts() throws SQLException
	{
		DataTable tablePosts = getDataTable("POSTS");
		
		Integer boardId = (Integer) tablePosts.getData("BoardId");
		BusinessObject board = new Board(requestSession);
		if (!board.getById(boardId))
			return false;
		
		Integer ownerId = (Integer) tablePosts.getData("OwnerId");
		BusinessObject owner = new Account(requestSession);
		if (!owner.getById(ownerId))
			return false;
		
		String title = (String) tablePosts.getData("Title");
		if (null == title || title.trim().isEmpty())
			return false;
		
		return true;
	}
	
	
	private boolean isValidReplies() throws SQLException
	{
		DataTable tableReplies = getDataTable("REPLIES");
		int countRecords = tableReplies.getSize();
		for (int i = 0; i < countRecords; ++i)
		{
			Integer ownerId = (Integer) tableReplies.getData("OwnerId", i);
			BusinessObject owner = new Account(requestSession);
			if (!owner.getById(ownerId))
				return false;
		}
		
		return true;
	}
}
