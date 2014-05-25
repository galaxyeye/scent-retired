package org.qiwur.scent.classifier.sgd;

import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.ContinuousValueEncoder;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Indicator;

public class BlockFeatureVectorEncoder extends ContinuousValueEncoder {

  protected BlockFeatureVectorEncoder(String name) {
    super(name);
  }

  public Vector addToVector(DomSegment segment) {
    Vector v = new RandomAccessSparseVector(Indicator.names.length);

    return addToVector(segment, v);
  }

  public Vector addToVector(DomSegment segment, Vector v) {
    for (String name : Indicator.names) {
      addToVector(name, segment.body().indic(name), v);
    }

    return v;
  }
}
