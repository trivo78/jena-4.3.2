/**
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

package org.apache.jena.fuseki.main;

import static org.apache.jena.riot.web.HttpOp1.initialDefaultHttpClient;
import static org.junit.Assert.*;

import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.test.HttpTest;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpOp1;
import org.apache.jena.sparql.engine.http.Params;
import org.apache.jena.web.HttpSC;
import org.junit.Test;

// This a mixture of testing HttpOp1 and testing basic operation of the SPARQL server
// especially error cases and unusual usage that the higher level APIs don't use.
@SuppressWarnings("deprecation")
public class TestHttpOp_AHC extends AbstractFusekiTest {

    static String pingURL           = urlRoot()+"$/ping";
    static String gspServiceURL     = serviceGSP();
    static String defaultGraphURL   = serviceGSP()+"?default";
    static String namedGraphURL     = serviceGSP()+"?graph=http://example/g";
    static String queryURL          = serviceQuery();
    static String updateURL         = serviceUpdate();

    static String simpleQuery = queryURL+"?query="+IRILib.encodeUriComponent("ASK{}");

    @Test public void correctDefaultResetBehavior() {
        HttpClient defaultClient = HttpOp1.getDefaultHttpClient();
        HttpOp1.setDefaultHttpClient(null);
        assertSame("Failed to reset to initial client!", initialDefaultHttpClient, HttpOp1.getDefaultHttpClient());
        HttpOp1.setDefaultHttpClient(defaultClient);
    }

    @Test public void httpGet_01() {
        assertNotNull(HttpOp1.execHttpGetString(pingURL));
    }

    @Test
    public void httpGet_02() {
        HttpTest.expect404(() -> HttpOp1.execHttpGet(urlRoot() + "does-not-exist"));
    }

    @Test public void httpGet_03() {
        assertNotNull(HttpOp1.execHttpGetString(pingURL));
    }

    @Test public void httpGet_04() {
        String x = HttpOp1.execHttpGetString(urlRoot()+"does-not-exist");
        assertNull(x);
    }

    @Test public void httpGet_05() {
        assertNotNull(HttpOp1.execHttpGetString(simpleQuery));
    }

    // SPARQL Query

    @Test public void queryGet_01() {
        assertNotNull(HttpOp1.execHttpGetString(simpleQuery));
    }

    public void queryGet_02() {
        // No query.
        HttpTest.execWithHttpException(HttpSC.BAD_REQUEST_400, () -> HttpOp1.execHttpGetString(queryURL + "?query="));
    }

    public void httpPost_01() {
        HttpTest.execWithHttpException(HttpSC.UNSUPPORTED_MEDIA_TYPE_415,
                () -> HttpOp1.execHttpPost(queryURL, "ASK{}", "text/plain"));
    }

    public void httpPost_02() {
        HttpTest.execWithHttpException(HttpSC.UNSUPPORTED_MEDIA_TYPE_415,
                () -> HttpOp1.execHttpPost(queryURL, "ASK{}", WebContent.contentTypeSPARQLQuery));
    }

    public void httpPost_03() {
        HttpTest.execWithHttpException(HttpSC.UNSUPPORTED_MEDIA_TYPE_415,
                () -> HttpOp1.execHttpPost(queryURL, "ASK{}", WebContent.contentTypeOctets));
    }

    @Test public void httpPost_04() {
        Params params = new Params().addParam("query", "ASK{}");
        try ( TypedInputStream in = HttpOp1.execHttpPostFormStream(queryURL, params, WebContent.contentTypeResultsJSON) ) {}
    }

    public void httpPost_05() {
        Params params = new Params().addParam("query", "ASK{}");
        // Query to Update
        HttpTest.execWithHttpException(HttpSC.BAD_REQUEST_400,
                () -> HttpOp1.execHttpPostFormStream(updateURL, params, WebContent.contentTypeResultsJSON));
    }

    @Test public void httpPost_06() {
        Params params = new Params().addParam("request", "CLEAR ALL");
        HttpOp1.execHttpPostForm(updateURL, params);
    }

    // GSP
    @Test public void gsp_01() {
        String x = HttpOp1.execHttpGetString(defaultGraphURL, "application/rdf+xml");
        assertTrue(x.contains("</"));
        assertTrue(x.contains(":RDF"));
    }

    @Test public void gsp_02() {
        String x = HttpOp1.execHttpGetString(defaultGraphURL, "application/n-triples");
        assertTrue(x.isEmpty());
    }

    static String graphString = "@prefix : <http://example/> . :s :p :o .";
    static String datasetString = "@prefix : <http://example/> . :s :p :o . :g { :sg :pg :og }";

    @Test public void gsp_03() {
        HttpOp1.execHttpPut(defaultGraphURL, WebContent.contentTypeTurtle, graphString);
    }

    @Test public void gsp_04() {
        HttpOp1.execHttpPut(defaultGraphURL, WebContent.contentTypeTurtle, graphString);
        String s1 = HttpOp1.execHttpGetString(defaultGraphURL, WebContent.contentTypeNTriples);
        assertFalse(s1.isEmpty());
        HttpOp1.execHttpDelete(defaultGraphURL);
        String s2 = HttpOp1.execHttpGetString(defaultGraphURL, WebContent.contentTypeNTriples);
        assertTrue(s2.isEmpty());
    }

    @Test public void gsp_05() {
        HttpOp1.execHttpDelete(defaultGraphURL);

        HttpOp1.execHttpPost(defaultGraphURL, WebContent.contentTypeTurtle, graphString);
        String s1 = HttpOp1.execHttpGetString(defaultGraphURL, WebContent.contentTypeNTriples);
        assertFalse(s1.isEmpty());
        HttpOp1.execHttpDelete(defaultGraphURL);
        String s2 = HttpOp1.execHttpGetString(defaultGraphURL, WebContent.contentTypeNTriples);
        assertTrue(s2.isEmpty());
    }

    @Test public void gsp_06() {
        HttpOp1.execHttpPost(namedGraphURL, WebContent.contentTypeTurtle, graphString);
        String s1 = HttpOp1.execHttpGetString(namedGraphURL, WebContent.contentTypeNTriples);
        assertFalse(s1.isEmpty());

        String s2 = HttpOp1.execHttpGetString(defaultGraphURL, WebContent.contentTypeNTriples);
        assertTrue(s2.isEmpty());

        HttpOp1.execHttpDelete(namedGraphURL);
        String s3 = HttpOp1.execHttpGetString(defaultGraphURL, WebContent.contentTypeNTriples);
        assertTrue(s3.isEmpty());

        HttpTest.expect404(()->HttpOp1.execHttpDelete(namedGraphURL));
    }

    @Test public void gsp_10() {
        HttpOp1.execHttpDelete(defaultGraphURL);
    }

    // Extended GSP - no ?default, no ?graph acts on the datasets as a whole.
    @Test public void gsp_12() {
        HttpTest.execWithHttpException(HttpSC.METHOD_NOT_ALLOWED_405, () -> HttpOp1.execHttpDelete(gspServiceURL));
    }

    @Test public void gsp_20() {
        String s1 = HttpOp1.execHttpGetString(gspServiceURL, WebContent.contentTypeNQuads);
        assertNotNull("Got 404 (via null)", s1);
        assertTrue(s1.isEmpty());

        HttpOp1.execHttpPost(gspServiceURL, WebContent.contentTypeTriG, datasetString);
        String s2 = HttpOp1.execHttpGetString(gspServiceURL, WebContent.contentTypeNQuads);
        assertFalse(s2.isEmpty());

        String s4 = HttpOp1.execHttpGetString(defaultGraphURL, WebContent.contentTypeNTriples);
        assertFalse(s4.isEmpty());
    }
}

