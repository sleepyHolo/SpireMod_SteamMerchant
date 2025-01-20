package SteamMerchant.stores;

import SteamMerchant.NewShopScreen;
import SteamMerchant.SteamShopRoom;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;

public class StoreRelic extends AbstractGoods{
    AbstractRelic relic;
    AbstractRelic sub_relic;
    private static final float RELIC_GOLD_OFFSET_X;
    private static final float RELIC_GOLD_OFFSET_Y;
    private static final float RELIC_PRICE_OFFSET_X;
    private static final float RELIC_PRICE_OFFSET_Y;
    private float x;
    private float y;
    private final Hitbox hb;
    private boolean hasSubRelic = false;

    public StoreRelic(AbstractRelic relic, AbstractRelic sub_relic, int slot, int relicDiscountIndex) {
        super(slot, relicDiscountIndex);
        this.relic = relic;
        this.sub_relic = sub_relic;
        float tempPrice = this.relic.getPrice();
        if (this.sub_relic != null) {
            this.hasSubRelic = true;
            tempPrice += this.sub_relic.getPrice();
            // 遗物组合套件打折20%
            tempPrice *= 0.8F;
        }
        if (AbstractDungeon.ascensionLevel >= 16) {
            // 高进阶改为增加15%价格
            tempPrice *= 1.15F;
        }
        this.setBasePrice(MathUtils.round(tempPrice));
        if (this.hasSubRelic) {
            // 计算打折套件的实际折扣
            this.basePrice = MathUtils.round(tempPrice * 1.25F);
            this.discount = MathUtils.round((1 - ((float) this.price / (float) this.basePrice)) * 100);
        }
        if (!this.hasSubRelic) {
            this.hb = new Hitbox(this.relic.hb.width, this.relic.hb.height);
        } else {
            this.hb = new Hitbox(2.0F * this.relic.hb.width, 2.0F * this.relic.hb.height);
        }
        this.gold_offset_x = RELIC_GOLD_OFFSET_X;
        this.gold_offset_y = RELIC_GOLD_OFFSET_Y;
        this.price_offset_x = RELIC_PRICE_OFFSET_X;
        this.price_offset_y = RELIC_PRICE_OFFSET_Y;

    }

    public StoreRelic(AbstractRelic relic, int slot, int relicDiscountIndex) {
        this(relic, null, slot, relicDiscountIndex);
    }

    public void update(float rugY) {
        if (this.relic == null) {
            return;
        }
        super.update();
        if (!this.isPurchased) {
            // 中心坐标
            this.x = NewShopScreen.SHOP_POS_X + (this.slot + 4) * NewShopScreen.SHOP_PAD_X;
            this.y = rugY + NewShopScreen.BOTTOM_ROW_Y + NewShopScreen.HALF_ROW;
            this.hb.move(this.x, this.y);
            this.hb.update();
            // 遗物hb
            if (!this.hasSubRelic) {
                this.relic.currentX = this.x;
                this.relic.currentY = this.y;
                this.relic.hb.move(this.relic.currentX, this.relic.currentY);
                this.relic.hb.update();
            } else {
                this.relic.currentX = this.x - 0.5F * this.relic.hb.width;
                this.relic.currentY = this.y;
                this.sub_relic.currentX = this.x + 0.5F * this.sub_relic.hb.width;
                this.sub_relic.currentY = this.y;
                this.relic.hb.move(this.relic.currentX, this.relic.currentY);
                this.relic.hb.update();
                this.sub_relic.hb.move(this.sub_relic.currentX, this.sub_relic.currentY);
                this.sub_relic.hb.update();
            }
            if (this.hb.hovered) {
                SteamShopRoom.shopScreen.moveHand(this.x - 190.0F * Settings.xScale, this.y - 70.0F * Settings.yScale);
                if (InputHelper.justClickedLeft) {
                    this.hb.clickStarted = true;
                }
                this.relic.scale = Settings.scale * 1.25F;
                if (this.hasSubRelic) {
                    this.sub_relic.scale = Settings.scale * 1.25F;
                }
            } else {
                this.relic.scale = MathHelper.scaleLerpSnap(this.relic.scale, Settings.scale);
                if (this.hasSubRelic) {
                    this.sub_relic.scale = MathHelper.scaleLerpSnap(this.sub_relic.scale, Settings.scale);;
                }
            }

            if (this.relic.hb.hovered && InputHelper.justClickedRight) {
                CardCrawlGame.relicPopup.open(this.relic);
            }
            if (this.hasSubRelic) {
                if (this.sub_relic.hb.hovered && InputHelper.justClickedRight) {
                    CardCrawlGame.relicPopup.open(this.sub_relic);
                }
            }
        }

        if (this.hb.clicked || this.hb.hovered && CInputActionSet.select.isJustPressed()) {
            this.hb.clicked = false;
            if (!Settings.isTouchScreen) {
                this.purchase();
            } else if (SteamShopRoom.shopScreen.touchRelic == null) {
                if (AbstractDungeon.player.gold < this.price) {
                    SteamShopRoom.shopScreen.playCantBuySfx();
                    SteamShopRoom.shopScreen.createSpeech(ShopScreen.getCantBuyMsg());
                } else {
                    SteamShopRoom.shopScreen.confirmButton.hideInstantly();
                    SteamShopRoom.shopScreen.confirmButton.show();
                    SteamShopRoom.shopScreen.confirmButton.isDisabled = false;
                    SteamShopRoom.shopScreen.confirmButton.hb.clickStarted = false;
                    SteamShopRoom.shopScreen.touchRelic = this;
                }
            }
        }

    }

    public void purchase() {
        if (this.relic == null || this.isPurchased) {
            return;
        }
        if (AbstractDungeon.player.gold >= this.price) {
            AbstractDungeon.player.loseGold(this.price);
            CardCrawlGame.sound.play("SHOP_PURCHASE", 0.1F);
            CardCrawlGame.metricData.addShopPurchaseData(this.relic.relicId);
            AbstractDungeon.getCurrRoom().relics.add(this.relic);
            this.relic.instantObtain(AbstractDungeon.player, AbstractDungeon.player.relics.size(), true);
            this.relic.flash();
            if (this.hasSubRelic) {
                CardCrawlGame.metricData.addShopPurchaseData(this.sub_relic.relicId);
                AbstractDungeon.getCurrRoom().relics.add(this.sub_relic);
                this.sub_relic.instantObtain(AbstractDungeon.player, AbstractDungeon.player.relics.size(), true);
                this.sub_relic.flash();
            }
            SteamShopRoom.shopScreen.notHoveredTimer = 1.0F;
            SteamShopRoom.shopScreen.speechTimer = MathUtils.random(40.0F, 60.0F);
            SteamShopRoom.shopScreen.playBuySfx();
            SteamShopRoom.shopScreen.createSpeech(ShopScreen.getBuyMsg());
            this.isPurchased = true;
        } else {
            SteamShopRoom.shopScreen.speechTimer = MathUtils.random(40.0F, 60.0F);
            SteamShopRoom.shopScreen.playCantBuySfx();
            SteamShopRoom.shopScreen.createSpeech(ShopScreen.getCantBuyMsg());
        }
    }

    public void render(SpriteBatch sb) {
        if (this.relic == null) {
            return;
        }
        this.relic.render(sb);
        if (this.hasSubRelic) {
            this.sub_relic.render(sb);
        }
        this.renderPrice(sb, this.x, this.y - 50.0F * Settings.yScale, 0F);
    }

    static {
        RELIC_GOLD_OFFSET_X = -120.0F * Settings.xScale;
        RELIC_GOLD_OFFSET_Y = -67.0F * Settings.yScale;
        RELIC_PRICE_OFFSET_X = -58.0F * Settings.xScale;
        RELIC_PRICE_OFFSET_Y = -62.0F * Settings.yScale;
    }
}
