package net.uglyblog.ui;


public class DashboardPage extends UserPage
{
	DashboardPage()
	{
		super();
		loadContent("dashboard page");
	}
	
	
	@Override
	protected void initStubs()
	{
		super.initStubs();
		stubs.add("boards");
	}
	
	
	public void addBoard(int id, String name, String moderator, int countPosts)
	{
		replaceStub("boards",
				"<tr>\n" +
				"<td><a href=\"/UglyBlog/Board?id=" + id + "\">" + name + "</a></td>\n" +
				"<td>Moderator: " + moderator + "</td>\n" +
				"<td>Posts: " + countPosts + "</td>\n" +
				"</tr>\n");
	}
}
