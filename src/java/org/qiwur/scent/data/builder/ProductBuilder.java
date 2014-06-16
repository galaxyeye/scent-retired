package org.qiwur.scent.data.builder;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.entity.EntityAttribute;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.utils.StringUtil;

public class ProductBuilder extends EntityBuilder {

	ProductBuilder(PageEntity pageEntity, Configuration conf) {
	  super(pageEntity, conf);
	}

	@Override
	public void process() {
	}

	protected String buildProductName(String pageEntityTitle) {
		String pageEntityName = pageEntity.firstText("产品名称");

		if (pageEntityName.isEmpty()) {
			pageEntityName = pageEntityTitle;
		}

		return pageEntityName;
	}

	protected String buildCategoryNavigator() {
		StringBuilder sb = new StringBuilder();

		if (pageEntity.contains("产品分类")) {
			String[] categories = StringUtils.split(pageEntity.firstText("产品分类"), " > ");

			for (int i = 0; i < categories.length; ++i) {
				sb.append("[[:Category:");
				sb.append(categories[i]);
				sb.append("]]");

				if (i < categories.length - 1) {
					sb.append(" > ");
				}
				else {
					sb.append("\n");
					sb.append("[[Category:");
					sb.append(categories[i]);
					sb.append("]]");
				}
			}
		}

		return sb.toString();
	}

	protected String buildProductTitle() {
		EntityAttribute manufacturer = pageEntity.first("生产商");
		EntityAttribute brand = pageEntity.first("品牌");
		EntityAttribute name = pageEntity.first("产品名称");
		EntityAttribute model = pageEntity.first("产品型号");

		StringBuilder sb = new StringBuilder();

		if (name != null) {
			if (manufacturer != null) sb.append(manufacturer.name());
			sb.append(' ');
			if (brand != null) sb.append(brand.name());
			sb.append(' ');
			sb.append(name.name());
			sb.append(' ');
			if (model != null) sb.append(model.name());
			sb.append(' ');
		}

		String pageEntityTitle = sb.toString();
		if (pageEntityTitle.length() < 10) {
			pageEntityTitle = null;
		}

		if (pageEntityTitle == null && pageEntity.contains("商品标题")) {
			String oldProductTitle = pageEntity.first("商品标题").value();

			// 如果标题太长，那么选取最大局部
			if (oldProductTitle.length() > 40) {
				String part = StringUtil.getLongestPart(oldProductTitle, " ");

				if (part.length() > 15) {
					pageEntityTitle = part;
				}
			}

			if (pageEntityTitle == null) {
				pageEntityTitle = oldProductTitle;
			}
		}

		return pageEntityTitle;
	}

	protected String buildProductTradePageTitle(String pageEntityTitle) {
		String platform = pageEntity.firstText("交易平台名称");
		if (platform.isEmpty()) platform = pageEntity.firstText("交易平台域名");

		String title = "购物通道：" + platform + " - " + pageEntityTitle;

		return title;
	}

	protected String buildProductIntroducePageTitle(String pageEntityTitle) {
		String title = "产品介绍：" + pageEntityTitle;

		return title;
	}

	protected void rebuildPrice() {
    if (!pageEntity.contains("价格") && pageEntity.contains("销售价")) {
      pageEntity.put("价格", pageEntity.first("销售价").value(), "TitleContainer");
    }

    if (!pageEntity.contains("销售价") && pageEntity.contains("价格")) {
      pageEntity.put("销售价", pageEntity.first("价格").value(), "TitleContainer");
    }
  }

	protected void rebuildModelNumber() {
  }

	protected void rebuildManufacturer() {
  }

}
