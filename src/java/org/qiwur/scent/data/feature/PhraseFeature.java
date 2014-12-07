package org.qiwur.scent.data.feature;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.classifier.BlockRule;
import org.qiwur.scent.classifier.PhraseRule;
import org.qiwur.scent.classifier.ScentRule;
import org.qiwur.scent.jsoup.block.BlockPattern;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Element;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public class PhraseFeature implements WebFeature {

	final Logger logger = LogManager.getLogger(PhraseFeature.class);

	private final String[] featureFiles;

	private Configuration conf;

  private Multimap<String, BlockRule> blockRules = TreeMultimap.create();
  private Map<String, Double> phraseRules = null;

	public PhraseFeature(Configuration conf, String... featureFiles) {
    this.featureFiles = featureFiles;
    this.conf = conf;

    try {
      load();
    }
    catch (Exception e) {
      logger.error(e);
    }
	}

	public String[] featureFiles() {
		return featureFiles;
	}

	public Set<String> getLabels() {
		return blockRules.keySet();
	}

  /**
   * all block rules with the same block label are independent
   * the final score is the max score calculated by all rules with the same block
   * */
  public double getScore(DomSegment segment, String label, Collection<BlockPattern> patterns) {
    Collection<BlockRule> rules = getRules(label);
    if (rules.isEmpty()) return 0.0;

    // implements the OR logic : if there are several block rules for one label, 
    // take the one who has max score
    double maxScore = ScentRule.MIN_SCORE;
    Element ele = segment.block();
    for (BlockRule rule : rules) {
      if (rule.leafOnly() && segment.hasChild()) {
        continue;
      }

      double score = rule.getScore(ele, patterns);
      if (score > maxScore) maxScore = score;
    }

    return maxScore;
  }

  public Collection<BlockRule> getRules(String label) {
    return blockRules.get(label);
  }

  // TODO : not correct
  public Map<String, Double> getPhraseRules(String label) {
    if (phraseRules == null) {
      phraseRules = Maps.newHashMap();
      for (BlockRule blockRule : blockRules.get(label)) {
        for (ScentRule rule : blockRule.getRules()) {
          if (rule instanceof PhraseRule) {
            PhraseRule r = (PhraseRule)rule;
            phraseRules.put(r.phrase(), r.score());
          }
        }
      }
    }

    return phraseRules;
  }

	@Override
	public String toString() {
	  return blockRules.toString();
	}

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  @Override
  public void reload() {
    load();
  }

  @Override
  public void reset() {
    this.blockRules.clear();
    if (this.phraseRules != null) {
      this.phraseRules.clear();
    }
  }

  @Override
  public void load() {
    PhraseFeatureParser parser = new PhraseFeatureParser(this.featureFiles);
    this.blockRules = parser.parse();
  }
}
