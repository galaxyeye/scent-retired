package org.qiwur.scent.app;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.learning.EntityCategoryLearner;
import org.qiwur.scent.wiki.Page;

public class WikiCategoryBuilder {

  private static Set<String> createdCategories = new HashSet<String>();
  private static Set<Page> pages = new HashSet<Page>();

  private static void buildCategoryPage(String name) {
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
      pages.add(new Page(title, text, summery));
    }
  }

  public static void main(String[] args) {
    Configuration conf = ScentConfiguration.create();
    EntityCategoryLearner learner = new EntityCategoryLearner(conf);

    for (String fullName : learner.words()) {
      buildCategoryPage(fullName);
    }

    int counter = 0;
    for (Page page : pages) {
      System.out.println("upload the " + counter + "th page" + " : " + page.title());
      counter++;

      page.upload();
    }
  }
}
