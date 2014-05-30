package org.qiwur.scent.data.builder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Builder {
  static final Logger logger = LogManager.getLogger(Builder.class);
	abstract public void process();
}
