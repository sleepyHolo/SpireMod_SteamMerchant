package SteamMerchant.stores;

import SteamMerchant.NewShopScreen;
import SteamMerchant.SteamShopRoom;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.shop.ShopScreen;

public class StorePurge extends AbstractGoods{
    private static final float CARD_GOLD_OFFSET_X;
    private static final float CARD_GOLD_OFFSET_Y;
    private static final float CARD_PRICE_OFFSET_X;
    private static final float CARD_PRICE_OFFSET_Y;
    private float x;
    private float y;
    private float scale;
    private final Hitbox hb;

    public StorePurge(int slot, int serviceDiscountIndex, int basePrice) {
        super(slot, serviceDiscountIndex);
        this.setBasePrice(basePrice);
        this.scale = 0.5F * Settings.scale;
        this.hb = new Hitbox(this.scale * CARD_IMG_WIDTH, this.scale * CARD_IMG_HEIGHT);
        this.gold_offset_x = CARD_GOLD_OFFSET_X;
        this.gold_offset_y = CARD_GOLD_OFFSET_Y;
        this.price_offset_x = CARD_PRICE_OFFSET_X;
        this.price_offset_y = CARD_PRICE_OFFSET_Y;
    }

    public void update(float rugY) {
        if (!this.isPurchased) {
            super.update();
        }
        this.scale = 0.65F * Settings.scale;
        this.x = NewShopScreen.SHOP_POS_X + this.slot * NewShopScreen.SHOP_PAD_X;
        this.y = rugY + NewShopScreen.BOTTOM_ROW_Y;
        this.hb.move(this.x, this.y);
        this.hb.update();
        if (this.hb.hovered && !this.isPurchased) {
            this.scale = 0.84F * Settings.scale;
            SteamShopRoom.shopScreen.moveHand(this.x - AbstractCard.IMG_WIDTH / 1.6F, this.y);
            if (InputHelper.justClickedLeft) {
                this.hb.clickStarted = true;
            }
            // tip
            TipHelper.renderGenericTip((float)InputHelper.mX, (float)InputHelper.mY - 70.0F * Settings.scale,
                    NewShopScreen.uiStrings.TEXT[10],
                    NewShopScreen.uiStrings.TEXT[13] + NewShopScreen.uiStrings.TEXT[16] + 15 + NewShopScreen.uiStrings.TEXT[18]);
        } else {
            this.scale = 0.65F * Settings.scale;
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
                    SteamShopRoom.shopScreen.touchPurge = true;
                }
            }
        }

    }

    public void purchase() {
        if (this.isPurchased) {
            return;
        }
        if (AbstractDungeon.player.gold >= this.price) {
            AbstractDungeon.previousScreen = AbstractDungeon.CurrentScreen.SHOP;
            AbstractDungeon.gridSelectScreen.open(CardGroup.getGroupWithoutBottledCards(
                    AbstractDungeon.player.masterDeck.getPurgeableCards()), 1,
                    NewShopScreen.uiStrings.TEXT[7], false, false, true, true);
            if (!AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
                AbstractDungeon.player.loseGold(this.price);
                AbstractDungeon.player.masterDeck.removeCard(AbstractDungeon.gridSelectScreen.selectedCards.get(0));
                // 服务价格上涨
                SteamShopRoom.shopScreen.serviceCostUp(15);
                this.isPurchased = true;
            }
        } else {
            SteamShopRoom.shopScreen.speechTimer = MathUtils.random(40.0F, 60.0F);
            SteamShopRoom.shopScreen.playCantBuySfx();
            SteamShopRoom.shopScreen.createSpeech(ShopScreen.getCantBuyMsg());
        }

    }

    public void render(SpriteBatch sb) {
        float width = scale * CARD_PIXEL_X;
        float height = scale * CARD_PIXEL_Y;
        float bot_left_x = x - width * 0.5F;
        float bot_left_y = y - height * 0.5F;
        if (this.isPurchased) {
            sb.draw(NewShopScreen.soldOutImg, bot_left_x, bot_left_y, width, height);
            return;
        }
        sb.draw(NewShopScreen.purgeServiceImg, bot_left_x, bot_left_y, width, height);
        this.renderPrice(sb, x, y, this.scale - 0.65F);
    }

    static {
        CARD_GOLD_OFFSET_X = -120.0F * Settings.xScale;
        CARD_GOLD_OFFSET_Y = -212.0F * Settings.yScale;
        CARD_PRICE_OFFSET_X = -57.0F * Settings.xScale;
        CARD_PRICE_OFFSET_Y = -207.0F * Settings.yScale;
    }
}
