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

import java.util.List;
import java.util.Map;
import static org.apache.jena.acl.DatasetACL.ADMIN_USER;
import org.apache.jena.http.TestBlankNodeBinary;
import org.apache.jena.query.Dataset;

import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.modify.UpdateResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Lorenzo
 */

public class ACLTestBase {
    
    private static final String USER_1 = "monger";
    private static final String USER_2 = "gonger";
    
    
    private static final String q01_create_graph  = "CREATE GRAPH <http://ggg1>";
    private static final String q02_clear_graph  = "CLEAR GRAPH <http://ggg1>";
    private static final String q03_drop_graph  = "DROP GRAPH <http://ggg1>";
    private static final String q04_select_all = "SELECT * WHERE { GRAPH ?g {?s ?p ?o}}";
    
    private final TS_DatasetFactory.DatasetFactoryId    dfiId;
    private final String                                datasetName;
    
    private static final String q01_insert_where_bind = 
        "INSERT {   " + 
        "    GRAPH ?g { " + 
        "        <http://s1><http://p1><http://o1>. " + 
        "        <http://s2><http://p2><http://o2>. " + 
        "    } " + 
        "    GRAPH ?g2 { " + 
        "        <http://s3><http://p3><http://o3>. " + 
        "    } " + 
        "}WHERE { " + 
        "    BIND( <http://graph_1> AS ?g) " + 
        "    BIND( <http://graph_2> AS ?g2) " + 
        "}";
    
    
    
    public ACLTestBase(TS_DatasetFactory.DatasetFactoryId id,String dsName) {
        dfiId = id;
        datasetName = dsName;
    }
    
    @Test
    public void testInsert_01() {
        System.out.println("Executing " + this.getClass().getName() + ".testInsert_01()");
        //with admin user
        final DatasetACL acl = new DatasetACL() {
            @Override
            public boolean checkGrapBase(DatasetACL.aclId id, String graphName, String user) {
                if (user.equals(ADMIN_USER))
                    return true;
                
                return false;
            }
        };
            
        final Dataset ds = TS_DatasetFactory.newInstance(dfiId, datasetName, acl);
        final RDFConnection conn = RDFConnection.connect(ds, DatasetACL.ADMIN_USER);
        final List<UpdateResult>   ur = DatasetActions.update(ds, q01_insert_where_bind,conn);
        Assert.assertEquals(1,ur.size());
        Assert.assertEquals(0,ur.get(0).deletedTuples.size());
        Assert.assertEquals(3,ur.get(0).updatedTuples.size());
        
        final List <Map<String,String>> qr = DatasetActions.query(ds, q04_select_all, conn);
        
        Assert.assertEquals(3,qr.size());
        
                
    }
    
    @Test
    public void testInsert_02() {
        System.out.println("Executing " + this.getClass().getName() + ".testInsert_02()");
        //with admin user
        final DatasetACL acl = new DatasetACL() {
            @Override
            public boolean checkGrapBase(DatasetACL.aclId id, String graphName, String user)  {
                if (user.equals(ADMIN_USER))
                    return true;
                
                
                if (user.equals(USER_1) && graphName.equals("http://graph_1"))
                    return true;
                    
                return false;
            }
        };
            
        final Dataset ds = TS_DatasetFactory.newInstance(dfiId, datasetName, acl);
        final RDFConnection conn = RDFConnection.connect(ds, USER_1);
        
        List<UpdateResult>   ur  = null;
        try { 
            ur = DatasetActions.update(ds, q01_insert_where_bind,conn);
        } catch(ACLException e) {
            Assert.assertEquals(USER_1,e.getUserName());
            Assert.assertEquals("http://graph_2",e.getGraphName());
        }
        Assert.assertTrue(ur == null || ur.size() == 0 );
        
        final RDFConnection conn2 = RDFConnection.connect(ds, ADMIN_USER);
        final List<Map<String,String>> qr = DatasetActions.query(ds, q04_select_all, conn2);
        Assert.assertTrue(qr == null || qr.size() == 0 );
        
                
    }
    @Test
    public void testQuery_01() {
        System.out.println("Executing " + this.getClass().getName() + ".testQuery_01()");
        final DatasetACL acl = new DatasetACL() {
            @Override
            public boolean checkGrapBase(DatasetACL.aclId id, String graphName, String user) {
                if (user.equals(ADMIN_USER))
                    return true;

                if (user.equals(USER_1) && graphName.equals("http://graph_1"))
                    return true;
                if (user.equals(USER_2) && graphName.equals("http://graph_2"))
                    return true;
                
                return false;
            }
        };
            
        final Dataset ds = TS_DatasetFactory.newInstance(dfiId, datasetName, acl);
        final RDFConnection conn1 = RDFConnection.connect(ds, DatasetACL.ADMIN_USER);
        final List<UpdateResult>   ur = DatasetActions.update(ds, q01_insert_where_bind,conn1);
        Assert.assertEquals(ur.size(),1);
        Assert.assertEquals(0,ur.get(0).deletedTuples.size());
        Assert.assertEquals(3,ur.get(0).updatedTuples.size());

        final List <Map<String,String>> qr = DatasetActions.query(ds, q04_select_all, conn1);
        Assert.assertEquals(3,qr.size());

        final RDFConnection conn2 = RDFConnection.connect(ds, USER_1);
        final List <Map<String,String>> qr2 = DatasetActions.query(ds, q04_select_all, conn2);
        Assert.assertEquals(2,qr2.size());

        final RDFConnection conn3 = RDFConnection.connect(ds, USER_2);
        final List <Map<String,String>> qr3 = DatasetActions.query(ds, q04_select_all, conn3);
        Assert.assertEquals(1,qr3.size());
        
    }
}
