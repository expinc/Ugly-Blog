package net.uglyblog.ui;


public class RegisterPage extends Page
{
	RegisterPage()
	{
		super();
		loadContent("register page");
	}

	
	@Override
	protected void initStubs()
	{
		stubs.add("register fail message");
	}
	
	
	public void showFailMessage()
	{
		replaceStub(
				"register fail message",
				"<p style=\"color:red\">" +
						"Register failed!\n" +
						"Maybe the account already exists or the format is invalid.\n" +
						"Account name format: 3 ~ 32 characters of letters, digits or underscore.\n" +
						"Password format: 4 ~ 32 characters of letters, digits or underscore.\n" +
						"</p>");
	}
}
