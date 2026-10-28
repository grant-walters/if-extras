package com.bowzin.ife.client.gui.cogs;


import com.bowzin.ife.client.config.IFEConfig;
import com.bowzin.ife.client.events.DelayedActionsManager;
import com.bowzin.ife.client.gui.HudCog;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class PartyCog extends HudCog {

    private String owner;
    private final ArrayList<String> members = new ArrayList<>();

    private static final Pattern LEADER = Pattern.compile("ᴘᴀʀᴛʏ: (.*) has been promoted to party leader");
    private static final Pattern MODERATOR = Pattern.compile("ᴘᴀʀᴛʏ: (.*) has been made a party moderator");
    private static final Pattern JOINED = Pattern.compile("ᴘᴀʀᴛʏ: (.*) has joined your party!");
    private static final Pattern OFFLINE = Pattern.compile("ᴘᴀʀᴛʏ: (.*) has been kicked from your party for being offline for .*");
    private static final Pattern LEFT = Pattern.compile("ᴘᴀʀᴛʏ: (.*) has left your party!");

    // TODO(bug/MEDIUM): party state is never cleared on disconnect or server switch (no
    //  ClientPlayConnectionEvents.DISCONNECT hook), so after relogging the cog keeps showing the old party, even if the
    //  server disbanded it or kicked you for being offline while you were gone.
    // TODO(bug/LOW): nextIsMembers/nextIsDivider never time out. If the "(n/m) ..." line doesn't come right after the
    //  header (another chat line lands in between, or the format changes), the flag stays set, and a later unrelated
    //  message matching the pattern gets parsed as the member list. Reset both flags on any non-matching message.
    private boolean nextIsMembers = false;
    private boolean nextIsDivider = false;

    public PartyCog(String id, IFEConfig.CogSettings settings) {
        super(id, settings, 100, false);
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::onChat);
        row()
                .center(this::getTitle)
                .draw((g, w, h) -> g.fill(0, 0, w, h, TITLE_BOX_COLOR))
                .add();
        row()
                .left(() -> Component.literal("Members").withColor(SUB_TEXT_COLOR))
                .right(this::getPartyCount)
                .add();
    }

    private void updateRows() {
        resetRows(2);
        for(String member : members) {
            row()
                    .right(()-> getFormattedMember(member))
                    .add();
        }
    }

    private Component getFormattedMember(String member) {
        if(member.equals(owner)) {
            return Component.literal("✯ ").withColor(ACCENT_COLOR).append(Component.literal(member).withColor(TEXT_COLOR));
        }
        return Component.literal(member).withColor(TEXT_COLOR);
    }

    private boolean onChat(Component component, boolean isOverlay) {
        String msg = component.getString();
        if (msg.matches("ᴘᴀʀᴛʏ: You are not in a party!")) {
            this.owner = null;
            this.members.clear();
            updateRows();
            return !cogSettings.isEnabled();
        }
        if (msg.matches("-*\\s*Party Members\\s*-*")) {
            nextIsMembers = true;
            return !cogSettings.isEnabled();
        }
        if (nextIsMembers && msg.matches("\\((\\d+)/(\\d+)\\) (.+)")) {
            nextIsDivider = true;
            nextIsMembers = false;
            String[] members = msg.substring(msg.indexOf('✯')+2).split(",\\s*");
            this.members.clear();
            this.members.addAll(List.of(members));
            this.owner = members[0];
            updateRows();
            setVisible(true);
            return !cogSettings.isEnabled();
        }
        if (nextIsDivider && msg.matches("-+")) {
            nextIsDivider = false;
            return !cogSettings.isEnabled();
        }
        if(msg.startsWith("ᴘᴀʀᴛʏ:")) {
            if(msg.matches("ᴘᴀʀᴛʏ: (.*) has joined your party!")) {
                Minecraft.getInstance().player.connection.sendCommand("p list");
                return !cogSettings.isEnabled();
            }
            if(msg.matches("ᴘᴀʀᴛʏ: (.*) has left your party!")) {
                Minecraft.getInstance().player.connection.sendCommand("p list");
                return !cogSettings.isEnabled();
            }
            if(msg.matches("ᴘᴀʀᴛʏ: (.*) has been kicked from your party for being offline for .*")) {
                Minecraft.getInstance().player.connection.sendCommand("p list");
                return !cogSettings.isEnabled();
            }
            if(msg.matches("ᴘᴀʀᴛʏ: (.*) has been made a party moderator")) {
                Minecraft.getInstance().player.connection.sendCommand("p list");
                return !cogSettings.isEnabled();
            }
            if(msg.matches("ᴘᴀʀᴛʏ: (.*) has been promoted to party leader")) {
                Minecraft.getInstance().player.connection.sendCommand("p list");
                return !cogSettings.isEnabled();
            }
            if(msg.equals("ᴘᴀʀᴛʏ: You have left your party") || msg.equals("ᴘᴀʀᴛʏ: Your party has been disbanded.") ) {
                owner=null;
                members.clear();
                updateRows();
                setVisible(false);
                return true;
            }
        }
        return true;
    }

    private Component getPartyCount() {
        return Component.literal(String.valueOf(members.size())).withColor(SUB_TEXT_COLOR);
    }


    public void addMember(String member) {
        members.add(member);
        updateRows();
    }

    public void removeMember(String member) {
        members.remove(member);
        updateRows();
    }

    private Component getTitle() {
        if (owner == null) {
            return Component.literal("Party")
                    .withColor(ACCENT_COLOR)
                    .withStyle(ChatFormatting.BOLD);
        }
        return Component.literal(owner + "'s Party")
                .withColor(ACCENT_COLOR)
                .withStyle(ChatFormatting.BOLD);
    }

    private void onJoin(ClientPacketListener handler, PacketSender sender, Minecraft client) {
        if(client.player!=null) {
            DelayedActionsManager.schedule(20, () -> client.player.connection.sendCommand("p list"));
        }
    }
}
