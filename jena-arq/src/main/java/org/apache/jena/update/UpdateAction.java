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

package org.apache.jena.update;

import java.io.InputStream ;
import java.util.List;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.QuerySolution ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.lang.UpdateParser ;
import org.apache.jena.sparql.modify.UpdateResult;
import org.apache.jena.sparql.modify.UpdateSink ;
import org.apache.jena.sparql.modify.UsingList ;
import org.apache.jena.sparql.modify.UsingUpdateSink ;
import org.apache.jena.sparql.modify.request.UpdateWithUsing ;
import org.apache.jena.sparql.util.Context;

/** A class of forms for executing SPARQL Update operations.
 * parse means the update request is in a String or an InputStream;
 * read means read the contents of a file.
 */

public class UpdateAction
{
    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param model
     */
    public static List<UpdateResult> readExecute(String filename, Model model,Context connCtx)
    {
        return readExecute(filename, toDatasetGraph(model.getGraph()),connCtx) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param graph
     */
    public static List<UpdateResult> readExecute(String filename, Graph graph,Context connCtx)
    {
        return readExecute(filename, toDatasetGraph(graph),connCtx) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param dataset
     */
    public static List<UpdateResult> readExecute(String filename, Dataset dataset,Context connCtx)
    {
        return readExecute(filename, dataset.asDatasetGraph(),connCtx) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param dataset
     */
    public static List<UpdateResult>readExecute(String filename, DatasetGraph dataset,Context connCtx)
    {
        return readExecute(filename, dataset, null,connCtx) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param dataset
     * @param inputBinding
     */
    public static List<UpdateResult> readExecute(String filename, Dataset dataset, QuerySolution inputBinding,Context connCtx) {
        UpdateRequest req = UpdateFactory.read(filename,connCtx) ;
        return execute(req, dataset, inputBinding) ;
    }

    /** Read a file containing SPARQL Update operations, and execute the operations.
     * @param filename
     * @param datasetGraph
     * @param inputBinding
     */
    public static List<UpdateResult> readExecute(String filename, DatasetGraph datasetGraph, Binding inputBinding,Context connCtx) {
        UpdateRequest req = UpdateFactory.read(filename,connCtx) ;
        return execute$(req, datasetGraph, inputBinding) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param model
     */
    public static List<UpdateResult> parseExecute(String updateString, Model model,Context connCtx)
    {
        return parseExecute(updateString, model.getGraph(),connCtx) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param graph
     */
    public static List<UpdateResult> parseExecute(String updateString, Graph graph,Context connCtx)
    {
        return parseExecute(updateString, toDatasetGraph(graph),connCtx) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     */
    public static List<UpdateResult> parseExecute(String updateString, Dataset dataset,Context connCtx)
    {
        return parseExecute(updateString, dataset.asDatasetGraph(),connCtx) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     */
    public static List<UpdateResult> parseExecute(String updateString, DatasetGraph dataset,Context connCtx)
    {
        UpdateRequest req = UpdateFactory.create(updateString,connCtx) ;
        return execute(req, dataset) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     * @param inputBinding
     */
    public static List<UpdateResult> parseExecute(String updateString, Dataset dataset, QuerySolution inputBinding,Context connCtx)
    {
        return parseExecute(updateString, dataset.asDatasetGraph(), BindingLib.asBinding(inputBinding),connCtx) ;
    }

    /** Parse a string containing SPARQL Update operations, and execute the operations.
     * @param updateString
     * @param dataset
     * @param inputBinding
     */
    public static List<UpdateResult> parseExecute(String updateString, DatasetGraph dataset, Binding inputBinding,Context connCtx)
    {
        UpdateRequest req = UpdateFactory.create(updateString,connCtx) ;
        return execute(req, dataset, inputBinding) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param model
     */
    public static List<UpdateResult> execute(UpdateRequest request, Model model)
    {
        return execute(request, model.getGraph()) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param graph
     */
    public static List<UpdateResult> execute(UpdateRequest request, Graph graph)
    {
        return execute(request, toDatasetGraph(graph)) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     */
    public static List<UpdateResult> execute(UpdateRequest request, Dataset dataset)
    {
        return execute(request, dataset.asDatasetGraph()) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     */
    public static List<UpdateResult> execute(UpdateRequest request, DatasetGraph dataset)
    {
        return execute$(request, dataset, null) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param dataset
     * @param inputBinding
     */
    public static List<UpdateResult> execute(UpdateRequest request, Dataset dataset, QuerySolution inputBinding)
    {
        return execute(request, dataset.asDatasetGraph(), BindingLib.asBinding(inputBinding)) ;
    }

    /** Execute SPARQL Update operations.
     * @param request
     * @param datasetGraph
     * @param inputBinding
     */
    public static List<UpdateResult> execute(UpdateRequest request, DatasetGraph datasetGraph, Binding inputBinding)
    {
        return execute$(request, datasetGraph, inputBinding) ;
    }


    private static DatasetGraph toDatasetGraph(Graph graph) {
        return DatasetGraphFactory.create(graph) ;
    }

    // All non-streaming updates come through here.
    private static List<UpdateResult> execute$(UpdateRequest request, DatasetGraph datasetGraph, Binding inputBinding)
    {
        UpdateProcessor uProc = UpdateExec.newBuilder().update(request).dataset(datasetGraph).initialBinding(inputBinding).build();
        if (uProc == null)
            throw new ARQException("No suitable update procesors are registered/able to execute your updates");
        return uProc.execute();
    }


    /** Execute a single SPARQL Update operation.
     * @param update
     * @param model
     */
    public static List<UpdateResult> execute(Update update, Model model,Context connCtx)
    {
        return execute(update, model.getGraph(),connCtx) ;
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param graph
     */
    public static List<UpdateResult> execute(Update update, Graph graph,Context connCtx)
    {
        return execute(update, toDatasetGraph(graph),connCtx) ;
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param dataset
     */
    public static List<UpdateResult> execute(Update update, Dataset dataset,Context connCtx)
    {
        return execute(update, dataset.asDatasetGraph(),connCtx) ;
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param dataset
     */
    public static List<UpdateResult> execute(Update update, DatasetGraph dataset,Context connCtx)
    {
        return execute(update, dataset, null,connCtx) ;
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param dataset
     * @param inputBinding
     */
    public static List<UpdateResult> execute(Update update, Dataset dataset, QuerySolution inputBinding,Context connCtx)
    {
        return execute(update, dataset.asDatasetGraph(), BindingLib.asBinding(inputBinding),connCtx) ;
    }

    /** Execute a single SPARQL Update operation.
     * @param update
     * @param datasetGraph
     * @param inputBinding
     */
    public static List<UpdateResult> execute(Update update, DatasetGraph datasetGraph, Binding inputBinding,Context connCtx)
    {
        return execute$(update, datasetGraph, inputBinding,connCtx) ;
    }

    private static List<UpdateResult> execute$(Update update, DatasetGraph datasetGraph, Binding inputBinding,Context connCtx)
    {
        UpdateRequest request = new UpdateRequest(connCtx) ;
        request.add(update) ;
        return execute$(request, datasetGraph, inputBinding) ;
    }

    // Streaming Updates:

    /** Parse update operations into a DatasetGraph by reading it from a file */
    public static List<UpdateResult> parseExecute(UsingList usingList, DatasetGraph dataset, String fileName)
    {
        return parseExecute(usingList, dataset, fileName, null, Syntax.defaultUpdateSyntax) ;
    }

    /** Parse update operations into a DatasetGraph by reading it from a file */
    public static List<UpdateResult> parseExecute(UsingList usingList, DatasetGraph dataset, String fileName, Syntax syntax)
    {
        return parseExecute(usingList, dataset, fileName, null, syntax) ;
    }

    /** Parse update operations into a DatasetGraph by reading it from a file */
    public static List<UpdateResult> parseExecute(UsingList usingList, DatasetGraph dataset, String fileName, String baseURI, Syntax syntax)
    {
        return parseExecute(usingList, dataset, fileName, (Binding)null, baseURI, syntax);
    }

    /** Parse update operations into a DatasetGraph by reading it from a file */
    public static List<UpdateResult> parseExecute(UsingList usingList, DatasetGraph dataset, String fileName, QuerySolution inputBinding, String baseURI, Syntax syntax)
    {
       return  parseExecute(usingList, dataset, fileName, BindingLib.asBinding(inputBinding), baseURI, syntax) ;
    }

    /** Parse update operations into a DatasetGraph by reading it from a file */
    @SuppressWarnings("resource")
    public static List<UpdateResult> parseExecute(UsingList usingList, DatasetGraph dataset, String fileName, Binding inputBinding, String baseURI, Syntax syntax)
    {
        InputStream in = null ;
        if ( fileName.equals("-") )
            in = System.in ;
        else {
            in = IO.openFile(fileName) ;
            if ( in == null )
                throw new UpdateException("File could not be opened: "+fileName) ;
        }
        final List<UpdateResult> ret = parseExecute(usingList, dataset, in, inputBinding, baseURI, syntax) ;
        if ( in != System.in )
            IO.close(in) ;
        return ret;
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param input     The source of the update request (must be UTF-8).
     */
    public static List<UpdateResult> parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input)
    {
        return parseExecute(usingList, dataset, input, Syntax.defaultUpdateSyntax) ;
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param input     The source of the update request (must be UTF-8).
     * @param syntax    The update language syntax
     */
    public static List<UpdateResult> parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, Syntax syntax)
    {
        return  parseExecute(usingList, dataset, input, null, syntax) ;
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param input     The source of the update request (must be UTF-8).
     * @param baseURI   The base URI for resolving relative URIs.
     */
    public static List<UpdateResult> parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, String baseURI)
    {
        return parseExecute(usingList, dataset, input, baseURI, Syntax.defaultUpdateSyntax) ;
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param dataset   The dataset to apply the changes to
     * @param input     The source of the update request (must be UTF-8).
     * @param baseURI   The base URI for resolving relative URIs (may be <code>null</code>)
     * @param syntax    The update language syntax
     */
    public static List<UpdateResult> parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, String baseURI, Syntax syntax)
    {
        return parseExecute(usingList, dataset, input, (Binding)null, baseURI, syntax);
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList    A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param dataset      The dataset to apply the changes to
     * @param input        The source of the update request (must be UTF-8).
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding
     *                     (i.e. UpdateDeleteWhere, UpdateModify).  May be <code>null</code>
     * @param baseURI      The base URI for resolving relative URIs (may be <code>null</code>)
     * @param syntax       The update language syntax
     */
    public static List<UpdateResult> parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, QuerySolution inputBinding, String baseURI, Syntax syntax)
    {
        return parseExecute(usingList, dataset, input, BindingLib.asBinding(inputBinding), baseURI, syntax) ;
    }

    /**
     * Parse update operations into a DatasetGraph by parsing from an InputStream.
     * @param usingList    A list of USING or USING NAMED statements that be added to all {@link UpdateWithUsing} queries
     * @param dataset      The dataset to apply the changes to
     * @param input        The source of the update request (must be UTF-8).
     * @param inputBinding Initial binding to be applied to Update operations that can apply an initial binding
     *                     (i.e. UpdateDeleteWhere, UpdateModify).  May be <code>null</code>
     * @param baseURI      The base URI for resolving relative URIs (may be <code>null</code>)
     * @param syntax       The update language syntax
     */
    public static List<UpdateResult>parseExecute(UsingList usingList, DatasetGraph dataset, InputStream input, Binding inputBinding, String baseURI, Syntax syntax)
    {
        @SuppressWarnings("deprecation")
        UpdateProcessorStreaming uProc = UpdateExecutionFactory.createStreaming(dataset, inputBinding) ;
        if (uProc == null)
            throw new ARQException("No suitable update procesors are registered/able to execute your updates");

        uProc.startRequest();
        try
        {
            UpdateSink sink = new UsingUpdateSink(uProc.getUpdateSink(), usingList) ;
            try
            {
                UpdateParser parser = UpdateFactory.setupParser(uProc.getPrologue(), baseURI, syntax) ;
                return parser.parse(sink, uProc.getPrologue(), input) ;
            }
            finally
            {
                sink.close() ;
            }
        }
        finally
        {
            uProc.finishRequest();
        }
        
        
    }
}
