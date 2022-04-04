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
import static org.apache.jena.acl.DatasetACL.DEF_GRAPH_NAME;
import org.apache.jena.query.Dataset;

import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.modify.UpdateResult;
import org.junit.Assert;
import org.junit.Test;

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
    private static final String q05_select_all = "SELECT * WHERE { GRAPH <http://graph_1> {?s ?p ?o}}";
    private static final String q06_select_all = "SELECT * WHERE { ?s ?p ?o}";
    
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
    
    
    private final static String q02_insert_data_dg = 
        "PREFIX mp: <http://mysparql.com/>" + System.lineSeparator() + 
        "INSERT DATA {" + System.lineSeparator() + 
        "mp:person0 mp:firstname \"Jay\" ." + System.lineSeparator() + 
        "mp:person0 mp:lastname \"Stevens\" ." + System.lineSeparator() + 
        "mp:person0 mp:state \"CA\" ." + System.lineSeparator() + 
        "mp:person1 mp:firstname \"John\" ." + System.lineSeparator() + 
        "mp:person1 mp:lastname \"Homlmes\" ." + System.lineSeparator() + 
        "mp:person1 mp:state \"CZ\" ." + System.lineSeparator() + 
        "mp:person2 mp:firstname \"Erwin\" ." + System.lineSeparator() + 
        "mp:person2 mp:lastname \"Rommel\" ." + System.lineSeparator() + 
        "mp:person2 mp:state \"PZ\" ." + System.lineSeparator() +
        "}"
    ;    

    private final static String q03_insert_data_dg = 
        "PREFIX mp: <http://mysparql.com/>" + System.lineSeparator() + 
        "INSERT DATA {" + System.lineSeparator() + 
        "mp:person10 mp:firstname \"Jay\" ." + System.lineSeparator() + 
        "mp:person10 mp:lastname \"Stevens\" ." + System.lineSeparator() + 
        "mp:person10 mp:state \"CA\" ." + System.lineSeparator() + 
        "mp:person11 mp:firstname \"John\" ." + System.lineSeparator() + 
        "mp:person11 mp:lastname \"Homlmes\" ." + System.lineSeparator() + 
        "mp:person11 mp:state \"CZ\" ." + System.lineSeparator() + 
        "mp:person12 mp:firstname \"Erwin\" ." + System.lineSeparator() + 
        "mp:person12 mp:lastname \"Rommel\" ." + System.lineSeparator() + 
        "mp:person12 mp:state \"CZ\" ." + System.lineSeparator() +
        "}"
    ;    
    
    private final static String q06_select_some_dg = 
            "PREFIX mp: <http://mysparql.com/>" + System.lineSeparator() + 
            "SELECT ?person WHERE {" + System.lineSeparator() + 
            "?person mp:state \"CZ\" ." + System.lineSeparator() + 
            "}"
    ;
    
    private final static String q07_insert_data_ng = 
        "PREFIX mp: <http://mysparql.com/>" + System.lineSeparator() + 
        "INSERT DATA { GRAPH mp:graph1 {" + System.lineSeparator() + 
        "mp:person10 mp:firstname \"Jay\" ." + System.lineSeparator() + 
        "mp:person10 mp:lastname \"Stevens\" ." + System.lineSeparator() + 
        "mp:person10 mp:state \"CA\" ." + System.lineSeparator() + 
        "mp:person11 mp:firstname \"John\" ." + System.lineSeparator() + 
        "mp:person11 mp:lastname \"Homlmes\" ." + System.lineSeparator() + 
        "mp:person11 mp:state \"CZ\" ." + System.lineSeparator() + 
        "mp:person12 mp:firstname \"Erwin\" ." + System.lineSeparator() + 
        "mp:person12 mp:lastname \"Rommel\" ." + System.lineSeparator() + 
        "mp:person12 mp:state \"CZ\" ." + System.lineSeparator() +
        "}}"
    ;    
    
    private final static String q08_select_some_ng = 
            "PREFIX mp: <http://mysparql.com/>" + System.lineSeparator() + 
            "SELECT ?person WHERE { GRAPH mp:graph1 {" + System.lineSeparator() + 
            "?person mp:state \"CZ\" ." + System.lineSeparator() + 
            "}}"
    ;
    
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
    public void testInsert_03() {
        System.out.println("Executing " + this.getClass().getName() + ".testInsert_03()");
        //with admin user
        
        
        final DatasetACL acl = new DatasetACL() {
            @Override
            public boolean checkGrapBase(DatasetACL.aclId id, String graphName, String user)  {
                if (user.equals(ADMIN_USER))
                    return true;
                
                
                if (user.equals(USER_1) && graphName.equals(DEF_GRAPH_NAME))
                    return true;
                
                
                if (user.equals(USER_2) && graphName.equals(DEF_GRAPH_NAME) && id == aclId.aiQuery)
                    return true;
                
                    
                return false;
            }
        };
            
        final Dataset ds = TS_DatasetFactory.newInstance(dfiId, datasetName, acl);
        final RDFConnection conn = RDFConnection.connect(ds, USER_1);
        //USER1 can insert
        List<UpdateResult>   ur  = DatasetActions.update(ds, q02_insert_data_dg ,conn);
        
        Assert.assertEquals(1,ur.size());
        Assert.assertTrue(ur.get(0).deletedTuples == null || ur.get(0).deletedTuples.size() == 0);
        Assert.assertEquals(9,ur.get(0).updatedTuples.size());
        
        final List <Map<String,String>> qr = DatasetActions.query(ds, q06_select_all, conn);
        
        Assert.assertEquals(9,qr.size());
        
        //USER2 can only query
        
        final RDFConnection conn2 = RDFConnection.connect(ds, USER_2);
        
        ur = null;  //reset
        try { 
            ur = DatasetActions.update(ds, q03_insert_data_dg ,conn2);
        } catch(ACLException e) {
            Assert.assertEquals(USER_2,e.getUserName());
            Assert.assertEquals(DEF_GRAPH_NAME,e.getGraphName());
        }
        Assert.assertTrue(ur == null || ur.size() == 0 );
        
        //now try to query
        final RDFConnection conn3 = RDFConnection.connect(ds, USER_2);
        final List<Map<String,String>> qr2 = DatasetActions.query(ds, q06_select_all, conn3);
        Assert.assertNotEquals(null, qr2);
        Assert.assertEquals(9, qr2.size());
        
                
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
            ur = DatasetActions.update(ds, q01_insert_where_bind ,conn);
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
    
    @Test
    public void testQuery_02() {
        System.out.println("Executing " + this.getClass().getName() + ".testQuery_02()");
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

        final List <Map<String,String>> qr = DatasetActions.query(ds, q05_select_all, conn1);
        Assert.assertEquals(2,qr.size());

        final RDFConnection conn2 = RDFConnection.connect(ds, USER_1);
        final List <Map<String,String>> qr2 = DatasetActions.query(ds, q05_select_all, conn2);
        Assert.assertEquals(2,qr2.size());

        final RDFConnection conn3 = RDFConnection.connect(ds, USER_2);
        final List <Map<String,String>> qr3 = DatasetActions.query(ds, q05_select_all, conn3);
        Assert.assertEquals(0,qr3.size());
        
    }
    
    @Test
    public void testQuery_03() {
        System.out.println("Executing " + this.getClass().getName() + ".testQuery_03()");
        
        
        final DatasetACL acl = new DatasetACL() {
            @Override
            public boolean checkGrapBase(DatasetACL.aclId id, String graphName, String user) {
                if (user.equals(ADMIN_USER))
                    return true;

                if (user.equals(USER_1) && graphName.equals(DEF_GRAPH_NAME))
                    return true;

                    
                return false;
            }
        };
            
        final Dataset ds = TS_DatasetFactory.newInstance(dfiId, datasetName, acl);
        final RDFConnection conn1 = RDFConnection.connect(ds, DatasetACL.ADMIN_USER);
        final List<UpdateResult>   ur = DatasetActions.update(ds, q03_insert_data_dg,conn1);
        Assert.assertEquals(1,ur.size());
        Assert.assertTrue(ur.get(0).deletedTuples == null || ur.get(0).deletedTuples.size() == 0);
        Assert.assertEquals(9,ur.get(0).updatedTuples.size());

        final List <Map<String,String>> qr = DatasetActions.query(ds, q06_select_some_dg, conn1);
        Assert.assertEquals(2,qr.size());
        

        final RDFConnection conn2 = RDFConnection.connect(ds, USER_1);
        final List <Map<String,String>> qr2 = DatasetActions.query(ds, q06_select_some_dg, conn2);
        Assert.assertEquals(2,qr2.size());

        final RDFConnection conn3 = RDFConnection.connect(ds, USER_2);
        final List <Map<String,String>> qr3 = DatasetActions.query(ds, q06_select_some_dg, conn3);
        Assert.assertEquals(0,qr3.size());

    }
    
    @Test
    public void testQuery_04() {
        System.out.println("Executing " + this.getClass().getName() + ".testQuery_04()");
        
        
        final DatasetACL acl = new DatasetACL() {
            @Override
            public boolean checkGrapBase(DatasetACL.aclId id, String graphName, String user) {
                if (user.equals(ADMIN_USER))
                    return true;

                if (user.equals(USER_1) && graphName.equals("http://mysparql.com/graph1"))
                    return true;

                    
                return false;
            }
        };
            
        final Dataset ds = TS_DatasetFactory.newInstance(dfiId, datasetName, acl);
        final RDFConnection conn1 = RDFConnection.connect(ds, DatasetACL.ADMIN_USER);
        final List<UpdateResult>   ur = DatasetActions.update(ds, q07_insert_data_ng,conn1);
        Assert.assertEquals(1,ur.size());
        Assert.assertTrue(ur.get(0).deletedTuples == null || ur.get(0).deletedTuples.size() == 0);
        Assert.assertEquals(9,ur.get(0).updatedTuples.size());

        final List <Map<String,String>> qr = DatasetActions.query(ds, q08_select_some_ng, conn1);
        Assert.assertEquals(2,qr.size());
        

        final RDFConnection conn2 = RDFConnection.connect(ds, USER_1);
        final List <Map<String,String>> qr2 = DatasetActions.query(ds, q08_select_some_ng, conn2);
        Assert.assertEquals(2,qr2.size());

        final RDFConnection conn3 = RDFConnection.connect(ds, USER_2);
        final List <Map<String,String>> qr3 = DatasetActions.query(ds, q08_select_some_ng, conn3);
        Assert.assertEquals(0,qr3.size());

    }
    
}
