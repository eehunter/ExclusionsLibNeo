package com.oyosite.ticon.exclusionslibneo.predicates;


import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.oyosite.ticon.exclusionslibneo.ExclusionsLib;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OverlapsStructureBlockPredicate implements BlockPredicate {

    public static final MapCodec<OverlapsStructureBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(
            (instance) ->
                    instance.group(
                            Vec3i.offsetCodec(16).optionalFieldOf("offset", BlockPos.ZERO).forGetter((predicate) -> predicate.offset),
                            RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("structures").forGetter(OverlapsStructureBlockPredicate::structure),
                            Codec.intRange(0, 32).optionalFieldOf("range", 0).forGetter((predicate) -> predicate.range)
                    ).apply(instance, OverlapsStructureBlockPredicate::new)
    );

    private final Vec3i offset;
    private final int range;
    private final List<Structure> structures;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<HolderSet<Structure>> rawStructures;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public OverlapsStructureBlockPredicate(Vec3i offset, Optional<HolderSet<Structure>> structures, int range) {
        this.rawStructures = structures;
        this.structures = structures
                .map(s -> s.stream().map(Holder::value).collect(Collectors.toList()))
                .orElse(null);
        this.offset = offset;
        this.range = range;

    }

    public boolean test(WorldGenLevel structureWorldAccess, BlockPos blockPos) {
        ServerLevel world = structureWorldAccess.getLevel();
        StructureManager accessor = world.structureManager();
        BlockPos blockPosOffset = blockPos.offset(this.offset);
        Predicate<StructureStart> predicate = makePredicate(blockPosOffset);
        for (var struct : accessor.getAllStructuresAt(blockPosOffset).entrySet()) {
            if (this.structures != null && !this.structures.contains(struct.getKey())) continue;
            for (long pos : struct.getValue()) {
                SectionPos sectionPos = SectionPos.of(new ChunkPos(pos), world.getMinSection());
                StructureStart start = accessor.getStartForStructure(
                        sectionPos, struct.getKey(), world.getChunk(sectionPos.getX(), sectionPos.getZ(), ChunkStatus.STRUCTURE_STARTS)
                );
                if (start != null && start.isValid() && predicate.test(start)) return true;
            }
        }
        return false;
    }

    private @NotNull Predicate<StructureStart> makePredicate(BlockPos blockPosOffset) {
        final BoundingBox exclusionZone = new BoundingBox(
                blockPosOffset.getX() - this.range, blockPosOffset.getY() - this.range, blockPosOffset.getZ() - this.range,
                blockPosOffset.getX() + this.range, blockPosOffset.getY() + this.range, blockPosOffset.getZ() + this.range
        );
        return start -> {
            for (StructurePiece piece : start.getPieces())
                if (piece.getBoundingBox().intersects(exclusionZone)) return true;
            return false;
        };
    }

    public Optional<HolderSet<Structure>> structure() {
        return rawStructures;
    }

    @Override
    public @NotNull BlockPredicateType<?> type() {
        return ExclusionsLib.OVERLAPS_STRUCTURE.get();
    }

}
