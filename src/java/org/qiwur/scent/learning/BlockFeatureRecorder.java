package org.qiwur.scent.learning;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.block.BlockLabel;
import org.jsoup.block.DomSegment;
import org.jsoup.block.DomSegments;
import org.jsoup.nodes.Document;

public class BlockFeatureRecorder {

  private static final Logger logger = LogManager.getLogger(BlockFeatureRecorder.class);

  private final DomSegments segments;

  private final Configuration conf;

  protected final String[] labels;

  protected final StringBuilder reporter = new StringBuilder();

  protected final String baseDir;

  public BlockFeatureRecorder(Document doc, Configuration conf) {
    Validate.notNull(doc);

    this.segments = doc.domSegments();
    this.conf = conf;
    this.labels = conf.getStrings("scent.classifier.block.labels");
    this.baseDir = conf.get("scent.sgd.train.base.dir");
  }

  public void process() {
    for (String label : labels) {
      for (DomSegment segment : segments.getAll(BlockLabel.fromString(label))) {
        saveBlock(segment, label);
      }
    }
  }

  private void saveBlock(DomSegment segment, String label) {
    try {
      File dir = new File(getOutputDir(segment, label));
      if (!dir.exists()) {
        FileUtils.forceMkdir(dir);
      }

      String path = dir.getAbsolutePath() + File.separator + DigestUtils.md5Hex(segment.block().baseUri()) + ".htm";
      PrintWriter writer = new PrintWriter(path, "UTF-8");
      writer.write(segment.outerHtml());
      writer.close();
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private String getOutputDir(DomSegment segment, String label) {
    return baseDir + File.separator + "segment" + File.separator + label;
  }
}
