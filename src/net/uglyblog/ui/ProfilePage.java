package net.uglyblog.ui;


public class ProfilePage extends UserPage
{
	ProfilePage()
	{
		super();
		loadContent("profile page");
	}
	
	
	@Override
	protected void initStubs()
	{
		super.initStubs();
		stubs.add("account");
		stubs.add("nickname");
		stubs.add("message");
	}
	
	
	public void showAccount(String account)
	{
		replaceStub(
				"account",
				"<tr><td>Account:</td><td><input type=\"text\" name=\"account\" value=\"" + account + "\" required disabled></td></tr>");
	}
	
	
	public void showNickname(String nickname)
	{
		replaceStub(
				"nickname",
				"<tr><td>Nickname:</td><td><input type=\"text\" name=\"nickname\" value=\"" + nickname + "\" required></td></tr>");
	}
}
