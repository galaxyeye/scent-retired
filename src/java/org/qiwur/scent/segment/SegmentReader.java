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
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.gora.mapreduce.GoraReducer;
import org.apache.gora.query.Query;
import org.apache.gora.query.Result;
import org.apache.gora.store.DataStore;
import org.apache.gora.store.DataStoreFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.storage.Bytes;
import org.apache.nutch.storage.Nutch;
import org.apache.nutch.storage.TableUtil;
import org.apache.nutch.storage.WebPage;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.storage.PageBlock;
import org.qiwur.scent.utils.ScentConfiguration;
import org.qiwur.scent.utils.SegmentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scans the web table and create host entries for each unique host.
 * 
 **/
public class SegmentReader implements Tool {

  public static final Logger LOG = LoggerFactory.getLogger(SegmentReader.class);

  private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

  private Configuration conf;

  // TODO : use fields
  static {
    FIELDS.add(WebPage.Field.STATUS);
  }

  static final String RegexParamName = "webtable.url.regex";

  private static enum Op {READ, SEG};

  /** Filters the entries from the table based on a regex **/
  public static class Mapper extends GoraMapper<String, WebPage, Text, PageBlock> {
    private Configuration conf;
    private Pattern pattern = null;

    @Override
    protected void map(String key, WebPage page, Context context) throws IOException, InterruptedException {
      // checks whether the Key passes the regex
      String url = TableUtil.unreverseUrl(key.toString());
      if (pattern == null || pattern.matcher(url).matches()) {
        long now = System.currentTimeMillis();
        for (DomSegment segment : SegmentUtil.segment(Bytes.toString(page.getContent()), conf)) {
          context.write(new Text(url), SegmentUtil.buildBlock(segment, now));
        }
      }
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      conf = context.getConfiguration();

      String regex = conf.get(RegexParamName, "");
      if (!regex.isEmpty()) {
        pattern = Pattern.compile(regex);
      }
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

  public SegmentReader() {
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  public int segment(String output, String regex, Configuration config) throws IOException, ClassNotFoundException, InterruptedException {
    Configuration conf = getConf();

    conf.set(RegexParamName, regex);

    Job job = new Job(getConf());
    job.setJobName("SegmentJob");

    DataStore<String, WebPage> inStore = DataStoreFactory.getDataStore(String.class, WebPage.class, conf);
    DataStore<String, PageBlock> outStore = DataStoreFactory.getDataStore(String.class, PageBlock.class, conf);

    LOG.info("\nCreating Hadoop Job: " + job.getJobName());
    job.setNumReduceTasks(getConf().getInt("scent.reduce.task.number", 3));
    job.setJarByClass(getClass());

    GoraMapper.initMapperJob(job, inStore, Text.class, PageBlock.class, SegmentReader.Mapper.class, true);
    GoraReducer.initReducerJob(job, outStore, SegmentReader.Reducer.class);
    boolean success = job.waitForCompletion(true);

    inStore.close();
    outStore.close();

    LOG.info("Segment completed with " + (success ? "success" : "failure"));

    return success ? 0 : 1;
  }

  public void read(String key, boolean dumpContent, boolean dumpHeaders,
      boolean dumpLinks, boolean dumpText) throws Exception {
    DataStore<String, WebPage> pageStore = DataStoreFactory.getDataStore(String.class, WebPage.class, conf);
    Query<String, WebPage> query = pageStore.newQuery();

    String reversedUrl = TableUtil.reverseUrl(key);
    query.setKey(reversedUrl);

    Result<String, WebPage> result = pageStore.execute(query);
    boolean found = false;

    // should happen only once
    while (result.next()) {
      try {
        WebPage page = result.get();
        String skey = result.getKey();

        // we should not get to this point but nevermind
        if (page == null || skey == null) {
          break;
        }

        found = true;

        String url = TableUtil.unreverseUrl(skey);
        long now = System.currentTimeMillis();
        for (DomSegment segment : SegmentUtil.segment(Bytes.toString(page.getContent()), conf)) {
          System.out.println(getBlockRepresentation(url, segment, dumpContent,
              dumpHeaders, dumpLinks, dumpText));
        }
      }catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (!found) {
      System.out.println(key + " not found");
    }

    result.close();
    pageStore.close();
  }

  private static String getBlockRepresentation(String key, DomSegment segment,
      boolean dumpContent, boolean dumpHeaders, boolean dumpLinks, boolean dumpText) {
    StringBuffer sb = new StringBuffer();
    sb.append("key:\t" + key).append("\n");
    sb.append("baseUrl:\t" + segment.getBaseUrl()).append("\n");

    if (dumpContent) {
      sb.append("content:start:\n");
      sb.append(segment.html());
      sb.append("\ncontent:end:\n");
    }

    return sb.toString();
  }

  @Override
  public int run(String[] args) throws Exception {
    Validate.notNull(conf);

    if (args.length < 1) {
      System.err
          .println("Usage: WebTableReader (-url [url] | -dump <out_dir> [-regex regex]) \n \t \t      [-crawlId <id>] [-content] [-headers] [-links] [-text]");
      System.err.println("    -crawlId <id>  - the id to prefix the schemas to operate on, \n \t \t     (default: storage.crawl.id)");
      System.err.println("    -stats [-sort] - print overall statistics to System.out");
      System.err.println("    [-sort]        - list status sorted by host");
      System.err.println("    -url <url>     - print information on <url> to System.out");
      System.err.println("    -dump <out_dir> [-regex regex] - dump the webtable to a text file in \n \t \t     <out_dir>");
      System.err.println("    -content       - dump also raw content");
      System.err.println("    -headers       - dump protocol headers");
      System.err.println("    -links         - dump links");
      System.err.println("    -text          - dump extracted text");
      System.err.println("    [-regex]       - filter on the URL of the webtable entry");
      return -1;
    }
    String param = null;
    boolean content = false;
    boolean links = false;
    boolean text = false;
    boolean headers = false;
    boolean toSort = false;
    String regex = ".+";
    Op op = null;
    try {
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-url")) {
          param = args[++i];
          op = Op.READ;
        } else if (args[i].equals("-sort")) {
          toSort = true;
        } else if (args[i].equals("-segment")) {
          op = Op.SEG;
          param = args[++i];
        } else if (args[i].equals("-content")) {
          content = true;
        } else if (args[i].equals("-headers")) {
          headers = true;
        } else if (args[i].equals("-links")) {
          links = true;
        } else if (args[i].equals("-text")) {
          text = true;
        } else if (args[i].equals("-regex")) {
          regex = args[++i];
        } else if (args[i].equals("-crawlId")) {
          getConf().set(Nutch.CRAWL_ID_KEY, args[++i]);
        }
      }
      if (op == null) {
        throw new Exception("Select one of -url | -segment");
      }
      switch (op) {
      case READ:
        read(param, content, headers, links, text);
        break;
      case SEG:
        segment(param, regex, getConf());
        break;
      }
      return 0;
    }catch (Exception e) {
      LOG.error("SegmentJob: {}", e);
      return -1;
    }
  }

  public static void main(String[] args) throws Exception {
    final int res = ToolRunner.run(ScentConfiguration.create(), new SegmentReader(), args);
    System.exit(res);
  }
}
