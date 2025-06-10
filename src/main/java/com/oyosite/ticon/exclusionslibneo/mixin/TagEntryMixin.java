package com.oyosite.ticon.exclusionslibneo.mixin;


import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oyosite.ticon.exclusionslibneo.extensions.TagEntryExclusionHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Objects;
import java.util.function.Function;

@Mixin(value = TagEntry.class)
public abstract class TagEntryMixin implements TagEntryExclusionHolder {

    @Final
    @Shadow
    private ResourceLocation id;
    @Final
    @Shadow
    private boolean tag;
    @Final
    @Shadow
    private boolean required;

    @Unique
    private Boolean excluded;

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static <O> Function<RecordCodecBuilder.Instance<O>, ? extends App<RecordCodecBuilder.Mu<O>, O>> wrap_create(
            Function<RecordCodecBuilder.Instance<O>, ? extends App<RecordCodecBuilder.Mu<O>, O>> builder) {
        return TagEntryMixin.codecBuilder(builder);
    }

    @Unique
    private static <O> Function<RecordCodecBuilder.Instance<O>, ? extends App<RecordCodecBuilder.Mu<O>, O>> codecBuilder(Function<RecordCodecBuilder.Instance<O>, ? extends App<RecordCodecBuilder.Mu<O>, O>> builder) {
        return instance -> instance.group(
                RecordCodecBuilder.mapCodec(builder).forGetter(Function.identity()),
                Codec.BOOL.optionalFieldOf("excluded", false).forGetter(exclusion -> (((TagEntryExclusionHolder) exclusion).exclusionsLib$isExcluded()))
        ).apply(instance, (exclusion, boolField) -> {
            ((TagEntryExclusionHolder) exclusion).exclusionsLib$setExcluded(boolField);
            return exclusion;
        });
    }

    @Override
    public Boolean exclusionsLib$isExcluded()
    {
        return Objects.requireNonNullElse(this.excluded, false);
    }

    @Override
    public void exclusionsLib$setExcluded(Boolean tagEntry)
    {
        this.excluded = tagEntry;
    }

    @Override
    public boolean exclusionsLib$isTag()
    {
        return this.tag;
    }

    @Override
    public boolean exclusionsLib$isRequired()
    {
        return this.required;
    }

    @Override
    public ResourceLocation exclusionsLib$getId()
    {
        return this.id;
    }

}
