package SteamMerchant;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.megacrit.cardcrawl.characters.AnimatedNpc;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.vfx.SpeechBubble;

import java.util.ArrayList;
import java.util.Collections;

public class SteamMerchant implements Disposable {
    private static final CharacterStrings characterStrings;
    public AnimatedNpc anim;
    public static final float DRAW_X;
    public static final float DRAW_Y;
    public Hitbox hb;
    private final ArrayList<String> idleMessages;
    private float speechTimer;
    private boolean saidWelcome;
    private final NewShopScreen shopScreen;
    protected float modX;
    protected float modY;

    public SteamMerchant() {
        this(0.0F, 0.0F);
    }

    public SteamMerchant(float x, float y) {
        this.hb = new Hitbox(360.0F * Settings.scale, 300.0F * Settings.scale);
        this.idleMessages = new ArrayList<>();
        this.speechTimer = 1.5F;
        this.saidWelcome = false;
        this.anim = new AnimatedNpc(DRAW_X + 256.0F * Settings.scale, AbstractDungeon.floorY + 30.0F * Settings.scale,
                "MerchantResources/img/merchant/skeleton.atlas",
                "MerchantResources/img/merchant/skeleton.json", "idle");
        if (AbstractDungeon.id.equals("TheEnding")) {
            Collections.addAll(this.idleMessages, characterStrings.OPTIONS);
        } else {
            Collections.addAll(this.idleMessages, characterStrings.TEXT);
        }
        this.speechTimer = 1.5F;
        this.modX = x;
        this.modY = y;
        this.hb.move(DRAW_X + (250.0F + x) * Settings.scale, DRAW_Y + (130.0F + y) * Settings.scale);
        this.shopScreen = SteamShopRoom.shopScreen;
        this.shopScreen.init();
    }

    public void update() {
        this.hb.update();
        if ((this.hb.hovered && InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed()) &&
                !AbstractDungeon.isScreenUp && !AbstractDungeon.isFadingOut && !AbstractDungeon.player.viewingRelics) {
            AbstractDungeon.overlayMenu.proceedButton.setLabel(characterStrings.NAMES[0]);
            this.saidWelcome = true;
            this.shopScreen.open();
            this.hb.hovered = false;
        }

        this.speechTimer -= Gdx.graphics.getDeltaTime();
        if (this.speechTimer < 0.0F) {
            String msg = this.idleMessages.get(MathUtils.random(0, this.idleMessages.size() - 1));
            if (!this.saidWelcome) {
                this.saidWelcome = true;
                this.welcomeSfx();
                msg = characterStrings.NAMES[1];
            } else {
                this.playMiscSfx();
            }
            if (MathUtils.randomBoolean()) {
                AbstractDungeon.effectList.add(new SpeechBubble(this.hb.cX - 50.0F * Settings.xScale,
                        this.hb.cY + 70.0F * Settings.yScale, 3.0F, msg, false));
            } else {
                AbstractDungeon.effectList.add(new SpeechBubble(this.hb.cX - 50.0F * Settings.xScale,
                        this.hb.cY + 70.0F * Settings.yScale, 3.0F, msg, true));
            }
            this.speechTimer = MathUtils.random(40.0F, 60.0F);
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

    public void render(SpriteBatch sb) {
        sb.setColor(Color.WHITE);
        sb.draw(ImageMaster.MERCHANT_RUG_IMG, DRAW_X + this.modX, DRAW_Y + this.modY,
                512.0F * Settings.scale, 512.0F * Settings.scale);
        if (this.hb.hovered) {
            sb.setBlendFunction(770, 1);
            sb.setColor(Settings.HALF_TRANSPARENT_WHITE_COLOR);
            sb.draw(ImageMaster.MERCHANT_RUG_IMG, DRAW_X + this.modX, DRAW_Y + this.modY,
                    512.0F * Settings.scale, 512.0F * Settings.scale);
            sb.setBlendFunction(770, 771);
        }
        if (this.anim != null) {
            this.anim.render(sb);
        }
        if (Settings.isControllerMode) {
            sb.setColor(Color.WHITE);
            sb.draw(CInputActionSet.select.getKeyImg(), DRAW_X - 32.0F + 150.0F * Settings.scale,
                    DRAW_Y - 32.0F + 100.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F,
                    Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64,
                    false, false);
        }
        this.hb.render(sb);
    }

    @Override
    public void dispose() {
        if (this.anim != null) {
            this.anim.dispose();
        }
    }

    static {
        characterStrings = CardCrawlGame.languagePack.getCharacterString("Merchant");
        DRAW_X = (float) Settings.WIDTH * 0.5F + 34.0F * Settings.xScale;
        DRAW_Y = AbstractDungeon.floorY - 109.0F * Settings.scale;
    }

}
