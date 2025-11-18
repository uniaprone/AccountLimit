package org.zzq.iPAccountDetection.AlertService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AlertGUIService {
    private static final int[] borderSlot = new int[]{4, 3,2,1,0,9,18,27,36,45,46,47,48,49,5,6,7,8,17,26,35,44,53,52,51,50};

    public static final Component AlertGUITitle = Component.text("警告").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);

    public Map<Integer, ItemStack> alertGUIMap(Set<String> accounts){
        Map<Integer, ItemStack> alertGUIMap = new HashMap<>();
        alertGUIMap.putAll(alertSlot());
        alertGUIMap.putAll(border());
        alertGUIMap.putAll(influenceAccounts(accounts));
        return alertGUIMap;
    }
    private Map<Integer, ItemStack> alertSlot(){
        Map<Integer, ItemStack> alertMap = new HashMap<>();
        ItemStack alert = new ItemStack(Material.PAPER);
        Component alertName = Component.text("⚠警告⚠").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
        setItemName(alert, alertName);
        Component firstLore = Component.text("您的账号数量已达上限").color(NamedTextColor.YELLOW);
        Component secondLore = Component.text("请立即退出").color(NamedTextColor.YELLOW);
        Component thirdLore = Component.text("否则您的所有账号都将遭到").color(NamedTextColor.YELLOW)
                .append(Component.text("封禁").color(NamedTextColor.RED));
        setItemLore(alert, List.of(firstLore, secondLore, thirdLore));
        alertMap.put(13, alert);
        return alertMap;
    }
    private Map<Integer, ItemStack> border(){
        Map<Integer, ItemStack> borderMap = new HashMap<>();
        ItemStack redWool = new ItemStack(Material.RED_WOOL);
        Component borderName = Component.text("⚠").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
        setItemName(redWool, borderName);
        for (int i : borderSlot){
            borderMap.put(i, redWool);
        }
        return borderMap;
    }

    private Map<Integer, ItemStack> influenceAccounts(Set<String> accounts){
        Map<Integer, ItemStack> influenceAccountsMap = new HashMap<>();
        int slot = 29;
        Iterator<String> iterator = accounts.iterator();
        while (iterator.hasNext()){
            String accountName = iterator.next();
            ItemStack redCandle = new ItemStack(Material.RED_CANDLE);
            Component account = Component.text(accountName);
            setItemName(redCandle, account);
            influenceAccountsMap.put(slot, redCandle);
            if(slot > 33 && slot <=37){
                slot = 38;
            } else if (slot > 42) {
                return  influenceAccountsMap;
            }
            slot = slot + 1;
        }
        return influenceAccountsMap;
    }
    private void setItemName(ItemStack itemStack, Component name){
        if (itemStack == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return;
        itemMeta.itemName(name);
        itemStack.setItemMeta(itemMeta);
    }

    private void setItemLore(ItemStack itemStack, List<Component> loreList){
        if (itemStack == null) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return;
        itemMeta.lore(loreList);
        itemStack.setItemMeta(itemMeta);
    }
}
