/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qiwur.scent.classifier.sgd;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.classifier.sgd.AdaptiveLogisticRegression;
import org.apache.mahout.classifier.sgd.CrossFoldLearner;
import org.apache.mahout.classifier.sgd.ModelDissector;
import org.apache.mahout.classifier.sgd.ModelSerializer;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.ep.State;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.Functions;
import org.apache.mahout.vectorizer.encoders.Dictionary;
import org.jsoup.nodes.Indicator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class SGDDissector {

  private final Configuration conf;
  private final BlockFeatureVectorEncoder biasEncoder;
  private final BlockFeatureVectorEncoder featureEncoder;
  private final int probes;
  Map<String, Set<Integer>> traceDictionary = Maps.newTreeMap();

  public SGDDissector(Configuration conf) {
    this.conf = conf;
    biasEncoder = new BlockFeatureVectorEncoder("html-blocks", Indicator.names);
    featureEncoder = new BlockFeatureVectorEncoder("html-blocks", Indicator.names);
    // this.probes = conf.getInt("scent.logistic.regression.classifier.probes", 1);
    this.probes = featureEncoder.numFeatures();

    biasEncoder.setProbes(probes);
    featureEncoder.setProbes(probes);
  }

  public void dissect(int leakType, Dictionary dictionary,
      AdaptiveLogisticRegression model, Iterable<File> files) throws IOException {
    CrossFoldLearner learner = model.getBest().getPayload().getLearner();
    learner.close();

    ModelDissector dissector = new ModelDissector();

    biasEncoder.setTraceDictionary(traceDictionary);
    featureEncoder.setTraceDictionary(traceDictionary);

    for (File file : permute(files, RandomUtils.getRandom()).subList(0, 500)) {
      int actualCategory = dictionary.intern(file.getParentFile().getName());
      traceDictionary.clear();
      dissector.update(featureEncoder.encode(file), traceDictionary, learner);
    }

    List<String> categoryNames = Lists.newArrayList(dictionary.values());
    List<ModelDissector.Weight> weights = dissector.summary(100);
    System.out.println("============");
    System.out.println("Model Dissection");
    for (ModelDissector.Weight w : weights) {
      System.out.printf("%s\t%.1f\t%s\t%.1f\t%s\t%.1f\t%s%n",
          w.getFeature(),
          w.getWeight(),
          categoryNames.get(w.getMaxImpact() + 1),
          w.getCategory(1),
          w.getWeight(1),
          w.getCategory(2),
          w.getWeight(2));
    }
  }

  public static void analyzeState(SGDInfo info, int leakType, int k,
      State<AdaptiveLogisticRegression.Wrapper, CrossFoldLearner> best) throws IOException {
    int bump = info.getBumps()[(int) Math.floor(info.getStep()) % info.getBumps().length];
    int scale = (int) Math.pow(10, Math.floor(info.getStep() / info.getBumps().length));

    double maxBeta;
    double nonZeros;
    double positive;
    double norm;

    double lambda = 0;
    double mu = 0;

    if (best != null) {
      CrossFoldLearner state = best.getPayload().getLearner();
      info.setAverageCorrect(state.percentCorrect());
      info.setAverageLL(state.logLikelihood());

      OnlineLogisticRegression model = state.getModels().get(0);
      // finish off pending regularization
      model.close();

      Matrix beta = model.getBeta();
      maxBeta = beta.aggregate(Functions.MAX, Functions.ABS);
      nonZeros = beta.aggregate(Functions.PLUS, new DoubleFunction() {
        @Override
        public double apply(double v) {
          return Math.abs(v) > 1.0e-6 ? 1 : 0;
        }
      });

      positive = beta.aggregate(Functions.PLUS, new DoubleFunction() {
        @Override
        public double apply(double v) {
          return v > 0 ? 1 : 0;
        }
      });
      norm = beta.aggregate(Functions.PLUS, Functions.ABS);

      lambda = best.getMappedParams()[0];
      mu = best.getMappedParams()[1];
    } else {
      maxBeta = 0;
      nonZeros = 0;
      positive = 0;
      norm = 0;
    }

    if (k % (bump * scale) == 0) {
      if (best != null) {
        ModelSerializer.writeBinary("/tmp/html-blocks-" + k + ".model", best.getPayload().getLearner().getModels().get(0));
      }

      info.setStep(info.getStep() + 0.25);
      System.out.printf("%.2f\t%.2f\t%.2f\t%.2f\t%.8g\t%.8g\t", maxBeta, nonZeros, positive, norm, lambda, mu);
      System.out.printf("%d\t%.3f\t%.2f\t", k, info.getAverageLL(), info.getAverageCorrect() * 100);
      System.out.println();
    }
  }

  public static List<File> permute(Iterable<File> files, Random rand) {
    List<File> r = Lists.newArrayList();

    for (File file : files) {
      int i = rand.nextInt(r.size() + 1);
      if (i == r.size()) {
        r.add(file);
      } else {
        r.add(r.get(i));
        r.set(i, file);
      }
    }

    return r;
  }

}
