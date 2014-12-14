package org.qiwur.scent.classifier;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;

public class ScentRule implements Comparable<ScentRule> {

  protected static final Logger logger = LogManager.getLogger(ScentRule.class);

  public static final double MIN_SCORE = -10000.0;
  public static final double MAX_SCORE = 10000.0;

  private final String name;
  private final double score;

  private Map<String, String> variables = new HashMap<String, String>();

  protected ScentRule(String name, double score) {
    this.name = name;
    this.score = score;
  }

  public String name() {
    return name;
  }

  public double score() {
    return score;
  }

  double getScore(Element ele) {
    return 0.0;
  }

  protected Map<String, String> variables() {
    return variables;
  }

  public void var(String name, String value) {
    variables.put(name, value);
  }

  public String var(String name) {
    return variables.get(name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ScentRule)) {
      return false;
    }

    ScentRule rule = (ScentRule)other;
    return rule.name.equals(name);
  }

  @Override
  public int compareTo(ScentRule other) {
    int r = name.compareTo(other.name);
//    if (r == 0) {
//      double x = score, y = other.score;
//
//      // compare function : 1. negative is smaller, 2. 
//      if (x > 0 && y > 0) return (int)(y - x);
//      else return (int)(x - y);
//    }

    return r;
  }
}
