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

package org.apache.jena.rdfconnection;

import java.util.List;
import org.apache.jena.sparql.modify.UpdateResult;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.modify.UpdateResult;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

/** SPARQL Update Operations on a connection.
 * 
 * @see RDFConnection
 * @see RDFConnectionFactory
 */  
public interface SparqlUpdateConnection extends Transactional, AutoCloseable
{
    /** Execute a SPARQL Update.
     * 
     * @param update
     */
    public List<UpdateResult> update(Update update);

    /** Execute a SPARQL Update.
     * 
     * @param update
     */
    public List<UpdateResult> update(UpdateRequest update); 
    
    /** Execute a SPARQL Update.
     * 
     * @param updateString
     */
    public List<UpdateResult> update(String updateString);
    
    /** Close this connection. */ 
    @Override public void close();
}

