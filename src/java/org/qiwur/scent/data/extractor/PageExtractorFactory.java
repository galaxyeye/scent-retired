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
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.plugin.Extension;
import org.qiwur.scent.plugin.PluginRuntimeException;

/**
 * Creates and caches {@link PageExtractor} plugins. DataExtractor plugins
 * should define the attribute "protocolName" with the name of the protocol that
 * they implement. Configuration object is used for caching. Cache key is
 * constructed from appending protocol name (eg. http) to constant
 * {@link PageExtractor#X_POINT_ID}.
 */
public class PageExtractorFactory extends DataExtractorFactory {

  public PageExtractorFactory(Configuration conf) {
    super(conf);
  }

  /**
   * Returns the appropriate {@link PageExtractor} implementation
   * 
   * @param extractorName
   *          Extractor name
   * @return The appropriate {@link PageExtractor} implementation for a given
   *         extractor name.
   * @throws DataExtractorNotFound
   *           when DataExtractor can not be found for extractorName
   */
  public PageExtractor create(String extractorName) throws DataExtractorNotFound {
    try {
      Extension extension = findExtension(extractorName);
      if (extension == null) {
        throw new DataExtractorNotFound(extractorName);
      }

      return (PageExtractor) extension.getExtensionInstance();
    } catch (PluginRuntimeException e) {
      throw new DataExtractorNotFound(extractorName, e.toString());
    }
  }

  /**
   * Returns the appropriate {@link PageExtractor} implementation
   * 
   * @param extractorName
   *          Extractor name
   * @param doc
   *          Document to extract
   * @return The appropriate {@link PageExtractor} implementation for a given
   *         extractor name.
   * @throws DataExtractorNotFound
   *           when DataExtractor can not be found for extractorName
   */
  public PageExtractor create(String extractorName, Document doc) throws DataExtractorNotFound {
    PageExtractor extractor = this.create("product");
    extractor.doc(doc);
    return extractor;
  }
}
