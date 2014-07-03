package org.qiwur.scent.data.extractor;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.select.DOMUtil;
import org.qiwur.scent.jsoup.select.Elements;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public class KeyValueExtractor {

	static final Logger logger = LogManager.getLogger(KeyValueExtractor.class);

	public static final int MIN_ATTRIBUTE_NAME_LENGTH = 2;
	public static final int MIN_ATTRIBUTE_VALUE_LENGTH = 1;
	public static final int MAX_ATTRIBUTE_NAME_LENGTH = 8;
	public static final int MAX_ATTRIBUTE_VALUE_LENGTH = 80;
	public static final String I_I_PATTERN_SEPERATORS = ":：";

	private Element root = null;
	private Multimap<String, String> attributes = LinkedListMultimap.create();

	public KeyValueExtractor(Element root) {
	  Validate.notNull(root);

	  this.root = root;
	}

	public boolean hasAttribute() {
		return !attributes.isEmpty();
	}

	public Multimap<String, String> getAttributes() {
		return attributes;
	}

  public Element element() {
    return root;
  }

	public void extractList(Element root) {
		return;
	}

	public void extractTable(Element table) {
		if (root == null) return;

		Elements trs = table.getElementsByTag("tr");

		for (Element tr : trs) {
			Element name = DOMUtil.findFirstChildHasText(tr, null, false);
			Element value = DOMUtil.findSecondChildHasText(tr, null, false);

			tryCollectKeyValuePair(name, value);
		}
	}

	public void extractDl(Element dl) {
		if (root == null) return;

		Elements elemens = dl.children();

		for (int i = 0; i < elemens.size() - 1;) {
			Element name = elemens.get(i);
			Element value = elemens.get(i + 1);

			if (name.tagName().equals("dt") && value.tagName().equals("dd")) {
				tryCollectKeyValuePair(name, value);

				i += 2;
			}
			else {
				// 继续寻找下一个K/V对
				i += 1;
			}
		}
	}

	/*
	 * 基于N-2特征模式的抽取算法。
	 * N-2特征模式是指：
	 * 1. 有较多的孩子节点
	 * 2. 每个孩子节点中，非空文本的直接子孩子数有两个
	 * 
	 * @param root The root element to find N-2 pattern
	 * @param minChildren The minimum direct children number of the element matches
	 * 	the N-2 pattern 
	 * @desiredTags The allowed type for the root matches N-2 pattern
	 */
	public void extractN2Pattern(Element root, int minChildren, String... desiredTags) {
		if (root == null) return;

		TreeMultimap<Double, Element> map = 
				DOMUtil.findMostChildrenElement(root, minChildren, desiredTags);

		if (map.size() > 0) {
		  extractMultimap(map);
		}
	}

	/*
	 * 
	 * 基于I:I特征模式的抽取算法。
	 * I:I特征模式是指文本以:或者其他符号分隔
	 * 
	 * 一个区块内至少有两个分隔符，才认为是键值对
	 * 
	 * */
	public void extractIIPattern(Element root, String sep) {
		if (root == null) return;

		for (Element e : root.children()) {
		  logger.debug("II : {}, {}", e.prettyName(), e.text());

			if (e.indic(Indicator.SEP) > 0) {
				String text = e.text();
				String[] parts = StringUtils.split(text, sep);

				logger.debug("parts : {}", Arrays.asList(parts));

				if (parts.length == 2) {
					tryCollectKeyValuePair(parts[0], parts[1]);
				}
			}
		}
	}

	/*
	 * 
	 * TreeMultimap按照直接孩子数多少来排列潜在的键值元素，直接孩子数越多，越有可能包含了键值对
	 * 
	 * */
	public void extractMultimap(Multimap<Double, Element> map) {
		if (root == null || map == null) return;

		for (Double k : map.keySet()) {
			for (Element root : map.get(k)) {

				double _child = root.indic(Indicator.C);
				double _grand_child = root.indic(Indicator.G);
				double _txt_blk = root.indic(Indicator.TB);

				double growth = (_grand_child - _child) / _child;
				double growth2 = (_txt_blk - _child) / _child;

				// 没有键值对
				// 推导过程：
				// 完美键值对模式：c1 + c1 == c2
				// 增加20%干扰项：c1 + c1 < c2 + 0.2c1 => 0.8 < (c2 - c1) / c1
				if (growth < 0.8 || growth2 < 0.8) {
					continue;
				}

				for (Element pair : root.children()) {
					final List<String> tagBlackList = Arrays.asList("script", "form");
					if (tagBlackList.contains(pair.tagName())) {
						continue;
					}

					final int MaxNumDirectChildren = 4;
					final int MaxNumAllChildren = 15;

					// 构造太复杂，不认为是键值对
					if (pair.indic(Indicator.C) > MaxNumDirectChildren
							|| pair.indic(Indicator.D) > MaxNumAllChildren) {
						continue;
					}

					// logger.debug("k : {}, e : {}, child : {}", k, root.prettyName(), pair.prettyName());

					Element nameElement = DOMUtil.findFirstChildHasText(pair, null, false);
					Element valueElement = DOMUtil.findSecondChildHasText(pair, null, false);

					tryCollectKeyValuePair(nameElement, valueElement);
				} // for
			} // for
		} // for
	} // extractMultimap

	private String stripAttrNameToNull(String attrName) {
//		logger.debug("attribute name : {}", attrName);

		attrName = StringUtil.stripNonChar(attrName, "()[]（）【】");

//		logger.debug("attribute name 2 : {}, {}", attrName, attrName.length());

		int l = attrName.length();

		if (l < MIN_ATTRIBUTE_NAME_LENGTH || l > MAX_ATTRIBUTE_NAME_LENGTH) {
			return null;
		}

		return attrName;
	}

	private String stripAttrValueToNull(String attrValue) {
		attrValue = attrValue.trim();

		int l = attrValue.length();

		if (l < MIN_ATTRIBUTE_VALUE_LENGTH || l > MAX_ATTRIBUTE_VALUE_LENGTH) {
			return null;
		}

		return attrValue;
	}

	// 从一对元素中抽取键值对
	private boolean tryCollectKeyValuePair(String attrName, String attrValue) {
		attrName = stripAttrNameToNull(attrName);
		attrValue = stripAttrValueToNull(attrValue);

		if (attrName != null && attrValue != null) {
			attributes.put(attrName, attrValue);
			return true;
		}

		return false;
	}

	// 从一对元素中抽取键值对
	private boolean tryCollectKeyValuePair(Element nameElement, Element valueElement) {
		if (nameElement == null || valueElement == null) {
			return false;
		}

		return tryCollectKeyValuePair(nameElement.text(), valueElement.text());
	}

}
