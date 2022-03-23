/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.jena.acl;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.modify.UpdateResult;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateAction;

/**
 *
 * @author Lorenzo
 */
class DatasetActions {
    private static class UpdateReturn {
        public List<UpdateResult>   result;
    }
    
    private static class QueryReturn {
        public List<Map<String,String>> result = new ArrayList<>();
    }

    public static List<Map<String,String>>  query(Dataset dataset, String query,RDFConnection conn) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            final QueryReturn rt = new QueryReturn();
            Txn.executeRead(conn, ()-> {
                    ResultSet rs = conn.query(QueryFactory.create(query)).execSelect();
                    while(rs.hasNext()) {
                        final QuerySolution qs = rs.next();
                        final Map<String,String> tmp = new TreeMap<>();
                        final Iterator<String> it = qs.varNames();
                        while(it.hasNext()) {
                            final String k = it.next();
                            final String v = qs.get(k).toString();
                            tmp.put(k, v);
                          
                        }
                        rt.result.add(tmp);
                    }
                    
                    ResultSetFormatter.outputAsJSON(out, rs);
            });

            try {
                    System.out.println("Query output : " + out.toString(StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                    System.err.println(e);
            }
            return rt.result;
    }

    public static List<UpdateResult>  update(Dataset dataset, String query,RDFConnection conn) {
            final UpdateReturn r = new UpdateReturn();
            Txn.executeWrite(conn, ()-> {
                    final List<UpdateResult> ur = conn.update(query);
                    if (ur != null) {
                        System.out.println("Update output ");
                        for(final UpdateResult u : ur ) {
                            System.out.println("******************");
                            System.out.println(u == null ? "<null>" : u.toString());
                        }
                    }
                    r.result = ur;
            });
            return r.result;
    }
    public static List<UpdateResult>  insertData(Dataset dataset, String query) {
        final List<UpdateResult>  ret = UpdateAction.parseExecute(query, dataset, null);
        RDFDataMgr.write(System.out, dataset, Lang.TRIG);
        return ret;
    }
    
}
