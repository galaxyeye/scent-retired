package org.qiwur.scent.data.extractor;

import org.apache.hadoop.conf.Configurable;
import org.qiwur.scent.plugin.FieldPluggable;

public interface DataExtractor extends FieldPluggable, Configurable {

  public final static String X_POINT_ID = DataExtractor.class.getName();

  public void process();
}
