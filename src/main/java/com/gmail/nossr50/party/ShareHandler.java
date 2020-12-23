package com.gmail.nossr50.party;

import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.config.party.ItemWeightConfig;
import com.gmail.nossr50.datatypes.party.ItemShareType;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.party.ShareMode;
import com.neetgames.mcmmo.player.OnlineMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.Misc;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ShareHandler {
    private ShareHandler() {}

    /**
     * Distribute Xp amongst party members.
     *
     * @param xp Xp without party sharing
     * @param mmoPlayer Player initiating the Xp gain
     * @param primarySkillType Skill being used
     * @return True is the xp has been shared
     */
    public static boolean handleXpShare(float xp, @NotNull OnlineMMOPlayer mmoPlayer, @NotNull Party party, @NotNull PrimarySkillType primarySkillType, @NotNull XPGainReason xpGainReason) {

        if (party.getPartyExperienceManager().getXpShareMode() != ShareMode.EQUAL) {
            return false;
        }

        List<Player> nearMembers = mcMMO.getPartyManager().getNearVisibleMembers(mmoPlayer);

        if (nearMembers.isEmpty()) {
            return false;
        }

        nearMembers.add(mmoPlayer.getPlayer());

        int partySize = nearMembers.size();
        double shareBonus = Math.min(Config.getInstance().getPartyShareBonusBase() + (partySize * Config.getInstance().getPartyShareBonusIncrease()), Config.getInstance().getPartyShareBonusCap());
        float splitXp = (float) (xp / partySize * shareBonus);

        for (Player otherMember : nearMembers) {
            OnlineMMOPlayer partyMember = mcMMO.getUserManager().queryPlayer(otherMember);

            //Profile not loaded
            if(partyMember == null) {
                continue;
            }

            partyMember.getExperienceHandler().beginUnsharedXpGain(primarySkillType, splitXp, xpGainReason, XPGainSource.PARTY_MEMBERS);
        }

        return true;
    }

    /**
     * Distribute Items amongst party members.
     *
     * @param drop Item that will get shared
     * @param mmoPlayer Player who picked up the item
     * @return True if the item has been shared
     */
    public static boolean handleItemShare(Item drop, OnlineMMOPlayer mmoPlayer) {
        ItemStack itemStack = drop.getItemStack();
        ItemShareType dropType = ItemShareType.getShareType(itemStack);

        if (dropType == null) {
            return false;
        }

        Party party = mmoPlayer.getParty();

        if (!party.sharingDrops(dropType)) {
            return false;
        }

        ShareMode shareMode = party.getItemShareMode();

        if (shareMode == ShareMode.NONE) {
            return false;
        }

        List<Player> nearMembers = mcMMO.getPartyManager().getNearMembers(mmoPlayer);

        if (nearMembers.isEmpty()) {
            return false;
        }

        Player winningPlayer = null;
        ItemStack newStack = itemStack.clone();

        nearMembers.add(mmoPlayer.getPlayer());
        int partySize = nearMembers.size();

        drop.remove();
        newStack.setAmount(1);

        switch (shareMode) {
            case EQUAL:
                int itemWeight = ItemWeightConfig.getInstance().getItemWeight(itemStack.getType());

                for (int i = 0; i < itemStack.getAmount(); i++) {
                    int highestRoll = 0;

                    for (Player member : nearMembers) {
                        OnlineMMOPlayer mcMMOMember = mcMMO.getUserManager().getPlayer(member);

                        //Profile not loaded
                        if(mcMMO.getUserManager().getPlayer(member) == null)
                        {
                            continue;
                        }

                        int itemShareModifier = mcMMOMember.getItemShareModifier();
                        int diceRoll = Misc.getRandom().nextInt(itemShareModifier);

                        if (diceRoll <= highestRoll) {
                            mcMMOMember.setItemShareModifier(itemShareModifier + itemWeight);
                            continue;
                        }

                        highestRoll = diceRoll;

                        if (winningPlayer != null) {
                            OnlineMMOPlayer mcMMOWinning = mcMMO.getUserManager().getPlayer(winningPlayer);
                            mcMMOWinning.setItemShareModifier(mcMMOWinning.getItemShareModifier() + itemWeight);
                        }

                        winningPlayer = member;
                    }

                    OnlineMMOPlayer mcMMOTarget = mcMMO.getUserManager().getPlayer(winningPlayer);
                    mcMMOTarget.setItemShareModifier(mcMMOTarget.getItemShareModifier() - itemWeight);
                    awardDrop(winningPlayer, newStack);
                }

                return true;

            case RANDOM:
                for (int i = 0; i < itemStack.getAmount(); i++) {
                    winningPlayer = nearMembers.get(Misc.getRandom().nextInt(partySize));
                    awardDrop(winningPlayer, newStack);
                }

                return true;

            default:
                return false;
        }
    }

    public static XPGainReason getSharedXpGainReason(XPGainReason xpGainReason) {
        if (xpGainReason == XPGainReason.PVE) {
            return XPGainReason.SHARED_PVE;
        }
        else if (xpGainReason == XPGainReason.PVP) {
            return XPGainReason.SHARED_PVP;
        }
        else {
            return xpGainReason;
        }
    }

    private static void awardDrop(Player winningPlayer, ItemStack drop) {
        if (winningPlayer.getInventory().addItem(drop).size() != 0) {
            winningPlayer.getWorld().dropItem(winningPlayer.getLocation(), drop);
        }

        winningPlayer.updateInventory();
    }
}
