package net.uglyblog.ui;


public final class PageFactory
{
	public enum PageType {INDEX, DASHBOARD, REGISTER, SYSTEM_CRASH, NO_SESSION, SYSTEM_BUSY, BOARD, PROFILE, POST}
	
	
	public static Page createPage(PageType pageType)
	{
		Page result;
		switch (pageType)
		{
			case INDEX:
				result = new IndexPage();
				break;
			case DASHBOARD:
				result = new DashboardPage();
				break;
			case REGISTER:
				result = new RegisterPage();
				break;
			case SYSTEM_CRASH:
				result = new SystemCrashPage();
				break;
			case NO_SESSION:
				result = new NoSessionPage();
				break;
			case SYSTEM_BUSY:
				result = new SystemBusyPage();
				break;
			case BOARD:
				result = new BoardPage();
				break;
			case PROFILE:
				result = new ProfilePage();
				break;
			case POST:
				result = new PostPage();
				break;
			default:
				result = null;
				break;
		}

		return result;
	}
}
