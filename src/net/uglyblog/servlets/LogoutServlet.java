package net.uglyblog.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import net.uglyblog.session.LoginSessionManager;
import net.uglyblog.session.RequestSession;
import net.uglyblog.ui.Page;
import net.uglyblog.ui.PageFactory;
import net.uglyblog.ui.PageFactory.PageType;


/**
 * Servlet implementation class LogoutServlet
 */
@WebServlet("/Logout")
public class LogoutServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
       
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogoutServlet() {
        super();
    }
    
    
    @Override
	protected void logPost(HttpServletRequest request, HttpServletResponse response)
	{
		String account = request.getParameter("account");
		System.out.println("Account " + account + " is logging out...");
	}
    
    
    @Override
	protected Page handlePost(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession)
	{
		terminateSession(request);
		Page responsePage = PageFactory.createPage(PageType.INDEX);
		
		return responsePage;
	}
    
    
    protected void terminateSession(HttpServletRequest request)
	{
    	Cookie[] cookies = request.getCookies();
		for (int i = 0; i < cookies.length; ++i)
		{
			if (cookies[i].getName().equals("uglyblog"))
			{
				String cookieId = cookies[i].getValue();
				LoginSessionManager.terminateSession(Integer.parseInt(cookieId));
			}
		}
	}
}
