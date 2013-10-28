/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2013 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
package org.apache.sling.resourcemerger.impl;

import org.apache.felix.scr.annotations.Property;
import org.apache.sling.api.adapter.AdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergedResourceAdapterFactory implements AdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(MergedResourceAdapterFactory.class);

    private static final Class<MergedValueMap> MERGED_VALUE_MAP_CLASS = MergedValueMap.class;

    @Property(name = "adaptables")
    public static final String[] ADAPTABLE_CLASSES = {
            MergedResource.class.getName()
    };

    @Property(name = "adapters")
    public static final String[] ADAPTER_CLASSES = {
            MERGED_VALUE_MAP_CLASS.getName(),
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
        if (type == MERGED_VALUE_MAP_CLASS) {
            return (AdapterType) new MergedValueMap(resource);
        }

        log.debug("Unable to adapt merged resource to requested type: {}", type.getClass().getName());
        return null;
    }

}
