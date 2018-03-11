package net.uglyblog.servlets;

import java.sql.Connection;

import javax.security.auth.login.LoginException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import net.uglyblog.bo.Account;
import net.uglyblog.dao.DataTable;
import net.uglyblog.session.RequestSession;
import net.uglyblog.ui.*;
import net.uglyblog.ui.PageFactory.PageType;


/**
 * Servlet implementation class ProfileServlet
 */
@WebServlet("/Profile")
public class ProfileServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
       
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProfileServlet() {
        super();
    }
    
    
    @Override
	protected void logGet(HttpServletRequest request, HttpServletResponse response)
	{
		String account = request.getParameter("account");
		System.out.println("Getting details of account " + account + "...");
	}
    
    
    @Override
    protected Page handleGet(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
	{
    	String account = checkSessionAccount(request);
    	if (null == account)
    		throw new LoginException();
		
    	Page responsePage = null;
		Account accountObject = Account.get(requestSession, account, null);
		if (null != accountObject)
		{
			DataTable tableAccounts = accountObject.getDataTable("ACCOUNTS");
			String nickname = (String) tableAccounts.getData("Nickname");
			
			ProfilePage profilePage = (ProfilePage) PageFactory.createPage(PageType.PROFILE);
			profilePage.showPageHeader(account);
			profilePage.showHelloMessage(account);
			profilePage.showDashboardLink();
			profilePage.showLogoutButton(account);
			profilePage.showAccount(account);
			profilePage.showNickname(nickname);
			
			responsePage = profilePage;
		}
		else
		{
			throw new Exception("Account not exists!");
		}
		
		return responsePage;
	}
    
    
    @Override
    protected void logPost(HttpServletRequest request, HttpServletResponse response)
    {
    	String account = getSessionAccount(request);
    	String method = request.getParameter("method");
    	if ("Change Profile".equals(method))
    		System.out.println("Changing profile of account " + account + "...");
    	else if ("Change Password".equals(method))
    		System.out.println("Changing password of account " + account + "...");
    }
    
    
    @Override
    protected Page handlePost(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
	{
    	String account = checkSessionAccount(request);
    	if (null == account)
    		throw new LoginException();
    	
    	Page responsePage = null;
    	requestSession.startTransaction(Connection.TRANSACTION_REPEATABLE_READ);
    	try
    	{
    		Account accountObject = Account.get(requestSession, account, null);
    		if (null != accountObject)
    		{
    			DataTable tableAccounts = accountObject.getDataTable("ACCOUNTS");
    			String method = request.getParameter("method");
    			boolean updateSucceed = true;
    			if ("Change Profile".equals(method))
    			{
    				String nickname = request.getParameter("nickname");
    				tableAccounts.setData(nickname, "Nickname");
    			}
    			else if ("Change Password".equals(method))
    			{
    				String password = request.getParameter("password");
    				String confirmPassword = request.getParameter("confirm-password");
    				if (false == password.equals(confirmPassword))
    					updateSucceed = false;
    				tableAccounts.setData(password, "Passcode");
    			}
    			if (updateSucceed)
    			{
    				if (false == accountObject.update(false))
    					updateSucceed = false;
    			}
    			String newNickname = (String) tableAccounts.getData("Nickname");

    			ProfilePage profilePage = (ProfilePage) PageFactory.createPage(PageType.PROFILE);
    			profilePage.showPageHeader(account);
    			profilePage.showHelloMessage(account);
    			profilePage.showDashboardLink();
    			profilePage.showLogoutButton(account);
    			profilePage.showAccount(account);
    			profilePage.showNickname(newNickname);
    			if (updateSucceed)
    			{
    				if ("Change Profile".equals(method))
    					profilePage.showMessage("Profile has been changed.", MessageType.INFO);
    				else if ("Change Password".equals(method))
    					profilePage.showMessage("Password has been changed.", MessageType.INFO);
    			}
    			else
    			{
    				if ("Change Profile".equals(method))
    					profilePage.showMessage("Profile is invalid!", MessageType.ERROR);
    				else if ("Change Password".equals(method))
    					profilePage.showMessage("Password is invalid!", MessageType.ERROR);
    			}

    			responsePage = profilePage;
    		}
    		else
    		{
    			throw new Exception("Account not exists!");
    		}
    		requestSession.makeCommit(true);
    	}
    	finally
    	{
    		requestSession.endTransaction();
    	}
		
		return responsePage;
	}
}
