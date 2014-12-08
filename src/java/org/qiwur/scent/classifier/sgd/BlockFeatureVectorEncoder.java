package org.qiwur.scent.classifier.sgd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.ContinuousValueEncoder;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;

public class BlockFeatureVectorEncoder extends ContinuousValueEncoder {

  static final Logger logger = LogManager.getLogger(BlockFeatureVectorEncoder.class);

  private List<String> features = new ArrayList<String>();

  private int vectorSizeFactor = 1;

  public BlockFeatureVectorEncoder(String name, String... features) {
    super(name);
    this.features.addAll(Arrays.asList(features));
  }

  public BlockFeatureVectorEncoder(String name, Collection<String> features) {
    super(name);
    features.addAll(features);
  }

  public int numFeatures() {
    return features.size();
  }

  public int vectorSize() {
    return vectorSizeFactor * numFeatures();
  }

  public void vectorSizeFactor(int factor) {
    this.vectorSizeFactor = factor;
  }

  public List<String> getFeatures() {
    return features;
  }

  public Vector encode(File file) {
    Vector v = new RandomAccessSparseVector(vectorSize());
    encode(file, v);
    return v;
  }

  public Vector encode(String html) {
    Vector v = new RandomAccessSparseVector(vectorSize());
    encode(html, v);
    return v;
  }

  public Vector encode(Element block) {
    Vector v = new RandomAccessSparseVector(vectorSize());
    encode(block, v);
    return v;
  }

  public void encode(File file, Vector v) {
    try {
      String content = FileUtils.readFileToString(file);
      encode(content, v);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  public void encode(String html, Vector v) {
    Document doc = Document.createShell("");
    Element body = doc.select("body").first();
    body.append(html);
    doc.calculateIndicators();

    if (!body.children().isEmpty()) {
      encode(body.child(0), v);
    }
  }

  public void encode(Element block, Vector v) {
    for (String feature : features) {
      addToVector((byte[])null, getValue(block, feature), v);
    }

    diagnose(block, v);
  }

  protected double getValue(Element ele, String feature) {
    return ele.indic(feature);
  }

  protected void diagnose(Element block, Vector v) {
    String report = "";
    for (String feature : features) {
      report += String.format("%-15s", feature);
    }
    logger.debug(report);

    report = "";
    for (String feature : features) {
      report += String.format("%-15s", getValue(block, feature));
    }
    logger.debug(report);

    report = "";
    for (Vector.Element e : v.all()) {
      report += String.format("%-15s", e.get());
    }
    logger.debug(report);

    // logger.debug(v.asFormatString());
  }
}
