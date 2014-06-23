package org.qiwur.scent.classifier;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.block.BlockPattern;
import org.qiwur.scent.jsoup.nodes.Element;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BlockRule implements Comparable<BlockRule> {

  protected static final Logger logger = LogManager.getLogger(BlockRule.class);

  private final String id;
  private final String label;
  private final boolean leafOnly;
  private final String domain;
  private final Set<ScentRule> rules = Sets.newHashSet();
  private final List<BlockPattern> allowedPatterns = Lists.newArrayList();
  private final List<BlockPattern> disallowedPatterns = Lists.newArrayList();
  
  public BlockRule(String id, String label, boolean leafOnly, String domain) {
    Validate.notNull(label);

    this.id = id;
    this.label = label;
    this.leafOnly = leafOnly;
    this.domain = domain;
  }

  public BlockRule(String id, String label, boolean leafOnly, String domain, List<BlockPattern> allowedPatterns, List<BlockPattern> disallowedPatterns) {
    this(id, label, leafOnly, domain);

    this.allowedPatterns.addAll(allowedPatterns);
    this.disallowedPatterns.addAll(disallowedPatterns);
  }

  public String getLabel() {
    return label;
  }

  public boolean leafOnly() {
    return this.leafOnly;
  }

  public Set<ScentRule> getRules() {
    return rules;
  }

  public double getScore(Element ele, Collection<BlockPattern> patterns) {
    // domain restriction
    if (!domain.isEmpty() && !ele.baseUri().contains(domain)) {
      return 0.0;
    }

    double score = getScore(ele);

    if (score > 0 && CollectionUtils.containsAny(disallowedPatterns, patterns)) {
      score = -100.0;
    }

    if (score > 0 && !allowedPatterns.isEmpty() && !CollectionUtils.containsAny(allowedPatterns, patterns)) {
      score = -100.0;
    }

    return score;
  }

  protected double getScore(Element ele) {
    double score = 0.0;

    // TODO : optimization
    for (ScentRule rule : rules) {
      double s = rule.getScore(ele);
      score += s;

      if (s <= -100) {
        ele.addAttr("data-break-rule", label + ":" + rule.name(), ",");
        break;
      }
    }

    return score;
  }

  public boolean addRule(ScentRule rule) {
    return rules.add(rule);
  }

  public boolean addRules(Collection<ScentRule> rule) {
    return rules.addAll(rule);
  }

  /**
   * Be careful that "global" means be global for each page, NOT for the whole application
   * */
  public void setGlobalVar(String name, String value) {
    for (ScentRule rule : rules) {
      rule.var(name, value);
    }
  }

  @Override
  public String toString() {
    return rules.toString();
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof BlockRule && id.equals(((BlockRule)other).id);
  }

  @Override
  public int compareTo(BlockRule other) {
    return id.compareTo(other.id);
  }
}
