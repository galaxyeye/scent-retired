package org.qiwur.scent.app;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.wiki.Page;
import org.qiwur.scent.learning.EntityAttributeLearner;
import org.qiwur.scent.utils.ScentConfiguration;

public class WikiPropertyBuilder {

  private Configuration conf;
  private Set<Page> pages = new HashSet<Page>();

  public WikiPropertyBuilder(Configuration conf) {
    this.conf = conf;
  }

  private void buildPropertyPage(String name) {
    String[] parts = StringUtils.split(name, '/');

    String type = "String";
    String property = parts[parts.length - 1];
    String title = "Property:Has " + property;
    String summery = "创建属性：" + property;
    String text = "这是类型为[[Has type::" + type + "]]的一个属性\n";

    Page page = new Page(title, text, summery);
    page.setConf(conf);
    pages.add(page);
  }

  public void uploadAll() {
    int counter = 0;
    for (Page page : pages) {
      System.out.println("upload the " + counter + "th page" + " : " + page.title());
      counter++;

      page.upload();
    }
  }

  public static void main(String[] args) {
    Configuration conf = ScentConfiguration.create();
    EntityAttributeLearner learner = new EntityAttributeLearner(conf);
    WikiPropertyBuilder builder = new WikiPropertyBuilder(conf);

    for (String fullName : learner.words()) {
      builder.buildPropertyPage(fullName);
    }

    builder.uploadAll();
  }
}
