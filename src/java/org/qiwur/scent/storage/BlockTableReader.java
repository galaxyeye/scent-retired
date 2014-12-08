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
package org.qiwur.scent.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.gora.mapreduce.GoraMapper;
import org.apache.gora.query.Query;
import org.apache.gora.query.Result;
import org.apache.gora.store.DataStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.storage.Bytes;
import org.apache.nutch.storage.Nutch;
import org.apache.nutch.storage.NutchJob;
import org.apache.nutch.storage.StorageUtils;
import org.apache.nutch.storage.TableUtil;
import org.qiwur.scent.utils.ScentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays information about the entries of the block table
 **/
public class BlockTableReader implements Tool {

  public static final Logger LOG = LoggerFactory.getLogger(BlockTableReader.class);

  private Configuration conf;
  
  /** Prints out the entry to the standard out **/
  private void read(String key, boolean dumpContent, boolean dumpText) 
      throws ClassNotFoundException, IOException, Exception {
    DataStore<String, PageBlock> datastore 
      = StorageUtils.createWebStore(getConf(), String.class, PageBlock.class);

    Query<String, PageBlock> query = datastore.newQuery();
    String reversedUrl = TableUtil.reverseUrl(key);
    query.setKey(reversedUrl);

    Result<String, PageBlock> result = datastore.execute(query);
    boolean found = false;
    // should happen only once
    while (result.next()) {
      try {
        PageBlock page = result.get();
        String skey = result.getKey();
        // we should not get to this point but nevermind
        if (page == null || skey == null)
          break;
        found = true;
        String url = TableUtil.unreverseUrl(skey);
        System.out.println(getPageRepresentation(url, page, dumpContent, dumpText));
      }catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (!found) {
      System.out.println(key + " not found");
    }

    result.close();
    datastore.close();
  }

  /** Filters the entries from the table based on a regex **/
  public static class PageBlockRegexMapper extends
      GoraMapper<String, PageBlock, Text, Text> {

    static final String regexParamName = "webtable.url.regex";
    static final String contentParamName = "webtable.dump.content";
    static final String linksParamName = "webtable.dump.links";
    static final String textParamName = "webtable.dump.text";
    static final String headersParamName = "webtable.dump.headers";

    public PageBlockRegexMapper() {
    }

    private Pattern regex = null;
    private boolean dumpContent, dumpHeaders, dumpLinks, dumpText;

    @Override
    protected void map(
        String key,
        PageBlock value,
        org.apache.hadoop.mapreduce.Mapper<String, PageBlock, Text, Text>.Context context)
        throws IOException, InterruptedException {
      // checks whether the Key passes the regex
      String url = TableUtil.unreverseUrl(key.toString());
      if (regex.matcher(url).matches()) {
        context.write(new Text(url), new Text(getPageRepresentation(key, value, dumpContent, dumpText)));
      }
    }

    @Override
    protected void setup(
        org.apache.hadoop.mapreduce.Mapper<String, PageBlock, Text, Text>.Context context)
        throws IOException, InterruptedException {
      regex = Pattern.compile(context.getConfiguration().get(regexParamName, ".+"));
      dumpContent = context.getConfiguration().getBoolean(contentParamName, false);
      dumpText = context.getConfiguration().getBoolean(textParamName, false);
    }
  }

  public void processDumpJob(String output, 
      Configuration config, String regex, boolean content, boolean text)
      throws IOException, ClassNotFoundException, InterruptedException {

    if (LOG.isInfoEnabled()) {
      LOG.info("PageBlock dump: starting");
    }

    Path outFolder = new Path(output);
    Job job = new NutchJob(getConf(), "db_dump");
    Configuration cfg = job.getConfiguration();
    cfg.set(PageBlockRegexMapper.regexParamName, regex);
    cfg.setBoolean(PageBlockRegexMapper.contentParamName, content);
    cfg.setBoolean(PageBlockRegexMapper.textParamName, text);

    DataStore<String, PageBlock> store = StorageUtils.createWebStore(
        job.getConfiguration(), String.class, PageBlock.class);
    Query<String, PageBlock> query = store.newQuery();
    //remove the __g__dirty field since it is not stored
    String[] fields = Arrays.copyOfRange(PageBlock._ALL_FIELDS, 1, PageBlock._ALL_FIELDS.length);
    query.setFields(fields);

    GoraMapper.initMapperJob(job, query, store, Text.class, Text.class,
        PageBlockRegexMapper.class, null, true);

    FileOutputFormat.setOutputPath(job, outFolder);
    job.setOutputFormatClass(TextOutputFormat.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    boolean success = job.waitForCompletion(true);

    if (LOG.isInfoEnabled()) {
      LOG.info("PageBlock dump: done");
    }
  }

  private static String getPageRepresentation(String key,
      PageBlock page, boolean dumpContent, boolean dumpText) {
    StringBuffer sb = new StringBuffer();
    sb.append("key:\t" + key).append("\n");
    sb.append("baseUrl:\t" + page.getBaseUrl()).append("\n");
    sb.append("baseSequence:\t" + page.getBaseSequence()).append("\n");
    sb.append("cssSelector:\t" + page.getCssSelector()).append("\n");
    sb.append("name:\t" + page.getName()).append("\n");
    sb.append("codeDigest:\t" + page.getCodeDigest()).append("\n");
    sb.append("textDigest:\t" + page.getTextDigest()).append("\n");
    sb.append("label:\t" + page.getLabel()).append("\n");
    sb.append("labelScore:\t" + page.getLabelScore()).append("\n");

    Map<CharSequence, CharSequence> markers = page.getMarkers();
    sb.append("markers:\t" + markers).append("\n");
    CharSequence batchId = page.getBatchId();
    if (batchId != null) {
      sb.append("batchId:\t" + batchId.toString()).append("\n");
    }

    Map<CharSequence, CharSequence> indicators = page.getIndicators();
    if (indicators != null) {
      Iterator<Entry<CharSequence, CharSequence>> iterator = indicators.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<CharSequence, CharSequence> entry = iterator.next();
        sb.append("metadata " + entry.getKey().toString())
          .append(" : \t")
          .append(entry.getValue()).append("\n");
      }
    }

    ByteBuffer content = page.getContent();
    if (content != null && dumpContent) {
      sb.append("content:start:\n");
      sb.append(Bytes.toString(content));
      sb.append("\ncontent:end:\n");
    }

    CharSequence text = page.getText();
    if (text != null && dumpText) {
      sb.append("text:start:\n");
      sb.append(text.toString());
      sb.append("\ntext:end:\n");
    }

    return sb.toString();
  }

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(ScentConfiguration.create(), new BlockTableReader(), args);
    System.exit(res);
  }

  private static enum Op {READ, DUMP};

  public int run(String[] args) throws Exception {
    if (args.length < 1) {
      System.err
          .println("Usage: PageBlockReader (-stats | -url [url] | -dump <out_dir> [-regex regex]) \n \t \t      [-crawlId <id>] [-content] [-headers] [-links] [-text]");
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
    boolean text = false;
    String regex = ".+";
    Op op = null;
    try {
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-url")) {
          param = args[++i];
          op = Op.READ;
        } else if (args[i].equals("-dump")) {
          op = Op.DUMP;
          param = args[++i];
        } else if (args[i].equals("-content")) {
          content = true;
        } else if (args[i].equals("-text")) {
          text = true;
        } else if (args[i].equals("-regex")) {
          regex = args[++i];
        } else if (args[i].equals("-crawlId")) {
          getConf().set(Nutch.CRAWL_ID_KEY, args[++i]);
        }
      }

      if (op == null) {
        throw new Exception("Select one of -url | -stats | -dump");
      }
      switch (op) {
      case READ:
        read(param, content, text);
        break;
      case DUMP:
        processDumpJob(param, getConf(), regex, content, text);
        break;
      }
      return 0;
    } catch (Exception e) {
      LOG.error("PageBlockReader: " + StringUtils.stringifyException(e));
      return -1;
    }
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }
}
