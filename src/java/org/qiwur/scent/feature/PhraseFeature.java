package org.qiwur.scent.feature;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.parser.Parser;
import org.qiwur.scent.utils.StringUtil;

public class PhraseFeature implements WebFeature {

	final Logger logger = LogManager.getLogger(PhraseFeature.class);

	private final String featureFile;

	private Configuration conf;

	private Map<String, Map<String, Double>> blockPhrases = new HashMap<String, Map<String, Double>>();

	public PhraseFeature(Configuration conf, String... featureFile) {
    this.featureFile = featureFile[0];
    this.conf = conf;

    load();
	}

	public String featureFile() {
		return featureFile;
	}

	public Set<String> getLabels() {
		return blockPhrases.keySet();
	}

	public Map<String, Double> getRules(String label) {
		return blockPhrases.get(label);
	}

	@Override
	public String toString() {
	  return blockPhrases.toString();
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
  public void reset() {
    blockPhrases.clear();
  }

  @Override
  public void load() {
    Validate.notEmpty(featureFile);

    try {
      Document doc = Jsoup.parse(new FileInputStream(featureFile), "utf-8", "", Parser.xmlParser());

      parse(doc);
    } catch (IOException e) {
      logger.error(e);
    }    
  }

  @Override
  public void reload() {
    reset();
    load();
  }

  private void parse(Document doc) {
    for (Element eleBlock : doc.select("block-features block")) {
      String type = eleBlock.attr("type");
      Map<String, Double> phrases = new HashMap<String, Double>();

      for (Element elePhrase : eleBlock.getElementsByTag("phrase")) {
        String name = elePhrase.attr("name");
        String score = elePhrase.attr("score");

        if (!name.isEmpty()) {
          phrases.put(name, StringUtil.parseDouble(score));
        }
      }

      if (!phrases.isEmpty()) {
        blockPhrases.put(type, phrases);
      }
    }
  }  
}
