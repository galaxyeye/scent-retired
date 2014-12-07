package org.qiwur.scent.app;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.data.wiki.Page;
import org.qiwur.scent.learning.EntityCategoryLearner;
import org.qiwur.scent.utils.ScentConfiguration;

public class WikiCategoryBuilder {

  private static final Logger logger = LogManager.getLogger(WikiCategoryBuilder.class);

  private Configuration conf;
  private Set<String> createdCategories = new HashSet<String>();
  private Set<Page> pages = new HashSet<Page>();

  public WikiCategoryBuilder(Configuration conf) {
    this.conf = conf;
  }

  private void buildCategoryPage(String name) {
    String[] parts = StringUtils.split(name, ">");

    for (int i = 0; i < parts.length; ++i) {
      String category = parts[i].trim();

      String title = "Category:" + category;
      if (createdCategories.contains(title))
        continue;

      String summery = "创建产品分类：" + category;
      String text = "这是一个产品分类。\n\n";

      text += "该分类所属的类别：\n\n";
      text += "<categorytree mode=parents>" + category + "</categorytree>\n\n";

      text += "属于该分类的类别：\n\n";
      text += "<categorytree mode=categories>" + category + "</categorytree>\n\n";

      if (i == 0) {
        text += "[[Category:产品分类]]\n\n";
      } else {
        text += "[[Category:" + parts[i - 1].trim() + "]]\n\n";
      }

      createdCategories.add(title);
      Page page = new Page(title, text, summery);
      page.setConf(conf);
      pages.add(page);
    }
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
    EntityCategoryLearner learner = new EntityCategoryLearner(conf);
    WikiCategoryBuilder builder = new WikiCategoryBuilder(conf);

    for (String fullName : learner.words()) {
      builder.buildCategoryPage(fullName);
    }

    builder.uploadAll();
  }
}
