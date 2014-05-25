package org.qiwur.scent.feature;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.parser.Parser;
import org.qiwur.scent.utils.StringUtil;

public class PhraseFeature {

	final Logger logger = LogManager.getLogger(PhraseFeature.class);

	private String configFile = null;

	protected Map<String, Map<String, Double>> blockPhrases = new HashMap<String, Map<String, Double>>();

	PhraseFeature() {}

	PhraseFeature(String file) {
    configFile = file;

    try {
      Document doc = Jsoup.parse(new FileInputStream(configFile), "utf-8", "", Parser.xmlParser());

      parse(doc);
    } catch (IOException e) {
      logger.error(e);
    }
	}

	public String configFile() {
		return configFile;
	}

	public Set<String> getLabels() {
		return blockPhrases.keySet();
	}

	public Map<String, Double> getRules(String label) {
		return blockPhrases.get(label);
	}

	private void parse(Document doc) {
    for (Element eleBlock : doc.select("block-features block")) {
      String type = eleBlock.attr("type");
      Map<String, Double> phrases = new HashMap<String, Double>();

      for (Element elePhrase : eleBlock.getElementsByTag("phrase")) {
        String attr = elePhrase.attr("attr");
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

	@Override
	public String toString() {
	  return blockPhrases.toString();
	}

}
