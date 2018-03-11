package net.uglyblog.ui;

import java.sql.Date;
import java.sql.Time;


public class BoardPage extends UserPage
{
	BoardPage()
	{
		super();
		loadContent("board page");
	}
	
	
	@Override
	protected void initStubs()
	{
		super.initStubs();
		stubs.add("moderator");
		stubs.add("post");
		stubs.add("board ID");
	}
	
	
	public void showModerator(String moderator)
	{
		replaceStub("moderator", "<h2>Moderator: " + moderator + "</h2>");
	}
	
	
	public void addPost(int boardId, int postId, String title, String owner, Date createDate, Time createTime, Date updateDate, Time updateTime, boolean enableDelete)
	{
		String postView = "<tr>" +
				"<td><a href=\"/UglyBlog/Post?id=" + postId + "&board-id=" + boardId + "\">" + title + "</a></td>" +
				"<td>Owner: " + owner + "</td>" +
				"<td>Created at: " + createDate + " " + createTime + "</td>" +
				"<td>Updated at: " + updateDate + " " + updateTime + "</td>";
		if (enableDelete)
		{
			replaceStub("post",
					postView +
					"<td>" +
						"<form action=\"Post\" method=\"post\" autocomplete=\"off\">" +
							"<input type=\"submit\" name=\"method\" value=\"Delete\">" +
							"<input type=\"hidden\" name=\"id\" value=\"" + postId + "\">" +
						"</form>" +
					"</td>" +
					"</tr>\n");
		}
		else
			replaceStub("post", postView + "</tr>\n");
	}
	
	
	public void setBoardId(int id)
	{
		replaceStub("board ID",
				"<tr>" +
				"<td></td>" +
				"<td><input type=\"hidden\" name=\"board-id\" value=\"" + id + "\" required></td>" +
				"</tr>");
	}
}
