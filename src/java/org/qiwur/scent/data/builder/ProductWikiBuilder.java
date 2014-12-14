package org.qiwur.scent.data.builder;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.builder.template.MiniTemplator;
import org.qiwur.scent.data.builder.template.MiniTemplator.TemplateSyntaxException;
import org.qiwur.scent.data.entity.EntityAttribute;
import org.qiwur.scent.data.entity.PageEntity;
import org.qiwur.scent.data.wiki.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ProductWikiBuilder extends ProductBuilder {

	private static final String ProductTemplateFileName = "wwwroot/template/product.template.wiki";
	private static final String ProductTradeTemplateFileName = "wwwroot/template/product.trade.template.wiki";
	private static final String ProductIntroduceTemplateFileName = "wwwroot/template/product.introduce.template.wiki";

	String pageEntityTitle = null;
	String pageEntityName = null;
	String pageEntityCategory = null;

	public ProductWikiBuilder(PageEntity pageEntity, Configuration conf) {
		super(pageEntity, conf);

		pageEntityTitle = buildProductTitle();
		pageEntityName = buildProductName(pageEntityTitle);
		pageEntityCategory = buildCategoryNavigator();
	}

	public Page build(String pageType) {
		if (pageType.equals(Page.ProductPage)) {
			return buildProductPage();
		}
		else if (pageType.equals(Page.TradePage)) {
			return buildProductPage();
		}
		else if (pageType.equals(Page.DetailPage)) {
			return buildDetailPage();
		}

		return null;
	}

	public Page buildProductPage() {
		Page page = new Page();

		if (pageEntityTitle == null) {
		  // logger.debug("invalid page title");
		  return null;
		}

		try {
			MiniTemplator template = new MiniTemplator(ProductTemplateFileName);

			page.title(pageEntityTitle);
			page.summery(pageEntityTitle);

			template.setVariable("产品名称", pageEntityName);
			template.setVariable("产品分类", pageEntityCategory);

			for (EntityAttribute attribute : pageEntity.get("网页关键词")) {
				template.setVariable("关键词", attribute.value());
				template.addBlock("关键词");
			}

			final String[] ignoredBaseInfo = {"产品名称", "产品分类"};
			for (EntityAttribute attribute : pageEntity.getCategorized("基本信息")) {
				if (!Arrays.asList(ignoredBaseInfo).contains(attribute.name())) {
					template.setVariable("属性名", attribute.name());
					template.setVariable("属性值", attribute.value());
					template.addBlock("基本信息");
				}
			}

			for (EntityAttribute attribute : pageEntity.getCategorized("规格参数")) {
				template.setVariable("属性名", attribute.name());
				template.setVariable("属性值", attribute.value());
				template.addBlock("规格参数");
			}

			for (EntityAttribute attribute : pageEntity.getUncategorized()) {
				template.setVariable("属性名", attribute.name());
				template.setVariable("属性值", attribute.value());
				template.addBlock("其他属性");
			}

			page.text(template.generateOutput());
		} catch (TemplateSyntaxException | IOException e) {
			logger.error(e);
		}

		return page;
	}

	public Page buildTradePage() {
		Page page = new Page();

		if (pageEntityTitle == null) return null;

		try {
			MiniTemplator template = new MiniTemplator(ProductTradeTemplateFileName);

			String title = buildProductTradePageTitle(pageEntityTitle);
			page.title(title);
			page.summery(title);

			template.setVariable("产品名称", pageEntityName);
			template.setVariable("交易平台名称", pageEntity.firstText("交易平台名称"));
			template.setVariable("交易平台域名", pageEntity.firstText("交易平台域名"));
			template.setVariable("销售价", pageEntity.firstText("销售价"));

			template.setVariable("可选颜色", pageEntity.join("颜色"));
			template.setVariable("可选版本", pageEntity.join("版本"));

			template.setVariable("售后服务", pageEntity.firstText("售后服务"));
			template.setVariable("物流信息", pageEntity.firstText("物流信息"));
			template.setVariable("支付说明", pageEntity.firstText("支付说明"));

			template.setVariable("商品详细介绍", buildProductIntroducePageTitle(pageEntityTitle));

			template.setVariable("网页标题", pageEntityTitle);
			template.setVariable("网页摘要", pageEntity.firstText("网页摘要"));
			template.setVariable("网页关键词", pageEntity.join("网页关键词"));
			template.setVariable("网页链接", pageEntity.firstText("购买链接"));

			page.text(template.generateOutput());
		} catch (TemplateSyntaxException | IOException e) {
			logger.error(e);
		}

		return page;
	}

	public Page buildDetailPage() {
		Page page = new Page();

		if (pageEntityTitle == null) return null;

		try {
			MiniTemplator template = new MiniTemplator(ProductIntroduceTemplateFileName);

			String title = buildProductIntroducePageTitle(pageEntityTitle);
			page.title(title);
			page.summery(title);

			template.setVariable("产品名称", pageEntityName);
			template.setVariable("商品详细介绍", html2wiki(pageEntity.firstText("商品详细介绍")));

			page.text(template.generateOutput());
		} catch (TemplateSyntaxException | IOException e) {
			logger.error(e);
		}

		return page;
	}

	// TODO : pretty format
	private String html2wiki(String html) {
		Document doc = Jsoup.parse(html);

		return doc.text();
	}
}
