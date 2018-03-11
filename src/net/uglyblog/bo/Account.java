package net.uglyblog.bo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import net.uglyblog.bo.Board.AuthorityType;
import net.uglyblog.dao.DataTable;
import net.uglyblog.session.RequestSession;


public class Account extends BusinessObject
{
	public enum AccountType {ADMIN, NORMAL}
	
	
	protected static Map<String, AccountType> str2AccountTypes;
	
	
	static
	{
		str2AccountTypes = new TreeMap<String, AccountType>();
		str2AccountTypes.put("Admin", AccountType.ADMIN);
		str2AccountTypes.put("Normal", AccountType.NORMAL);
	}
	
	
	public static String accountType2Str(AccountType accountType)
	{
		switch (accountType)
		{
			case ADMIN:
				return "Admin";
			case NORMAL:
				return "Normal";
			default:
				return null;
		}
	}
	
	
	public static AccountType str2AccountType(String str)
	{
		return str2AccountTypes.get(str);
	}
	
	
	public Account(RequestSession requestSession)
	{
		super(requestSession);
		DataTable tableAccounts = DataTable.Create("ACCOUNTS");
		dataTables.put("ACCOUNTS", tableAccounts);
		DataTable tableAccountBoardAuthorities = DataTable.Create("ACCOUNT_BOARD_AUTHORITIES");
		dataTables.put("ACCOUNT_BOARD_AUTHORITIES", tableAccountBoardAuthorities);
	}
	
	
	@Override
	public boolean getById(int id) throws SQLException
	{
		if (BusinessObjectManager.isLogEnabled())
			System.out.println("Getting account #" + id + "...");
		clear();
		
		requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
		try
		{
			DataTable tableAccounts = DataTable.getByIdFromDb(requestSession, "ACCOUNTS", id);
			if (0 >= tableAccounts.getSize())
				return false;
			dataTables.put("ACCOUNTS", tableAccounts);
			
			Map<String, Object> abaConditionFields = new TreeMap<String, Object>();
			abaConditionFields.put("AccountId", id);
			DataTable tableAccountBoardAuthorities = DataTable.getFromDb(requestSession, "ACCOUNT_BOARD_AUTHORITIES", abaConditionFields);
//			if (0 >= tableAccountBoardAuthorities.getSize())
//				return false;
			dataTables.put("ACCOUNT_BOARD_AUTHORITIES", tableAccountBoardAuthorities);
			
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
		return isValidMasterData(forUpdate) && isValidBoardAuthorities();
	}
	
	
	@Override
	public int create(boolean doAutoComplete) throws SQLException
	{		
		if (BusinessObjectManager.isLogEnabled())
			System.out.println("Creating account...");
		
		int newId = -1;
		requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
		try
		{
			if (doAutoComplete && !autoComplete())
				return -1;
			if (false == isValid(false))
				return -1;
			
			DataTable tableAccounts = dataTables.get("ACCOUNTS");
			List<Integer> newAccountIds = tableAccounts.insertIntoDb(requestSession);
			if (!newAccountIds.isEmpty())
			{
				newId = newAccountIds.get(0);
				tableAccounts.setData(newId, "Id");
				
				completeBoardAuthorityIds();
				DataTable tableBoardAuthorities = dataTables.get("ACCOUNT_BOARD_AUTHORITIES");
				int countRecords = tableBoardAuthorities.getSize();
				List<Integer> newAuthorityIds = tableBoardAuthorities.insertIntoDb(requestSession);
				int countAuthorityIds = newAuthorityIds.size();
				if (countRecords != countAuthorityIds)
					return -1;
				for (int i = 0; i < countAuthorityIds; ++i)
				{
					tableBoardAuthorities.setData(newAuthorityIds.get(i), "Id", i);
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
			System.out.println("Updating account...");
		
		requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
		try
		{
			if (doAutoComplete && !autoComplete())
					return false;			
			if (false == isValid(true))
				return false;
			
			DataTable tableAccounts = dataTables.get("ACCOUNTS");
			int result = tableAccounts.updateIntoDb(requestSession);
			if (0 >= result)
				return false;
			
			completeBoardAuthorityIds();
			DataTable tableBoardAuthorities = dataTables.get("ACCOUNT_BOARD_AUTHORITIES");
			int countRecords = tableBoardAuthorities.getSize();
			int countUpdates = 0;
			for (int i = 0; i < countRecords; ++i)
			{
				Integer boardAuthorityId = (Integer) tableBoardAuthorities.getData("Id", i);
				if (null == boardAuthorityId || 0 >= boardAuthorityId)
				{
					result = tableBoardAuthorities.insertIntoDb(requestSession, i);
					tableBoardAuthorities.setData(result, "Id");
				}
				else
					result = tableBoardAuthorities.updateIntoDb(requestSession, i);
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
	public boolean autoComplete()
	{
		DataTable tableAccounts = dataTables.get("ACCOUNTS");
		String accountType = (String) tableAccounts.getData("Type");
		if (null == accountType || accountType.trim().isEmpty())
			tableAccounts.setData(Account.accountType2Str(AccountType.NORMAL), "Type");
		
		String nickname = (String) tableAccounts.getData("Nickname");
		if (null == nickname || nickname.trim().isEmpty())
		{
			String accountName = (String) tableAccounts.getData("AccountName");
			tableAccounts.setData(accountName, "Nickname");
		}
		
		DataTable tableBoardAuthorities = dataTables.get("ACCOUNT_BOARD_AUTHORITIES");
		int countRecords = tableBoardAuthorities.getSize();
		Integer id = (Integer) tableAccounts.getData("Id");
		String authorityTypeStr = Board.AuthorityType2Str(AuthorityType.READ);
		for (int i = 0; i < countRecords; ++i)
		{
			if (null != id && id > 0)
				tableBoardAuthorities.setData(id, "AccountId", i);
			
			String authority = (String) tableBoardAuthorities.getData("Authority", i);
			if (null == authority || authority.trim().isEmpty())
			{
				tableBoardAuthorities.setData(authorityTypeStr, "Authority", i);
			}
		}
			
		return true;
	}
	
	
	private void completeBoardAuthorityIds()
	{
		DataTable tableAccounts = getDataTable("ACCOUNTS");
		Integer accountId = (Integer) tableAccounts.getData("Id");
		
		DataTable tableBoardAuthorities = getDataTable("ACCOUNT_BOARD_AUTHORITIES");
		int countRecords = tableBoardAuthorities.getSize();
		for (int i = 0; i < countRecords; ++i)
		{
			tableBoardAuthorities.setData(accountId, "AccountId", i);
		}
	}
	
	
	@Override
	protected String getMainTableName()
	{
		return "ACCOUNTS";
	}
	
	
	public static Account get(RequestSession requestSession, String accountName, String password)
	{
		if (BusinessObjectManager.isLogEnabled())
			System.out.println("Getting account \'" + accountName + "\'...");
		
		Account result = null;
		if (null == accountName)
			return null;
		
		try
		{
			requestSession.startTransaction(Connection.TRANSACTION_REPEATABLE_READ);
			try
			{
				Map<String, Object> conditionFields = new TreeMap<String, Object>();
				conditionFields.put("AccountName", accountName);
				if (null != password)
					conditionFields.put("Passcode", password);
				DataTable resultData = DataTable.getFromDb(requestSession, "ACCOUNTS", conditionFields);
				if (0 < resultData.getSize())
				{
					int id = (Integer)resultData.getData("Id");
					result = new Account(requestSession);
					result.getById(id);
				}
				requestSession.makeCommit(true);
			}
			finally
			{
				requestSession.endTransaction();
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			result = null;
		}
		
		return result;
	}
	
	
	public static Account register(RequestSession requestSession, String accountName, String password, String confirmPass)
	{		
		if (null == accountName || null == password || null == confirmPass || !password.equals(confirmPass))
			return null;
		
		Account result = new Account(requestSession);
		DataTable tableAccounts = result.getDataTable("ACCOUNTS");
		tableAccounts.append();
		tableAccounts.setData(accountName, "AccountName");
		tableAccounts.setData(password, "Passcode");
		
		int newKey = -1;
		try
		{
			requestSession.startTransaction(Connection.TRANSACTION_REPEATABLE_READ);
			try
			{
				newKey = result.create(true);
				requestSession.makeCommit(true);
			}
			finally
			{
				requestSession.endTransaction();
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		
		if (0 < newKey)
			return result;
		else
			return null;
	}
	
	
	protected static boolean isValidAccountName(String accountName)
	{
		if (accountName.length() < 3 || accountName.length() > 32)
			return false;
		
		for (char c : accountName.toCharArray())
		{
			if (!Character.isLetter(c) && !Character.isDigit(c) && '_' != c)
				return false;
		}
		
		return true;
	}
	
	
	protected static boolean isValidPassword(String password)
	{
		if (password.length() < 4 || password.length() > 32)
			return false;
		
		for (char c : password.toCharArray())
		{
			if (!Character.isLetter(c) && !Character.isDigit(c) && '_' != c)
				return false;
		}
		
		return true;
	}
	
	
	protected static boolean isValidNickname(String nickname)
	{
		if (null == nickname)
			return false;
		String trimedNickname = nickname.trim();
		if (trimedNickname.length() < 1 || trimedNickname.length() > 16)
			return false;
		
		return true;
	}
	
	
	protected static Account create(RequestSession requestSession, String accountName, String password)
	{
		Account newAccount = new Account(requestSession);
		DataTable tableAccounts = newAccount.getDataTable("ACCOUNTS");
		tableAccounts.setData(accountName, "AccountName");
		tableAccounts.setData(password, "Passcode");

		try
		{
			int newId = newAccount.create(true);
			if (-1 == newId)
				newAccount = null;
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			newAccount = null;
		}
		
		return newAccount;
	}
	
	
	protected boolean isValidMasterData(boolean forUpdate) throws SQLException
	{
		DataTable tableAccounts = getDataTable("ACCOUNTS");
		String tmpStr = (String)tableAccounts.getData("AccountName");
		if (false == isValidAccountName(tmpStr))
			return false;
		if (false == forUpdate && null != get(requestSession, tmpStr, null))
			return false;
		tmpStr = (String)tableAccounts.getData("Passcode");
		if (false == isValidPassword(tmpStr))
			return false;
		tmpStr = (String)tableAccounts.getData("Type");
		if (null == Account.str2AccountType(tmpStr))
			return false;
		tmpStr = (String)tableAccounts.getData("Nickname");
		if (false == isValidNickname(tmpStr))
			return false;		
		
		return true;
	}
	
	
	protected boolean isValidBoardAuthorities() throws SQLException
	{
		DataTable tableBoardAuthorities = getDataTable("ACCOUNT_BOARD_AUTHORITIES");
		int countRecords = tableBoardAuthorities.getSize();
		for (int i = 0; i < countRecords; ++i)
		{
			int boardId = (int) tableBoardAuthorities.getData("BoardId", i);
			Board board = new Board(requestSession);
			if (false == board.getById(boardId))
				return false;
			
			String authority = (String)tableBoardAuthorities.getData("Authority", i);
			if (null == Board.str2AuthorityType(authority))
				return false;
		}
		
		return true;
	}
}
