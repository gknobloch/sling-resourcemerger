/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.resourcemerger.impl;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceProviderFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;

import java.util.Map;

@Component(metatype = false)
@Service(value = ResourceProviderFactory.class)
@Properties({
        @Property(name = ResourceProvider.ROOTS, value = {"/virtual"}, propertyPrivate = true)
})
/**
 * The <code>VirtualResourceProviderFactory</code> creates virtual resource
 * providers.
 */
public class VirtualResourceProviderFactory implements ResourceProviderFactory {

    private String virtualRootPath;

    /**
     * {@inheritDoc}
     */
    public ResourceProvider getResourceProvider(Map<String, Object> stringObjectMap) throws LoginException {
        return new VirtualResourceProvider(virtualRootPath);
    }

    /**
     * {@inheritDoc}
     */
    public ResourceProvider getAdministrativeResourceProvider(Map<String, Object> stringObjectMap) throws LoginException {
        return new VirtualResourceProvider(virtualRootPath);
    }

    @Activate
    private void configure(Map<String, ?> properties) {
        String[] virtualRootPaths = PropertiesUtil.toStringArray(properties.get(ResourceProvider.ROOTS), new String[0]);
        if (virtualRootPaths.length > 0) {
            virtualRootPath = virtualRootPaths[0];
        }
    }

}
