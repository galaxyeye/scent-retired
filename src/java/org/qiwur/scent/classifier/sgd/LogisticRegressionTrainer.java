package org.qiwur.scent.classifier.sgd;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.classifier.sgd.AdaptiveLogisticRegression;
import org.apache.mahout.classifier.sgd.CrossFoldLearner;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.ModelSerializer;
import org.apache.mahout.ep.State;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.Dictionary;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.sgd.NewsgroupHelper;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;

public class LogisticRegressionTrainer {

  private final Configuration conf;

  protected final String[] labels;

  public LogisticRegressionTrainer(Configuration conf) {
    this.conf = conf;
    this.labels = conf.getStrings("scent.segment.labels");
  }

  public void train() throws IOException {
    Multiset<String> overallCounts = HashMultiset.create();

    int leakType = 0;
//    if (args.length > 1) {
//      leakType = Integer.parseInt(args[1]);
//    }

    Dictionary segments = new Dictionary();

//    helper.getEncoder().setProbes(2);
    // TODO : load category number from configuration
    AdaptiveLogisticRegression learningAlgorithm = 
        new AdaptiveLogisticRegression(labels.length, NewsgroupHelper.FEATURES, new L1());
    learningAlgorithm.setInterval(800);
    learningAlgorithm.setAveragingWindow(500);

    List<File> files = Lists.newArrayList();
    File base = new File(conf.get("scent.sgd.train.base.dir"));
    for (File segment : base.listFiles()) {
      if (segment.isDirectory()) {
        segments.intern(segment.getName());
        files.addAll(Arrays.asList(segment.listFiles()));
      }
    }

    Collections.shuffle(files);
    System.out.println(files.size() + " training files");
    SGDInfo info = new SGDInfo();

    int k = 0;
    for (File file : files) {
      String ng = file.getParentFile().getName();
      int actual = segments.intern(ng);

      Document doc = Jsoup.parse(file, "utf-8");
      DomSegment segment = new DomSegment(doc.getElementById("bodyElement"));

      Vector v = new BlockFeatureVectorEncoder("segments").addToVector(segment);
      // Vector v = helper.encodeFeatureVector(file, actual, leakType, overallCounts);
      learningAlgorithm.train(actual, v);

      k++;
      State<AdaptiveLogisticRegression.Wrapper, CrossFoldLearner> best = learningAlgorithm.getBest();

      SGDHelper.analyzeState(info, leakType, k, best);
    } // for

    learningAlgorithm.close();
    SGDHelper.dissect(leakType, segments, learningAlgorithm, files, overallCounts);
    System.out.println("exiting main");

    ModelSerializer.writeBinary("/tmp/scent-document-segments.model",
        learningAlgorithm.getBest().getPayload().getLearner().getModels().get(0));

    List<Integer> counts = Lists.newArrayList();
    System.out.println("Word counts");
    for (String count : overallCounts.elementSet()) {
      counts.add(overallCounts.count(count));
    }

    Collections.sort(counts, Ordering.natural().reverse());
    k = 0;
    for (Integer count : counts) {
      System.out.println(k + "\t" + count);
      k++;
      if (k > 1000) {
        break;
      }
    }
  }

  public static void main(String[] args) throws IOException {
    Configuration conf = ScentConfiguration.create();

    LogisticRegressionTrainer trainer = new LogisticRegressionTrainer(conf);
    trainer.train();
  }
}
