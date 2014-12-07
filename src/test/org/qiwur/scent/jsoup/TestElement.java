package org.qiwur.scent.jsoup;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.qiwur.scent.jsoup.nodes.Document;

public class TestElement {

  File htmlFile = null;

  @Before
  public void startUp() {
    htmlFile = new File("build/test/data/test.html");
    if (!htmlFile.exists()) {
      htmlFile = new File("test/data/test.html");
      if (!htmlFile.exists()) {
        htmlFile = null;
      }
    }
  }

  @Test
  public void testXPath() throws IOException {
    Document doc = Jsoup.parse(htmlFile, "utf-8");

    assertEquals(doc.select("body").first().xpath(), "/html/body");
    // assertEquals(doc.select("a.city:nth-child(5)").first().xpath(), "/html/body");
  }
}
