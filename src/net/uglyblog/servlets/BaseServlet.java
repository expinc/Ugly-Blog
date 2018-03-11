package net.uglyblog.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import net.uglyblog.session.LoginSessionManager;
import net.uglyblog.session.RequestSession;
import net.uglyblog.ui.Page;
import net.uglyblog.ui.PageFactory;
import net.uglyblog.ui.PageFactory.PageType;


/**
 * Servlet implementation class BaseServlet
 */
@WebServlet("/BaseServlet")
public abstract class BaseServlet extends HttpServlet {
	public enum MessageType {INFO, WARNING, ERROR}
	protected enum RequestType {POST, GET}
	
	private static final long serialVersionUID = 1L;
		
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BaseServlet() {
        super();
    }

    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response, RequestType.GET);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response, RequestType.POST);
	}
	
	
	protected final void handleRequest(HttpServletRequest request, HttpServletResponse response, RequestType requestType) throws ServletException, IOException
	{
		RequestSession requestSession = new RequestSession();
		response.setContentType("text/html");
		RequestType requestTransformType = (RequestType) request.getAttribute("request-transform-type");
		if (null != requestTransformType)
			requestType = requestTransformType;
		
		Page responsePage = null;
		try
		{
			switch (requestType)
			{
				case POST:
					logPost(request, response);
					responsePage = handlePost(request, response, requestSession);
					break;
				case GET:
					logGet(request, response);
					responsePage = handleGet(request, response, requestSession);
					break;
				default:
					break;
			}
		}
		catch (LoginException e)
		{
			responsePage = PageFactory.createPage(PageType.NO_SESSION);
		}
		catch (Exception e)
		{
			responsePage = PageFactory.createPage(PageType.SYSTEM_CRASH);
			e.printStackTrace();
		}
		
		try
		{
			requestSession.dispose();
		}
		catch (SQLException e)
		{
			responsePage = PageFactory.createPage(PageType.SYSTEM_CRASH);
			e.printStackTrace();
		}
		requestSession = null;
		
		if (null != responsePage)
		{
			PrintWriter responseWriter = response.getWriter();
			responseWriter.write(responsePage.getContent());
			responseWriter.flush();
			responseWriter.close();
		}
	}
	
	
	protected void logPost(HttpServletRequest request, HttpServletResponse response) {}
	
	
	protected Page handlePost(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
	{
		return null;
	}
	
	
	protected void logGet(HttpServletRequest request, HttpServletResponse response) {}
	
	
	protected Page handleGet(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
	{
		return null;
	}
	
	
	protected String checkSessionAccount(HttpServletRequest request)
	{
		String account = null;
		Cookie[] cookies = request.getCookies();
		if (null != cookies)
		{
			for (int i = 0; i < cookies.length; ++i)
			{
				if (cookies[i].getName().equals("uglyblog"))
				{
					String cookieId = cookies[i].getValue();
					account = LoginSessionManager.checkSession(Integer.parseInt(cookieId));
				}
			}
		}
		
		return account;
	}
	
	
	protected String getSessionAccount(HttpServletRequest request)
	{
		String account = null;
		Cookie[] cookies = request.getCookies();
		for (int i = 0; i < cookies.length; ++i)
		{
			if (cookies[i].getName().equals("uglyblog"))
			{
				String cookieId = cookies[i].getValue();
				account = LoginSessionManager.getSessionAccount(Integer.parseInt(cookieId));
			}
		}
		
		return account;
	}
}
