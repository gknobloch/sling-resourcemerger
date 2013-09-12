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
package org.apache.sling.resourcemerger.api;

import org.apache.sling.api.resource.Resource;

/**
 * Merged resources are collections of physical {@link Resource}s aggregated
 * into one object.
 */
public interface MergedResource extends Resource {

    /**
     * Gets merged resource's relative path
     *
     * @return The merged resource's relative path
     */
    public String getRelativePath();

    /**
     * Adds a physical resource to the merged resource
     *
     * @param resource The physical resource to add to the merged resource
     */
    public void addMappedResource(Resource resource);

    /**
     * Returns an iterable of the physical resources mapped to the current merged resource.
     *
     * @return Iterable of the physical resources mapped to the current merged resource.
     */
    public Iterable<Resource> getMappedResources();

}
