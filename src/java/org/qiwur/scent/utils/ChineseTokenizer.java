package org.qiwur.scent.utils;

import edu.fudan.ml.types.Dictionary;
import edu.fudan.nlp.cn.tag.CWSTagger;

public class ChineseTokenizer {
  static CWSTagger cwsTag = null;

  private static CWSTagger getCWSTagger() {
    if (cwsTag == null) {
      try {
        cwsTag = new CWSTagger("models/seg.m", new Dictionary("models/dict.txt"));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return cwsTag;
  }

  /**
   * Tokenizes the text and returns an array of tokens.
   * 
   * @param text
   *          The text
   * @return The tokens
   */
  public static String[] tokenize(final CharSequence text) {
    String[] tokens = getCWSTagger().tag2Array(text.toString());

    return tokens;
  }

  /**
   * Tokenizes the text and returns an array of single Chinese characters.
   * 
   * 
   * @param text
   *          The text
   * @return The tokens
   */
  public static String[] atomicTokenize(final CharSequence text) {
    String[] tokens = getCWSTagger().tag2Array(text.toString());

    return tokens;
  }
}
