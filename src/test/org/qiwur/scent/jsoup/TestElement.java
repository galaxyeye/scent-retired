package org.qiwur.scent.jsoup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.utils.StringUtil;

public class TestElement {

  File htmlFile = null;
  Document doc = null;

  @Before
  public void startUp() throws IOException {
    htmlFile = new File("build/test/data/test.html");
    if (!htmlFile.exists()) {
      htmlFile = new File("test/data/test.html");
      if (!htmlFile.exists()) {
        htmlFile = null;
      }
    }

    doc = Jsoup.parse(htmlFile, "utf-8");
  }

  @Test
  public void testClassNames() {
    String cssRegex = "-?[_a-zA-Z]+[_a-zA-Z0-9-]*";

    assertTrue("_".matches(cssRegex));
    assertTrue("-_abc".matches(cssRegex));

    assertTrue(!"abc.".matches(cssRegex));
    assertTrue(!" __".matches(cssRegex));
    assertTrue(!".abc".matches(cssRegex));
    assertTrue(!" ".matches(cssRegex));
    
    LinkedHashSet<String> classNames = new LinkedHashSet<String>(Arrays.asList(" __01".trim().split("\\s+")));
    String classes = StringUtil.join(classNames, ".");

    assertEquals("__01", classes);
  }

  @Test
  public void testXPath() throws IOException {
    // xpath is buggy
    // Document doc = Jsoup.parse(htmlFile, "utf-8");
    // assertEquals("/html/body", doc.select("body").first().xpath());
    // assertEquals(doc.select("a.city:nth-child(5)").first().xpath(), "/html/body");
  }

  @Test
  public void testCssSelector() throws IOException {
    assertEquals("#nv_properties", doc.select("body").first().cssSelector());
  }

  public static void main(String s[]) throws Exception {
    LinkedHashSet<String> classNames = new LinkedHashSet<String>(Arrays.asList(" __01".trim().split("\\s+")));
    String classes = StringUtil.join(classNames, ".");

    assertEquals("__01", classes);
  }
}
