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
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.shop.ShopScreen;

public class StorePotion extends AbstractGoods{
    AbstractPotion potion;
    AbstractPotion sub_potion;
    private static final float POTION_GOLD_OFFSET_X;
    private static final float POTION_GOLD_OFFSET_Y;
    private static final float POTION_PRICE_OFFSET_X;
    private static final float POTION_PRICE_OFFSET_Y;
    private float x;
    private float y;
    private final Hitbox hb;
    private boolean hasSubPotion = false;

    public StorePotion(AbstractPotion relic, AbstractPotion sub_relic, int slot, int potionDiscountIndex) {
        super(slot, potionDiscountIndex);
        this.potion = relic;
        this.sub_potion = sub_relic;
        float tempPrice = this.potion.getPrice();
        if (this.sub_potion != null) {
            this.hasSubPotion = true;
            tempPrice += this.sub_potion.getPrice();
            // 药水组合套件打折20%
            tempPrice *= 0.8F;
        }
        if (AbstractDungeon.ascensionLevel >= 16) {
            // 高进阶改为增加15%价格
            tempPrice *= 1.15F;
        }
        this.setBasePrice(MathUtils.round(tempPrice));
        if (this.hasSubPotion) {
            // 计算打折套件的实际折扣
            this.basePrice = MathUtils.round(tempPrice * 1.25F);
            this.discount = MathUtils.round((1 - ((float) this.price / (float) this.basePrice)) * 100);
        }
        if (!this.hasSubPotion) {
            this.hb = new Hitbox(this.potion.hb.width, this.potion.hb.height);
        } else {
            this.hb = new Hitbox(2.0F * this.potion.hb.width, 2.0F * this.potion.hb.height);
        }
        this.gold_offset_x = POTION_GOLD_OFFSET_X;
        this.gold_offset_y = POTION_GOLD_OFFSET_Y;
        this.price_offset_x = POTION_PRICE_OFFSET_X;
        this.price_offset_y = POTION_PRICE_OFFSET_Y;

    }

    public StorePotion(AbstractPotion potion, int slot, int potionDiscountIndex) {
        this(potion, null, slot, potionDiscountIndex);
    }

    public void update(float rugY) {
        if (this.potion == null) {
            return;
        }
        super.update();
        if (!this.isPurchased) {
            // 中心坐标
            this.x = NewShopScreen.SHOP_POS_X + (this.slot + 4) * NewShopScreen.SHOP_PAD_X;
            this.y = rugY + NewShopScreen.BOTTOM_ROW_Y - NewShopScreen.HALF_ROW;
            this.hb.move(this.x, this.y);
            this.hb.update();
            // 药水hb
            if (!this.hasSubPotion) {
                this.potion.posX = this.x;
                this.potion.posY = this.y;
                this.potion.hb.move(this.potion.posX, this.potion.posY);
                this.potion.hb.update();
            } else {
                this.potion.posX = this.x - 0.5F * this.potion.hb.width;
                this.potion.posY = this.y;
                this.sub_potion.posX = this.x + 0.5F * this.sub_potion.hb.width;
                this.sub_potion.posY = this.y;
                this.potion.hb.move(this.potion.posX, this.potion.posY);
                this.potion.hb.update();
                this.sub_potion.hb.move(this.sub_potion.posX, this.sub_potion.posY);
                this.sub_potion.hb.update();
            }
            if (this.hb.hovered) {
                SteamShopRoom.shopScreen.moveHand(this.x - 190.0F * Settings.xScale, this.y - 70.0F * Settings.yScale);
                if (InputHelper.justClickedLeft) {
                    this.hb.clickStarted = true;
                }
                this.potion.scale = Settings.scale * 1.25F;
                if (this.hasSubPotion) {
                    this.sub_potion.scale = Settings.scale * 1.25F;
                }
            } else {
                this.potion.scale = MathHelper.scaleLerpSnap(this.potion.scale, Settings.scale);
                if (this.hasSubPotion) {
                    this.sub_potion.scale = MathHelper.scaleLerpSnap(this.sub_potion.scale, Settings.scale);;
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
                    SteamShopRoom.shopScreen.touchPotion = this;
                }
            }
        }

    }

    public void purchase() {
        if (this.potion == null || this.isPurchased) {
            return;
        }
        if (AbstractDungeon.player.hasRelic("Sozu")) {
            AbstractDungeon.player.getRelic("Sozu").flash();
            return;
        }
        if (AbstractDungeon.player.gold >= this.price) {
            if (AbstractDungeon.player.obtainPotion(this.potion)) {
                AbstractDungeon.player.loseGold(this.price);
                CardCrawlGame.sound.play("SHOP_PURCHASE", 0.1F);
                CardCrawlGame.metricData.addShopPurchaseData(this.potion.ID);
                if (this.hasSubPotion) {
                    if (AbstractDungeon.player.obtainPotion(this.sub_potion)) {
                        CardCrawlGame.metricData.addShopPurchaseData(this.sub_potion.ID);
                        AbstractDungeon.getCurrRoom().potions.add(this.sub_potion);
                    }
                }
                SteamShopRoom.shopScreen.notHoveredTimer = 1.0F;
                SteamShopRoom.shopScreen.speechTimer = MathUtils.random(40.0F, 60.0F);
                SteamShopRoom.shopScreen.playBuySfx();
                SteamShopRoom.shopScreen.createSpeech(ShopScreen.getBuyMsg());
                this.isPurchased = true;
            }
        } else {
            SteamShopRoom.shopScreen.speechTimer = MathUtils.random(40.0F, 60.0F);
            SteamShopRoom.shopScreen.playCantBuySfx();
            SteamShopRoom.shopScreen.createSpeech(ShopScreen.getCantBuyMsg());
        }
    }

    public void render(SpriteBatch sb) {
        if (this.potion == null) {
            return;
        }
        this.potion.shopRender(sb);
        if (this.hasSubPotion) {
            this.sub_potion.shopRender(sb);
        }
        this.renderPrice(sb, this.x, this.y - 50.0F * Settings.yScale, 0F);
    }

    static {
        POTION_GOLD_OFFSET_X = -120.0F * Settings.xScale;
        POTION_GOLD_OFFSET_Y = -67.0F * Settings.yScale;
        POTION_PRICE_OFFSET_X = -58.0F * Settings.xScale;
        POTION_PRICE_OFFSET_Y = -62.0F * Settings.yScale;
    }
}
