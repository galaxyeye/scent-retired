package org.qiwur.scent.learning;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.ObjectCache;

public class WordsLearnerFactory {

  private final Logger logger = WordsLearner.logger;

  private final Configuration conf;

  public WordsLearnerFactory(Configuration conf) {
    this.conf = conf;
  }

  public WordsLearner getWordsLearner() {
    ObjectCache objectCache = ObjectCache.get(conf);
    String cacheId = "WordsLearner";

    if (objectCache.getObject(cacheId) != null) {
      return (WordsLearner) objectCache.getObject(cacheId);
    } else {
      WordsLearner learner = new WordsLearner(conf);
      objectCache.setObject(cacheId, learner);
      return learner;
    }
  }
}
