package org.zzq.iPAccountDetection.AlertService;

import org.apache.commons.logging.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Set;
import java.util.logging.Logger;

public class AlertService {
    private AlertGUIService alertGUIService;
    private Logger logger;
    public AlertService(AlertGUIService alertGUIService, Logger logger){
        this.alertGUIService = alertGUIService;
        this.logger = logger;
    }
    public void openAlertGUI(Player player, Set<String> accounts){
        AlertHolder alertHolder = new AlertHolder();
        Inventory alertInventory = Bukkit.createInventory(alertHolder, 54, AlertGUIService.AlertGUITitle);
        alertGUIService.alertGUIMap(accounts).forEach(alertInventory::setItem);
        player.openInventory(alertInventory);
    }
}
