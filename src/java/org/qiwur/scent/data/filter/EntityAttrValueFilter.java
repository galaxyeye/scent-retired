package org.qiwur.scent.data.filter;

import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.EntityAttribute;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.feature.AttrValueFeature;
import org.qiwur.scent.feature.FeatureManager;
import org.qiwur.scent.utils.StringUtil;

public class EntityAttrValueFilter extends BadWordFilter {

	public String[] DontFilterAttributes = {"交易平台名称"};
	
	private final PageEntity pageEntity;

	public EntityAttrValueFilter(PageEntity pageEntity, Configuration conf) {
	  super(conf.get("scent.bad.attr.value.words.file"), conf);
		this.pageEntity = pageEntity;
	}

	public void process() {
		List<String> DontFilterAttributeList = Arrays.asList(DontFilterAttributes);

		// 删除敏感词
		for (EntityAttribute attribute : pageEntity.attributes()) {
			if (DontFilterAttributeList.contains(attribute.name())) continue;

			String value = filter(attribute.value());
			if (!value.isEmpty()) {
				attribute.value(value);
			}
		}
	}
}
