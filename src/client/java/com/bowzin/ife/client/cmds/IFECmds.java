package com.bowzin.ife.client.cmds;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;


public class IFECmds {

    private IFECmds() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildCtx) ->
                dispatcher.register(literal("ife")
                         .then(literal("test").executes(IFECmds::test))
                ));
    }

    private static int test(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().sendFeedback(Component.literal("I really should remove this command...").withColor(TextColor.DARK_RED));
        return 1;
    }
}
