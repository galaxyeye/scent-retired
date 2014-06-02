package org.qiwur.scent.app;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.learning.EntityAttributeLearner;
import org.qiwur.scent.wiki.Page;

public class WikiPropertyBuilder {

  private static Set<Page> pages = new HashSet<Page>();

  private static void buildPropertyPage(String name) {
    String[] parts = StringUtils.split(name, '/');

    String type = "String";
    String property = parts[parts.length - 1];
    String title = "Property:Has " + property;
    String summery = "创建属性：" + property;
    String text = "这是类型为[[Has type::" + type + "]]的一个属性\n";

    pages.add(new Page(title, text, summery));
  }

  public static void main(String[] args) throws IOException {
    Configuration conf = ScentConfiguration.create();
    EntityAttributeLearner learner = EntityAttributeLearner.create(conf);

    for (String fullName : learner.words()) {
      buildPropertyPage(fullName);
    }

    int counter = 0;
    for (Page page : pages) {
      System.out.println("upload the " + counter + "th page" + " : " + page.title());
      counter++;

      page.upload();
    }
  }
}
