package org.qiwur.scent.utils;

import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import com.google.common.collect.TreeMultimap;

public class PrintUtil {

  public static void printIntegerTreeMultimap(TreeMultimap<Integer, Node> map, int limit) {
    for (Integer number : map.keySet()) {
      Set<Node> nodes = map.get(number);

      for (Node n : nodes) {
        if (limit-- <= 0)
          return;

        if (n instanceof Element) {
          Element e = (Element) n;
          // print("---------" + e.attr("class") + "--------------");

          if (e.text().length() < 1000) {
            System.out.println(number + ": " + e.text());
          }
        }
      }
    }
  }

  public static void printDoubleTreeMultimap(TreeMultimap<Double, Node> map, int limit) {
    for (Double number : map.keySet()) {
      Set<Node> nodes = map.get(number);

      for (Node n : nodes) {
        if (limit-- <= 0)
          return;

        if (n instanceof Element) {
          Element e = (Element) n;
          // print("---------" + e.attr("class") + "--------------");

          if (e.text().length() < 1000) {
            System.out.println(number + ": " + e.text());
          }
        }
      }
    }
  }

  public static void print(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }

}
