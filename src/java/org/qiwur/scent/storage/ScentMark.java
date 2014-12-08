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

import org.apache.avro.util.Utf8;

public enum ScentMark {
  SEGMENT_MARK("_segmrk_"), CLASSIFY_MARK("_clsmrk_"), EXTRACT_MARK("_extmrk_");

  private Utf8 name;

  ScentMark(String name) {
    this.name = new Utf8(name);
  }

  public void putMark(PageBlock block, Utf8 markValue) {
      block.getMarkers().put(name, markValue);
  }

  public void putMark(PageBlock block, String markValue) {
    putMark(block, new Utf8(markValue));
  }

  public Utf8 removeMark(PageBlock block) {
    return (Utf8) block.getMarkers().put(name, null);
  }

  public Utf8 checkMark(PageBlock block) {
    return (Utf8) block.getMarkers().get(name);
  }

  /**
   * Remove the mark only if the mark is present on the block.
   * @param block The block to remove the mark from.
   * @return If the mark was present.
   */
  public Utf8 removeMarkIfExist(PageBlock block) {
    if (checkMark(block) != null) {
      return removeMark(block);
    }
    return null;
  }

  public Utf8 getName() {
	return name;
  }
}
