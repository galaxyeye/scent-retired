package org.qiwur.scent.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class TestString {

  static void replaceSpecifiedCharacters() {
    String title = "当当 价：";
    int pos = StringUtils.lastIndexOf(title, '-');
    String suffix = title.substring(pos + 1).replaceAll("[\\s:：]", "");

    System.out.println(suffix);
  }

  static void getDomainFromUri() {
    String regex = "^((http:|https:)*[a-zA-Z0-9-]{0,}\\.)";
    ArrayList<String> cases = new ArrayList<String>();
    cases.add("http://www.google.com");
    cases.add("ww.socialrating.it");
    cases.add("www-01.hopperspot.com");
    cases.add("wwwsupernatural-brasil.blogspot.com");
    cases.add("http://xtop10.net");
    cases.add("http://dangdang.com");
    cases.add("zoyanailpolish.blogspot.com");
    cases.add("http://www.qiwur.com/wiki/首页");
    cases.add("http://product.suning.com/102603784.html");
    cases.add("/home/vincent/Downloads/scent/jd348143.html");

    for (String t : cases) {
      // InternetDomainName domain = InternetDomainName.from(t);
      // InternetDomainName topDomain = domain.topPrivateDomain();

      // String res = t.replaceAll(regex, "");

      // String[] parts = t.split("\\.");

      // System.out.println(Arrays.asList(parts));

      // String d = t.substring(t.indexOf(".") + 1);
      //
      // if (!d.contains(".")) {
      // d = t.replaceAll("(http://|https://)", "");
      // }

      String d = NetUtil.getDomain(t);

      System.out.println(d);
    }
  }

  static void stringSetTest() {
    Set<String> candidates = new HashSet<String>();
    Set<String> candidates2 = new HashSet<String>();

    candidates.add("nav_top");
    candidates.add("mainMenu");
    candidates.add("image-detail");
    candidates.add("image      detail");

    for (String candidate : candidates) {
      candidate = StringUtils.join(candidate.split("(?=\\p{Upper})"), " ");
      candidates2.add(candidate.replaceAll("[-_]", " ").toLowerCase());
    }

    System.out.print(candidates2);
  }

  @Test
  public void testPattern() {
    String text = "￥799.00 (降价通知)";
    // text = text.replaceAll("¥|,|'", "");
    System.out.println(text);

    String patternString = "[1-9](,{0,1}\\d+){0,8}(\\.\\d{1,2})|[1-9](,{0,1}\\d+){0,8}";

    Pattern pattern = Pattern.compile(patternString);
    Matcher matcher = pattern.matcher(text);

    // if (matcher.find()) {
    // System.out.print(matcher.group());
    // }

    int count = 0;
    while (matcher.find()) {
      count++;
      System.out.println("found: " + count + " : " + matcher.start() + " - " + matcher.end() + ", " + matcher.group());
    }
  }

  @Test
  public void testTrimNonChar() {
    String text = "天王表 正品热卖 机械表 全自动 男士商务气派钢带手表GS5733T/D尊贵大气 个性表盘  ";

    assertEquals(text.trim(), "天王表 正品热卖 机械表 全自动 男士商务气派钢带手表GS5733T/D尊贵大气 个性表盘  ");
    assertEquals(StringUtil.trimNonChar(text), "天王表 正品热卖 机械表 全自动 男士商务气派钢带手表GS5733T/D尊贵大气 个性表盘");
  }

  public static void main(String[] args) throws Exception {

  }
}
