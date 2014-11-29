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
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;

import org.apache.avro.util.Utf8;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.gora.mapreduce.GoraReducer;
import org.apache.gora.store.DataStore;
import org.apache.gora.store.DataStoreFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.storage.Bytes;
import org.apache.nutch.storage.TableUtil;
import org.apache.nutch.storage.WebPage;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.storage.PageBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Scans the web table and create host entries for each unique host.
 * 
 * 
 **/
public class SegmentJob implements Tool {

  public static final Logger LOG = LoggerFactory.getLogger(SegmentJob3.class);
  
  private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

  private Configuration conf;

  static {
    FIELDS.add(WebPage.Field.STATUS);
  }

  /**
   * Maps each WebPage to a host key.
   */
  public static class Mapper extends GoraMapper<String, WebPage, Text, PageBlock> {

    @Override
    protected void map(String key, WebPage value, Context context) throws IOException, InterruptedException {
      LOG.debug("map");

      String reversedUrl = TableUtil.getReversedHost(key);

      PageBlock block = new PageBlock();

      long now = System.currentTimeMillis();
      block.setBaseUrl("http://123.com");
      // block.setXpath(value);
      block.setBaseSequence(1);
      block.setBuildTime(now);
      block.setContent(ByteBuffer.wrap(Bytes.toBytes("<html></html>")));
      block.setBatchId(new Utf8("111111111"));
      java.util.Map<java.lang.CharSequence,java.lang.CharSequence> kvs = Maps.newHashMap();
      block.setKvpairs(kvs);
      block.setMarkers(kvs);

      context.write(new Text(reversedUrl), block);
    }
  }

  public static class Reducer extends GoraReducer<Text, PageBlock, String, PageBlock> {
    @Override
    protected void reduce(Text key, Iterable<PageBlock> values, Context context)
      throws IOException, InterruptedException {

      LOG.debug("reduce");

      int i = 0;
      for (PageBlock value : values) {
        ++i;
        context.write(key.toString() + i, value);
      }
    }
  }

  public SegmentJob() {
  }

  public SegmentJob(Configuration conf) {
    setConf(conf);
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  /**
   * Creates and returns the {@link Job} for submitting to Hadoop mapreduce.
   * @param inStore
   * @param outStore
   * @param numReducer
   * @return
   * @throws IOException
   */
  public Job createJob(DataStore<String, WebPage> inStore, DataStore<String, PageBlock> outStore, int numReducer) 
      throws IOException {
    Job job = new Job(getConf());
    job.setJobName("Segment Job");

    LOG.info("\nCreating Hadoop Job: " + job.getJobName());
    job.setNumReduceTasks(numReducer);
    job.setJarByClass(getClass());

    /* Mappers are initialized with GoraMapper.initMapper() or 
     * GoraInputFormat.setInput()*/
    GoraMapper.initMapperJob(job, inStore, Text.class, PageBlock.class, SegmentJob.Mapper.class, true);

    /* Reducers are initialized with GoraReducer#initReducer().
     * If the output is not to be persisted via Gora, any reducer
     * can be used instead. */
    GoraReducer.initReducerJob(job, outStore, SegmentJob.Reducer.class);

    return job;
  }

  @Override
  public int run(String[] args) throws Exception {
    DataStore<String, WebPage> inStore = DataStoreFactory.getDataStore(String.class, WebPage.class, conf);
    DataStore<String, PageBlock> outStore = DataStoreFactory.getDataStore(String.class, PageBlock.class, conf);

    Job job = createJob(inStore, outStore, 3);
    boolean success = job.waitForCompletion(true);

    inStore.close();
    outStore.close();

    LOG.info("Segment completed with " + (success ? "success" : "failure"));

    return success ? 0 : 1;
  }

  public static void main(String[] args) throws Exception {
    final int res = ToolRunner.run(ScentConfiguration.create(), new SegmentJob(), args);
    System.exit(res);
  }
}
