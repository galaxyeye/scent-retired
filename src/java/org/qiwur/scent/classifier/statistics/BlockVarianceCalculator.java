package org.qiwur.scent.classifier.statistics;

import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.nodes.IndicatorIndex;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

/*
 * 寻找网页中的区块
 * 一个区块的特征是根节点的各个子孩子高度相似，也就是链接数、文本块数、图像数差不多
 * */
public class BlockVarianceCalculator {

  private static final Logger logger = LogManager.getFormatterLogger(BlockVarianceCalculator.class);

  /*
   * 样本的最小孩子数
   */
  private final int numMinItem;

  /*
   * 最大样本数
   */
  private final int sampleSize;

  /*
   * 统计指标
   */
  private final String[] indicators;

  /*
   * 统计结果参考值
   */
  private final int varianceReferenceValue;

  /*
   * 是否打印统计结果细节
   */
  private final boolean logVariance;

  private final String columnSeparator = "^^";

  private String reportHeader = "";
  private final Set<String> reportRows = Sets.newLinkedHashSet();

  private final Document doc;

  private final Configuration conf;

  public BlockVarianceCalculator(Document doc, Configuration conf) {
    numMinItem = conf.getInt("scent.stat.segment.item.min", 3);
    sampleSize = conf.getInt("scent.stat.segment.variance.sample.max", 100);
    logVariance = conf.getBoolean("scent.stat.segment.variance.log", false);
    varianceReferenceValue = conf.getInt("scent.stat.segment.variance.reference.value", 3);
    indicators = conf.getStrings("scent.stat.segment.variance.indicators");

    this.doc = doc;
    this.conf = conf;
  }

  public void calculate() {
    if (indicators.length == 0) {
      logger.error("can not calculate block variance, no any indicator");
      return;
    }

    // 寻找内部结构相似度高的区域
    TreeMultimap<Double, Element> blocks = calculateVariance(doc.indicatorIndex(Indicator.C));
    doc.indicatorIndex(IndicatorIndex.Blocks, blocks);
  }

  public String getVarianceString() {
    StringBuilder sb = new StringBuilder();

    sb.append(reportHeader);
    for (String row : reportRows) {
      sb.append(row);
    }

    return sb.toString();
  }

  private TreeMultimap<Double, Element> calculateVariance(TreeMultimap<Double, Element> sampleRoots) {
    TreeMultimap<Double, Element> blocks = TreeMultimap.create();
    final int minDepth = doc.body().depth() + 1;

    DescriptiveStatistics stats[] = new DescriptiveStatistics[indicators.length + 1];
    for (int i = 0; i < stats.length; ++i) {
      stats[i] = new DescriptiveStatistics();
    }

    if (logVariance) {
      buildLogHeader();
    }

    // 样本集是以直接子孩子的个数排序的，孩子数多的排在前面
    int counter = 0;
    for (Double _child : sampleRoots.keys()) {
      if (_child < numMinItem || counter++ > sampleSize) {
        // 已经计算了足够多的数据，不用继续下去，直接结束
        break;
      }

      for (Element root : sampleRoots.get(_child)) {
        if (!probablyCandidate(root, minDepth, sampleSize)) {
          // 确定是噪音数据点，计算下一个
          continue;
        }

        stats = calculate(root, stats);

        double varianceMean = stats[indicators.length].getMean();

        // 一组高度相似的数据，平均方差一般小于1，这里放宽到3
        if (varianceMean < varianceReferenceValue) {
          blocks.put(varianceMean, root);
        }

        if (logVariance) {
          buildLogLine(root, stats);
        }
      }
    } // for

    return blocks;
  }

  private DescriptiveStatistics[] calculate(Element root, DescriptiveStatistics[] stats) {
    // 本轮计算初始化
    for (DescriptiveStatistics stat : stats) {
      stat.clear();
    }

    /*
     * 计算本样本的各个孩子的特征指标的方差
     */
    for (Element child : root.children()) {
      for (int i = 0; i < indicators.length; ++i) {
        stats[i].addValue(child.indic(indicators[i]));
      }
    }

    for (int i = 0; i < indicators.length; ++i) {
      stats[indicators.length].addValue(stats[i].getVariance());
    }

    return stats;
  }

  private void buildLogHeader() {
    reportHeader = String.format("%-40s%s%13s%s", "Element", columnSeparator, "-child", columnSeparator);

    for (int i = 0; i < indicators.length; ++i) {
      reportHeader += String.format("%-13s%s", indicators[i], columnSeparator);
    }

    reportHeader += String.format("%-13s%s", "Mean", columnSeparator);
    reportHeader += "\n";
  }

  private void buildLogLine(Element root, DescriptiveStatistics[] stats) {
    String row = String.format("%-40s%s%-13.2f%s", root.prettyName(), columnSeparator, root.indic(Indicator.C), columnSeparator);

    for (int i = 0; i < stats.length; ++i) {
      double value = stats[i].getVariance();

      if (i == stats.length - 1) {
        value = stats[i].getMean();
      }

      row += String.format("%-13.2f%s", value, columnSeparator);      
    }

    row += "\n";

    reportRows.add(row);
  }

  /**
	 * a primary filter
	 * */
  private boolean probablyCandidate(Element root, int minDepth, int sampleSize) {
    if (root.depth() < minDepth) {
      return false;
    }

    // And more?
    if (StringUtil.in(root.tagName(), "form", "style")) {
      return false;
    }

    double _img = root.indic(Indicator.IMG);
    double _ch = root.indic(Indicator.CH);
    double _a = root.indic(Indicator.A);
    double _blk_txt = root.indic(Indicator.TB);

    if (_img >= 3 || _a >= 3) {
      return true;
    }

    if (_img + _blk_txt + _a <= 3) {
      return false;
    }

    if (_img == 0 && (_ch / _blk_txt <= 1 || _ch / _blk_txt >= 50)) {
      return false;
    }

    return true;
  }

}
