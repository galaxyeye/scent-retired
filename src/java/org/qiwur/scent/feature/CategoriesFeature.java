package org.qiwur.scent.feature;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.FiledLines;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Multiset;

public final class CategoriesFeature extends FiledLines {

	static final Logger logger = LogManager.getLogger(CategoriesFeature.class);

	public static final int MinProductCateSize = 1;

	public static final int MaxProductCateSize = 10;

	public static final String BadProductCateWordsFile = "conf/bad-product-cate-words.txt";

	static CategoriesFeature instance = null;

	private CategoriesFeature() throws IOException {
		super(StringUtil.LongerFirstComparator, BadProductCateWordsFile);
	}

	public static CategoriesFeature getInstance() {
		if (instance == null) {
			try {
				instance = new CategoriesFeature();
			} catch (IOException e) {
				logger.error(e);
			}
		}

		return instance;
	}

	public static Multiset<String> badWords() {
		return getInstance().getLines(BadProductCateWordsFile);
	}

	public static boolean validate(String name) {
		// 商品标题，至少10个字符，最多200个字符
    	if (name == null || name.length() <= MinProductCateSize || name.length() >= MaxProductCateSize) {
    		return false;
    	}

    	return true;
	}

	// 预处理
	public static String preprocess(String name) {
		if (!validate(name)) return "";

		name = StringUtil.stripNonChar(name, StringUtil.DefaultKeepChars).trim();
		for (String word : badWords()) {
			name = name.replace(word, "");
		}

		if (!validate(name)) return "";

		return name;
    }
}
