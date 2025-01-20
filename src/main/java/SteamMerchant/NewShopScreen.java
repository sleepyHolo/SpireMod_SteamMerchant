package SteamMerchant;

import SteamMerchant.stores.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.localization.TutorialStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.Merchant;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.ui.buttons.ConfirmButton;
import com.megacrit.cardcrawl.ui.DialogWord.AppearEffect;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.vfx.FloatyEffect;
import com.megacrit.cardcrawl.vfx.ShopSpeechBubble;
import com.megacrit.cardcrawl.vfx.SpeechTextEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

public class NewShopScreen {
    private static final Logger logger = LogManager.getLogger(NewShopScreen.class.getName());
    private static final TutorialStrings tutorialStrings;
    private static final CharacterStrings characterStrings;
    public static final UIStrings uiStrings;
    private static Texture rugImg;
    public  static Texture purgeServiceImg;
    public  static Texture upgradeServiceImg;
    public  static Texture renewServiceImg;
    public  static Texture cardRewardServiceImg;
    public  static Texture soldOutImg;
    private static Texture handImg;
    private static Texture clockImg;
    public  static Texture salePriceImg;
    private float rugY;
    public float speechTimer;
    private static final float TITLE_X;
    private static final float TITLE_Y;
    private static final float CLOCK_WIDTH;
    private static final float CLOCK_HEIGHT;
    private static final float SPEECH_TEXT_R_X;
    private static final float SPEECH_TEXT_L_X;
    private static final float SPEECH_TEXT_Y;
    public static float SHOP_POS_X;
    public static float SHOP_PAD_X;
    public static float SHOP_PAD_Y;
    public static float TOP_ROW_Y;
    public static float BOTTOM_ROW_Y;
    public static float HALF_ROW;
    private ShopSpeechBubble speechBubble;
    private SpeechTextEffect dialogTextEffect;
    private static final String WELCOME_MSG;
    private ArrayList<String> idleMessages;
    private boolean saidWelcome;
    private boolean somethingHovered;
    public ArrayList<StoreCard> cards;
    public StoreCard touchCard;
    private static final float CARD_PRICE_JITTER = 0.1F;
    private ArrayList<StoreRelic> relics;
    public StoreRelic touchRelic;
    private static final float RELIC_PRICE_JITTER = 0.05F;
    private ArrayList<StorePotion> potions;
    public StorePotion touchPotion;
    private static final float POTION_PRICE_JITTER = 0.05F;
    public boolean purgeAvailable;
    public StorePurge purge = null;
    public boolean touchPurge;
    public StoreUpgrade upgrade = null;
    public boolean touchUpgrade;
    public StoreRenew renew = null;
    public boolean touchRenew;
    public StoreCardReward reward = null;
    public boolean touchReward;
    private static final int PURGE_COST_RAMP = 25;
    private final FloatyEffect f_effect;
    private float handTimer;
    private float handX;
    private float handY;
    private float handTargetX;
    private float handTargetY;
    private static final float HAND_SPEED = 6.0F;
    private static float HAND_W;
    private static float HAND_H;
    public float notHoveredTimer;
    public ConfirmButton confirmButton;
    private int onSaleType;
    private float nextTime;


    public NewShopScreen() {
        this.rugY = (float) Settings.HEIGHT / 2.0F + 540.0F * Settings.yScale;
        this.speechTimer = 0.0F;
        this.speechBubble = null;
        this.dialogTextEffect = null;
        this.idleMessages = new ArrayList<>();
        this.saidWelcome = false;
        this.somethingHovered = false;
        this.cards = new ArrayList<>();
        this.relics = new ArrayList<>();
        this.potions = new ArrayList<>();
        this.purgeAvailable = false;
        this.f_effect = new FloatyEffect(20.0F, 0.1F);
        this.handTimer = 1.0F;
        this.handX = (float)Settings.WIDTH / 2.0F;
        this.handY = (float)Settings.HEIGHT;
        this.handTargetX = 0.0F;
        this.handTargetY = (float)Settings.HEIGHT;
        this.notHoveredTimer = 0.0F;
        this.confirmButton = new ConfirmButton();
        // touch
        this.touchRelic = null;
        this.touchPotion = null;
        this.touchCard = null;
        this.touchPurge = false;
        this.touchUpgrade = false;
        this.touchRenew = false;
        this.touchReward = false;
        // sale
        this.onSaleType = 0;
        this.nextTime = 0.0F;
    }

    public void init() {
        this.idleMessages.clear();
        if (AbstractDungeon.id.equals("TheEnding")) {
            Collections.addAll(this.idleMessages, Merchant.ENDING_TEXT);
        } else {
            Collections.addAll(this.idleMessages, characterStrings.TEXT);
        }
        if (rugImg == null) {
            setImages();
        }
        HAND_W = (float)handImg.getWidth() * Settings.scale;
        HAND_H = (float)handImg.getHeight() * Settings.scale;
        // 其他设置
        this.purgeAvailable = true;
        // 游戏保存的应该是ShopScreen的相关量,直接拿来作为自己的服务总基础价格就好
        ShopScreen.actualPurgeCost = ShopScreen.purgeCost;
        if (AbstractDungeon.player.hasRelic("Smiling Mask")) {
            ShopScreen.actualPurgeCost = 50;   // 移除服务永久50
        }
        // 初始化商品
        this.setStoreGoods();

    }

    public void open() {
        this.resetTouchscreenVars();
        CardCrawlGame.sound.play("SHOP_OPEN");
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.SHOP;
        AbstractDungeon.overlayMenu.proceedButton.hide();
        AbstractDungeon.overlayMenu.cancelButton.show(characterStrings.NAMES[12]);
        // hide relics & potions
//        for (StoreRelic r: this.relics) {
//            r.hide();
//        }
//        for (StorePotion p: this.potions) {
//            p.hide();
//        }

        this.rugY = (float)Settings.HEIGHT;
        this.handX = (float)Settings.WIDTH / 2.0F;
        this.handY = (float)Settings.HEIGHT;
        this.handTargetX = this.handX;
        this.handTargetY = this.handY;
        this.handTimer = 1.0F;
        this.speechTimer = 1.5F;
        this.speechBubble = null;
        this.dialogTextEffect = null;
        AbstractDungeon.overlayMenu.showBlackScreen();
        for (StoreCard c: this.cards) {
            UnlockTracker.markCardAsSeen(c.cardID);
        }

        if (ModHelper.isModEnabled("Hoarder")) {
            this.purgeAvailable = false;
        }

    }

    private void setImages() {
        // 地毯等图片
        String lang = "eng";
        if (Objects.requireNonNull(Settings.language) == Settings.GameLanguage.ZHS) {
            lang = "zhs";
        }
        rugImg = ImageMaster.loadImage("MerchantResources/img/rug/" + lang + ".png");
        purgeServiceImg = ImageMaster.loadImage("MerchantResources/img/purge/" + lang + ".png");
        upgradeServiceImg = ImageMaster.loadImage("MerchantResources/img/upgrade/" + lang + ".png");
        cardRewardServiceImg = ImageMaster.loadImage("MerchantResources/img/reward/" + lang + ".png");
        soldOutImg = ImageMaster.loadImage("MerchantResources/img/sold_out/" + lang + ".png");
        renewServiceImg = ImageMaster.loadImage("MerchantResources/img/merchant/box.png");
        handImg = ImageMaster.loadImage("MerchantResources/img/merchant/merchantHand.png");
        clockImg = ImageMaster.loadImage("MerchantResources/img/merchant/clock.png");
        salePriceImg = ImageMaster.loadImage("MerchantResources/img/merchant/sale_price.png");

    }

    private void setStoreGoods() {
        this.relics.clear();
        this.relics = new ArrayList<>();
        // cards & potions
        this.renewStoreGoods();
        int serviceDiscountIndex = AbstractDungeon.merchantRng.random(0, 3);
        // 刷新商店
        this.renew = new StoreRenew(0, serviceDiscountIndex, 50);
        // 移除卡牌服务
        this.purge = new StorePurge(1, serviceDiscountIndex, ShopScreen.actualPurgeCost);
        // 升级卡牌服务
        this.upgrade = new StoreUpgrade(2, serviceDiscountIndex, ShopScreen.actualPurgeCost);
        // 卡牌掉落
        this.reward = new StoreCardReward(3, serviceDiscountIndex, MathUtils.round(
                65.0F * AbstractDungeon.merchantRng.random(0.9F, 1.1F)));
        // relics
        int relicDiscountIndex = AbstractDungeon.merchantRng.random(0, 4);
        for(int i = 0; i < 3; ++i) {
            AbstractRelic tempRelic = null;
            if (i != 1) {
                tempRelic = AbstractDungeon.returnRandomRelicEnd(ShopScreen.rollRelicTier());
            } else {
                tempRelic = AbstractDungeon.returnRandomRelicEnd(AbstractRelic.RelicTier.SHOP);
            }
            StoreRelic relic = null;
            if (i != 2) {
                relic = new StoreRelic(tempRelic, i, relicDiscountIndex);
            } else {
                relic = new StoreRelic(tempRelic, AbstractDungeon.returnRandomRelicEnd(ShopScreen.rollRelicTier()),
                        i, relicDiscountIndex);
            }
            if (!Settings.isDailyRun) {
                // 重设基础价格
                relic.resetBasePrice(MathUtils.round((float)relic.basePrice * AbstractDungeon.merchantRng.random(0.95F, 1.05F)));
            }
            this.relics.add(relic);
        }

    }

    public void renewStoreGoods() {
        this.cards.clear();
        this.cards = new ArrayList<>();
        this.potions.clear();
        this.potions = new ArrayList<>();
        // 商店第一行
        // cards
        int cardDiscountIndex = AbstractDungeon.merchantRng.random(0, 6);
        AbstractCard c;
        for (c = AbstractDungeon.getCardFromPool(AbstractDungeon.rollRarity(), AbstractCard.CardType.ATTACK, true).makeCopy();
             c.color == AbstractCard.CardColor.COLORLESS;
             c = AbstractDungeon.getCardFromPool(AbstractDungeon.rollRarity(), AbstractCard.CardType.ATTACK, true).makeCopy()) {
        }
        this.cards.add(new StoreCard(c, 0, cardDiscountIndex));
        for (c = AbstractDungeon.getCardFromPool(AbstractDungeon.rollRarity(), AbstractCard.CardType.ATTACK, true).makeCopy();
             Objects.equals(c.cardID, this.cards.get(this.cards.size() - 1).cardID) || c.color == AbstractCard.CardColor.COLORLESS;
             c = AbstractDungeon.getCardFromPool(AbstractDungeon.rollRarity(), AbstractCard.CardType.ATTACK, true).makeCopy()) {
        }
        this.cards.add(new StoreCard(c, 1, cardDiscountIndex));
        for (c = AbstractDungeon.getCardFromPool(AbstractDungeon.rollRarity(), AbstractCard.CardType.SKILL, true).makeCopy();
             c.color == AbstractCard.CardColor.COLORLESS;
             c = AbstractDungeon.getCardFromPool(AbstractDungeon.rollRarity(), AbstractCard.CardType.SKILL, true).makeCopy()) {
        }
        this.cards.add(new StoreCard(c, 2, cardDiscountIndex));
        for (c = AbstractDungeon.getCardFromPool(AbstractDungeon.rollRarity(), AbstractCard.CardType.SKILL, true).makeCopy();
             Objects.equals(c.cardID, (this.cards.get(this.cards.size() - 1)).cardID) || c.color == AbstractCard.CardColor.COLORLESS;
             c = AbstractDungeon.getCardFromPool(AbstractDungeon.rollRarity(), AbstractCard.CardType.SKILL, true).makeCopy()) {
        }
        this.cards.add(new StoreCard(c, 3, cardDiscountIndex));
        for (c = AbstractDungeon.getCardFromPool(AbstractDungeon.rollRarity(), AbstractCard.CardType.POWER, true).makeCopy();
             c.color == AbstractCard.CardColor.COLORLESS;
             c = AbstractDungeon.getCardFromPool(AbstractDungeon.rollRarity(), AbstractCard.CardType.POWER, true).makeCopy()) {
        }
        this.cards.add(new StoreCard(c, 4, cardDiscountIndex));
        // 无色卡
        this.cards.add(new StoreCard(AbstractDungeon.getColorlessCardFromPool(AbstractCard.CardRarity.UNCOMMON).makeCopy(),
                5, cardDiscountIndex));
        this.cards.add(new StoreCard(AbstractDungeon.getColorlessCardFromPool(AbstractCard.CardRarity.RARE).makeCopy(),
                6, cardDiscountIndex));

        // 商店第二行
        // potions
        int potionDiscountIndex = AbstractDungeon.merchantRng.random(0, 4);
        for(int i = 0; i < 3; ++i) {
            StorePotion potion = null;
            if (i != 2) {
                potion = new StorePotion(AbstractDungeon.returnRandomPotion(), i, potionDiscountIndex);
            } else {
                potion = new StorePotion(AbstractDungeon.returnRandomPotion(),
                        AbstractDungeon.returnRandomPotion(), i, potionDiscountIndex);
            }
            if (!Settings.isDailyRun) {
                // 重设基础价格
                potion.resetBasePrice(MathUtils.round((float)potion.basePrice * AbstractDungeon.merchantRng.random(0.95F, 1.05F)));
            }
            this.potions.add(potion);
        }
    }

    public void serviceCostUp(int amount) {
        ShopScreen.purgeCost += amount;
        ShopScreen.actualPurgeCost = ShopScreen.purgeCost;
        if (AbstractDungeon.player.hasRelic("Smiling Mask")) {
            ShopScreen.actualPurgeCost = 50;
        }
        // reset all service cost
        this.purge.resetBasePrice(ShopScreen.actualPurgeCost);
        this.upgrade.resetBasePrice(ShopScreen.actualPurgeCost);
    }

    public boolean getSale() {
        return (this.onSaleType > 0);

    }


    public int calculateSale() {
        // 获取当前是否处于特卖时间
        // 0: 无特卖, 1-4分别为春促到冬促
        float time = CardCrawlGame.playtime;
        while (time >= 2880.0F) {
            time -= 2880.0F;
        }
        if (time < 15.78F) {
            if (this.nextTime <= 0.0F) {
                this.nextTime = 15.78F - time;
            }
            return 4;
        }
        if (time < 583.89F) {
            if (this.nextTime <= 0.0F) {
                this.nextTime = 583.89F - time;
            }
            return 0;
        }
        if (time < 647.01F) {
            if (this.nextTime <= 0.0F) {
                this.nextTime = 647.01F - time;
            }
            return 1;
        }
        if (time < 1404.49F) {
            if (this.nextTime <= 0.0F) {
                this.nextTime = 1404.49F - time;
            }
            return 0;
        }
        if (time < 1522.85F) {
            if (this.nextTime <= 0.0F) {
                this.nextTime = 1522.85F - time;
            }
            return 2;
        }
        if (time < 2611.73F) {
            if (this.nextTime <= 0.0F) {
                this.nextTime = 2611.73F - time;
            }
            return 0;
        }
        if (time < 2674.85F) {
            if (this.nextTime <= 0.0F) {
                this.nextTime = 2674.85F - time;
            }
            return 3;
        }
        if (time < 2706.41F) {
            if (this.nextTime <= 0.0F) {
                this.nextTime = 2706.41F - time;
            }
            return 0;
        }
        if (this.nextTime <= 0.0F) {
            this.nextTime = 2895.78F - time;
        }
        return 4;

    }

    public void moveHand(float x, float y) {
        this.handTargetX = x - 50.0F * Settings.xScale;
        this.handTargetY = y + 90.0F * Settings.yScale;

    }

    public void createSpeech(String msg) {
        boolean isRight = MathUtils.randomBoolean();
        float x = MathUtils.random(660.0F, 1260.0F) * Settings.scale;
        float y = (float)Settings.HEIGHT - 380.0F * Settings.scale;
        this.speechBubble = new ShopSpeechBubble(x, y, 4.0F, msg, isRight);
        float offset_x = 0.0F;
        if (isRight) {
            offset_x = SPEECH_TEXT_R_X;
        } else {
            offset_x = SPEECH_TEXT_L_X;
        }
        this.dialogTextEffect = new SpeechTextEffect(x + offset_x, y + SPEECH_TEXT_Y, 4.0F, msg, AppearEffect.BUMP_IN);

    }

    public void update() {
        if (Settings.isTouchScreen) {
            this.confirmButton.update();
            if (InputHelper.justClickedLeft && this.confirmButton.hb.hovered) {
                this.confirmButton.hb.clickStarted = true;
            }

            if (InputHelper.justReleasedClickLeft && !this.confirmButton.hb.hovered) {
                this.resetTouchscreenVars();
            } else if (this.confirmButton.hb.clicked) {
                this.confirmButton.hb.clicked = false;
                if (this.touchCard != null) {
                    this.touchCard.purchase();
                } else if (this.touchRelic != null) {
                    this.touchRelic.purchase();
                } else if (this.touchPotion != null) {
                    this.touchPotion.purchase();
                } else if (this.touchPurge) {
                    this.purge.purchase();
                } else if (this.touchUpgrade) {
                    this.upgrade.purchase();
                } else if (this.touchReward) {
                    this.reward.purchase();
                } else if (this.touchRenew) {
                    this.renew.purchase();
                }
                this.resetTouchscreenVars();
            }
        }

        if (this.handTimer != 0.0F) {
            this.handTimer -= Gdx.graphics.getDeltaTime();
            if (this.handTimer < 0.0F) {
                this.handTimer = 0.0F;
            }
        }

        // 更新时间,不必要检查是否小于零(由calculateSale完成)
        this.nextTime -= Gdx.graphics.getDeltaTime();
        // update
        this.onSaleType = this.calculateSale();
        this.f_effect.update();
        this.somethingHovered = false;
        this.updateRug();
        this.updateGoods();
        this.updateSpeech();
        this.updateHand();

        // 判断未操作时间
        if (!this.somethingHovered) {
            this.notHoveredTimer += Gdx.graphics.getDeltaTime();
            if (this.notHoveredTimer > 1.0F) {
                this.handTargetY = (float)Settings.HEIGHT;
            }
        } else {
            this.notHoveredTimer = 0.0F;
        }

    }

    private void updateSpeech() {
        if (this.speechBubble != null) {
            this.speechBubble.update();
            if (this.speechBubble.hb.hovered && this.speechBubble.duration > 0.3F) {
                this.speechBubble.duration = 0.3F;
                this.dialogTextEffect.duration = 0.3F;
            }

            if (this.speechBubble.isDone) {
                this.speechBubble = null;
            }
        }

        if (this.dialogTextEffect != null) {
            this.dialogTextEffect.update();
            if (this.dialogTextEffect.isDone) {
                this.dialogTextEffect = null;
            }
        }

        this.speechTimer -= Gdx.graphics.getDeltaTime();
        if (this.speechBubble == null && this.dialogTextEffect == null && this.speechTimer <= 0.0F) {
            this.speechTimer = MathUtils.random(40.0F, 60.0F);
            if (!this.saidWelcome) {
                this.createSpeech(WELCOME_MSG);
                this.saidWelcome = true;
                this.welcomeSfx();
            } else {
                this.playMiscSfx();
                this.createSpeech(this.getIdleMsg());
            }
        }

    }

    private void resetTouchscreenVars() {
        if (Settings.isTouchScreen) {
            this.confirmButton.hide();
            this.confirmButton.isDisabled = false;
            this.touchRelic = null;
            this.touchCard = null;
            this.touchPotion = null;
            this.touchPurge = false;
            this.touchUpgrade = false;
            this.touchRenew = false;
            this.touchReward = false;
        }

    }

    private void welcomeSfx() {
        int roll = MathUtils.random(2);
        if (roll == 0) {
            CardCrawlGame.sound.play("VO_MERCHANT_3A");
        } else if (roll == 1) {
            CardCrawlGame.sound.play("VO_MERCHANT_3B");
        } else {
            CardCrawlGame.sound.play("VO_MERCHANT_3C");
        }

    }

    private void playMiscSfx() {
        int roll = MathUtils.random(5);
        if (roll == 0) {
            CardCrawlGame.sound.play("VO_MERCHANT_MA");
        } else if (roll == 1) {
            CardCrawlGame.sound.play("VO_MERCHANT_MB");
        } else if (roll == 2) {
            CardCrawlGame.sound.play("VO_MERCHANT_MC");
        } else if (roll == 3) {
            CardCrawlGame.sound.play("VO_MERCHANT_3A");
        } else if (roll == 4) {
            CardCrawlGame.sound.play("VO_MERCHANT_3B");
        } else {
            CardCrawlGame.sound.play("VO_MERCHANT_3C");
        }

    }

    public void playBuySfx() {
        int roll = MathUtils.random(2);
        if (roll == 0) {
            CardCrawlGame.sound.play("VO_MERCHANT_KA");
        } else if (roll == 1) {
            CardCrawlGame.sound.play("VO_MERCHANT_KB");
        } else {
            CardCrawlGame.sound.play("VO_MERCHANT_KC");
        }

    }

    public void playCantBuySfx() {
        int roll = MathUtils.random(2);
        if (roll == 0) {
            CardCrawlGame.sound.play("VO_MERCHANT_2A");
        } else if (roll == 1) {
            CardCrawlGame.sound.play("VO_MERCHANT_2B");
        } else {
            CardCrawlGame.sound.play("VO_MERCHANT_2C");
        }

    }

    private String getIdleMsg() {
        return (String)this.idleMessages.get(MathUtils.random(this.idleMessages.size() - 1));
    }

    private void updateRug() {
        if (this.rugY != 0.0F) {
            this.rugY = MathUtils.lerp(this.rugY, (float)Settings.HEIGHT / 2.0F - 540.0F * Settings.yScale,
                    Gdx.graphics.getDeltaTime() * 5.0F);
            if (Math.abs(this.rugY - 0.0F) < 0.5F) {
                this.rugY = 0.0F;
            }
        }

    }

    private void updateHand() {
        if (this.handTimer == 0.0F) {
            if (this.handX != this.handTargetX) {
                this.handX = MathUtils.lerp(this.handX, this.handTargetX, Gdx.graphics.getDeltaTime() * 6.0F);
            }

            if (this.handY != this.handTargetY) {
                if (this.handY > this.handTargetY) {
                    this.handY = MathUtils.lerp(this.handY, this.handTargetY, Gdx.graphics.getDeltaTime() * 6.0F);
                } else {
                    this.handY = MathUtils.lerp(this.handY, this.handTargetY, Gdx.graphics.getDeltaTime() * 6.0F / 4.0F);
                }
            }
        }

    }

    private void updateGoods() {
        // cards
        Iterator<StoreCard> ic = this.cards.iterator();
        while(ic.hasNext()) {
            StoreCard c = ic.next();
            if (Settings.isFourByThree) {
                c.update(this.rugY + 50.0F * Settings.scale);
            } else {
                c.update(this.rugY);
            }
            if (c.isPurchased) {
                ic.remove();
            }
        }
        // relics
        Iterator<StoreRelic> ir = this.relics.iterator();
        while(ir.hasNext()) {
            StoreRelic r = ir.next();
            if (Settings.isFourByThree) {
                r.update(this.rugY + 50.0F * Settings.scale);
            } else {
                r.update(this.rugY);
            }
            if (r.isPurchased) {
                ir.remove();
            }
        }
        // potions
        Iterator<StorePotion> ip = this.potions.iterator();
        while(ip.hasNext()) {
            StorePotion p = ip.next();
            if (Settings.isFourByThree) {
                p.update(this.rugY + 50.0F * Settings.scale);
            } else {
                p.update(this.rugY);
            }
            if (p.isPurchased) {
                ip.remove();
            }
        }
        // renew
        if (Settings.isFourByThree) {
            this.renew.update(this.rugY + 50.0F * Settings.scale);
        } else {
            this.renew.update(this.rugY);
        }
        // purge
        if (Settings.isFourByThree) {
            this.purge.update(this.rugY + 50.0F * Settings.scale);
        } else {
            this.purge.update(this.rugY);
        }
        // upgrade
        if (Settings.isFourByThree) {
            this.upgrade.update(this.rugY + 50.0F * Settings.scale);
        } else {
            this.upgrade.update(this.rugY);
        }
        // reward
        if (Settings.isFourByThree) {
            this.reward.update(this.rugY + 50.0F * Settings.scale);
        } else {
            this.reward.update(this.rugY);
        }

    }

    public void render(@NotNull SpriteBatch sb) {
        sb.setColor(Color.WHITE);
        sb.draw(rugImg, 0.0F, this.rugY, (float)Settings.WIDTH, (float)Settings.HEIGHT);
        this.renderTitle(sb);
        this.renderGoods(sb);
        sb.draw(handImg, this.handX + this.f_effect.x, this.handY + this.f_effect.y, HAND_W, HAND_H);
        if (this.speechBubble != null) {
            this.speechBubble.render(sb);
        }

        if (this.dialogTextEffect != null) {
            this.dialogTextEffect.render(sb);
        }

        if (Settings.isTouchScreen) {
            this.confirmButton.render(sb);
        }

    }

    private void renderGoods(SpriteBatch sb) {
        for (StoreCard c: this.cards) {
            c.render(sb);
        }
        for (StoreRelic r: this.relics) {
            r.render(sb);
        }
        for (StorePotion p: this.potions) {
            p.render(sb);
        }
        this.renew.render(sb);
        this.purge.render(sb);
        this.upgrade.render(sb);
        this.reward.render(sb);
    }

    private void renderTitle(SpriteBatch sb) {
        sb.draw(clockImg, TITLE_X, TITLE_Y - 0.9F * CLOCK_HEIGHT + this.rugY, CLOCK_WIDTH, CLOCK_HEIGHT);
        // time
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.buttonLabelFont,
                String.format(" %02d:%02d", (int) this.nextTime / 60, (int) this.nextTime % 60),
                TITLE_X + 0.1F * CLOCK_WIDTH, TITLE_Y - 0.2F * CLOCK_HEIGHT + this.rugY, Color.WHITE);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipHeaderFont,
                this.onSaleType == 0 ? uiStrings.TEXT[5] : uiStrings.TEXT[6],
                TITLE_X + 0.2F * CLOCK_WIDTH, TITLE_Y - 0.5F * CLOCK_HEIGHT + this.rugY, Color.WHITE);
        // title
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.dungeonTitleFont, uiStrings.TEXT[this.onSaleType],
                TITLE_X + 1.6F * CLOCK_WIDTH, TITLE_Y + this.rugY, Color.valueOf("#88b6eb"));
    }

    static {
        tutorialStrings = CardCrawlGame.languagePack.getTutorialString("Shop Tip");
        characterStrings = CardCrawlGame.languagePack.getCharacterString("Shop Screen");
        uiStrings = CardCrawlGame.languagePack.getUIString("Shop Misc");
        rugImg = null;
        purgeServiceImg = null;
        soldOutImg = null;
        handImg = null;
        SPEECH_TEXT_R_X = 164.0F * Settings.scale;
        SPEECH_TEXT_L_X = -166.0F * Settings.scale;
        SPEECH_TEXT_Y = 126.0F * Settings.scale;
        TITLE_X = 200.0F * Settings.scale;
        TITLE_Y = 1000.0F * Settings.scale;
        CLOCK_WIDTH = 200.0F * Settings.scale;
        CLOCK_HEIGHT = 140.0F * Settings.scale;
        SHOP_POS_X = 260.0F * Settings.xScale;
        SHOP_PAD_X = 235.0F * Settings.xScale;
        SHOP_PAD_Y = 320.0F * Settings.yScale;
        TOP_ROW_Y = 700.0F * Settings.yScale;
        BOTTOM_ROW_Y = 300.0F * Settings.yScale;
        HALF_ROW = 100.0F * Settings.scale;
        WELCOME_MSG = characterStrings.NAMES[0];
    }

    private static enum StoreSelectionType {
        RELIC,
        COLOR_CARD,
        COLORLESS_CARD,
        POTION,
        PURGE;

        private StoreSelectionType() {
        }
    }
}
