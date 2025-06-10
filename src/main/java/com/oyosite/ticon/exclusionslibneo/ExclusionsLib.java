package com.oyosite.ticon.exclusionslibneo;

import com.mojang.logging.LogUtils;
import com.oyosite.ticon.exclusionslibneo.predicates.OverlapsStructureBlockPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExclusionsLib.MODID)
public class ExclusionsLib {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "exclusions_lib";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<BlockPredicateType<?>> BLOCK_PREDICATE_TYPES = DeferredRegister.create(Registries.BLOCK_PREDICATE_TYPE, MODID);

    public static final DeferredHolder<BlockPredicateType<?>, BlockPredicateType<OverlapsStructureBlockPredicate>> OVERLAPS_STRUCTURE = BLOCK_PREDICATE_TYPES.register("overlaps_structure", () -> () -> OverlapsStructureBlockPredicate.CODEC);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ExclusionsLib(IEventBus modEventBus, ModContainer modContainer) {
        BLOCK_PREDICATE_TYPES.register(modEventBus);
    }
}
