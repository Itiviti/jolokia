/*
 * Copyright 2009-2011 Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jolokia.client.request;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.json.simple.JSONObject;

/**
 * Request for a MBean search
 *
 * @author roland
 * @since 26.03.11
 */
public class J4pSearchRequest extends AbtractJ4pMBeanRequest {

    /**
     * Create request with a objectname, which can be a pattern
     *
     * @param pMBeanPattern pattern to use for a search
     */
    protected J4pSearchRequest(ObjectName pMBeanPattern) {
        super(J4pType.SEARCH,pMBeanPattern);
    }

    /**
     * Create a search request
     *
     * @param pMBeanPattern MBean pattern as string
     * @throws MalformedObjectNameException if the provided pattern is not a valid {@link ObjectName}
     */
    public J4pSearchRequest(String pMBeanPattern) throws MalformedObjectNameException {
        this(new ObjectName(pMBeanPattern));
    }

    @Override
    J4pSearchResponse createResponse(JSONObject pResponse) {
        return new J4pSearchResponse(this,pResponse);
    }
}