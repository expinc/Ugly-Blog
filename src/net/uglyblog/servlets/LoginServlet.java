package net.uglyblog.servlets;

import java.sql.Connection;

import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import net.uglyblog.bo.Account;
import net.uglyblog.session.LoginSessionManager;
import net.uglyblog.session.RequestSession;
import net.uglyblog.ui.*;
import net.uglyblog.ui.PageFactory.PageType;


/**
 * Servlet implementation class Login
 */
@WebServlet("/Login")
public class LoginServlet extends BaseServlet {	
	private static final long serialVersionUID = 1L;


	/**
     * Default constructor. 
     */
    public LoginServlet() {
    	super();
    }

	
	@Override
	protected void logPost(HttpServletRequest request, HttpServletResponse response)
	{
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		System.out.println("Login request: account - " + account + ", password - " + password);
	}

	
	@Override
	protected Page handlePost(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
	{
		Page responsePage = null;
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		
		requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
		try
		{
			Account loginAccount = Account.get(requestSession, account, password);
			if (null != loginAccount)
			{
				int cookieId = LoginSessionManager.create(account);
				if (0 != cookieId)
				{
					Cookie cookie = new Cookie("uglyblog", Integer.valueOf(cookieId).toString());
					response.addCookie(cookie);
					System.out.println(account + " has logged into the system.");

					RequestDispatcher dispatcher = request.getRequestDispatcher("/Dashboard");
					dispatcher.forward(request, response);
				}
				else
				{
					Page busyPage = PageFactory.createPage(PageType.SYSTEM_BUSY);
					responsePage = busyPage;
					System.out.println("System busy when " + account + " was logining.");
				}
			}
			else
			{
				IndexPage indexPage = (IndexPage)PageFactory.createPage(PageType.INDEX);
				indexPage.showFailMessage();
				responsePage = indexPage;	
				System.out.println(account + " failed to login.");
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
