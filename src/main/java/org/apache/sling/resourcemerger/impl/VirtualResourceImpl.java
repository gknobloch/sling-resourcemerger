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

import org.apache.sling.api.resource.*;
import org.apache.sling.resourcemerger.api.VirtualResource;
import org.apache.sling.resourcemerger.api.VirtualResourceConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VirtualResourceImpl implements VirtualResource {

    private final String virtualPath;
    private final List<Resource> mappedResources = new ArrayList<Resource>();
    private List<Resource> children = null;

    private VirtualResourceImpl(String virtualPath) {
        this.virtualPath = virtualPath;
    }

    public VirtualResourceImpl(String virtualPath, List<Resource> mappedResources) {
        this.virtualPath = virtualPath;
        this.mappedResources.addAll(mappedResources);
    }

    /**
     * {@inheritDoc}
     */
    public String getPath() {
        return virtualPath;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        // As mapping is done on the path, the resource name are equal
        // We can just use the first one
        return mappedResources.isEmpty()
                ? null
                : mappedResources.get(0).getName();
    }

    /**
     * {@inheritDoc}
     */
    public Resource getParent() {
        return getResourceResolver().getResource(virtualPath.substring(0, virtualPath.lastIndexOf("/")));
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<Resource> listChildren() {
        computeChildren();
        return children.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<Resource> getChildren() {
        computeChildren();
        return children;
    }

    /**
     * {@inheritDoc}
     */
    public Resource getChild(String relPath) {
        return getResourceResolver().getResource(this, relPath);
    }

    /**
     * {@inheritDoc}
     */
    public String getResourceType() {
        // Using the last mapped resource's type
        return mappedResources.isEmpty()
                ? null
                : mappedResources.get(mappedResources.size() - 1).getResourceType();
    }

    /**
     * {@inheritDoc}
     */
    public String getResourceSuperType() {
        // Using the last mapped resource's super type
        // TODO: Loop and get value
        // Problem for instance with JcrNodeResource, which returns <unset> instead of null.
        // The same convention might not be applied for all implementations of the Resource API.
        return mappedResources.isEmpty()
                ? null
                : mappedResources.get(mappedResources.size() - 1).getResourceSuperType();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isResourceType(String resourceType) {
        // Looping over mapped resources to check if one of them is of the provided resource type
        for (Resource mr : mappedResources) {
            if (mr.isResourceType(resourceType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public ResourceMetadata getResourceMetadata() {
        // Using the last mapped resource's metadata
        return mappedResources.isEmpty()
                ? null
                : mappedResources.get(mappedResources.size() - 1).getResourceMetadata();
    }

    /**
     * {@inheritDoc}
     */
    public ResourceResolver getResourceResolver() {
        // Using the last mapped resource's resource resolver
        return mappedResources.isEmpty()
                ? null
                : mappedResources.get(mappedResources.size() - 1).getResourceResolver();
    }

    /**
     * {@inheritDoc}
     */
    public void addMappedResource(Resource resource) {
        mappedResources.add(resource);
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<Resource> getMappedResources() {
        return mappedResources;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        if (type == VirtualResource.class) {
            return (AdapterType) this;

        } else if (type == ValueMap.class) {
            return (AdapterType) new VirtualValueMap(this);
        }

        return null;
    }

    /**
     * Virtual resources are considered equal if their paths are equal,
     * regardless of the list of mapped resources.
     * @param o Object to compare with
     * @return Returns <code>true</code> if both virtual resource have the
     * same path.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != getClass()) {
            return false;
        }

        Resource r = (Resource) o;
        return r.getPath().equals(getPath());
    }

    /**
     * Internal helper to compute the list of virtual child resources for the
     * current virtual resource.
     */
    private void computeChildren() {
        if (children == null) {
            children = new ArrayList<Resource>();
            for (Resource r : mappedResources) {
                // Check if previously defined children have to be ignored
                if (r.adaptTo(ValueMap.class).get(VirtualResourceConstants.PN_HIDE_CHILDREN, Boolean.FALSE)) {
                    // Clear current children list
                    children.clear();
                }

                // Browse children of current physical resource
                for (Resource child : r.getChildren()) {
                    String childVirtualPath = getPath() + "/" + child.getName();

                    if (child.adaptTo(ValueMap.class).get(VirtualResourceConstants.PN_HIDE_RESOURCE, Boolean.FALSE)) {
                        // Child resource has to be hidden
                        children.remove(new VirtualResourceImpl(childVirtualPath));

                    } else {
                        // Check if the child resource already exists in the children list
                        VirtualResource virtualChild = new VirtualResourceImpl(childVirtualPath);
                        int virtualChildIndex = -1;
                        if (children.contains(virtualChild)) {
                            // Get current index of the virtual child
                            virtualChildIndex = children.indexOf(virtualChild);
                            virtualChild = (VirtualResource) children.get(virtualChildIndex);
                        }
                        // Add a new mapped resource to the virtual resource
                        virtualChild.addMappedResource(child);
                        boolean virtualChildExists = virtualChildIndex > -1;

                        // Check if children need reordering
                        int orderBeforeIndex = -1;
                        String orderBefore = ResourceUtil.getValueMap(child).get(VirtualResourceConstants.PN_ORDER_BEFORE, String.class);
                        if (orderBefore != null && !orderBefore.equals(virtualChild.getName())) {
                            // Get a dummy virtual resource just to know the index of that virtual resource
                            VirtualResource orderBeforeRes = new VirtualResourceImpl(getPath() + "/" + orderBefore);
                            if (children.contains(orderBeforeRes)) {
                                orderBeforeIndex = children.indexOf(orderBeforeRes);
                            }
                        }

                        if (orderBeforeIndex > -1) {
                            // Add virtual child at the right position
                            children.add(orderBeforeIndex, virtualChild);
                            if (virtualChildExists) {
                                children.remove(virtualChildIndex > orderBeforeIndex ? ++virtualChildIndex : virtualChildIndex);
                            }
                        } else if (!virtualChildExists) {
                            // Only add the virtual child if it did not exist yet
                            children.add(virtualChild);
                        }
                    }
                }
            }
        }
    }

}
