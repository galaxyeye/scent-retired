package org.qiwur.scent.feature;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.classifier.BlockRule;
import org.qiwur.scent.classifier.ScentRule;
import org.qiwur.scent.classifier.StatRule;
import org.qiwur.scent.classifier.statistics.StatIndicator;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.block.BlockPattern;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.parser.Parser;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public final class BlockStatFeature implements WebFeature {

  protected static final Logger logger = LogManager.getLogger(BlockStatFeature.class);

  public static final String defaultConfigFile = "conf/block-stat-feature-default.xml";

  private Configuration conf;

  private final List<String> featureFiles = Lists.newArrayList();

  private Map<String, String> globalVars = Maps.newHashMap();

  private Map<String, StatIndicator> indicators = Maps.newHashMap();

  private Multimap<String, BlockRule> blockRules = LinkedListMultimap.create();

  private int maxPlaceholders = 10;

  public BlockStatFeature(Configuration conf, String... featureFiles) {
    this.featureFiles.add(defaultConfigFile); // must come first
    this.featureFiles.addAll(Arrays.asList(featureFiles));

    this.conf = conf;
    load();
  }

  public List<String> configFile() {
    return featureFiles;
  }

  public Set<String> getLabels() {
    return blockRules.keySet();
  }

  /**
   * all block rules with the same block label are independent
   * the final score is the max score calculated by all rules with the same block
   * */
  public double getScore(Element ele, String label, Collection<BlockPattern> patterns) {
    Collection<BlockRule> rules = getRules(label);
    if (rules.isEmpty()) return 0.0;

    // implements the OR logic : if there are several block rules for one label, 
    // take the one who has max score
    double maxScore = ScentRule.MIN_SCORE;
    for (BlockRule rule : rules) {
      double score = rule.getScore(ele, patterns);
      if (score > maxScore) maxScore = score;
    }

    return maxScore;
  }

  /**
   * Be careful that "global" means be global for each page, NOT for the whole application
   * */
  public void setGlobalVar(String name, String value) {
    globalVars.put(name, value);

    for (BlockRule rule : blockRules.values()) {
      rule.setGlobalVar(name, value);
    }
  }

  public String getGlobalVar(String name) {
    return globalVars.get(name);
  }

  @Override
  public void reset() {
    globalVars.clear();
    indicators.clear();
    blockRules.clear();
  }

  @Override
  public void load() {
    for (String file : this.featureFiles) {
      load(file);
    }
  }

  @Override
  public void reload() {
    reset();
    load();
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  protected Collection<BlockRule> getRules(String label) {
    return blockRules.get(label);
  }

  protected void load(String file) {
    try {
      Document doc = Jsoup.parse(new FileInputStream(file), "utf-8", "", Parser.xmlParser());

      parse(doc);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private void parse(Document doc) {
    for (Element ele : doc.select("block-features indicators indicator")) {
      String name = ele.attr("name");
      indicators.put(name, new StatIndicator(name, ele.text()));
    }

    for (Element block : doc.select("block-features block")) {
      if (StringUtils.isEmpty(block.attr("label"))) continue;

      BlockRule blockRule = parseBlockRule(block);
      blockRules.put(blockRule.getLabel(), blockRule);
    }
  }

  private String getBlockRuleId(Element eleBlock) {
    String label = eleBlock.attr("label");
    return eleBlock.baseUri() + "/" + label + "#" + eleBlock.attr("id");
  }

  private BlockRule parseBlockRule(Element block) {
    String label = block.attr("label");
    String id = getBlockRuleId(block);
    String leafOnly = block.attr("leaf-only");
    String domain = block.attr("domain");
    String allowedPatterns = block.attr("patterns");
    String disallowedPatterns = block.attr("-patterns");
    BlockRule blockRule = new BlockRule(id, label, leafOnly.equals("true"), domain,
        parsePatterns(allowedPatterns), parsePatterns(disallowedPatterns));

    for (Element eleRule : block.getElementsByTag("rule")) {
      StatIndicator indicator = indicators.get(eleRule.attr("indicator"));

      if (indicator == null) {
        logger.error("invalid rule, miss indicator {}", eleRule.attr("indicator"));
        continue;
      }

      String[] range = eleRule.attr("range").split(",", 2);
      if (range.length != 2) {
        logger.error("invalid rule range {}", eleRule.attr("range"));
        continue;
      }

      Double min = StringUtil.parseDouble(range[0]);
      Double max = StringUtil.parseDouble(range[1]);
      if (range[0].trim().isEmpty()) min = StatRule.MIN_SCORE;
      if (range[1].trim().isEmpty()) max = StatRule.MAX_SCORE;

      double score = StringUtil.parseDouble(eleRule.attr("score"));
      double nag_score = StringUtil.parseDouble(eleRule.attr("-score"));

      StatRule rule = new StatRule(indicator, min, max, score, nag_score);

      // add reference : _1, _2, _3, ...
      if (!indicator.isSimple()) {
        for (int i = 1; i <= maxPlaceholders; ++i) {
          String ref = "_" + i;
          String var = eleRule.attr(ref);
          if (!var.isEmpty()) {
            rule.ref("$" + ref, var);
          }
        }
      }

      blockRule.addRule(rule);
    }

    return blockRule;
  }

  private List<BlockPattern> parsePatterns(String text) {
    List<BlockPattern> patterns = Lists.newArrayList();

    for (String pattern : text.split(",")) {
      BlockPattern p = BlockPattern.fromString(pattern);
      if (BlockPattern.patterns.contains(p)) {
        patterns.add(p);
      }
    }

    return patterns;
  }

  public String dumpGlobalVars() {
    return globalVars.toString();
  }

  @Override
  public String toString() {
    return blockRules.toString();
  }
}
