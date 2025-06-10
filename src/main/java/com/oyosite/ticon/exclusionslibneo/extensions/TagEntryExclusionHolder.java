package com.oyosite.ticon.exclusionslibneo.extensions;

import net.minecraft.resources.ResourceLocation;

public interface TagEntryExclusionHolder {
    Boolean exclusionsLib$isExcluded();
    void exclusionsLib$setExcluded(Boolean tagEntry);

    boolean exclusionsLib$isTag();
    boolean exclusionsLib$isRequired();
    ResourceLocation exclusionsLib$getId();

}
