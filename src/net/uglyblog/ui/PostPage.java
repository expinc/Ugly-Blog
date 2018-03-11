package net.uglyblog.ui;

import java.sql.Date;
import java.sql.Time;


public class PostPage extends UserPage
{
	PostPage()
	{
		super();
		loadContent("post page");
	}

	
	@Override
	protected void initStubs()
	{
		super.initStubs();
		stubs.add("back to board");
		stubs.add("post header");
		stubs.add("post content");
		stubs.add("post id");
		stubs.add("update post");
		stubs.add("reply");
		stubs.add("post id for reply");
	}
	
	
	public void showBackToBoard(int boardId, String boardName)
	{
		replaceStub("back to board",
				"<a href=\"/UglyBlog/Board?id=" + boardId + "\">Back to " + boardName + "</a>");
	}
	
	
	public void showPostHeader(int boardId, boolean enableEdit, String title, String owner, Date createDate, Time createTime, Date updateDate, Time updateTime)
	{
		if (enableEdit)
		{
			replaceStub("post header",
				"<tr hidden><td><input type=\"hidden\" name=\"board-id\" value=\"" + boardId + "\" required></td></tr>" +
				"<tr><td>Title</td><td><input type=\"text\" size=\"40\" maxlength=\"32\" name=\"title\" value=\"" + title + "\" required></td></tr>\n" +
				"<tr><td>Owner</td><td>" + owner + "</td></tr>\n" +
				"<tr><td>Created at</td><td>" + createDate + " " + createTime + "</td></tr>\n" +
				"<tr><td>Updated at</td><td>" + updateDate + " " + updateTime + "</td></tr>\n");
		}
		else
		{
			replaceStub("post header",
					"<tr><td>Title</td><td><input type=\"text\" size=\"40\" maxlength=\"32\" name=\"title\" value=\"" + title + "\" required disabled></td></tr>\n" +
					"<tr><td>Owner</td><td>" + owner + "</td></tr>\n" +
					"<tr><td>Created at</td><td>" + createDate + " " + createTime + "</td></tr>\n" +
					"<tr><td>Updated at</td><td>" + updateDate + " " + updateTime + "</td></tr>\n");
		}
	}
	
	
	public void showPostContent(boolean enableEdit, String content)
	{
		if (enableEdit)
		{
			replaceStub("post content",
				"<tr><td><textarea cols=\"32\" rows = \"16\" maxlength=\"1024\" name=\"content\">" + content + "</textarea></tr></td>");
		}
		else
		{
			replaceStub("post content",
					"<tr><td><textarea cols=\"32\" rows = \"16\" maxlength=\"1024\" name=\"content\" disabled>" + content + "</textarea></td></tr>");
		}
	}
	
	
	public void setPostId(int id)
	{
		replaceStub("post id",
				"<input type=\"hidden\" name=\"id\" value=\"" + id + "\">");
	}
	
	
	public void showUpdateButton()
	{
		replaceStub("update post",
				"<input type=\"submit\" name=\"method\" value=\"Update\">");
	}
	
	
	public void addReply(String ownerName, Date createDate, Time createTime, Date updateDate, Time updateTime, String content)
	{
		replaceStub("reply",
			"<table border=\"1\">\n" +
					"<tr><td>Owner: " + ownerName + "</td></tr>\n" +
					"<tr><td>Created at: " + createDate + " " + createTime + "</td></tr>\n" +
					"<tr><td>Updated at: " + updateDate + " " + updateTime + "</td></tr>\n" +
			"</table>\n" +
			"<table border=\"1\">\n" +
				"<tr><td>Content:</td></tr>\n" +
				"<tr><td><textarea cols=\"32\" rows = \"8\" maxlength=\"1024\" name=\"content\" disabled>" + content + "</textarea></td></tr>\n" +
			"</table>\n" +
			"<h4>-------------------------------------------------------------------------</h4>\n");
	}
	
	
	public void setPostInfoForReply(int postId, String title)
	{
		replaceStub("post id for reply",
			"<input type=\"hidden\" name=\"post-id\" value=\"" + postId + "\" required>" +
			"<input type=\"hidden\" name=\"title\" value=\"" + title + "\">");
	}
}
