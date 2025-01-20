package SteamMerchant.stores;

import SteamMerchant.NewShopScreen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;

public class StoreNull extends AbstractGoods{
    private static final float PIXEL_X;
    private static final float PIXEL_Y;
    private float x;
    private float y;
    private float scale;

    public StoreNull(int slot) {
        // 这个没有价格相关
        super(slot, 0);
    }

    public void update(float rugY) {
        // 不用更新价格
        this.scale = Settings.scale;
        this.x = NewShopScreen.SHOP_POS_X + this.slot * NewShopScreen.SHOP_PAD_X;
        this.y = rugY + NewShopScreen.BOTTOM_ROW_Y;
    }

    public void render(SpriteBatch sb) {
        sb.draw(NewShopScreen.soldOutImg, x, y, scale * PIXEL_X, scale * PIXEL_Y);
    }

    static {
        PIXEL_X = 512.0F;
        PIXEL_Y = 512.0F;
    }
}
