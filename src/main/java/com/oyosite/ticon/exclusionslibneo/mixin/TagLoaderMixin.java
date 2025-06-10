package com.oyosite.ticon.exclusionslibneo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.oyosite.ticon.exclusionslibneo.ExclusionsLib;
import com.oyosite.ticon.exclusionslibneo.extensions.TagEntryExclusionHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = TagLoader.class)
public class TagLoaderMixin {

    @WrapOperation(
            method = "build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagEntry;build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/function/Consumer;)Z", ordinal = 0)
    )
    private <T> boolean exclusionsLib$removeEntry(TagEntry entry, TagEntry.Lookup<T> valueGetter, Consumer<T> idConsumer, Operation<Boolean> original, TagEntry.Lookup<T> valueGetter1, List<TagLoader.EntryWithSource> entries, @Local LocalRef<LinkedHashSet<T>> builder) {
        TagEntryExclusionHolder holder = (TagEntryExclusionHolder) entry;
        if (!holder.exclusionsLib$isExcluded()) return original.call(entry, valueGetter, idConsumer);

        ExclusionsLib.LOGGER.info("[Exclusions Lib] The Following Tag has been detected: {}", entry);
        List<T> list = new ArrayList<>(builder.get());
        ResourceLocation id = holder.exclusionsLib$getId();
        boolean required = holder.exclusionsLib$isRequired();
        if (holder.exclusionsLib$isTag())
        {
            Collection<T> collection = valueGetter.tag(id);
            if (collection == null) return !required;
            list.removeAll(collection);
        }
        else
        {
            T object = valueGetter.element(id);
            if (object == null) return !required;
            list.remove(object);
        }

        builder.set(new LinkedHashSet<>(list));
        return true;
    }

}
