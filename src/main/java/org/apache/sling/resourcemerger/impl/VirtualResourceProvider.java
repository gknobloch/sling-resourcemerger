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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.resourcemerger.api.VirtualResource;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The <code>VirtualResourceProvider</code> is the resource provider providing
 * access to {@link VirtualResource} objects.
 */
public class VirtualResourceProvider implements ResourceProvider {

    private final String virtualRoot;

    public VirtualResourceProvider(String virtualRoot) {
        this.virtualRoot = virtualRoot;
    }

    /**
     * {@inheritDoc}
     */
    public Resource getResource(ResourceResolver resourceResolver, HttpServletRequest request, String path) {
        return getResource(resourceResolver, path);
    }

    /**
     * {@inheritDoc}
     */
    public Resource getResource(ResourceResolver resourceResolver, String path) {
        return getVirtualResource(resourceResolver, path);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<Resource> listChildren(Resource resource) {
        Resource virtualResource = getVirtualResource(resource.getResourceResolver(), resource.getPath());
        return virtualResource != null ? virtualResource.listChildren() : null;
    }

    /**
     * Gets a virtual resource
     *
     * @param resourceResolver Resource resolver
     * @param path             Absolute path of the virtual resource to get
     * @return A virtual resource
     */
    private Resource getVirtualResource(ResourceResolver resourceResolver, String path) {
        List<Resource> mappedResources = new ArrayList<Resource>();

        String[] searchPaths = resourceResolver.getSearchPath();
        if (searchPaths != null) {
            // Loop over resource resolver's search paths
            for (String searchPath : searchPaths) {
                // Try to get the corresponding physical resource for this search path
                Resource searchRes = resourceResolver.getResource(path.replaceFirst(virtualRoot, searchPath));
                if (searchRes != null) {
                    // Physical resource exists, add it to the list of mapped resources
                    mappedResources.add(0, searchRes);
                }
            }

            if (!mappedResources.isEmpty()) {
                // Create a new virtual resource based on the list of mapped physical resources
                return new VirtualResourceImpl(path, mappedResources);
            }
        }

        // Either search paths were not defined, or the resource does not exist in any of them
        return null;
    }

}
