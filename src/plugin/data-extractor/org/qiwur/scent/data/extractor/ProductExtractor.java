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
  protected void rebuild() {
    super.rebuild();

    rebuildPrice();
    rebuildModelNumber();
    rebuildManufacturer();
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

  private void rebuildPrice() {
    if (!pageEntity.contains("价格") && pageEntity.contains("销售价")) {
      pageEntity.put("价格", pageEntity.first("销售价").value(), "ProductShow");
    }

    if (!pageEntity.contains("销售价") && pageEntity.contains("价格")) {
      pageEntity.put("销售价", pageEntity.first("价格").value(), "ProductShow");
    }
  }

  private void rebuildModelNumber() {
  }

  private void rebuildManufacturer() {
  }

}
