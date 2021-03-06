package org.qiwur.scent.block.locator;

import org.apache.hadoop.conf.Configuration;
import org.jsoup.block.BlockLabel;
import org.jsoup.block.DomSegment;
import org.jsoup.nodes.Document;

/*
 * 商品展示区一般包含一下信息：
 * 
 * 价格，评论，配送，数量，销量，规格，颜色分类，服务内容，评价，购买，支付方式，其他说明
 * 
 * */
public class ProductShowLocator extends BlockLocator {

	public ProductShowLocator(Document doc, Configuration conf) {
    super(doc, BlockLabel.fromString("ProductShow"));
	}

  @Override
  protected DomSegment quickLocate() {
    return null;
  }

  @Override
  protected DomSegment deepLocate() {
    return null;
  }
}
