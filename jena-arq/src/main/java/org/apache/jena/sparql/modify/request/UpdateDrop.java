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

package org.apache.jena.sparql.modify.request;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.modify.UpdateResult;
import org.apache.jena.sparql.util.Context;

public class UpdateDrop extends UpdateDropClear 
{
    public UpdateDrop(String iri, boolean silent,Context connCtx)       { super(iri, silent,connCtx) ; }
    public UpdateDrop(Target target, boolean silent,Context connCtx)    { super(target, silent,connCtx) ; }
    public UpdateDrop(Node target, boolean silent,Context connCtx)      { super(target, silent,connCtx) ; }
    
    public UpdateDrop(String iri,Context connCtx)                       { super(iri, false,connCtx) ; }
    public UpdateDrop(Target target,Context connCtx)                    { super(target, false,connCtx) ; }
    public UpdateDrop(Node target,Context connCtx)                      { super(target, false,connCtx) ; }

    public UpdateDrop(String iri, boolean silent)                       { super(iri, silent,null) ; }
    public UpdateDrop(Target target, boolean silent)                    { super(target, silent,null) ; }
    public UpdateDrop(Node target, boolean silent)                      { super(target, silent,null) ; }
    
    public UpdateDrop(String iri)                                       { super(iri, false,null) ; }
    public UpdateDrop(Target target)                                    { super(target, false,null) ; }
    public UpdateDrop(Node target)                                      { super(target, false,null) ; }
    
    @Override
    public UpdateResult visit(UpdateVisitor visitor){ 
        visitor.visit(this) ; 
        return null;
    }
}
