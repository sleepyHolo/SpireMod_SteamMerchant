package SteamMerchant.stores;

import SteamMerchant.NewShopScreen;
import SteamMerchant.SteamShopRoom;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;

public class StoreCard extends AbstractGoods{
    public AbstractCard card;
    public String cardID;
    private static final float CARD_GOLD_OFFSET_X;
    private static final float CARD_GOLD_OFFSET_Y;
    private static final float CARD_PRICE_OFFSET_X;
    private static final float CARD_PRICE_OFFSET_Y;
    public boolean isPurchased = false;
//    private boolean renderTip = false;
//    private float hoverDuration = 0.0F;

    public StoreCard(AbstractCard card, int slot, int cardDiscountIndex) {
        super(slot, cardDiscountIndex);
        this.card = card;
        this.cardID = this.card.cardID;
        float tempPrice = AbstractCard.getPrice(card.rarity) * AbstractDungeon.merchantRng.random(0.9F, 1.1F);
        if (card.color == AbstractCard.CardColor.COLORLESS) {
            // 白卡价格增加25%
            tempPrice *= 1.25F;
        }
        if (AbstractDungeon.ascensionLevel >= 16) {
            // 高进阶改为增加20%价格
            tempPrice *= 1.2F;
        }
        this.setBasePrice(MathUtils.round(tempPrice));
        this.gold_offset_x = CARD_GOLD_OFFSET_X;
        this.gold_offset_y = CARD_GOLD_OFFSET_Y;
        this.price_offset_x = CARD_PRICE_OFFSET_X;
        this.price_offset_y = CARD_PRICE_OFFSET_Y;

    }

    public void update(float rugY) {
        if (this.card == null) {
            return;
        }
        super.update();
        if (!this.isPurchased) {
            this.card.update();
            this.card.targetDrawScale = 0.65F * Settings.scale;
            // 重设碰撞箱
//            this.card.hb.width = NewShopScreen.SHOP_PAD_X;
//            this.card.hb.height = NewShopScreen.SHOP_PAD_Y;
            this.card.current_x = NewShopScreen.SHOP_POS_X + this.slot * NewShopScreen.SHOP_PAD_X;
            this.card.current_y = rugY + NewShopScreen.TOP_ROW_Y;
            this.card.hb.move(this.card.current_x, this.card.current_y);
            this.card.hb.update();
            if (this.card.hb.hovered) {
                // 配置悬浮的卡牌效果,因为涉及到AbstractCard的私有属性所以自己写(失败力)
                this.card.hover();
                this.card.targetDrawScale = 0.8F * Settings.scale;
                this.card.drawScale = this.card.targetDrawScale;
//                this.hoverDuration += Gdx.graphics.getDeltaTime();
//                if (this.hoverDuration > 0.2F && !Settings.hideCards) {
//                    this.renderTip = true;
//                }

                SteamShopRoom.shopScreen.moveHand(this.card.current_x - AbstractCard.IMG_WIDTH / 1.6F, this.card.current_y);
                if (InputHelper.justClickedLeft) {
                    this.card.hb.clickStarted = true;
                }
                if (InputHelper.justClickedRight) {
                    CardCrawlGame.cardPopup.open(this.card);
                }
            } else {
                this.card.unhover();
                this.card.targetDrawScale = 0.65F * Settings.scale;
//                this.hoverDuration = 0.0F;
//                this.renderTip = false;
            }
        }

        if (this.card.hb.clicked || this.card.hb.hovered && CInputActionSet.select.isJustPressed()) {
            this.card.hb.clicked = false;
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
                    SteamShopRoom.shopScreen.touchCard = this;
                }
            }
        }

    }

    public void purchase() {
        if (this.card == null || this.isPurchased) {
            return;
        }
        if (AbstractDungeon.player.gold >= this.price) {
            AbstractDungeon.player.loseGold(this.price);
            CardCrawlGame.sound.play("SHOP_PURCHASE", 0.1F);
            CardCrawlGame.metricData.addShopPurchaseData(this.card.getMetricID());
            AbstractDungeon.topLevelEffects.add(new FastCardObtainEffect(this.card,
                    this.card.current_x, this.card.current_y));
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

    public void hide() {
        if (this.card == null) {
            return;
        }
        this.card.current_y = (float) Settings.HEIGHT + 200.0F * Settings.scale;

    }

    public void render(SpriteBatch sb) {
        if (this.card == null) {
            return;
        }
        this.card.render(sb);
        this.renderPrice(sb, this.card.current_x, this.card.current_y, this.card.drawScale - 0.6F);
//        if (renderTip) {
//            // 因为renderCardTip方法必须card.renderTip为true,但是后者是私有属性
//            // 所以就改不了,那只能不要渲染Tip了
//            this.card.renderCardTip(sb);
//        }

    }

    static {
        CARD_GOLD_OFFSET_X = -120.0F * Settings.xScale;
        CARD_GOLD_OFFSET_Y = -212.0F * Settings.yScale;
        CARD_PRICE_OFFSET_X = -57.0F * Settings.xScale;
        CARD_PRICE_OFFSET_Y = -207.0F * Settings.yScale;
    }
}
