package org.qiwur.scent.entity.extractor;

import org.qiwur.scent.data.extractor.PageExtractor;
import org.qiwur.scent.jsoup.block.BlockLabel;

public class ProductExtractor extends PageExtractor {

  public ProductExtractor() {
  }

  @Override
  protected void installUserExtractors() {
    installExtractor(BlockLabel.fromString("ProductDetail"), ProductDetailExtractor.class);
    installExtractor(BlockLabel.fromString("ProductShow"), ProductShowExtractor.class);
  }
}
