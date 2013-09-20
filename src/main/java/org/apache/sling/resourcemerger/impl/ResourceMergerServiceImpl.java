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

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.resourcemerger.api.MergedResource;
import org.apache.sling.resourcemerger.api.ResourceMergerService;

@Component(metatype = false)
@Service(value = ResourceMergerService.class)
public class ResourceMergerServiceImpl implements ResourceMergerService {

    public MergedResource merge(ResourceResolver resolver, String mergeRootPath, String[] basePaths, String relativePath) {
        List<Resource> mappedResources = new ArrayList<Resource>();

        if (basePaths != null) {
            // Loop over provided base paths
            for (String basePath : basePaths) {
                // Try to get the corresponding physical resource for this base path
                Resource baseRes = resolver.getResource(ResourceUtil.normalize(basePath + "/" + relativePath));
                if (baseRes != null) {
                    // Physical resource exists, add it to the list of mapped resources
                    mappedResources.add(0, baseRes);
                }
            }

            if (!mappedResources.isEmpty()) {
                // Create a new merged resource based on the list of mapped physical resources
                return new MergedResourceImpl(resolver, mergeRootPath, relativePath, mappedResources);
            }
        }

        // Either base paths were not defined, or the resource does not exist in any of them
        return null;
    }

}
