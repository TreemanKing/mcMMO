/*
package com.gmail.nossr50.config.collectionconfigs;

import com.gmail.nossr50.config.ConfigCollection;
import com.gmail.nossr50.config.ConfigConstants;
import com.gmail.nossr50.datatypes.skills.ItemType;
import com.gmail.nossr50.datatypes.skills.ItemMaterialCategory;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.repair.repairables.Repairable;
import com.gmail.nossr50.skills.repair.repairables.RepairableFactory;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.skills.SkillUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * This config
 *//*

@ConfigSerializable
public class RepairConfig extends ConfigCollection {

    public static final String REPAIRABLES = "Repairables";
    public static final String ITEM_ID = "ItemId";
    public static final String MATERIAL_TYPE = "ItemMaterialCategory";
    public static final String REPAIR_MATERIAL = "RepairMaterial";
    public static final String MAXIMUM_DURABILITY = "MaximumDurability";
    public static final String ITEM_TYPE = "ItemType";
    public static final String METADATA = "Metadata";
    public static final String XP_MULTIPLIER = "XpMultiplier";
    public static final String MINIMUM_LEVEL = "MinimumLevel";
    public static final String MINIMUM_QUANTITY = "MinimumQuantity";

    public RepairConfig() {
        //super(McmmoCore.getDataFolderPath().getAbsoluteFile(), fileName, false);
        super("repair", mcMMO.p.getDataFolder().getAbsoluteFile(), ConfigConstants.RELATIVE_PATH_SKILLS_DIR, true, false, true, false);
        register();
    }

    */
/**
     * The version of this config
     *
     * @return
     *//*

    @Override
    public double getConfigVersion() {
        return 1;
    }

    @Override
    public void register() {
        //Grab the "keys" under the Repairables node
        ArrayList<ConfigurationNode> repairChildrenNodes = new ArrayList<>(getChildren(REPAIRABLES));

        //TODO: Remove Debug
        if(repairChildrenNodes.size() <= 0) {
            mcMMO.p.getLogger().severe("DEBUG: Repair MultiConfigContainer key list is empty");
            return;
        }

        for (ConfigurationNode repairNode : repairChildrenNodes) {
            // Validate all the things!
            List<String> errorMessages = new ArrayList<String>();

            */
/*
             * Match the name of the key to a Material constant definition
             *//*

            String repairChildNodeName = repairNode.getString();
            Material itemMaterial = Material.matchMaterial(repairChildNodeName);

            if (itemMaterial == null) {
                mcMMO.p.getLogger().severe("Repair Invalid material: " + repairChildNodeName);
                continue;
            }

            */
/*
             * Determine Repair Material Type
             *//*

            ItemMaterialCategory repairMaterialType = ItemMaterialCategory.OTHER;
            String repairMaterialTypeString = getRepairMaterialTypeString(repairChildNodeName);

            if (hasNode(REPAIRABLES, repairChildNodeName, MATERIAL_TYPE)) {
                ItemStack repairItem = new ItemStack(itemMaterial);

                if (ItemUtils.isWoodTool(repairItem)) {
                    repairMaterialType = ItemMaterialCategory.WOOD;
                }
                else if (ItemUtils.isStoneTool(repairItem)) {
                    repairMaterialType = ItemMaterialCategory.STONE;
                }
                else if (ItemUtils.isStringTool(repairItem)) {
                    repairMaterialType = ItemMaterialCategory.STRING;
                }
                else if (ItemUtils.isLeatherArmor(repairItem)) {
                    repairMaterialType = ItemMaterialCategory.LEATHER;
                }
                else if (ItemUtils.isIronArmor(repairItem) || ItemUtils.isIronTool(repairItem)) {
                    repairMaterialType = ItemMaterialCategory.IRON;
                }
                else if (ItemUtils.isGoldArmor(repairItem) || ItemUtils.isGoldTool(repairItem)) {
                    repairMaterialType = ItemMaterialCategory.GOLD;
                }
                else if (ItemUtils.isDiamondArmor(repairItem) || ItemUtils.isDiamondTool(repairItem)) {
                    repairMaterialType = ItemMaterialCategory.DIAMOND;
                }
            }
            else {
                //If a material cannot be matched, try matching the material to its repair material type string from the config
                try {
                    repairMaterialType = ItemMaterialCategory.valueOf(repairMaterialTypeString.toUpperCase());
                }
                catch (IllegalArgumentException ex) {
                    errorMessages.add("Repair Config: " + repairChildNodeName + " has an invalid " + MATERIAL_TYPE + " of " + repairMaterialTypeString);
                    continue;
                }
            }

            // Repair Material
            String repairMaterialName = getRepairMaterialStringName(repairChildNodeName);
            Material repairMaterial = (repairMaterialName == null ? repairMaterialType.getDefaultMaterial() : Material.matchMaterial(repairMaterialName));

            if (repairMaterial == null) {
                errorMessages.add(repairChildNodeName + " has an invalid repair material: " + repairMaterialName);
            }

            // Maximum Durability
            short maximumDurability = (itemMaterial != null ? itemMaterial.getMaxDurability() : getRepairableMaximumDurability(repairChildNodeName));

            if (maximumDurability <= 0) {
                maximumDurability = getRepairableMaximumDurability(repairChildNodeName);
            }

            if (maximumDurability <= 0) {
                errorMessages.add("Maximum durability of " + repairChildNodeName + " must be greater than 0!");
            }

            // Item Type
            ItemType repairItemType = ItemType.OTHER;
            String repairItemTypeString = "";

            if(hasNode(REPAIRABLES, repairChildNodeName, ITEM_TYPE))
                repairItemTypeString = getStringValue(REPAIRABLES, repairChildNodeName, ITEM_TYPE);
            else
                repairItemTypeString = "OTHER";

            if (!hasNode(REPAIRABLES, repairChildNodeName, ITEM_TYPE) && itemMaterial != null) {
                ItemStack repairItem = new ItemStack(itemMaterial);

                if (ItemUtils.isMinecraftTool(repairItem)) {
                    repairItemType = ItemType.TOOL;
                }
                else if (ItemUtils.isArmor(repairItem)) {
                    repairItemType = ItemType.ARMOR;
                }
            }
            else {
                try {
                    repairItemType = ItemType.valueOf(repairItemTypeString);
                }
                catch (IllegalArgumentException ex) {
                    errorMessages.add(repairChildNodeName + " has an invalid ItemType of " + repairItemTypeString);
                }
            }

            byte repairMetadata = -1;

            //Set the metadata byte
            if(hasNode(REPAIRABLES, repairChildNodeName, REPAIR_MATERIAL, METADATA))
                repairMetadata = (byte) getIntValue(REPAIRABLES, repairChildNodeName, REPAIR_MATERIAL, METADATA);

            int minimumLevel = getIntValue(REPAIRABLES, repairChildNodeName, MINIMUM_LEVEL);

            double xpMultiplier = 1;

            if(hasNode(REPAIRABLES, repairChildNodeName, XP_MULTIPLIER))
                xpMultiplier = getDoubleValue(REPAIRABLES, repairChildNodeName, XP_MULTIPLIER);

            // Minimum Quantity
            int minimumQuantity = SkillUtils.getRepairAndSalvageQuantities(new ItemStack(itemMaterial), repairMaterial, repairMetadata);

            if (minimumQuantity <= 0) {
                minimumQuantity = getIntValue(REPAIRABLES, repairChildNodeName, MINIMUM_QUANTITY);
            }

            */
/*
             * VALIDATE
             * Just make sure the values we may have just grabbed from the config aren't below 0
             *//*


            //Validate min level
            if(minimumLevel < 0)
                minimumLevel = 0;

            //Validate XP Mult
            if(xpMultiplier < 0)
                xpMultiplier = 0;

            //Validate Minimum Quantity
            if (minimumQuantity <= 0) {
                minimumQuantity = 2;
                errorMessages.add("Minimum quantity for "+repairChildNodeName+" in repair config should be above 0");
            }

            Repairable repairable = RepairableFactory.getRepairable(itemMaterial, repairMaterial, repairMetadata, minimumLevel, minimumQuantity, maximumDurability, repairItemType, repairMaterialType, xpMultiplier);
            genericCollection.add(repairable);

            for (String error : errorMessages) {
                //McmmoCore.getLogger().warning(issue);
                mcMMO.p.getLogger().warning(error);
            }
        }
    }

    private String getRepairMaterialTypeString(String key) {
        return getStringValue(REPAIRABLES, key, MATERIAL_TYPE);
    }

    private short getRepairableMaximumDurability(String key) {
        return getShortValue(REPAIRABLES, key, MAXIMUM_DURABILITY);
    }

    */
/**
     * Gets the Repair Material String Name defined in the config
     * @param key the key name of the repairable child node under the Repairables parent node
     * @return the Repair Material String Name defined in the config
     *//*

    private String getRepairMaterialStringName(String key) {
        return getStringValue(REPAIRABLES, key, REPAIR_MATERIAL);
    }
}
*/
