package org.qiwur.scent.classifier.statistics;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.qiwur.scent.jsoup.nodes.Element;

import com.google.common.collect.Sets;

public class BlockRule implements Comparable<BlockRule> {

  private final String label;

  private Set<StatRule> rules = Sets.newHashSet();

  public BlockRule(String label) {
    Validate.notNull(label);

    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public Set<StatRule> getRules() {
    return rules;
  }

  public double getScore(Element ele) {
    double score = 0.0;

    for (StatRule rule : rules) {
      score += rule.getScore(ele);
    }

    return score;
  }

  public boolean addRule(StatRule rule) {
    return rules.add(rule);
  }

  /**
   * Be careful that "global" means be global for each page, NOT for the whole application
   * */
  public void setGlobalVar(String name, String value) {
    for (StatRule rule : rules) {
      rule.var(name, value);
    }
  }

  @Override
  public String toString() {
    return rules.toString();
  }

  @Override
  public int compareTo(BlockRule other) {
    return label.compareTo(other.label);
  }
}
