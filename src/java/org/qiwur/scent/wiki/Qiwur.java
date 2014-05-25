package org.qiwur.scent.wiki;

import java.io.IOException;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Qiwur {

	static final Logger logger = LogManager.getLogger(Qiwur.class);

	public static final String Username = "galaxyeyebot";
	public static final String Password = "432916^galaxyeyebot^qiwur";
	public static final int Throttle = 5;

	Wiki wiki = new Wiki("www.qiwur.com");

	private static Qiwur instance = null;
	private static boolean alreadLogin = false;

	public static Qiwur create() {
		if (instance == null) {
			instance = new Qiwur();
			instance.login();

			alreadLogin = true;
		}

		return instance;
	}

  private Qiwur() {
  }

	public void login() {
		if (alreadLogin) return;

		forceLogin();
	}

	public void forceLogin() {
		try {
			wiki.login(Username, Password);
		} catch (FailedLoginException | IOException e) {
			logger.error(e);
		}

		wiki.setThrottle(Throttle);
		wiki.setUsingCompressedRequests(false);		
	}

	public void logout() {
		wiki.logout();
	}

	public void edit(String title, String text, String summery) throws IOException, LoginException {
		wiki.edit(title, text, summery);
	}
}
