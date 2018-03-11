package net.uglyblog.servlets;

import java.sql.*;

import javax.security.auth.login.LoginException;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import net.uglyblog.bo.*;
import net.uglyblog.dao.DataTable;
import net.uglyblog.session.RequestSession;
import net.uglyblog.ui.*;
import net.uglyblog.ui.PageFactory.PageType;


/**
 * Servlet implementation class PostServlet
 */
@WebServlet("/Post")
public class PostServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;
	private static final int abstractLength = 8;
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PostServlet() {
        super();
    }

    
    @Override
    protected void logPost(HttpServletRequest request, HttpServletResponse response)
    {
    	String title = request.getParameter("title");
    	String method = request.getParameter("method");
    	if (method.equals("Create"))
    		System.out.println("Creating post \"" + title + "\"...");
    	else if (method.equals("Update"))
    		System.out.println("Updating post \"" + title + "\"...");
    	else if (method.equals("Reply"))
    		System.out.println("Replying post \"" + title + "\"...");
    	else if (method.equals("Delete"))
    		System.out.println("Deleting post \"" + title + "\"...");
    }
	
	
    @Override
	protected Page handlePost(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
	{
    	String account = checkSessionAccount(request);
    	if (null == account)
    		throw new LoginException();
    	
    	String method = request.getParameter("method");
    	if (method.equals("Create"))
    		return handlePostOfCreate(request, response, requestSession);
    	else if (method.equals("Update"))
    		return handlePostOfUpdate(request, response, requestSession);
    	else if (method.equals("Reply"))
    		return handlePostOfReply(request, response, requestSession);
    	else if (method.equals("Delete"))
    		return handlePostOfDelete(request, response, requestSession);
    	else
    		throw new Exception("Invalid method!");
	}
    
    
    @Override
    protected void logGet(HttpServletRequest request, HttpServletResponse response)
    {
    	String id = request.getParameter("id");
    	System.out.println("Getting post #" + id + "...");
    }
	
	
    @Override
	protected Page handleGet(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
	{
    	String account = checkSessionAccount(request);
    	if (null == account)
    		throw new LoginException();
    	
    	Page responsePage = null;
    	int id = Integer.parseInt(request.getParameter("id"));
    	BusinessObject post = new Post(requestSession);
    	requestSession.startTransaction(Connection.TRANSACTION_READ_COMMITTED);
    	try
    	{
    		if (post.getById(id))
    		{
    			DataTable tablePosts = post.getDataTable("POSTS");
    			String title = (String) tablePosts.getData("Title");
    			Date createDate = (Date) tablePosts.getData("CreateDate");
    			Time createTime = (Time) tablePosts.getData("CreateTime");
    			Date updateDate = (Date) tablePosts.getData("UpdateDate");
    			Time updateTime = (Time) tablePosts.getData("UpdateTime");
    			String content = (String) tablePosts.getData("Content");
    			
    			Integer ownerId = (Integer) tablePosts.getData("OwnerId");
    			BusinessObject owner = new Account(requestSession);
    			if (!owner.getById(ownerId))
    				throw new Exception("Owner not exists!");
    			else
    				requestSession.makeCommit(true);
    			DataTable tableAccounts = owner.getDataTable("ACCOUNTS");
    			String ownerAccount = (String) tableAccounts.getData("AccountName");
    			boolean enableEdit = (account.equals(ownerAccount));
    			String ownerName = (String) tableAccounts.getData("Nickname");
    			
    			PostPage postPage = (PostPage) PageFactory.createPage(PageType.POST);
    			postPage.showPageHeader(title);	
    			postPage.showHelloMessage(account);
    			postPage.showProfileLink();
    			postPage.showDashboardLink();
    			postPage.showLogoutButton(account);
    			
    			Integer boardId = (Integer) tablePosts.getData("BoardId");
    			BusinessObject board = new Board(requestSession);
    			if (!board.getById(boardId))
    				throw new Exception("Board not exists!");
    			DataTable tableBoards = board.getDataTable("BOARDS");
    			String boardName = (String) tableBoards.getData("Name");
    			postPage.showBackToBoard(boardId, boardName);
    			postPage.showPostHeader(boardId, enableEdit, title, ownerName, createDate, createTime, updateDate, updateTime);
    			postPage.showPostContent(enableEdit, content);
    			postPage.setPostId(id);
    			if (enableEdit)
    				postPage.showUpdateButton();
    			
    			DataTable tableReplies = post.getDataTable("REPLIES");
    			showReplies(postPage, tableReplies, requestSession);
    			
    			postPage.setPostInfoForReply(id, title);
    			
    			String message = (String) request.getAttribute("message");
				MessageType messageType = (MessageType) request.getAttribute("message-type");
				postPage.showMessage(message, messageType);
				
    			responsePage = postPage;
    		}
    		else
    			throw new Exception("Post not exists!");
    	}
    	finally
    	{
    		requestSession.endTransaction();
    	}
    	
		return responsePage;
	}
    
    
    private Page handlePostOfCreate(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
    {
    	Page responsePage = null;
    	int boardId = Integer.parseInt(request.getParameter("board-id"));
    	BusinessObject post = new Post(requestSession);
    	requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
    	try
    	{    		
    		String account = getSessionAccount(request);
    		BusinessObject accountObject = Account.get(requestSession, account, null);
    		if (null == accountObject)
    			throw new Exception("Account not exists!");
    		DataTable tableAccounts = accountObject.getDataTable("ACCOUNTS");
    		Integer accountId = (Integer) tableAccounts.getData("Id");
    		
    		String title = request.getParameter("title");
    		String content = request.getParameter("content");
    		
    		DataTable tablePosts = post.getDataTable("POSTS");
    		tablePosts.append();
    		tablePosts.setData(accountId, "OwnerId");
    		tablePosts.setData(boardId, "BoardId");
    		tablePosts.setData(title, "Title");
    		tablePosts.setData(content, "Content");
    		
    		int newId = post.create(true);
    		if (0 < newId)
    		{
    			requestSession.makeCommit(true);
    			request.setAttribute("message", "Post \"" + title + "\" has been created.");
    			request.setAttribute("message-type", MessageType.INFO);
    		}
    		else
    		{
    			request.setAttribute("message", "Post \"" + title + "\" has failed to create.");
    			request.setAttribute("message-type", MessageType.ERROR);
    		}
    	}
    	finally
    	{
    		requestSession.endTransaction();
    	}
    		
    	request.setAttribute("request-transform-type", RequestType.GET);
    	RequestDispatcher dispatcher = request.getRequestDispatcher("/Board?id=" + boardId);
    	dispatcher.forward(request, response);
    	
    	
		return responsePage;
    }
    
    
    private Page handlePostOfUpdate(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
    {    	
    	Page responsePage = null;
    	String idStr = request.getParameter("id");
    	int id = Integer.parseInt(idStr);
    	BusinessObject post = new Post(requestSession);
    	requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
    	try
    	{
    		if (!post.getById(id))
    			throw new Exception("Post not exists!");
    		
    		String title = request.getParameter("title");
    		String content = request.getParameter("content");
    		DataTable tablePosts = post.getDataTable("POSTS");
    		tablePosts.setData(title, "Title");
    		tablePosts.setData(content, "Content");
    		long currentTimeMillis = System.currentTimeMillis();
    		java.sql.Date currentDate = new java.sql.Date(currentTimeMillis);
    		java.sql.Time currentTime = new java.sql.Time(currentTimeMillis);
    		tablePosts.setData(currentDate, "UpdateDate");
    		tablePosts.setData(currentTime, "UpdateTime");
    		
    		if (post.update(false))
    		{
    			requestSession.makeCommit(true);
    			request.setAttribute("message", "Post \"" + title + "\" has been updated.");
    			request.setAttribute("message-type", MessageType.INFO);
    		}
    		else
    		{
    			request.setAttribute("message", "Post \"" + title + "\" has failed to update.");
    			request.setAttribute("message-type", MessageType.ERROR);
    		}
    	}
    	finally
    	{
    		requestSession.endTransaction();
    	}
    	
    	request.setAttribute("request-transform-type", RequestType.GET);
    	RequestDispatcher dispatcher = request.getRequestDispatcher("/Post?id=" + id);
    	dispatcher.forward(request, response);
    	
    	return responsePage;
    }
    
    
    private Page handlePostOfReply(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
    {
    	Page responsePage = null;
    	String idStr = request.getParameter("post-id");
    	int id = Integer.parseInt(idStr);
    	BusinessObject post = new Post(requestSession);
    	requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
    	try
    	{
    		if (!post.getById(id))
    			throw new Exception("Post not exists!");

    		String content = request.getParameter("content");
    		String account = getSessionAccount(request);
    		BusinessObject owner = Account.get(requestSession, account, null);
    		if (null == owner)
    			throw new Exception("Account not exists!");
    		int ownerId = owner.getId();
    		
    		DataTable tableReplies = post.getDataTable("REPLIES");
    		tableReplies.append();
    		int lastIndex = tableReplies.getSize() - 1;
    		tableReplies.setData(ownerId, "OwnerId", lastIndex);
    		tableReplies.setData(content, "Content", lastIndex);

    		String contentAbstract = null;
    		if (content.length() > abstractLength)
    			contentAbstract = content.substring(0, abstractLength - 1) + "...";
    		else
    			contentAbstract = content;
    		
    		if (post.update(true))
    		{
    			requestSession.makeCommit(true);
    			request.setAttribute("message", "Reply \"" + contentAbstract + "\" has been created.");
    			request.setAttribute("message-type", MessageType.INFO);
    		}
    		else
    		{
    			request.setAttribute("message", "Reply \"" + contentAbstract + "\" has failed to create.");
    			request.setAttribute("message-type", MessageType.ERROR);
    		}
    	}
    	finally
    	{
    		requestSession.endTransaction();
    	}

    	request.setAttribute("request-transform-type", RequestType.GET);
    	RequestDispatcher dispatcher = request.getRequestDispatcher("/Post?id=" + id);
    	dispatcher.forward(request, response);

    	return responsePage;
    }
    
    
    private Page handlePostOfDelete(HttpServletRequest request, HttpServletResponse response, RequestSession requestSession) throws Exception
    {
    	Page responsePage = null;
    	String idStr = request.getParameter("id");
    	int id = Integer.parseInt(idStr);
    	BusinessObject post = new Post(requestSession);
    	Integer boardId = null;
    	requestSession.startTransaction(Connection.TRANSACTION_SERIALIZABLE);
    	try
    	{
    		if (!post.getById(id))
    			throw new Exception("Post not exists!");
    		
    		DataTable tablePosts = post.getDataTable("POSTS");
    		String title = (String) tablePosts.getData("Title");
    		boardId = (Integer) tablePosts.getData("BoardId");
    		if (post.delete())
    		{
    			requestSession.makeCommit(true);
    			request.setAttribute("message", "Post \"" + title + "\" has been deleted.");
    			request.setAttribute("message-type", MessageType.INFO);
    		}
    		else
    		{
    			request.setAttribute("message", "Post \"" + title + "\" has failed to delete.");
    			request.setAttribute("message-type", MessageType.ERROR);
    		}
    	}
    	finally
    	{
    		requestSession.endTransaction();
    	}
    	
    	request.setAttribute("request-transform-type", RequestType.GET);
    	RequestDispatcher dispatcher = request.getRequestDispatcher("/Board?id=" + boardId);
    	dispatcher.forward(request, response);
    	
    	return responsePage;
    }
    
    
    private void showReplies(PostPage page, DataTable tableReplies, RequestSession requestSession) throws Exception
    {
    	int countRecords = tableReplies.getSize();
    	for (int i = 0; i < countRecords; ++i)
    	{
    		Integer ownerId = (Integer) tableReplies.getData("OwnerId", i);
    		BusinessObject owner = new Account(requestSession);
    		if (!owner.getById(ownerId))
    			throw new Exception("Account not exists!");
    		DataTable tableAccounts = owner.getDataTable("ACCOUNTS");
    		String ownerName = (String) tableAccounts.getData("Nickname");
    		
    		Date createDate = (Date) tableReplies.getData("CreateDate", i);
    		Time createTime = (Time) tableReplies.getData("CreateTime", i);
    		Date updateDate = (Date) tableReplies.getData("UpdateDate", i);
    		Time updateTime = (Time) tableReplies.getData("UpdateTime", i);
    		String content = (String) tableReplies.getData("Content", i);
    		
    		page.addReply(ownerName, createDate, createTime, updateDate, updateTime, content);
    	}
    }
}
