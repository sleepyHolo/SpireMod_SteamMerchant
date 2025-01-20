package SteamMerchant.patches;

import SteamMerchant.SteamShopRoom;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.shop.ShopScreen;

public class ShopScreenReplacePatch {

    // 破坏原始 shopScreen 的 update
    @SpirePatch(clz = ShopScreen.class, method = "update", paramtypez = {})
    public static class UpdateKill {
        public static void Replace(ShopScreen __instance){
            SteamShopRoom.shopScreen.update();
        }
    }

    // 破坏原始 shopScreen 的 render
    @SpirePatch(clz = ShopScreen.class, method = "render", paramtypez = {SpriteBatch.class})
    public static class RenderKill {
        public static void Replace(ShopScreen __instance, SpriteBatch sb){
            SteamShopRoom.shopScreen.render(sb);
        }
    }

    // 破坏原始 shopScreen 的 open
    @SpirePatch(clz = ShopScreen.class, method = "open", paramtypez = {})
    public static class OpenKill {
        public static void Replace(ShopScreen __instance){
            SteamShopRoom.shopScreen.open();
        }
    }

}
