package org.qiwur.scent.feature;

import org.apache.hadoop.conf.Configurable;

public interface WebFeature extends Configurable {
  public void load();
  public void reload();
  public void reset();
}
