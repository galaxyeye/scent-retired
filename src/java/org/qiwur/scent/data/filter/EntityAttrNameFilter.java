package org.qiwur.scent.data.filter;

import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.EntityAttribute;
import org.qiwur.scent.entity.PageEntity;

import com.google.common.collect.Lists;

public class EntityAttrNameFilter extends BadWordFilter {
	private final PageEntity pageEntity;

	public EntityAttrNameFilter(PageEntity pageEntity, Configuration conf) {
	  super(conf.get("scent.bad.attr.name.file"), conf);
		this.pageEntity = pageEntity;
	}

	public void process() {
	  List<EntityAttribute> removal = Lists.newArrayList();

		for (EntityAttribute attribute : pageEntity.attributes()) {
			String name = filter(attribute.name());
			if (name.isEmpty()) {
			  removal.add(attribute);
			}
		}

		pageEntity.removeAll(removal);
	}
}
