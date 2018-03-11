package net.uglyblog.ui;

import net.uglyblog.servlets.BaseServlet.MessageType;

public class UserPage extends Page
{
	UserPage()
	{
		super();
	}
	
	
	@Override
	protected void initStubs()
	{
		super.initStubs();
		stubs.add("header");
		stubs.add("hello");
		stubs.add("profile");
		stubs.add("dashboard");
		stubs.add("logout");
		stubs.add("message");
	}
	
	
	public void showPageHeader(String info)
	{
		String header = "<h1>Ugly Blog";
		if (null == info || info.isEmpty())
			header += "</h1>";
		else
			header += " - " + info + "</h1>";
		replaceStub("header", header);
	}
	
	
	public void showHelloMessage(String account)
	{
		replaceStub("hello", "Hello " + account + "!");
	}
	
	
	public void showProfileLink()
	{
		replaceStub("profile",
				"<td><a href=\"/UglyBlog/Profile\">My Profile</a></td>");
	}
	
	
	public void showDashboardLink()
	{
		replaceStub("dashboard",
				"<td><a href=\"/UglyBlog/Dashboard\">Dashboard</a></td>");
	}
	
	
	public void showLogoutButton(String account)
	{
		replaceStub("logout",
				"<td>\n" +
				"<form action=\"Logout\" method=\"post\" autocomplete=\"off\">\n" +
				"<input type=\"submit\" value=\"Logout\">\n" +
				"<input type=\"hidden\" name=\"account\" value=\"" + account + "\">\n" +
				"</form>" +
				"</td>\n");
	}
	
	
	public void showMessage(String message, MessageType type)
	{
		if (null != message)
		{
			String content = null;
			switch (type)
			{
				case INFO:
					content = "<h3>" + message + "</h3>";
					break;
				case WARNING:
					content = "<h3 style=\"color:yellow\">" + message + "</h3>";
					break;
				case ERROR:
					content = "<h3 style=\"color:red\">" + message + "</h3>";
					break;
				default:
					break;
			}
			replaceStub("message", content);
		}
	}
}
