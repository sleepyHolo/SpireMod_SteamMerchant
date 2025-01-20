package SteamMerchant.stores;

import SteamMerchant.NewShopScreen;
import SteamMerchant.SteamShopRoom;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;

public class AbstractGoods {
    public static final float CARD_PIXEL_X;
    public static final float CARD_PIXEL_Y;
    public static final float CARD_IMG_WIDTH;
    public static final float CARD_IMG_HEIGHT;
    protected int slot;
    public int discountIndex;
    public int basePrice;
    public int price;
    public int discount;
    private boolean onSale;
    public boolean isPurchased;
    protected float gold_offset_x;
    protected float gold_offset_y;
    protected float price_offset_x;
    protected float price_offset_y;
    private static final float GOLD_IMG_SIZE;

    public AbstractGoods(int slot, int discountIndex) {
        this.isPurchased = false;
        this.slot = slot;
        this.discountIndex = discountIndex;
        this.onSale = SteamShopRoom.shopScreen.getSale();
    }

    public void setBasePrice(int basePrice) {
        this.basePrice = basePrice;
        this.initDiscount();
    }

    public void resetBasePrice(int basePrice) {
        // 这个不会改变折扣
        this.basePrice = basePrice;
        this.price = MathUtils.round((float)this.basePrice * (1.0F - (float)this.discount / 100.0F));
    }

    public void initDiscount() {
        int minDiscount = -8;
        int maxDiscount = 8;
        if (this.discountIndex == this.slot) {
            minDiscount += 4;
            maxDiscount += 8;
        }
        if (SteamShopRoom.shopScreen.getSale()) {
            minDiscount += 12;
            maxDiscount += 12;
        }
        this.discount = AbstractDungeon.merchantRng.random(minDiscount, maxDiscount);
        // 限制打折范围在10%到95%
        if (this.discount < 2) {
            this.discount = 0;
        }
        if (this.discount > 19) {
            this.discount = 19;
        }
        this.discount *= 5;
        this.price = MathUtils.round((float)this.basePrice * (1.0F - (float)this.discount / 100.0F));

    }

    public void update() {
        if (this.isPurchased) {
            return;
        }
        // 获取特卖信息
        boolean nowSale = SteamShopRoom.shopScreen.getSale();
        if (nowSale != this.onSale) {
            this.onSale = nowSale;
            this.initDiscount();
        }

    }

    public void renderPrice(SpriteBatch sb, float x, float y, float rel_scale) {
        // gold
        sb.setColor(Color.WHITE);
        float gold_delta_x = 62 * Settings.scale;
        if (this.discount != 0) {
            gold_delta_x = 0;
        }
        sb.draw(ImageMaster.UI_GOLD,
                x + this.gold_offset_x + gold_delta_x,  y + this.gold_offset_y - rel_scale * 200.0F * Settings.scale,
                GOLD_IMG_SIZE, GOLD_IMG_SIZE);
        // draw price bg
        float tmp_y = y + this.price_offset_y - rel_scale * 200.0F * Settings.scale;
        Color price_color = Color.WHITE;
        float price_delta_x = 72 * Settings.scale;
        float price_delta_y = 37 * Settings.scale;
        if (this.discount != 0) {
            price_color = Color.SKY;
            price_delta_x = 10 * Settings.scale;
            price_delta_y = 27 * Settings.scale;
            sb.draw(NewShopScreen.salePriceImg, x + this.price_offset_x, tmp_y);
            // render base price
            FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipHeaderFont, Integer.toString(this.basePrice),
                    x + this.price_offset_x + 10 * Settings.scale, tmp_y + 47 * Settings.scale, Color.LIGHT_GRAY);
            FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipHeaderFont, "------",
                    x + this.price_offset_x + 5 * Settings.scale, tmp_y + 47 * Settings.scale, Color.LIGHT_GRAY);
            // render discount
            FontHelper.renderFontLeftTopAligned(sb, FontHelper.buttonLabelFont, "-" + this.discount + "%",
                    x + this.price_offset_x + 80 * Settings.scale, tmp_y + 38 * Settings.scale, Color.GREEN);
        }
        if (this.price > AbstractDungeon.player.gold) {
            price_color = Color.SALMON;
        }
        // render price
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipHeaderFont, Integer.toString(this.price),
                x + this.price_offset_x + price_delta_x, tmp_y + price_delta_y, price_color);
    }

    static {
        GOLD_IMG_SIZE = (float) ImageMaster.UI_GOLD.getWidth() * Settings.scale;
        CARD_PIXEL_X = 512.0F;
        CARD_PIXEL_Y = 512.0F;
        CARD_IMG_WIDTH = 420.0F;
        CARD_IMG_HEIGHT = 300.0F;
    }
}
