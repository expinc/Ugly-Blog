package net.uglyblog.meta;

import java.util.*;


public abstract class TableExplorer
{
	public enum ColumnType {STRING, INTEGER, DATE, TIME}
	
	private static Map<String, Map<String, ColumnType>> tableSchemas;
	private static Map<String, Map<String, ColumnType>> sheetSchemas;
	
	
	static
	{
		tableSchemas = new TreeMap<String, Map<String, ColumnType>>();
		initAccounts();
		initBoards();
		initAccountBoardAuthorities();
		initPosts();
		initReplies();
		
		sheetSchemas = new TreeMap<String, Map<String, ColumnType>>();
		initBoardHeaders();
		initPostHeaders();
	}
	
	
	public static Map<String, ColumnType> getTableSchema(String tableName)
	{
		return tableSchemas.get(tableName);
	}
	
	
	public static Map<String, ColumnType> getSheetSchema(String tableName)
	{
		return sheetSchemas.get(tableName);
	}
	
	
	private static void initAccounts()
	{
		Map<String, ColumnType> columns = new TreeMap<String, ColumnType>();
		columns.put("Id", ColumnType.INTEGER);
		columns.put("AccountName", ColumnType.STRING);
		columns.put("Passcode", ColumnType.STRING);
		columns.put("Type", ColumnType.STRING);
		columns.put("Nickname", ColumnType.STRING);
		tableSchemas.put("ACCOUNTS", columns);
	}
	
	
	private static void initBoards()
	{
		Map<String, ColumnType> columns = new TreeMap<String, ColumnType>();
		columns.put("Id", ColumnType.INTEGER);
		columns.put("Name", ColumnType.STRING);
		tableSchemas.put("BOARDS", columns);
	}
	
	
	private static void initAccountBoardAuthorities()
	{
		Map<String, ColumnType> columns = new TreeMap<String, ColumnType>();
		columns.put("Id", ColumnType.INTEGER);
		columns.put("AccountId", ColumnType.INTEGER);
		columns.put("BoardId", ColumnType.INTEGER);
		columns.put("Authority", ColumnType.STRING);
		tableSchemas.put("ACCOUNT_BOARD_AUTHORITIES", columns);
	}
	
	
	private static void initPosts()
	{
		Map<String, ColumnType> columns = new TreeMap<String, ColumnType>();
		columns.put("Id", ColumnType.INTEGER);
		columns.put("BoardId", ColumnType.INTEGER);
		columns.put("OwnerId", ColumnType.INTEGER);
		columns.put("Title", ColumnType.STRING);
		columns.put("CreateDate", ColumnType.DATE);
		columns.put("CreateTime", ColumnType.TIME);
		columns.put("UpdateDate", ColumnType.DATE);
		columns.put("UpdateTime", ColumnType.TIME);
		columns.put("Content", ColumnType.STRING);
		tableSchemas.put("POSTS", columns);
	}
	
	
	private static void initReplies()
	{
		Map<String, ColumnType> columns = new TreeMap<String, ColumnType>();
		columns.put("Id", ColumnType.INTEGER);
		columns.put("PostId", ColumnType.INTEGER);
		columns.put("OwnerId", ColumnType.INTEGER);
		columns.put("CreateDate", ColumnType.DATE);
		columns.put("CreateTime", ColumnType.TIME);
		columns.put("UpdateDate", ColumnType.DATE);
		columns.put("UpdateTime", ColumnType.TIME);
		columns.put("Content", ColumnType.STRING);
		tableSchemas.put("REPLIES", columns);
	}
	
	
	private static void initBoardHeaders()
	{
		Map<String, ColumnType> columns = new TreeMap<String, ColumnType>();
		columns.put("Id", ColumnType.INTEGER);
		columns.put("Name", ColumnType.STRING);
		columns.put("Moderator", ColumnType.STRING);
		columns.put("CountPosts", ColumnType.INTEGER);
		sheetSchemas.put("BoardHeaders", columns);
	}
	
	
	private static void initPostHeaders()
	{
		Map<String, ColumnType> columns = new TreeMap<String, ColumnType>();
		columns.put("Id", ColumnType.INTEGER);
		columns.put("Title", ColumnType.STRING);
		columns.put("OwnerId", ColumnType.INTEGER);
		columns.put("Owner", ColumnType.STRING);
		columns.put("CreateDate", ColumnType.DATE);
		columns.put("CreateTime", ColumnType.TIME);
		columns.put("UpdateDate", ColumnType.DATE);
		columns.put("UpdateTime", ColumnType.TIME);
		sheetSchemas.put("PostHeaders", columns);
	}
}
