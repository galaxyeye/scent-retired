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

package org.qiwur.scent.data.extractor;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.plugin.Extension;
import org.qiwur.scent.plugin.ExtensionPoint;
import org.qiwur.scent.plugin.PluginRepository;
import org.qiwur.scent.plugin.PluginRuntimeException;
import org.qiwur.scent.utils.ObjectCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates and caches {@link DataExtractor} plugins. DataExtractor plugins
 * should define the attribute "protocolName" with the name of the protocol that
 * they implement. Configuration object is used for caching. Cache key is
 * constructed from appending protocol name (eg. http) to constant
 * {@link DataExtractor#X_POINT_ID}.
 */
public class DataExtractorFactory {

  public static final Logger logger = LoggerFactory.getLogger(DataExtractorFactory.class);

  private final ExtensionPoint extensionPoint;

  private final Configuration conf;

  public DataExtractorFactory(Configuration conf) {
    this.conf = conf;
    this.extensionPoint = PluginRepository.get(conf).getExtensionPoint(DataExtractor.X_POINT_ID);
    if (this.extensionPoint == null) {
      throw new RuntimeException("x-point " + DataExtractor.X_POINT_ID + " not found.");
    }
  }

  /**
   * Returns the appropriate {@link DataExtractor} implementation for a url.
   * 
   * @param extractorName
   *          Extractor name
   * @return The appropriate {@link DataExtractor} implementation for a given
   *         extractor name.
   * @throws DataExtractorNotFound
   *           when DataExtractor can not be found for extractorName
   */
  public DataExtractor getExtractor(String extractorName) throws DataExtractorNotFound {
    ObjectCache objectCache = ObjectCache.get(conf);
    try {
      String cacheId = DataExtractor.X_POINT_ID + extractorName;

      if (objectCache.getObject(cacheId) != null) {
        return (DataExtractor) objectCache.getObject(cacheId);
      } else {
        Extension extension = findExtension(extractorName);
        if (extension == null) {
          throw new DataExtractorNotFound(extractorName);
        }

        DataExtractor extractor = (DataExtractor) extension.getExtensionInstance();
        objectCache.setObject(cacheId, extractor);

        return extractor;
      }
    } catch (PluginRuntimeException e) {
      throw new DataExtractorNotFound(extractorName, e.toString());
    }
  }

  private Extension findExtension(String name) throws PluginRuntimeException {
    Extension[] extensions = this.extensionPoint.getExtensions();

    for (int i = 0; i < extensions.length; i++) {
      Extension extension = extensions[i];

      if (extension.getAttribute("extractorName").contains(name)) {
    	  return extension;
      }
    }

    return null;
  }

//  public Collection<WebPage.Field> getFields() {
//    Collection<WebPage.Field> fields = new HashSet<WebPage.Field>();
//    for (Extension extension : this.extensionPoint.getExtensions()) {
//      DataExtractor protocol;
//      try {
//        protocol = (DataExtractor) extension.getExtensionInstance();
//        Collection<WebPage.Field> pluginFields = protocol.getFields();
//        if (pluginFields != null) {
//          fields.addAll(pluginFields);
//        }
//      } catch (PluginRuntimeException e) {
//        // ignore
//      }
//    }
//    return fields;
//  }
}
