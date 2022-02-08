package org.apache.jena.sparql.modify;

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



import java.util.List;
import org.apache.jena.sparql.core.Quad;

/**
 *
 * @author Lorenzo
 */
public class UpdateResult {
    public final List<Quad> deletedTuples;
    public final List<Quad> updatedTuples;
    public UpdateResult(List<Quad> del, List<Quad> upd) {
        deletedTuples = del; // processQuadList(del, db);
        updatedTuples = upd; // processQuadList(upd, db);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Deleted quads");
        printQuadList(deletedTuples, sb);
        sb.append(System.lineSeparator());
        sb.append("Updated quads");
        printQuadList(updatedTuples, sb);
        
        return sb.toString();
        
    }
    private void printQuadList(List<Quad> src, StringBuilder tgt) {
        if (src == null) {
            tgt.append(System.lineSeparator());
            tgt.append("\t <NONE> ");
            return;
        }

        for(final Quad q : src) {
            tgt.append(System.lineSeparator());
            tgt.append("\t");
            tgt.append(q.toString());
        }
    }
    
}
