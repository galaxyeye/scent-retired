package org.qiwur.scent.jsoup;

import java.io.IOException;

import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.printer.DomStatisticsPrinter;

public class JsoupTestState {

  private static String fragment =           "<dl class='item fore1'>" +
            "<dt>特色栏目</dt>" +
            "<dd>" +
              "<div><a target=_blank href=http://my.jd.com/personal/guess.html>为我推荐</a></div>" +
              "<div><a target=_blank href=http://shipingou.jd.com/>视频购物</a></div>" +
              "<div><a target=_blank href=http://club.jd.com/>京东社区</a></div>" +
              "<div><a target=_blank href=http://xiaoyuan.jd.com/>校园频道</a></div>" +
              "<div><a target=_blank href=http://read.jd.com/>在线读书</a></div>" +
              "<div><a target=_blank href=http://diy.jd.com/>装机大师</a></div>" +
              "<div><a target=_blank href=http://market.jd.com/giftcard/>京东卡</a></div>" +
              "<div><a target=_blank href=http://channel.jd.com/jiazhuang.html>家装城</a></div>" +
              "<div><a target=_blank href=http://dapeigou.jd.com/>搭配购</a></div>" +
              "<div><a target=_blank href=http://xihuan.jd.com/>我喜欢</a></div>" +
            "</dd>" +
            "<dt>特色栏目</dt>" +
            "<dd>" +
              "<div><a target=_blank href=http://my.jd.com/personal/guess.html>为我推荐</a></div>" +
              "<div><a target=_blank href=http://shipingou.jd.com/>视频购物</a></div>" +
              "<div><a target=_blank href=http://club.jd.com/>京东社区</a></div>" +
              "<div><a target=_blank href=http://xiaoyuan.jd.com/>校园频道</a></div>" +
              "<div><a target=_blank href=http://read.jd.com/>在线读书</a></div>" +
              "<div><a target=_blank href=http://diy.jd.com/>装机大师</a></div>" +
              "<div><a target=_blank href=http://market.jd.com/giftcard/>京东卡</a></div>" +
              "<div><a target=_blank href=http://channel.jd.com/jiazhuang.html>家装城</a></div>" +
              "<div><a target=_blank href=http://dapeigou.jd.com/>搭配购</a></div>" +
              "<div><a target=_blank href=http://xihuan.jd.com/>我喜欢</a></div>" +
            "</dd>" +
          "</dl>";

  public static void main(String[] args) throws IOException {
    Document doc = Jsoup.parse(fragment);

    new DomStatisticsPrinter(doc).process();
  }
}
