/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.qiwur.scent.segment;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.gora.mapreduce.GoraReducer;
import org.apache.gora.store.DataStore;
import org.apache.gora.store.DataStoreFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.nutch.storage.Bytes;
import org.apache.nutch.storage.Nutch;
import org.apache.nutch.storage.TableUtil;
import org.apache.nutch.storage.WebPage;
import org.jsoup.block.DomSegment;
import org.qiwur.scent.storage.PageBlock;
import org.qiwur.scent.storage.ScentMark;
import org.qiwur.scent.utils.FileUtil;
import org.qiwur.scent.utils.ScentConfiguration;
import org.qiwur.scent.utils.SegmentUtil;
import org.qiwur.scent.utils.StringUtil;

/**
 * Scans the web table and create host entries for each unique host.
 * 
 **/
public class SegmentJob implements Tool {

  protected static final Logger LOG = LogManager.getLogger(SegmentJob.class);

  private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

  private Configuration conf;

  static {
    FIELDS.add(WebPage.Field.CONTENT);
    FIELDS.add(WebPage.Field.CONTENT_TYPE);
    FIELDS.add(WebPage.Field.MARKERS);
  }

  static final String RegexParamName = "scent.segment.webpage.url.regex";
  static final String LimitParamName = "scent.segment.webpage.read.limit";
  static final String WriteDbParamName = "scent.segment.write.db";

  /** Filters the entries from the table based on a regex **/
  public static class Mapper extends GoraMapper<String, WebPage, Text, PageBlock> {
    private Configuration conf;
    private Pattern pattern = null;
    private boolean writeDb = false;
    private int limit = -1;
    private int count = 0;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      conf = context.getConfiguration();

      pattern = Pattern.compile(conf.get(RegexParamName, ".+"));
      limit = conf.getInt(LimitParamName, -1);
      writeDb = conf.getBoolean(WriteDbParamName, false);
    }

    @Override
    protected void map(String key, WebPage page, Context context) throws IOException, InterruptedException {
      // filtering
      if (org.apache.nutch.storage.Mark.FETCH_MARK.checkMark(page) == null) {
        return;
      }

      // content type must be html/xhtml
      if (page.getContentType() != null && !page.getContentType().toString().contains("html")) {
        return;
      }

      // check whether the Key passes the regex
      String url = TableUtil.unreverseUrl(key.toString());
      if (!pattern.matcher(url).matches()) {
        return;
      }

      // check row limit
      if (limit > 0 && count > limit) {
        return;
      }

      ++count;

      // batch id
      long now = System.currentTimeMillis();
      int randomSeed = Math.abs(new Random().nextInt());
      String batchId = (now / 1000) + "-" + randomSeed;

      Set<DomSegment> segments = SegmentUtil.segment(Bytes.toString(page.getContent()), url, conf);
      for (DomSegment segment : segments) {
        if (writeDb) {
          PageBlock block = SegmentUtil.buildBlock(segment, now, batchId);

          // use block digest to be the combine key to filter the common blocks such as page headers
          String combineKey = block.getCodeDigest() + "." + block.getTextDigest();
          context.write(new Text(combineKey), block);
        }
        else {
          String output = getBlockRepresentation(url, segment);
          System.out.println(output);
        }
      } // for

      LOG.debug("{}. {} segments in {}", count, segments.size(), url);
    }
  }

  public static class Combiner extends GoraReducer<Text, PageBlock, Text, PageBlock> {
    @Override
    protected void reduce(Text key, Iterable<PageBlock> blocks, Context context)
      throws IOException, InterruptedException {

      int counter = 0;
      for (PageBlock block : blocks) {
        if (counter == 0) {
          // write the first record only and ignore the rest because they are the same
          // this is the meaning of "combine"!
          context.write(key, block);
        }

        ++counter;
      }

      boolean debug = false;
      if (debug) {
        counter = 0;
        String pageUrl = null;
        StringBuilder debugContent = new StringBuilder();
        for (PageBlock block : blocks) {
          if (counter == 0) {
            pageUrl = block.getBaseUrl().toString();
          }
  
          ++counter;
  
          if (counter > 10) {
            debugContent.append("\n:content_begin:\n");
            debugContent.append(Bytes.toString(block.getContent()));
            debugContent.append("\n:content_end:\n");
          }
        }
  
        if (counter > 10) {
          LOG.debug("Combiner : page block {} count {}", key, counter);
          cache(pageUrl, debugContent.toString(), "block.html");
        }
      } // debug
    } // reduce
  }

  /**
   * Map [0, maxSequence] to [0, numPartitions]
   * */
  public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<Text, PageBlock> {
    private int maxSequence = 1;

    @Override
    public int getPartition(Text key, PageBlock block, int numPartitions) {
      int sequence = block.getBaseSequence();
      if (sequence > maxSequence) {
        maxSequence = sequence;
      }

      float tmp = (1.0f * sequence / maxSequence) * numPartitions;
      int r = (int)tmp;
      if (r > numPartitions - 1) {
        r = numPartitions - 1;
      }

      return r;
    }
  }

  public static class Reducer extends GoraReducer<Text, PageBlock, String, PageBlock> {
    @Override
    protected void reduce(Text key, Iterable<PageBlock> blocks, Context context)
      throws IOException, InterruptedException {

      int counter = 0;
      for (PageBlock block : blocks) {
        if (counter == 0) {
          ScentMark.SEGMENT_MARK.putMark(block, block.getBatchId().toString());

          // write the first record only and ignore the rest because they are the same
          String K3 = block.getBaseUrl() + "#" + block.getBaseSequence();
          context.write(K3, block);
        }

        ++counter;
      }

      if (counter > 2) {
        LOG.debug("Reducer : page block {} count {}", key, counter);
      }
    }
  }

  public SegmentJob() {
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  public int segment(String regex, int limit, boolean writeDb)
      throws IOException, ClassNotFoundException, InterruptedException {
    Validate.notNull(conf);

    conf.set(RegexParamName, regex);
    conf.setInt(LimitParamName, limit);
    conf.setBoolean(WriteDbParamName, writeDb);

    Job job = new Job(getConf());
    job.setJobName("SegmentJob");

    DataStore<String, WebPage> pageStore = DataStoreFactory.getDataStore(String.class, WebPage.class, conf);
    DataStore<String, PageBlock> blockStore = DataStoreFactory.getDataStore(String.class, PageBlock.class, conf);

    LOG.info("\nCreating Hadoop Job: " + job.getJobName());
    // job.setNumReduceTasks(getConf().getInt("scent.reduce.task.number", 50));
    job.setJarByClass(getClass());

    job.setPartitionerClass(SegmentJob.Partitioner.class);
    job.setCombinerClass(SegmentJob.Combiner.class);

    GoraMapper.initMapperJob(job, pageStore, Text.class, PageBlock.class, SegmentJob.Mapper.class, true);
    GoraReducer.initReducerJob(job, blockStore, SegmentJob.Reducer.class);

    boolean success = job.waitForCompletion(true);

    pageStore.close();
    blockStore.close();

    LOG.info("SegmentJob completed with " + (success ? "success" : "failure"));

    return success ? 0 : 1;
  }

  private static String getBlockRepresentation(String key, DomSegment segment) {
    StringBuffer sb = new StringBuffer();
    sb.append("key:\t" + key).append("\n");
    sb.append("baseUrl:\t" + segment.getBaseUrl()).append("\n");
    sb.append("baseSequence:\t" + segment.baseSequence()).append("\n");
    sb.append("cssSelector:\t" + segment.cssSelector()).append("\n");

    sb.append("content:start:\n");
    String text = segment.text();
    if (text.length() > 10) {
      sb.append(StringUtils.substring(segment.text(), 0, 300));
    }
    else {
      sb.append(segment.html());
    }
    sb.append("\ncontent:end:\n");

    return sb.toString();
  }

  @Override
  public int run(String[] args) throws Exception {
    Validate.notNull(conf);

    if (args.length < 1) {
      System.err.println("Usage: SegmentJob -regex <regex> \n \t \t      [-crawlId <id>] [-limit <limit>] [-writeDb]");
      System.err.println("    -regex <regex> - filter on the URL of the webtable entry");
      System.err.println("    -crawlId <id>  - the id to prefix the schemas to operate on, \n \t \t     (default: storage.crawl.id)");
      System.err.println("    -limit <limit> - the limit reading webpage table");
      System.err.println("    -writeDb       - write segment result into pageblock table");
      return -1;
    }

    String regex = ".+";
    int limit = -1;
    boolean writeDb = false;
    try {
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-regex")) {
          regex = args[++i];
        } if (args[i].equals("-limit")) {
          limit = StringUtil.tryParseInt(args[++i], -1);
        } else if (args[i].equals("-crawlId")) {
          getConf().set(Nutch.CRAWL_ID_KEY, args[++i]);
        } else if (args[i].equals("-writeDb")) {
          writeDb = true;
        }
      }

      segment(regex, limit, writeDb);
    }catch (Exception e) {
      LOG.error("SegmentJob: {}", e.toString());
      return -1;
    }

    return 0;
  }

  private static void cache(String pageUri, String content, String suffix) {
    try {
      File file = new File(FileUtil.getFileForPage(pageUri, "/tmp/segment", suffix));
      FileUtils.write(file, content, "utf-8");

      LOG.debug("saved in {}", file.getAbsoluteFile());
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  public static void main(String[] args) throws Exception {
    final int res = ToolRunner.run(ScentConfiguration.create(), new SegmentJob(), args);
    System.exit(res);
  }
}
