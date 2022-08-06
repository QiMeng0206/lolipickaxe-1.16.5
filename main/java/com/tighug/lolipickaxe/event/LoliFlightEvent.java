package com.tighug.lolipickaxe.event;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tighug.lolipickaxe.Lolipickaxe;
import com.tighug.lolipickaxe.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class LoliFlightEvent implements Lolipickaxe.LoliEvent {
    private final Map<String, Map<String, Number>> flyingPlayers = Maps.newHashMap();
    private final Set<String> set = Sets.newHashSet();

    public void addFlyingPlayer(String s, Map<String, Number> m) {
        if (!set.contains(s)) {
            flyingPlayers.put(s, m);
            set.add(s);
        }
    }

    public void addFlyingPlayer(String s, float speed) {
        HashMap<String, Number> value = Maps.newHashMap();
        value.put("duration", 40);
        value.put("speed", speed);
        addFlyingPlayer(s, value);
    }

    public void addFlyingPlayer(String s){
        addFlyingPlayer(s, 0.05F);
    }

    @SubscribeEvent
    public void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayerEntity || event.phase == TickEvent.Phase.END) return;
        PlayerEntity player = event.player;
        String text = ((StringTextComponent) player.getName()).getText();
        set.remove(text);
        if (!flyingPlayers.containsKey(text)) return;
        Map<String, Number> map = flyingPlayers.get(text);
        if (map.get("duration").intValue() > 0){
            map.put("duration", map.get("duration").intValue() - 1);
            player.abilities.setFlyingSpeed(map.get("speed").floatValue());
            if (!player.abilities.mayfly){
                CompoundNBT nbt = new CompoundNBT();
                player.abilities.mayfly = true;
                nbt.putBoolean("mayfly", true);
                NetworkHandler.Pack.FLIGHT.sendToServer(nbt);
            }
        }
        else {
            flyingPlayers.remove(text);
            player.abilities.setFlyingSpeed(0.05F);
            if (!(player.isSpectator() || player.isCreative())){
                CompoundNBT nbt = new CompoundNBT();
                player.abilities.flying = false;
                player.abilities.mayfly = false;
                nbt.putBoolean("mayfly", false);
                NetworkHandler.Pack.FLIGHT.sendToServer(nbt);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerOut(PlayerEvent.PlayerLoggedOutEvent event) {
        String text = ((StringTextComponent) event.getPlayer().getName()).getText();
        flyingPlayers.remove(text);
        set.remove(text);
    }
}
