package com.jmane2026.oldschoollevels.common.commands;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.LevelingHandler;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.EnumArgument;

@EventBusSubscriber(modid = OldSchoolLevels.MODID)
public class OSLCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("osl")
                .requires(source -> {
                    if (source.getEntity() instanceof ServerPlayer player) {
                        NameAndId identity = new NameAndId(player.getGameProfile().id(), player.getGameProfile().name());
                        return source.getServer().getPlayerList().isOp(identity);
                    }
                    return false;
                })
                .then(Commands.literal("set")
                        .then(Commands.argument("skill", EnumArgument.enumArgument(Skill.class))
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 99))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            Skill skill = context.getArgument("skill", Skill.class);
                                            int level = IntegerArgumentType.getInteger(context, "level");

                                            long xp = ExperienceUtils.getXpForLevel(level);
                                            LevelingHandler.setXp(player, skill, xp);

                                            context.getSource().sendSuccess(() -> Component.literal("Set " + skill.getDisplayName() + " to level " + level), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}