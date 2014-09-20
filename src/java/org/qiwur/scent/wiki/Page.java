package org.qiwur.scent.wiki;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Page implements Configurable {

	public static final Logger logger = LogManager.getLogger(Page.class);

	public static final String ProductPage = "ProductPage";
	public static final String TradePage = "TradePage";
	public static final String DetailPage = "DetailPage";

  private Configuration conf;
  private String title = null;
  private String text = null;
  private String summery = null;

	public Page() {
	}

	public Page(String title, String text) {
		this.title = title;
		this.text = text;
	}

	public Page(String title, String text, String summery) {
		this.title = title;
		this.text = text;
		this.summery = summery;
	}

	public void upload() {
		try {
			if (StringUtils.isNotEmpty(title) && StringUtils.isNotEmpty(text)) {
				if (summery == null)
					summery = title;

				Qiwur q = Qiwur.create(conf);
				q.edit(normarizeTitle(title), text, summery);
			} else {
				logger.info("invalid page title or text");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public String summery() {
		return summery;
	}

	public void summery(String summery) {
		this.summery = summery;
	}

	public String title() {
		return title;
	}

	public void title(String title) {
		this.title = title;
	}

	public String text() {
		return text;
	}

	public void text(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("title : ");
		sb.append(title);
		sb.append("\n");
		sb.append("text : ");
		sb.append("\n");
		sb.append(text);

		return sb.toString();
	}

	private String normarizeTitle(String title) {
		return title;
	}

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }
}
