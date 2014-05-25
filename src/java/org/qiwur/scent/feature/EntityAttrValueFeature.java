package org.qiwur.scent.feature;

import java.io.IOException;

import org.qiwur.scent.utils.FiledLines;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Multiset;

public final class EntityAttrValueFeature extends FiledLines {

	public static final int MinAttributeValueSize = 1;
	public static final int MaxAttributeValueSize = 200;

	public static final String BadAttributeValueWordsFile = "conf/bad-attribute-value-words.txt";
	public static final String KnownColorsFile = "conf/known-colors.txt";

	private static EntityAttrValueFeature instance = null;

	private EntityAttrValueFeature() throws IOException {
		super(StringUtil.LongerFirstComparator, BadAttributeValueWordsFile, KnownColorsFile);
	}

	public static EntityAttrValueFeature getInstance() {
		if (instance == null) {
			try {
				instance = new EntityAttrValueFeature();
			} catch (IOException e) {
				logger.error(e);
			}
		}

		return instance;
	}

	public static Multiset<String> knownColors() {
		return getInstance().getLines(KnownColorsFile);
	}

	public static Multiset<String> badAttriValueWords() {
		return getInstance().getLines(BadAttributeValueWordsFile);
	}

	public static boolean validate(String name) {
		// 商品标题，至少10个字符，最多200个字符
    	if (name == null || name.length() <= MinAttributeValueSize || name.length() >= MaxAttributeValueSize) {
    		return false;
    	}

    	return true;
	}

	// 预处理
	public static String preprocess(String name) {
		if (!validate(name)) return "";

		name = StringUtil.trimNonChar(name);
		for (String word : badAttriValueWords()) {
			name = name.replace(word, "");
		}

		if (!validate(name)) return "";

		return name;
    }
}
