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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(metatype = false)
@Service(AdapterFactory.class)
@Properties({
        @Property(name = "service.description", value = "Merged Resources Adapter")
})
public class MergedResourceAdapterFactory implements AdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(MergedResourceAdapterFactory.class);

    private static final Class<ValueMap> VALUE_MAP_CLASS = ValueMap.class;

    @Property(name = "adaptables")
    public static final String[] ADAPTABLE_CLASSES = {
            MergedResource.class.getName()
    };

    @Property(name = "adapters")
    public static final String[] ADAPTER_CLASSES = {
            VALUE_MAP_CLASS.getName(),
    };

    public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
        if (adaptable instanceof MergedResource) {
            return getAdapter((MergedResource) adaptable, type);
        }

        log.debug("Unable to handle adaptable: {}", adaptable.getClass().getName());
        return null;
    }

    @SuppressWarnings("unchecked")
    private <AdapterType> AdapterType getAdapter(MergedResource resource, Class<AdapterType> type) {
        if (type == VALUE_MAP_CLASS) {
            return (AdapterType) new MergedValueMap(resource);
        }

        log.debug("Unable to adapt merged resource to requested type: {}", type.getClass().getName());
        return null;
    }

}
