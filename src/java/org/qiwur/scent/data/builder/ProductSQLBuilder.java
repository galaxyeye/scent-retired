package org.qiwur.scent.data.builder;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.PageEntity;

public class ProductSQLBuilder extends ProductBuilder {

	ProductSQLBuilder(PageEntity product, Configuration conf) {
		super(product, conf);
	}

	public void process() {
		
	}
}
