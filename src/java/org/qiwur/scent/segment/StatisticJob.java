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

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.gora.mapreduce.GoraReducer;
import org.apache.gora.query.Query;
import org.apache.gora.store.DataStore;
import org.apache.gora.store.DataStoreFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.nutch.storage.Bytes;
import org.apache.nutch.storage.Nutch;
import org.apache.nutch.storage.TableUtil;
import org.apache.nutch.storage.WebPage;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.storage.PageBlock;
import org.qiwur.scent.utils.ScentConfiguration;
import org.qiwur.scent.utils.SegmentUtil;
import org.qiwur.scent.utils.StringUtil;

/**
 * Scans the web table and create host entries for each unique host.
 * 
 **/
public class StatisticJob implements Tool {

  protected static final Logger LOG = LogManager.getLogger(StatisticJob.class);

  private Configuration conf;

  static final String RegexParamName = "scent.segment.webpage.url.regex";
  static final String LimitParamName = "scent.segment.webpage.read.limit";

  public StatisticJob() {
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  /** Filters the entries from the table based on a regex **/
  public static class Mapper extends GoraMapper<String, WebPage, Text, PageBlock> {
    private Configuration conf;
    private Pattern pattern = null;
    private int limit = -1;
    private int count = 0;

    @Override
    protected void map(String key, WebPage page, Context context) throws IOException, InterruptedException {
      // checks whether the Key passes the regex
      String url = TableUtil.unreverseUrl(key.toString());

      if (pattern == null || pattern.matcher(url).matches()) {
        if (limit > 0 && count++ > limit) {
          return;
        }

        long now = System.currentTimeMillis();
        for (DomSegment segment : SegmentUtil.segment(Bytes.toString(page.getContent()), conf)) {
          context.write(new Text(url), SegmentUtil.buildBlock(segment, now));
        }
      }
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      conf = context.getConfiguration();

      pattern = Pattern.compile(conf.get(RegexParamName, ".+"));
      limit = conf.getInt(LimitParamName, -1);
    }
  }

  public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<Text, PageBlock> {
    @Override
    public int getPartition(Text key, PageBlock block, int numPartitions) {
      return block.getBaseSequence() / 10;
    }
  }

  public static class Reducer extends GoraReducer<Text, PageBlock, String, PageBlock> {
    @Override
    protected void reduce(Text key, Iterable<PageBlock> blocks, Context context)
      throws IOException, InterruptedException {

      for (PageBlock block : blocks) {
        context.write(key.toString(), block);
      }
    }
  }

  public int segment(String regex, int limit) throws IOException, ClassNotFoundException, InterruptedException {
    Validate.notNull(conf);

    conf.set(RegexParamName, regex);
    conf.setInt(LimitParamName, limit);

    Job job = new Job(getConf());
    job.setJobName("SegmentJob");

    DataStore<String, WebPage> pageStore = DataStoreFactory.getDataStore(String.class, WebPage.class, conf);
    Query<String, WebPage> query = pageStore.newQuery();
    String[] fields = Arrays.copyOfRange(WebPage._ALL_FIELDS, 1, WebPage._ALL_FIELDS.length);
    query.setFields(fields);

    LOG.info("\nCreating Hadoop Job: " + job.getJobName());
    job.setNumReduceTasks(getConf().getInt("scent.reduce.task.number", 50)); // how to choose this value?
    job.setJarByClass(getClass());

    job.setCombinerClass(StatisticJob.Reducer.class);
    job.setPartitionerClass(StatisticJob.Partitioner.class);

    GoraMapper.initMapperJob(job, pageStore, Text.class, PageBlock.class, StatisticJob.Mapper.class, true);

    Path outFolder = new Path("/tmp/1");
    FileOutputFormat.setOutputPath(job, outFolder);
    job.setOutputFormatClass(TextOutputFormat.class);

    boolean success = job.waitForCompletion(true);

    pageStore.close();

    LOG.info("Segment completed with " + (success ? "success" : "failure"));

    return success ? 0 : 1;
  }

  private static String getBlockRepresentation(String key, DomSegment segment) {
    StringBuffer sb = new StringBuffer();
    sb.append("key:\t" + key).append("\n");
    sb.append("baseUrl:\t" + segment.getBaseUrl()).append("\n");

    sb.append("content:start:\n");
    sb.append(segment.html());
    sb.append("\ncontent:end:\n");

    return sb.toString();
  }

  @Override
  public int run(String[] args) throws Exception {
    Validate.notNull(conf);

    if (args.length < 1) {
      System.err.println("Usage: StatisticJob -regex <regex> \n \t \t      [-crawlId <id>] [-limit <limit>] [-writedb]");
      System.err.println("    -regex <regex> - filter on the URL of the webtable entry");
      System.err.println("    -crawlId <id>  - the id to prefix the schemas to operate on, \n \t \t     (default: storage.crawl.id)");
      System.err.println("    -limit <limit> - the limit reading webpage table");
      return -1;
    }

    String regex = ".+";
    int limit = -1;
    try {
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-regex")) {
          regex = args[++i];
        } if (args[i].equals("-limit")) {
          limit = StringUtil.tryParseInt(args[++i], -1);
        } else if (args[i].equals("-crawlId")) {
          getConf().set(Nutch.CRAWL_ID_KEY, args[++i]);
        }
      }

      segment(regex, limit);
    }catch (Exception e) {
      LOG.error("SegmentJob: {}", e.toString());
      return -1;
    }

    return 0;
  }

  public static void main(String[] args) throws Exception {
    final int res = ToolRunner.run(ScentConfiguration.create(), new StatisticJob(), args);
    System.exit(res);
  }
}
