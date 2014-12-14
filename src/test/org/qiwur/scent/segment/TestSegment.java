/*
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
 */
package org.qiwur.scent.segment;

import org.apache.commons.lang.StringUtils;
import org.apache.gora.query.Query;
import org.apache.gora.query.Result;
import org.apache.gora.store.DataStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.storage.Bytes;
import org.apache.nutch.storage.StorageUtils;
import org.apache.nutch.storage.WebPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jsoup.block.DomSegment;
import org.qiwur.scent.utils.ScentConfiguration;
import org.qiwur.scent.utils.SegmentUtil;

/**
 * This class provides common routines for setup/teardown of an in-memory data
 * store.
 */
public class TestSegment {

  protected Configuration conf;
  protected FileSystem fs;
  protected Path testdir = new Path("build/test/working");
  protected DataStore<String, WebPage> pageStore;
  protected boolean persistentDataStore = false;

  @Before
  public void setUp() throws Exception {
    conf = ScentConfiguration.create();
    // conf.set("storage.data.store.class", "org.apache.gora.memory.store.MemStore");
    fs = FileSystem.get(conf);
    pageStore = StorageUtils.createWebStore(conf, String.class, WebPage.class);
  }

  @After
  public void tearDown() throws Exception {
    // empty the database after test
    if (!persistentDataStore) {
      pageStore.deleteByQuery(pageStore.newQuery());
      pageStore.flush();
      pageStore.close();
    }

    fs.delete(testdir, true);
  }

  /** Prints out the entry to the standard out **/
  @Test
  public void read() throws Exception {
    Query<String, WebPage> query = pageStore.newQuery();
    Result<String, WebPage> result = pageStore.execute(query);

    // should happen only once
    while (result.next()) {
      try {
        WebPage page = result.get();
        String skey = result.getKey();
        // we should not get to this point but nevermind
        if (page == null || skey == null) {
          break;
        }

        long now = System.currentTimeMillis();
        for (DomSegment segment : SegmentUtil.segment(Bytes.toString(page.getContent()), page.getBaseUrl().toString(), conf)) {
          System.out.println(StringUtils.substring(segment.html(), 0, 20));
        }
      }catch (Exception e) {
        e.printStackTrace();
      }
    }

    result.close();
  }
}
