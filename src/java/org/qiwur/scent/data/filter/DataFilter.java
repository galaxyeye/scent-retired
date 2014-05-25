package org.qiwur.scent.data.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface DataFilter {

  static final Logger logger = LogManager.getLogger(DataFilter.class);

	public String filter(String text);
}
