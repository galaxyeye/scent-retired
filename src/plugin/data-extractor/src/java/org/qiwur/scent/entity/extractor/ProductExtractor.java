package org.qiwur.scent.entity.extractor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.extractor.PageExtractor;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.storage.WebPage.Field;

public class ProductExtractor extends PageExtractor {

  private static Map<String, String> configuratedExtractors = new HashMap<String, String>();

  static {
    // TODO : load from configuration
    configuratedExtractors.put("ProductDetail", ProductDetailExtractor.class.getName());
    configuratedExtractors.put("RelativeProduct", RelativeProductExtractor.class.getName());
  }

  @Override
  public Collection<Field> getFields() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Configuration getConf() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setConf(Configuration arg0) {
    // TODO Auto-generated method stub
  }

  @Override
  public void process() {
    // TODO Auto-generated method stub
  }

  public ProductExtractor() {
    
  }

  public ProductExtractor(Document doc, Configuration conf) {
    super(doc, conf);
  }

  @Override
  protected void installExtractors() {
    super.installExtractors();

//    for (Entry<String, String> entry : configuratedExtractors.entrySet()) {
//      for (DomSegment segment : getSegments(entry.getKey())) {
//        addExtractor(getExtractor(segment, entry.getValue(), conf));
//      }
//    }
  }
}
