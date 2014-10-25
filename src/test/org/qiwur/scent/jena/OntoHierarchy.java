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
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * <p>
 * Execution wrapper for class hierarchy example
 * </p>
 */
public class OntoHierarchy {

  public static void main(String[] args) {
    OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);

    m.getDocumentManager().addAltEntry("http://www.heppnetz.de/ontologies/goodrelations/v1.owl",
        "file:/home/vincent/workspace/scent/conf/good-relations-words.owl");

    m.read("http://www.heppnetz.de/ontologies/goodrelations/v1.owl");

    new ClassHierarchy().showHierarchy(System.out, m);
    // Iterator<OntClass> i = m.listClasses();

//    Iterator<AnnotationProperty> ai = m.listAnnotationProperties();
//    while (ai.hasNext()) {
//      System.out.println(ai.next());
//    }
//
//    // Property p = ResourceFactory.createProperty("http://qiwur.com/gr-transformer#", "sectionLabel");
//    Property p = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#", "deprecated");
//    Property p2 = ResourceFactory.createProperty("http://qiwur.com/gr-transformer#", "sectionLabel");
//    Iterator<OntClass> ci = m.listClasses();
//    while (ci.hasNext()) {
//      OntClass c = ci.next();
//
//      StmtIterator si = c.listProperties(p);
//      while (si.hasNext()) {
//        System.out.println(si.next());
//      }
//
//      StmtIterator si2 = c.listProperties(p2);
//      while (si2.hasNext()) {
//        System.out.println(si2.next());
//      }
//    }
  }
}
