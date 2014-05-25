package org.qiwur.scent.learning;

import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.EntityAttribute;

import com.google.common.collect.Multiset;

public class EntityAttributeLearner implements Learner {
  public static final LearningDomain domain = LearningDomain.ProductAttribute;

  private final Configuration conf;
  private final WordsLearner wordsLearner;

  public EntityAttributeLearner(Configuration conf) {
    this.conf = conf;
    this.wordsLearner = new WordsLearnerFactory(this.conf).getWordsLearner();
  }

  @Override
  public Multiset<String> words() {
    return wordsLearner.words(domain);
  }

  public void learn(Collection<EntityAttribute> attrs) {
    for (EntityAttribute attr : attrs) {
      learn(attr);
    }
  }

  @Override
  public void learn(Object attr) {
    if (attr instanceof EntityAttribute) {
      wordsLearner.learn(domain, ((EntityAttribute) attr).fullName());
    }
  }

  @Override
  public void save() {
    wordsLearner.save();
  }
}
