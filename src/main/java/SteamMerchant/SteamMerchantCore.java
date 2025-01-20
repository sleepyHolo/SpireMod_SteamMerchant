package SteamMerchant;

import basemod.BaseMod;
import basemod.ModPanel;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.RelicGetSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.localization.TutorialStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.Objects;

@SpireInitializer
public class SteamMerchantCore implements RelicGetSubscriber, EditStringsSubscriber, PostInitializeSubscriber {
    public static String MOD_ID = "SteamMerchant";

    public SteamMerchantCore() {
        BaseMod.subscribe(this);
//        BaseMod.addSaveField("StoreCard", new StoreCard());

    }

    public static void initialize() {
        new SteamMerchantCore();
    }

    public void receiveRelicGet(AbstractRelic relic) {
        AbstractDungeon.shopRelicPool.remove("Membership Card");
        AbstractDungeon.uncommonRelicPool.remove("The Courier");
    }

    @Override
    public void receiveEditStrings() {
        String lang = "eng";
        if (Objects.requireNonNull(Settings.language) == Settings.GameLanguage.ZHS) {
            lang = "zhs";
        }
        BaseMod.loadCustomStringsFile(UIStrings.class, "MerchantResources/localization/" + lang + "/ui.json");

    }

    @Override
    public void receivePostInitialize() {
        Texture badgeTexture = new Texture("MerchantResources/img/badge.png");
        ModPanel settingsPanel = new ModPanel();
        BaseMod.registerModBadge(badgeTexture,
                "SteamMerchant Mod", "__name__", "Referring Steam WorkShop.", settingsPanel);
    }

}
