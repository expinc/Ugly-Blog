package net.uglyblog.servlets;

import javax.security.auth.login.LoginException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import net.uglyblog.bo.Board;
import net.uglyblog.dao.DataTable;
import net.uglyblog.session.RequestSession;
import net.uglyblog.ui.*;
import net.uglyblog.ui.PageFactory.PageType;


/**
 * Servlet implementation class DashboardServlet
 */
@WebServlet("/Dashboard")
public class DashboardServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
       
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DashboardServlet() {
        super();
    }
    
    
    @Override
	protected void logPost(HttpServletRequest request, HttpServletResponse response)
	{
		System.out.println("Getting dashboard...");
	}
    
    
    @Override
	protected Page handlePost(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws LoginException
	{
    	String account = request.getParameter("account");    	
    	return getResponsePage(requestSession, account);
	}
    
    
    @Override
    protected void logGet(HttpServletRequest request, HttpServletResponse response)
    {
    	logPost(request, response);
    }
    
    
    @Override
    protected Page handleGet(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
	{
    	String account = checkSessionAccount(request);
    	if (null == account)
    		throw new LoginException();
    	
		return getResponsePage(requestSession, account);
	}
    
    
    private Page getResponsePage(RequestSession requestSession, String account)
    {
    	Page responsePage = PageFactory.createPage(PageType.DASHBOARD);
		DashboardPage dashboardPage = (DashboardPage)responsePage;
		dashboardPage.showPageHeader(null);
		dashboardPage.showHelloMessage(account);
		dashboardPage.showProfileLink();
		dashboardPage.showLogoutButton(account);

		DataTable boardList = Board.getBoardHeaders(requestSession);
		for (int i = 0; i < boardList.getSize(); ++i)
		{
			int boardId = (Integer) boardList.getData("Id", i);
			String boardName = (String) boardList.getData("Name", i);
			String moderator = (String) boardList.getData("Moderator", i);
			int countPosts = (Integer) boardList.getData("CountPosts", i);
			dashboardPage.addBoard(boardId, boardName, moderator, countPosts);
		}
		
		return responsePage;
    }
}
