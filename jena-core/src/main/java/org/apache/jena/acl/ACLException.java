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

/** Hopefully, you won't see this! */

package org.apache.jena.acl;

import org.apache.jena.shared.JenaException ;

public class ACLException extends JenaException
{
    public ACLException(String msg, Throwable cause)    { super(msg, cause) ; }
    public ACLException(String msg)                     { super(msg) ; }
    public ACLException(Throwable cause)                { super(cause) ; }
    public ACLException()                               { super() ; }
}
