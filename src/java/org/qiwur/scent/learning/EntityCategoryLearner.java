package org.qiwur.scent.learning;

import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.EntityCategory;

import com.google.common.collect.Multiset;

public class EntityCategoryLearner implements Learner {
  public static final LearningDomain domain = LearningDomain.EntityCategory;

  private final Configuration conf;
  private final WordsLearner wordsLearner;

  public EntityCategoryLearner(Configuration conf) {
    this.conf = conf;
    this.wordsLearner = new WordsLearnerFactory(this.conf).getWordsLearner();
  }

  @Override
  public Multiset<String> words() {
    return wordsLearner.words(domain);
  }

  public void learn(Collection<EntityCategory> attrs) {
    for (EntityCategory attr : attrs) {
      learn(attr);
    }
  }

  @Override
  public void learn(Object attr) {
    if (attr instanceof EntityCategory) {
      wordsLearner.learn(domain, ((EntityCategory) attr).fullName());
    }
  }

  @Override
  public void save() {
    wordsLearner.save();
  }
}
