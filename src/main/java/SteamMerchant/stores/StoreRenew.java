package SteamMerchant.stores;

import SteamMerchant.NewShopScreen;
import SteamMerchant.SteamShopRoom;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.shop.ShopScreen;

public class StoreRenew extends AbstractGoods{
    private static final float CARD_GOLD_OFFSET_X;
    private static final float CARD_GOLD_OFFSET_Y;
    private static final float CARD_PRICE_OFFSET_X;
    private static final float CARD_PRICE_OFFSET_Y;
    private static final float BOX_PIXEL_SIZE;
    private float x;
    private float y;
    private float scale;
    private final Hitbox hb;

    public StoreRenew(int slot, int serviceDiscountIndex, int basePrice) {
        super(slot, serviceDiscountIndex);
        this.setBasePrice(basePrice);
        this.scale = 0.5F * Settings.scale;
        this.hb = new Hitbox(this.scale * CARD_IMG_WIDTH, this.scale * CARD_IMG_WIDTH);
        this.gold_offset_x = CARD_GOLD_OFFSET_X;
        this.gold_offset_y = CARD_GOLD_OFFSET_Y;
        this.price_offset_x = CARD_PRICE_OFFSET_X;
        this.price_offset_y = CARD_PRICE_OFFSET_Y;
    }

    public void update(float rugY) {
        if (!this.isPurchased) {
            super.update();
        }
        this.scale = 0.5F * Settings.scale;
        this.x = NewShopScreen.SHOP_POS_X + this.slot * NewShopScreen.SHOP_PAD_X;
        this.y = rugY + NewShopScreen.BOTTOM_ROW_Y + 60.0F * Settings.yScale;
        this.hb.move(this.x, this.y);
        this.hb.update();
        if (this.hb.hovered && !this.isPurchased) {
            this.scale = 0.6F * Settings.scale;
            SteamShopRoom.shopScreen.moveHand(this.x - AbstractCard.IMG_WIDTH / 1.6F, this.y);
            if (InputHelper.justClickedLeft) {
                this.hb.clickStarted = true;
            }
            // tip
            TipHelper.renderGenericTip((float)InputHelper.mX, (float)InputHelper.mY - 70.0F * Settings.scale,
                    NewShopScreen.uiStrings.TEXT[19],
                    NewShopScreen.uiStrings.TEXT[20] + NewShopScreen.uiStrings.TEXT[17] + 25 + NewShopScreen.uiStrings.TEXT[21]);
        } else {
            this.scale = 0.5F * Settings.scale;
        }

        if (this.hb.clicked || this.hb.hovered && CInputActionSet.select.isJustPressed()) {
            this.hb.clicked = false;
            if (!Settings.isTouchScreen) {
                this.purchase();
            } else if (SteamShopRoom.shopScreen.touchCard == null) {
                if (AbstractDungeon.player.gold < this.price) {
                    SteamShopRoom.shopScreen.playCantBuySfx();
                    SteamShopRoom.shopScreen.createSpeech(ShopScreen.getCantBuyMsg());
                } else {
                    SteamShopRoom.shopScreen.confirmButton.hideInstantly();
                    SteamShopRoom.shopScreen.confirmButton.show();
                    SteamShopRoom.shopScreen.confirmButton.isDisabled = false;
                    SteamShopRoom.shopScreen.confirmButton.hb.clickStarted = false;
                    SteamShopRoom.shopScreen.touchRenew = true;
                }
            }
        }

    }

    public void purchase() {
        if (this.isPurchased) {
            return;
        }
        if (AbstractDungeon.player.gold >= this.price) {
            AbstractDungeon.player.loseGold(this.price);
            CardCrawlGame.sound.play("SHOP_PURCHASE", 0.1F);
            SteamShopRoom.shopScreen.renewStoreGoods();
            // 服务价格上涨
            this.basePrice += 25;
            this.resetBasePrice(this.basePrice);
//            this.isPurchased = true;
        } else {
            SteamShopRoom.shopScreen.speechTimer = MathUtils.random(40.0F, 60.0F);
            SteamShopRoom.shopScreen.playCantBuySfx();
            SteamShopRoom.shopScreen.createSpeech(ShopScreen.getCantBuyMsg());
        }

    }

    public void render(SpriteBatch sb) {
        if (this.isPurchased) {
            return;
        }
        float width = scale * BOX_PIXEL_SIZE;
        float height = scale * BOX_PIXEL_SIZE;
        float bot_left_x = x - width * 0.5F;
        float bot_left_y = y - height * 0.5F;
        sb.draw(NewShopScreen.renewServiceImg, bot_left_x, bot_left_y, width, height);
        this.renderPrice(sb, x, y + 40.0F * Settings.yScale, this.scale - 0.5F);
    }

    static {
        BOX_PIXEL_SIZE = 280.0F * Settings.scale;
        CARD_GOLD_OFFSET_X = -120.0F * Settings.xScale;
        CARD_GOLD_OFFSET_Y = -212.0F * Settings.yScale;
        CARD_PRICE_OFFSET_X = -57.0F * Settings.xScale;
        CARD_PRICE_OFFSET_Y = -207.0F * Settings.yScale;
    }
}
