/**
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

package org.qiwur.scent.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.nodes.Indicator;

/**
 * Utility to create Hadoop {@link Configuration}s that include Scent-specific
 * resources.
 */
public class ScentConfiguration {

  public static final String UUID_KEY = "scent.conf.uuid";

  protected static final Logger logger = LogManager.getLogger(Configuration.class);

  private ScentConfiguration() {
  } // singleton

  /*
   * Configuration.hashCode() doesn't return values that correspond to a unique
   * set of parameters. This is a workaround so that we can track instances of
   * Configuration created by Scent.
   */
  private static void setUUID(Configuration conf) {
    UUID uuid = UUID.randomUUID();
    conf.set(UUID_KEY, uuid.toString());
  }

  /**
   * Retrieve a Scent UUID of this configuration object, or null if the
   * configuration was created elsewhere.
   * 
   * @param conf
   *          configuration instance
   * @return uuid or null
   */
  public static String getUUID(Configuration conf) {
    return conf.get(UUID_KEY);
  }

  /**
   * Create a {@link Configuration} for Scent. This will load the standard Scent
   * resources, <code>scent-default.xml</code> and <code>scent-site.xml</code>
   * overrides.
   */
  public static Configuration create() {
    Configuration conf = new Configuration();
    setUUID(conf);
    addScentResources(conf);

    initStaticVariables(conf);

    // TODO : is it the right place to rebuild labels?
    rebuildLabels(conf);

    return conf;
  }

  /**
   * Create a {@link Configuration} from supplied properties.
   * 
   * @param addScentResources
   *          if true, then first <code>scent-default.xml</code>, and then
   *          <code>scent-site.xml</code> will be loaded prior to applying the
   *          properties. Otherwise these resources won't be used.
   * @param scentProperties
   *          a set of properties to define (or override)
   */
  public static Configuration create(boolean addScentResources, Properties scentProperties) {
    Configuration conf = new Configuration();
    setUUID(conf);

    if (addScentResources) {
      addScentResources(conf);
    }

    for (Entry<Object, Object> e : scentProperties.entrySet()) {
      conf.set(e.getKey().toString(), e.getValue().toString());
    }

    // TODO : is it the right place to rebuild labels?
    rebuildLabels(conf);

    return conf;
  }

  public static void rebuildLabels(Configuration conf) {
    Set<String> labels = BlockLabel.mergeLabels(conf.getStringCollection("scent.classifier.block.labels"));
    conf.set("scent.classifier.block.labels", StringUtils.join(labels, ","));

    Collection<String> inheritableLabels = conf.getStringCollection("scent.classifier.block.inheritable.labels");
    for (String label : inheritableLabels) {
      BlockLabel.inheritableLabels.add(BlockLabel.fromString(label));
    }
  }

  /**
   * Add the standard Scent resources to {@link Configuration}.
   * 
   * @param conf
   *          Configuration object to which configuration is to be added.
   */
  private static Configuration addScentResources(Configuration conf) {
    String defaultResource = "scent-default.xml";
    String specifiedResource = "scent-site.xml";

    conf.addResource(defaultResource);
    conf.addResource(specifiedResource);

    File file = new File(conf.getResource(defaultResource).getPath());
    if (!file.exists()) {
      logger.fatal("{} does not exists", defaultResource);
    }

    file = new File(conf.getResource(specifiedResource).getPath());
    if (!file.exists()) {
      logger.fatal("{} does not exists", specifiedResource);
    }

    try {
      for (String resource : conf.getStrings("scent.conf.import")) {
        file = new File(file.getParent() + File.separatorChar + resource);

        conf.addResource(new FileInputStream(file));
      }
    } catch (FileNotFoundException e) {
      logger.error(e);
    }

    return conf;
  }

  private static void initStaticVariables(Configuration conf) {
    // TODO : use jsoup.init() or something else to keep jsoup sub system independent
    Indicator.separators = conf.getStrings("scent.stat.indicator.separators", Indicator.separators);
  }
}
