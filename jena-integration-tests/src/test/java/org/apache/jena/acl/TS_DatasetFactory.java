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
package org.apache.jena.acl;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;

/**
 *
 * @author Lorenzo
 */
public class TS_DatasetFactory {
    public enum DatasetFactoryId {
        dfiMem,
        dfiTDB1,
        dfiTDB2
    }
    public static Dataset newInstance(DatasetFactoryId id, String name,DatasetACL acl) {
        Dataset ret = null;
        switch(id) {
            case dfiMem:
                ret = DatasetFactory.createTxnMem(acl);
                break;
            case dfiTDB1: {
                final org.apache.jena.tdb.base.file.Location loc = org.apache.jena.tdb.base.file.Location.create("./run/" + name);
                ret = org.apache.jena.tdb.TDBFactory.createDataset(loc);
                if (ret.asDatasetGraph() != null) {
                    ret.begin(ReadWrite.WRITE);
                    ret.asDatasetGraph().clear();
                    ret.commit();
                }
                break;
            }
            case dfiTDB2: {
                final org.apache.jena.dboe.base.file.Location loc = org.apache.jena.dboe.base.file.Location.create("./run/" + name );
                ret = org.apache.jena.tdb2.TDB2Factory.connectDataset(loc);  
                if (ret.asDatasetGraph() != null) {
                    ret.begin(ReadWrite.WRITE);
                    ret.asDatasetGraph().clear();
                    ret.commit();
                }
                
                break;
                
            }
        }
        return ret;
    }
}
