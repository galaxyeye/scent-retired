package org.qiwur.scent.data.extractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;

public class ProductExtractor extends PageExtractor {

  private static Map<String, String> configuratedExtractors = new HashMap<String, String>();

  static {
    // TODO : load from configuration
    configuratedExtractors.put("ProductDetail", ProductDetailExtractor.class.getName());
    configuratedExtractors.put("RelativeProduct", RelativeProductExtractor.class.getName());
  }

  public ProductExtractor(Document doc, Configuration conf) {
    super(doc, conf);
  }

  @Override
  protected void installExtractors() {
    super.installExtractors();

    for (Entry<String, String> entry : configuratedExtractors.entrySet()) {
      for (DomSegment segment : getSegments(entry.getKey())) {
        addExtractor(getExtractor(segment, entry.getValue(), conf));
      }
    }
  }
}
