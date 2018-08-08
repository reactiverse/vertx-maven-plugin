/*
 *
 *   Copyright (c) 2016-2018 Red Hat, Inc.
 *
 *   Red Hat licenses this file to you under the Apache License, version
 *   2.0 (the "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *   implied.  See the License for the specific language governing
 *   permissions and limitations under the License.
 */
package io.reactiverse.vertx.maven.plugin.components;

/**
 * Exception thrown when the application package cannot be built correctly.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PackagingException extends Exception {

    /**
     * Creates a new {@link PackagingException}.
     *
     * @param e the cause
     */
    public PackagingException(Exception e) {
        super("Unable to build the package", e);
    }

    /**
     * Creates a new {@link PackagingException}.
     *
     * @param msg the message
     */
    public PackagingException(String msg) {
        super(msg);
    }

    /**
     * Creates a new {@link PackagingException}.
     *
     * @param msg   the message
     * @param cause the cause
     */
    public PackagingException(String msg, Exception cause) {
        super(msg, cause);
    }
}
