/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package com.genband.util.log.slf4j;

/**
 * Exception thrown when the SLF4J adapter encounters a problem.
 *
 */
public class SLF4JLoggingException extends RuntimeException {

    /**
     * Generated serial version ID.
     */
    private static final long serialVersionUID = -1618650972455089998L;

    public SLF4JLoggingException(final String msg) {
        super(msg);
    }

    public SLF4JLoggingException(final String msg, final Exception ex) {
        super(msg, ex);
    }

    public SLF4JLoggingException(final Exception ex) {
        super(ex);
    }
}
