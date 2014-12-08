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
package org.qiwur.scent.data.extractor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

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
import org.apache.nutch.storage.Nutch;
import org.apache.nutch.storage.WebPage;
import org.qiwur.scent.storage.PageBlock;
import org.qiwur.scent.storage.ScentMark;
import org.qiwur.scent.utils.ScentConfiguration;
import org.qiwur.scent.utils.StringUtil;

/**
 * Scans the web table and create host entries for each unique host.
 * 
 **/
public class ExtractJob implements Tool {

  protected static final Logger LOG = LogManager.getLogger(ExtractJob.class);

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
  public static class Mapper extends GoraMapper<String, PageBlock, Text, PageBlock> {
    private Configuration conf;

    @Override
    protected void map(String key, PageBlock block, Context context) throws IOException, InterruptedException {
      // filtering
      if (ScentMark.CLASSIFY_MARK.checkMark(block) == null) {
        return;
      }

      // TODO
      context.write(new Text(key), block);
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      conf = context.getConfiguration();
    }
  }

  public static class Reducer extends GoraReducer<Text, PageBlock, String, PageBlock> {

    private Configuration conf;
    private String[] labels;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      conf = context.getConfiguration();
      labels = conf.getStrings("scent.classifier.block.labels");
    }

    @Override
    protected void reduce(Text key, Iterable<PageBlock> blocks, Context context)
      throws IOException, InterruptedException {

      // TDOO : we can do road runner here

      // new PageExtractor(doc, conf);
      // extract segment via proper extractors

      // PageExtractor extractor = new PageExtractor(doc, conf);

      for (PageBlock block : blocks) {
        // String html = Bytes.toString(block.getContent());
        // Document doc = Jsoup.parseFragment();
        // PageEntity entity = extractor.extract(doc);
        // block.setKVs(entity.getKVs());
      }
    }
  }

  public ExtractJob() {
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  public int classify(String regex, int limit, boolean writeDb)
      throws IOException, ClassNotFoundException, InterruptedException {
    Validate.notNull(conf);

    conf.set(RegexParamName, regex);
    conf.setInt(LimitParamName, limit);
    conf.setBoolean(WriteDbParamName, writeDb);

    Job job = new Job(getConf());
    job.setJobName("SegmentJob");

    DataStore<String, PageBlock> store = DataStoreFactory.getDataStore(String.class, PageBlock.class, conf);

    LOG.info("\nCreating Hadoop Job: " + job.getJobName());
    // job.setNumReduceTasks(getConf().getInt("scent.reduce.task.number", 50));
    job.setJarByClass(getClass());

    GoraMapper.initMapperJob(job, store, Text.class, PageBlock.class, ExtractJob.Mapper.class, true);
    GoraReducer.initReducerJob(job, store, ExtractJob.Reducer.class);

    boolean success = job.waitForCompletion(true);

    store.close();

    LOG.info("SegmentJob completed with " + (success ? "success" : "failure"));

    return success ? 0 : 1;
  }

  @Override
  public int run(String[] args) throws Exception {
    Validate.notNull(conf);

    if (args.length < 1) {
      System.err.println("Usage: SegmentJob -regex <regex> \n \t \t      [-crawlId <id>] [-limit <limit>] [-writeDb]");
      System.err.println("    -regex <regex> - filter on the URL of the webtable entry");
      System.err.println("    -crawlId <id>  - the id to prefix the schemas to operate on, \n \t \t     (default: storage.crawl.id)");
      System.err.println("    -algo <algo>   - choose algorithm to classify. choices : 1. DecisionTree 2. LogisticRegression");
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

      classify(regex, limit, writeDb);
    }catch (Exception e) {
      LOG.error("SegmentJob: {}", e.toString());
      return -1;
    }

    return 0;
  }

  public static void main(String[] args) throws Exception {
    final int res = ToolRunner.run(ScentConfiguration.create(), new ExtractJob(), args);
    System.exit(res);
  }
}
