/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.jena.acl;

import java.util.List;
import java.util.Map;
import static org.apache.jena.acl.DatasetACL.ADMIN_USER;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.modify.UpdateResult;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Lorenzo
 */
public class ACLTestSuite {
    
    private static final String USER_1 = "monger";
    private static final String USER_2 = "gonger";
    
    private static final String q01_create_graph  = "CREATE GRAPH <http://ggg1>";
    private static final String q02_clear_graph  = "CLEAR GRAPH <http://ggg1>";
    private static final String q03_drop_graph  = "DROP GRAPH <http://ggg1>";
    private static final String q04_select_all = "SELECT * WHERE { GRAPH ?g {?s ?p ?o}}";
    
    
    
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
    
    
    
    public ACLTestSuite() {
    }
    
    @Test
    public void testInsert_01() {
        //with admin user
        final DatasetACL acl = new DatasetACL() {
            @Override
            public void checkGrap(DatasetACL.aclId id, String graphName, String user) throws ACLException {
                if (user.equals(ADMIN_USER))
                    return;
            }
        };
            
        final Dataset ds = DatasetFactory.createTxnMem(acl);
        final RDFConnection conn = RDFConnection.connect(ds, DatasetACL.ADMIN_USER);
        final List<UpdateResult>   ur = DatasetActions.update(ds, q01_insert_where_bind,conn);
        Assert.assertEquals(ur.size(),1);
        Assert.assertEquals(ur.get(0).deletedTuples.size(),0);
        Assert.assertEquals(ur.get(0).updatedTuples.size(),3);
        
        final List <Map<String,String>> qr = DatasetActions.query(ds, q04_select_all, conn);
        
        Assert.assertEquals(qr.size(),3);
        
                
    }
    
    @Test
    public void testInsert_02() {
        //with admin user
        final DatasetACL acl = new DatasetACL() {
            @Override
            public void checkGrap(DatasetACL.aclId id, String graphName, String user) throws ACLException {
                if (user.equals(ADMIN_USER))
                    return;
                
                
                if (user.equals(USER_1) && graphName.equals("http://graph_1"))
                    return;
                    
                throw new ACLException(graphName, user);
            }
        };
            
        final Dataset ds = DatasetFactory.createTxnMem(acl);
        final RDFConnection conn = RDFConnection.connect(ds, USER_1);
        
        List<UpdateResult>   ur  = null;
        try { 
            ur = DatasetActions.update(ds, q01_insert_where_bind,conn);
        } catch(ACLException e) {
            Assert.assertEquals(e.getUserName(),USER_1);
            Assert.assertEquals(e.getGraphName(),"http://graph_2");
        }
        Assert.assertTrue(ur == null || ur.size() == 0 );
        
        final RDFConnection conn2 = RDFConnection.connect(ds, ADMIN_USER);
        final List<Map<String,String>> qr = DatasetActions.query(ds, q04_select_all, conn2);
        Assert.assertTrue(qr == null || qr.size() == 0 );
        
                
    }
    
}
