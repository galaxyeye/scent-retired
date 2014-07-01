package org.qiwur.scent.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

<<<<<<< HEAD
=======
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.BlockPattern;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.w3c.dom.Document;
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
import org.xml.sax.SAXException;

import ruc.irm.similarity.sentence.morphology.MorphoSimilarity;
import ruc.irm.similarity.word.hownet2.concept.BaseConceptParser;

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

<<<<<<< HEAD
  public static void main(String[] args) throws Exception {
=======
  static void stringSetTest() {
    Set<String> candidates = new HashSet<String>();
    Set<String> candidates2 = new HashSet<String>();

    candidates.add("nav_top");
    candidates.add("mainMenu");
    candidates.add("image-detail");
    candidates.add("image      detail");

    for (String candidate : candidates) {
      candidate = StringUtils.join(candidate.split("(?=\\p{Upper})"), " ");
      candidates2.add(candidate.replaceAll("[-_]", " ").toLowerCase());
    }

    System.out.print(candidates2);
  }

  public static void testPattern() {
    String text = "￥799.00 (降价通知)";
    // text = text.replaceAll("¥|,|'", "");
    System.out.println(text);

    String patternString = "[1-9](,{0,1}\\d+){0,8}(\\.\\d{1,2})|[1-9](,{0,1}\\d+){0,8}";

    Pattern pattern = Pattern.compile(patternString);
    Matcher matcher = pattern.matcher(text);

    // if (matcher.find()) {
    // System.out.print(matcher.group());
    // }

    int count = 0;
    while (matcher.find()) {
      count++;
      System.out.println("found: " + count + " : " + matcher.start() + " - " + matcher.end() + ", " + matcher.group());
    }
  }

  public static void testTrimNonChar() {
    String text = "天王表 正品热卖 机械表 全自动 男士商务气派钢带手表GS5733T/D尊贵大气 个性表盘  ";
    System.out.println(text.trim());
    System.out.println(StringUtil.trimNonChar(text));
  }

  private static void testBlockLabel() {
    Configuration conf = ScentConfiguration.create();
    String[] labels = conf.getStrings("scent.classifier.block.labels");

    for (String label : labels) {
      BlockLabel l = BlockLabel.fromString(label);
      if (BlockLabel.builtinLabels.contains(l)) {
        System.out.println(l);
      }
    }
  }

  private static void testBlockTracker() {
    // Map<BlockLabel, Double> trackees = new HashMap<BlockLabel, Double>();
    // BlockLabelTracker tracker = new BlockLabelTracker();
    DomSegment segment = new DomSegment(null, null, null);

    int count = 0;
    for (BlockLabel label : BlockLabel.builtinLabels) {
      double sim = 0.1 + count++ / 10.0;
      // trackees.put(label, sim);
      // tracker.set(label, sim);
      segment.tag(label, sim);
    }

    System.out.println(BlockLabel.fromString("BadBlock").text().length());
    System.out.println(BlockLabel.BadBlock.text().length());

    if (BlockLabel.BadBlock.equals(BlockLabel.fromString("BadBlock"))) {
      System.out.println("gooooooooood");
    }

    int b = BlockLabel.fromString("BadBlock").compareTo(BlockLabel.BadBlock);
    System.out.println(b);

    System.out.println(segment.labelTracker().get(BlockLabel.BadBlock));
    System.out.println(segment.labelTracker().get(BlockLabel.fromString("BadBlock")));
  }

  public static void main(String[] args) {
    String cls = "aa bb cc";
    cls = cls.replaceAll("\\s+", ".");
    System.out.println(cls);
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
  }
}
