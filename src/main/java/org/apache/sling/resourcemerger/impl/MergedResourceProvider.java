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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.resourcemerger.api.MergedResource;
import org.apache.sling.resourcemerger.api.MergedResourceConstants;
import org.apache.sling.resourcemerger.api.ResourceMergerService;

/**
 * The <code>MergedResourceProvider</code> is the resource provider providing
 * access to {@link org.apache.sling.resourcemerger.api.MergedResource} objects.
 */
public class MergedResourceProvider implements ResourceProvider {

    private final String mergeRootPath;
    private ResourceMergerService resourceMerger;

    public MergedResourceProvider(ResourceMergerService resourceMerger, String mergeRootPath) {
        this.resourceMerger = resourceMerger;
        this.mergeRootPath = mergeRootPath;
    }

    /**
     * {@inheritDoc}
     */
    public Resource getResource(ResourceResolver resolver, HttpServletRequest request, String path) {
        return getResource(resolver, path);
    }

    /**
     * {@inheritDoc}
     */
    public Resource getResource(ResourceResolver resolver, String path) {
        return resourceMerger.merge(resolver, resolver.getSearchPath(), mergeRootPath, getRelativePath(path));
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<Resource> listChildren(Resource resource) {
        if (resource instanceof MergedResource) {
            MergedResource mergedResource = (MergedResource) resource;
            List<Resource> children = new ArrayList<Resource>();

            for (Resource mappedResource : mergedResource.getMappedResources()) {
                // Check if previously defined children have to be ignored
                if (mappedResource.adaptTo(ValueMap.class).get(MergedResourceConstants.PN_HIDE_CHILDREN, Boolean.FALSE)) {
                    // Clear current children list
                    children.clear();
                }

                // Browse children of current physical resource
                for (Resource child : mappedResource.getChildren()) {
                    String childRelativePath = ResourceUtil.normalize(mergedResource.getRelativePath() + "/" + child.getName());

                    if (child.adaptTo(ValueMap.class).get(MergedResourceConstants.PN_HIDE_RESOURCE, Boolean.FALSE)) {
                        // Child resource has to be hidden
                        children.remove(new MergedResourceImpl(mergeRootPath, childRelativePath));

                    } else {
                        // Check if the child resource already exists in the children list
                        MergedResource mergedResChild = new MergedResourceImpl(mergeRootPath, childRelativePath);
                        int mergedResChildIndex = -1;
                        if (children.contains(mergedResChild)) {
                            // Get current index of the merged resource's child
                            mergedResChildIndex = children.indexOf(mergedResChild);
                            mergedResChild = (MergedResource) children.get(mergedResChildIndex);
                        }
                        // Add a new mapped resource to the merged resource
                        mergedResChild.addMappedResource(child);
                        boolean mergedResChildExists = mergedResChildIndex > -1;

                        // Check if children need reordering
                        int orderBeforeIndex = -1;
                        String orderBefore = ResourceUtil.getValueMap(child).get(MergedResourceConstants.PN_ORDER_BEFORE, String.class);
                        if (orderBefore != null && !orderBefore.equals(mergedResChild.getName())) {
                            // Get a dummy merged resource just to know the index of that merged resource
                            MergedResource orderBeforeRes = new MergedResourceImpl(mergeRootPath, mergedResource.getRelativePath() + "/" + orderBefore);
                            if (children.contains(orderBeforeRes)) {
                                orderBeforeIndex = children.indexOf(orderBeforeRes);
                            }
                        }

                        if (orderBeforeIndex > -1) {
                            // Add merged resource's child at the right position
                            children.add(orderBeforeIndex, mergedResChild);
                            if (mergedResChildExists) {
                                children.remove(mergedResChildIndex > orderBeforeIndex ? ++mergedResChildIndex : mergedResChildIndex);
                            }
                        } else if (!mergedResChildExists) {
                            // Only add the merged resource's child if it did not exist yet
                            children.add(mergedResChild);
                        }
                    }
                }
            }

            return children.iterator();
        }

        // Return null for resources that aren't a MergedResource
        return null;
    }

    /**
     * Gets the relative path out of merge root path
     *
     * @param path Absolute path
     * @return Relative path
     */
    private String getRelativePath(String path) {
        return StringUtils.removeStart(path, mergeRootPath);
    }

}