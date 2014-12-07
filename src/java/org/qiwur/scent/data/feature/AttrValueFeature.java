package org.qiwur.scent.data.feature;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Multiset;

public final class AttrValueFeature extends LinedFeature {

	public static final int MinAttributeValueSize = 1;
	public static final int MaxAttributeValueSize = 200;

	public static String BadAttributeValueWordsFile = "conf/feature/default/bad-attr-value-words.txt";

	public AttrValueFeature(Configuration conf, String[] featureFile) {
    super(conf, featureFile);

    BadAttributeValueWordsFile = conf.get("scent.bad.attr.value.words.file", "conf/feature/default/bad-attr-value-words.txt");
	}

	public Multiset<String> badWords() {
		return lines();
	}

	public boolean validate(String name) {
		// 商品标题，至少10个字符，最多200个字符
    	if (name == null || name.length() <= MinAttributeValueSize || name.length() >= MaxAttributeValueSize) {
    		return false;
    	}

    	return true;
	}

	// 预处理
  public String preprocess(String name) {
    if (!validate(name))
      return "";

    name = StringUtil.trimNonChar(name);
    for (String word : badWords()) {
      name = name.replace(word, "");
    }

    if (!validate(name))
      return "";

    return name;
  }
}
