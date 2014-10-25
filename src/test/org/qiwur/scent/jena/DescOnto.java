/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Package
///////////////
package org.qiwur.scent.jena;

// Imports
///////////////
import java.util.Iterator;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * <p>
 * Execution wrapper for describe-class example
 * </p>
 */
public class DescOnto {
  // Constants
  // ////////////////////////////////

  // Static variables
  // ////////////////////////////////

  // Instance variables
  // ////////////////////////////////

  // Constructors
  // ////////////////////////////////

  // External signature methods
  // ////////////////////////////////

  public static void main(String[] args) {
    // read the argument file, or the default
    String source = (args.length == 0) ? "http://www.w3.org/2001/sw/WebOnt/guide-src/wine" : args[0];

    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);

    // we have a local copy of the wine ontology
    addFoodWineAltPaths(m.getDocumentManager());

    // read the source document
    m.read(source);

    DescribeClass dc = new DescribeClass();

    if (args.length >= 2) {
      // we have a named class to describe
      OntClass c = m.getOntClass(args[1]);
      dc.describeClass(System.out, c);
    } else {
      for (Iterator<OntClass> i = m.listClasses(); i.hasNext();) {
        // now list the classes
        dc.describeClass(System.out, i.next());
      }
    }
  }

  // Internal implementation methods
  // ////////////////////////////////

  /**
   * Add alternate paths to save downloading the default ontologies from the Web
   */
  protected static void addFoodWineAltPaths(OntDocumentManager odm) {
    // we have a local copy of the wine ontology
    odm.addAltEntry("http://www.w3.org/2001/sw/WebOnt/guide-src/wine", "file:/tmp/data/camera.owl");
    odm.addAltEntry("http://www.w3.org/2001/sw/WebOnt/guide-src/wine", "file:/tmp/data/good-relations-words.owl");
  }

  // ==============================================================================
  // Inner class definitions
  // ==============================================================================

}
