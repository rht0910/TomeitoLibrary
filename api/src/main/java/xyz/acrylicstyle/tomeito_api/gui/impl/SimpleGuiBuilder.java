package xyz.acrylicstyle.tomeito_api.gui.impl;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import util.ICollection;
import xyz.acrylicstyle.tomeito_api.gui.ClickableItem;
import xyz.acrylicstyle.tomeito_api.gui.GuiBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SimpleGuiBuilder implements GuiBuilder {
    @NotNull private Map<Integer, Consumer<InventoryClickEvent>> clickEvents = new HashMap<>();
    @NotNull private final Inventory inventory;
    private boolean clickable = true;

    /**
     * Constructs GuiBuilder with specified inventory.
     * After calling constructor, you must call {@link #register(Plugin)} or the events doesn't work.
     */
    public SimpleGuiBuilder(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Constructs GuiBuilder with newly created inventory with specified name and the size.
     * After calling constructor, you must call {@link #register(Plugin)} or the events doesn't work.
     */
    public SimpleGuiBuilder(@NotNull String name, int size) {
        this.inventory = Bukkit.createInventory(this, size, name);
    }

    /**
     * Constructs GuiBuilder with newly created inventory with specified size.
     * After calling constructor, you must call {@link #register(Plugin)} or the events doesn't work.
     */
    public SimpleGuiBuilder(int size) {
        this.inventory = Bukkit.createInventory(this, size);
    }

    @NotNull
    public Map<Integer, Consumer<InventoryClickEvent>> getClickEvents() {
        return ICollection.asCollection(clickEvents).clone();
    }

    @Override
    public boolean isClickable() {
        return clickable;
    }

    @Override
    public @NotNull SimpleGuiBuilder setClickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }

    public @NotNull SimpleGuiBuilder setClickEvents(@NotNull Map<Integer, Consumer<InventoryClickEvent>> clickEvents) {
        this.clickEvents = clickEvents;
        return this;
    }

    @NotNull
    public SimpleGuiBuilder setItem(int slot, @NotNull ItemStack itemStack) {
        this.inventory.setItem(slot, itemStack);
        return this;
    }

    @NotNull
    public SimpleGuiBuilder setItem(int slot, @NotNull ClickableItem clickableItem) {
        this.inventory.setItem(slot, clickableItem.getItemStack());
        clickEvents.put(slot, clickableItem.getExecutor());
        return this;
    }

    @NotNull
    public SimpleGuiBuilder register(@NotNull Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        return this;
    }

    @Override
    @NotNull
    public Inventory getInventory() { return inventory; }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() == null) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() != this) return;
        if (e.getClickedInventory() == null || e.getClickedInventory().getHolder() != this) return;
        if (!clickable) e.setCancelled(true);
        if (clickEvents.containsKey(e.getSlot())) clickEvents.get(e.getSlot()).accept(e);
    }
}
