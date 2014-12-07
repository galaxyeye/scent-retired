package org.qiwur.scent.utils;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Set;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.storage.Bytes;
import org.qiwur.scent.classifier.statistics.BlockVarianceCalculator;
import org.qiwur.scent.diagnosis.BlockPatternFormatter;
import org.qiwur.scent.diagnosis.BlockVarianceFormatter;
import org.qiwur.scent.diagnosis.IndicatorsFormatter;
import org.qiwur.scent.diagnosis.ScentDiagnoser;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.segment.DomSegmentsBuilder;
import org.qiwur.scent.storage.PageBlock;

import com.google.common.collect.Maps;

public class SegmentUtil {
  public static Set<DomSegment> segment(String html, String baseUri, Configuration conf) {
    Document doc = Jsoup.parse(html, baseUri);
    // Calculate all indicators, this step is essential for the extraction
    doc.calculateIndicators();

    ScentDiagnoser diagnoser = new ScentDiagnoser(doc, conf);
    diagnoser.addFormatter(new IndicatorsFormatter(doc, conf));
    diagnoser.addFormatter(new BlockPatternFormatter(doc, conf));

    // calculate code blocks
    BlockVarianceCalculator calculator = new BlockVarianceCalculator(doc, conf);
    calculator.calculate();
    diagnoser.addFormatter(new BlockVarianceFormatter(calculator, conf));

    // build code segments, calculate code patterns
    Set<DomSegment> segmentSet = new DomSegmentsBuilder(doc, conf).build();

    return segmentSet;
  }

  public static PageBlock buildBlock(DomSegment segment, long time) {
    PageBlock block = PageBlock.newBuilder().build();

    block.setBaseUrl(segment.getBaseUrl() + "#" + segment.baseSequence());
    block.setBaseSequence(segment.baseSequence());
    block.setName(segment.name());
    block.setXpath(segment.xpath());
    block.setBuildTime(time);
    block.setContent(ByteBuffer.wrap(Bytes.toBytes(segment.html())));
    block.setContentMD5(segment.md5hex());
    // TODO : set a batch id
    block.setBatchId(new Date().toString());

    return block;
  }

  public static PageBlock mokePageBlock() {
    PageBlock block = new PageBlock();

    long now = System.currentTimeMillis();
    block.setBaseUrl("http://123.com");
    // block.setXpath(value);
    block.setBaseSequence(1);
    block.setName("moke-block");
    block.setBuildTime(now);
    block.setContent(ByteBuffer.wrap(Bytes.toBytes("<html></html>")));
    block.setBatchId(new Utf8("111111111"));
    java.util.Map<java.lang.CharSequence,java.lang.CharSequence> kvs = Maps.newHashMap();
    block.setKvpairs(kvs);
    block.setMarkers(kvs);

    return block;
  }
}
