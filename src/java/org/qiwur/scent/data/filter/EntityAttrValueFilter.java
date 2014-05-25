package org.qiwur.scent.data.filter;

import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.EntityAttribute;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.feature.EntityAttrValueFeature;
import org.qiwur.scent.utils.StringUtil;

public class EntityAttrValueFilter extends BadWordFilter {

	public String[] DontFilterAttributes = {"交易平台名称"};
	
	private final PageEntity product;

	public EntityAttrValueFilter(PageEntity product, Configuration conf) {
	  super("conf/bad-attribute-name.txt", conf);
		this.product = product;
	}

	public void process() {
		List<String> DontFilterAttributeList = Arrays.asList(DontFilterAttributes);
		// 删除敏感词
		for (EntityAttribute attribute : product.attributes().values()) {
			if (DontFilterAttributeList.contains(attribute.name())) continue;

			String value = EntityAttrValueFeature.preprocess(attribute.value());

			value = StringUtil.trimNonChar(value);

			if (!value.isEmpty()) {
				attribute.value(value);
			}
		}
	}
}
