package net.uglyblog.servlets;

import java.sql.*;

import javax.security.auth.login.LoginException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.uglyblog.bo.Account;
import net.uglyblog.bo.Board;
import net.uglyblog.bo.BusinessObject;
import net.uglyblog.dao.DataTable;
import net.uglyblog.session.RequestSession;
import net.uglyblog.ui.*;
import net.uglyblog.ui.PageFactory.PageType;

/**
 * Servlet implementation class BoardServlet
 */
@WebServlet("/Board")
public class BoardServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
       
	
    /**
     * @see BaseServlet#BaseServlet()
     */
    public BoardServlet() {
        super();
    }
    
    
    @Override
    protected void logGet(HttpServletRequest request, HttpServletResponse response)
    {
    	String id = request.getParameter("id");
    	System.out.println("Getting board #" + id + "...");
    }
    
    
    @Override
    protected Page handleGet(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
	{
    	String account = checkSessionAccount(request);
    	if (null == account)
    		throw new LoginException();
    	
    	Page responsePage = null;
		String id = request.getParameter("id");
		Board board = new Board(requestSession);
		requestSession.startTransaction(Connection.TRANSACTION_REPEATABLE_READ);
		try
		{
			if (true == board.getById(Integer.parseInt(id)))
			{
				DataTable tableBoards = board.getDataTable("BOARDS");
				String boardName = (String)tableBoards.getData("Name");
				System.out.println("Listing posts of board - " + boardName + "...");

				BoardPage boardPage = (BoardPage)PageFactory.createPage(PageType.BOARD);
				boardPage.showPageHeader(boardName);	
				boardPage.showHelloMessage(account);
				boardPage.showProfileLink();
				boardPage.showDashboardLink();
				boardPage.showLogoutButton(account);

				BusinessObject moderator = board.getModerator();
				if (null == moderator)
					throw new Exception("Moderator not found");
				DataTable tableAccounts = moderator.getDataTable("ACCOUNTS");
				String moderatorName = (String)tableAccounts.getData("Nickname");
				boardPage.showModerator(moderatorName);

				BusinessObject currentUser = Account.get(requestSession, account, null);
				if (null == currentUser)
					throw new Exception("Account not exists!");
				int currentUserId = currentUser.getId();
				DataTable postList = board.getPostHeaders();
				for (int i = 0; i < postList.getSize(); ++i)
				{
					Integer postId = (Integer)postList.getData("Id", i);
					String title = (String)postList.getData("Title", i);
					Integer ownerId = (Integer) postList.getData("OwnerId", i);
					String owner = (String)postList.getData("Owner", i);
					Date createDate = (Date)postList.getData("CreateDate", i);
					Time createTime = (Time)postList.getData("CreateTime", i);
					Date updateDate = (Date)postList.getData("UpdateDate", i);
					Time updateTime = (Time)postList.getData("UpdateTime", i);
					boolean enableDelete = (currentUserId == ownerId);
					boardPage.addPost(Integer.parseInt(id), postId, title, owner, createDate, createTime, updateDate, updateTime, enableDelete);
				}
				
				boardPage.setBoardId(Integer.parseInt(id));
				String message = (String) request.getAttribute("message");
				MessageType messageType = (MessageType) request.getAttribute("message-type");
				boardPage.showMessage(message, messageType);
				
				responsePage = boardPage;
			}
			else
				throw new Exception("Board not found");
			requestSession.makeCommit(true);
		}
		finally
		{
			requestSession.endTransaction();
		}
		
		return responsePage;
	}
}
