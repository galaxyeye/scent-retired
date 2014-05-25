package org.qiwur.scent.learning;

import com.google.common.collect.Multiset;

public interface Learner {
	public void learn(Object o);
  public Multiset<String> words();
  public void save();
}
