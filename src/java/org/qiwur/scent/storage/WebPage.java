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

@SuppressWarnings("all")
public class WebPage {
  public static enum Field {
    BASE_URL(0,"baseUrl"),
    STATUS(1,"status"),
    FETCH_TIME(2,"fetchTime"),
    PREV_FETCH_TIME(3,"prevFetchTime"),
    FETCH_INTERVAL(4,"fetchInterval"),
    RETRIES_SINCE_FETCH(5,"retriesSinceFetch"),
    MODIFIED_TIME(6,"modifiedTime"),
    PREV_MODIFIED_TIME(7,"prevModifiedTime"),
    PROTOCOL_STATUS(8,"protocolStatus"),
    CONTENT(9,"content"),
    CONTENT_TYPE(10,"contentType"),
    PREV_SIGNATURE(11,"prevSignature"),
    SIGNATURE(12,"signature"),
    TITLE(13,"title"),
    TEXT(14,"text"),
    PARSE_STATUS(15,"parseStatus"),
    SCORE(16,"score"),
    REPR_URL(17,"reprUrl"),
    HEADERS(18,"headers"),
    OUTLINKS(19,"outlinks"),
    INLINKS(20,"inlinks"),
    MARKERS(21,"markers"),
    METADATA(22,"metadata"),
    BATCH_ID(23,"batchId"),
    ;
    private int index;
    private String name;
    Field(int index, String name) {this.index=index;this.name=name;}
    public int getIndex() {return index;}
    public String getName() {return name;}
    public String toString() {return name;}
  };
}
