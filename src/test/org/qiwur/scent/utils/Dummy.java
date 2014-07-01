package org.qiwur.scent.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.helpers.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.math.Arrays;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.BlockPattern;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.xml.sax.SAXException;

import ruc.irm.similarity.sentence.morphology.MorphoSimilarity;
import ruc.irm.similarity.word.hownet2.concept.BaseConceptParser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Dummy {

  static void initXsimilarity() {
    loadUserConcept();
  }

  static void loadUserConcept() {
    File conceptFile = new File("conf/commerce-concept.xml");
    try {
      BaseConceptParser.load(conceptFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static void testXsimilarity() {
    initXsimilarity();

    String s1 = "htc desire 816w 3g(wcdmagsm) 手机 轻盈白 双卡双待 联通定制";
    String s2 = "Desire 816wHTC Desire 816w 3G(WCDMA/GSM) 手机 轻盈白 双卡双待 联通定制";

    // FuzzyProbability p = FuzzySimilarity.getSentenceSimilarity(s1, s2);
    double sim = MorphoSimilarity.getInstance().getSimilarity(s1, s2);
    System.out.print("sim:" + sim);
  }


  static void loadXMLFile() throws IOException, ParserConfigurationException, SAXException {
//    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//    Document doc = db.parse(new File("conf/log4j2.xml"));
//
//    System.out.print(doc.getNodeName());
  }

  static void loadFile() throws IOException {
    int i;
    char c;

    InputStream is = new FileInputStream("conf/block-attr-feature.xml");
    try {
      while ((i = is.read()) != -1) {
        // converts integer to character
        c = (char) i;

        // prints character
        System.out.print(c);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (is != null)
        is.close();
    }
  }

  public static void main(String[] args) throws Exception {
  }
}
