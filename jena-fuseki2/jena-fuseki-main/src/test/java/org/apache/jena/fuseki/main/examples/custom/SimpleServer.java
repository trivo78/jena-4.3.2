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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.jena.fuseki.main.examples.custom;

import static org.apache.jena.fuseki.main.BaseFusekiTest.datasetPath;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;

/**
 *
 * @author Lorenzo
 */
public class SimpleServer {
     protected static DatasetGraph dsgTesting = DatasetGraphFactory.createTxnMem();
     
    public static final String datasetName()    { return "sparql"; }
    public static final String datasetPath()    { return "/"+datasetName(); }     
    public static void main(String[] args) {
        JenaSystem.init();
        FusekiLogging.setLogging();

        // Normally done with ServiceLoader
        // A file /META-INF/services/org.apache.jena.fuseki.main.sys.FusekiModule
        // in the jar file with contents:
        //    org.apache.jena.fuseki.main.examples.ExampleModule
        //
        // The file is typically put into the jar by having
        //   src/main/resources/META-INF/services/org.apache.jena.fuseki.main.sys.FusekiModule

        // Create server.
       
        
        FusekiServer server =
        FusekiServer.create()
              .port(8000)
              //.verbose(true)
              .add(datasetPath(), dsgTesting)
              .enablePing(true)
              .enableMetrics(true)
              .build()
        ;
        int port = server.getPort();
        
        server.start();
        
        
        pressAnyKeyToContinue();
        server.stop();
        
    }
    
    private static void pressAnyKeyToContinue(){ 
           System.out.println("Press Enter key to continue...");
           try
           {
               System.in.read();
           }  
           catch(Exception e)
           {}  
    }


}

 