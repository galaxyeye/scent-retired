package org.qiwur.scent.data.wiki;

import java.io.IOException;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.ObjectCache;

public class Qiwur {

	static final Logger logger = LogManager.getLogger(Qiwur.class);

	public static final int Throttle = 5;

  private final Configuration conf;
  private final String domain;
  private final String username;
  private final String password;
  private final Wiki wiki;

  private boolean alreadyLogin = false;

	public static Qiwur create(Configuration conf) {
    ObjectCache objectCache = ObjectCache.get(conf);
    final String cacheId = Qiwur.class.getName();

    if (objectCache.getObject(cacheId) != null) {
      return (Qiwur) objectCache.getObject(cacheId);
    }
    else {
      Qiwur qiwur = new Qiwur(conf);
      qiwur.login();
      objectCache.setObject(cacheId, qiwur);
      return qiwur;
    }
	}

  private Qiwur(Configuration conf) {
    this.conf = conf;

    this.domain = this.conf.get("scent.wiki.domain");
    this.username = this.conf.get("scent.wiki.username");
    this.password = this.conf.get("scent.wiki.password");

    logger.debug(domain);
    logger.debug(username);
    logger.debug(password);
    
    this.wiki = new Wiki(domain);
  }

	public void login() {
		if (!alreadyLogin) {
		  forceLogin();
		}
	}

	public void forceLogin() {
		try {
			wiki.login(username, password);
	    wiki.setThrottle(Throttle);
	    wiki.setUsingCompressedRequests(false);

      alreadyLogin = true;
		} catch (FailedLoginException | IOException e) {
			logger.error(e);
		}
	}

	public void logout() {
		wiki.logout();
    alreadyLogin = false;
	}

	public void edit(String title, String text, String summery) throws IOException, LoginException {
	  login();
		wiki.edit(title, text, summery);
	}
}
