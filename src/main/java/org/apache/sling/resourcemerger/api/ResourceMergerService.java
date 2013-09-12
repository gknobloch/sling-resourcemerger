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

import org.apache.sling.api.resource.ResourceResolver;

/**
 * Service to merge multiple base paths into one single resource.
 */
public interface ResourceMergerService {

    /**
     * Gets a {@link MergedResource} from provided base paths, merge root path
     * and relative path.
     *
     * @param resourceResolver The resource resolver
     * @param mergeRootPath    The merge root path
     * @param basePaths        An array of base paths
     * @param relativePath     The relative path to merge from base paths
     * @return Returns the merged resource
     */
    MergedResource merge(ResourceResolver resourceResolver, String mergeRootPath, String[] basePaths, String relativePath);

}
