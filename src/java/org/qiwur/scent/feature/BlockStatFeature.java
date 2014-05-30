package org.qiwur.scent.feature;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.classifier.statistics.StatIndicator;
import org.qiwur.scent.classifier.statistics.StatRule;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.parser.Parser;
import org.qiwur.scent.utils.ObjectCache;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class BlockStatFeature {

  protected static final Logger logger = LogManager.getLogger(BlockStatFeature.class);

  public static final String defaultConfigFile = "conf/block-stat-feature-default.xml";

  private List<String> configFiles = new ArrayList<String>();

  private Map<String, String> globalVars = new HashMap<String, String>();

  private Map<String, StatIndicator> indicators = new HashMap<String, StatIndicator>();

  private Multimap<String, StatRule> rules = LinkedListMultimap.create();

  private int maxPlaceholders = 10;

  private BlockStatFeature(String... configFiles) {
    Validate.notNull(configFiles);
    Validate.notEmpty(configFiles);

    this.configFiles.addAll(Arrays.asList(configFiles));
    
    for (String file : this.configFiles) {
      load(file);
    }
  }

  public static BlockStatFeature create(String configFile, Configuration conf) {
    ObjectCache objectCache = ObjectCache.get(conf);
    final String cacheId = configFile;

    if (objectCache.getObject(cacheId) != null) {
      return (BlockStatFeature) objectCache.getObject(cacheId);
    } else {
      BlockStatFeature feature = new BlockStatFeature(defaultConfigFile, configFile);
      objectCache.setObject(cacheId, feature);
      return feature;
    }
  }

  public List<String> configFile() {
    return configFiles;
  }

  public Set<String> getLabels() {
    return rules.keySet();
  }

  public Collection<StatRule> getRules(String label) {
    return rules.get(label);
  }

  public void setGlobalVar(String name, String value) {
    globalVars.put(name, value);

    for (StatRule rule : rules.values()) {
      rule.var(name, value);
    }
  }

  public String getGlobalVar(String name) {
    return globalVars.get(name);
  }

  private void load(String file) {
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

    for (Element eleBlock : doc.select("block-features block")) {
      String type = eleBlock.attr("type");

      for (Element eleRule : eleBlock.getElementsByTag("rule")) {
        StatIndicator indicator = indicators.get(eleRule.attr("indicator"));

        if (indicator == null) {
          logger.error("invalid rule, miss indicator");
          continue;
        }

        StatRule rule = null;

        String[] range = eleRule.attr("range").split(",", 2);
        if (range.length != 2) {
          logger.error("invalid rule range {}", eleRule.attr("range"));
          continue;
        }

        double min = StringUtil.parseDouble(range[0]);
        double max = StringUtil.parseDouble(range[1]);
        if (range[0].trim().isEmpty()) min = Double.MIN_VALUE;
        if (range[1].trim().isEmpty()) max = Double.MAX_VALUE;

        double score = StringUtil.parseDouble(eleRule.attr("score"));
        double nag_score = StringUtil.parseDouble(eleRule.attr("-score"));

        rule = new StatRule(indicator, min, max, score, nag_score);

        if (!indicator.isSimple()) {
          for (int i = 1; i <= maxPlaceholders; ++i) {
            String ref = "_" + i;
            String var = eleRule.attr(ref);
            if (!var.isEmpty()) {
              rule.ref("$" + ref, var);
            }
          }
        }

        rules.put(type, rule);
      } // for
    } // for
  }

  public String dumpGlobalVars() {
    return globalVars.toString();
  }

  @Override
  public String toString() {
    return rules.toString();
  }
}
