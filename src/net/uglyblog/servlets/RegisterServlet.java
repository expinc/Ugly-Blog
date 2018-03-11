package net.uglyblog.servlets;

import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import net.uglyblog.bo.Account;
import net.uglyblog.session.RequestSession;
import net.uglyblog.ui.Page;
import net.uglyblog.ui.PageFactory;
import net.uglyblog.ui.PageFactory.PageType;
import net.uglyblog.ui.RegisterPage;


/**
 * Servlet implementation class Register
 */
@WebServlet("/Register")
public class RegisterServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
        super();
    }

	
	@Override
	protected void logPost(HttpServletRequest request, HttpServletResponse response)
	{
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirm-password");
		System.out.println(
				"Register request: account - " + account +
				", password - " + password +
				", confirm password - " + confirmPassword);
	}

	
	@Override
	protected Page handlePost(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
	{
		Page responsePage = null;
		String account = request.getParameter("account");
		String password = request.getParameter("password");
		String confirmPassword = request.getParameter("confirm-password");
		
		Account newAccount = null;
		newAccount = Account.register(requestSession, account, password, confirmPassword);
		if (null != newAccount)
		{
			RequestDispatcher dispatcher = request.getRequestDispatcher("/Login");
			dispatcher.forward(request, response);
		}
		else
		{
			RegisterPage registerPage = (RegisterPage)PageFactory.createPage(PageType.REGISTER);
			registerPage.showFailMessage();
			responsePage = registerPage;
			System.out.println("Register account " + account + " failed.");
		}
		
		return responsePage;
	}

}
