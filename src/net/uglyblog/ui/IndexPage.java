package net.uglyblog.ui;


public class IndexPage extends Page
{
	IndexPage()
	{
		super();
		loadContent("index page");
	}
	
	
	@Override
	protected void initStubs()
	{
		stubs.add("login fail message");
	}
	
	
	public void showFailMessage()
	{
		replaceStub(
				"login fail message",
				"<p style=\"color:red\">Wrong account or password!</p>");
	}
}
