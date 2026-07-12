package com.jmane2026.oldschoollevels.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record TeleportLocation(String name, BlockPos pos, ResourceKey<Level> dimension) {
    public static final Codec<TeleportLocation> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(TeleportLocation::name),
                    BlockPos.CODEC.fieldOf("pos").forGetter(TeleportLocation::pos),
                    ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(TeleportLocation::dimension)
            ).apply(instance, TeleportLocation::new)
    );

    public static final StreamCodec<FriendlyByteBuf, TeleportLocation> STREAM_CODEC = StreamCodec.of(
            (buf, loc) -> {
                buf.writeUtf(loc.name);
                buf.writeBlockPos(loc.pos);
                buf.writeResourceKey(loc.dimension);
            },
            buf -> new TeleportLocation(buf.readUtf(), buf.readBlockPos(), buf.readResourceKey(Registries.DIMENSION))
    );
}