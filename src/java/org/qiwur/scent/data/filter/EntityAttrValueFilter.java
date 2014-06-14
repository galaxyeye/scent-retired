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
	  super("conf/bad-attribute-value-words.txt", conf);
		this.pageEntity = pageEntity;
	}

	public void process() {
		List<String> DontFilterAttributeList = Arrays.asList(DontFilterAttributes);

    String featureFile = getConf().get("scent.bad.attr.value.words.file");
    AttrValueFeature valueFeature = FeatureManager.get(getConf(), AttrValueFeature.class, featureFile);

		// 删除敏感词
		for (EntityAttribute attribute : pageEntity.attributes()) {
			if (DontFilterAttributeList.contains(attribute.name())) continue;

			String value = valueFeature.preprocess(attribute.value());
			value = StringUtil.trimNonChar(value);

			if (!value.isEmpty()) {
				attribute.value(value);
			}
		}
	}
}
