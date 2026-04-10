package com.musclemem.client;

import com.musclemem.SkillType;
import com.musclemem.gui.SkillScreenHandler;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

public class SkillScreen extends AbstractContainerScreen<SkillScreenHandler> {

    private static final int W = 240;
    private static final int H = 248;
    private static final int TITLE_H = 20;
    private static final int ROW_H   = 22;
    private static final int PAD     = 10;
    private static final int BAR_W     = 45;
    private static final int BAR_H   = 4;

    private static final int COL_NAME  = PAD;
    private static final int COL_STARS = 120;
    private static final int COL_BAR   = 152;

    private static final int BG       = 0xFF1A1A2E;
    private static final int ROW_A    = 0xFF16213E;
    private static final int ROW_B    = 0xFF0F3460;
    private static final int BORDER   = 0xFF533483;
    private static final int TITLE_C  = 0xFFE94560;
    private static final int NAME_C   = 0xFFEEEEEE;
    private static final int STAR_ON  = 0xFFFFD700;
    private static final int STAR_OFF = 0xFF555566;
    private static final int BAR_BG   = 0xFF111122;
    private static final int BAR_FILL = 0xFF00B4D8;
    private static final int BAR_MAX  = 0xFFFFD700;
    private static final int PROG_C   = 0xFFAAAAAA;
    private static final int MAX_C    = 0xFFFFD700;

    public SkillScreen(SkillScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, W, H);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = -9999;
        this.titleLabelY     = -9999;
        // Ekranı ortala
        this.leftPos = (this.width - W) / 2;
        this.topPos  = (this.height - H) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);

        int x = this.leftPos;
        int y = this.topPos;

        // Arka plan
        g.fill(x, y, x + W, y + H, BG);

        // Kenarlık
        g.fill(x,         y,         x + W,     y + 1,     BORDER);
        g.fill(x,         y + H - 1, x + W,     y + H,     BORDER);
        g.fill(x,         y,         x + 1,     y + H,     BORDER);
        g.fill(x + W - 1, y,         x + W,     y + H,     BORDER);

        // Başlık arka planı
        g.fill(x + 1, y + 1, x + W - 1, y + TITLE_H, 0xFF12122A);
        g.fill(x + 1, y + TITLE_H, x + W - 1, y + TITLE_H + 1, BORDER);

        ActiveTextCollector tr = g.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_ONLY);

        // Başlık
        String titleStr = "Muscle Memory";
        int titleW = this.font.width(titleStr);
        tr.accept(TextAlignment.LEFT, x + (W - titleW) / 2, y + 6, styled(titleStr, TITLE_C));

        // Satırlar
        SkillType[] skills = SkillType.values();
        for (int i = 0; i < skills.length; i++) {
            int rowY  = y + TITLE_H + 1 + i * ROW_H;
            int rowBg = (i % 2 == 0) ? ROW_A : ROW_B;
            g.fill(x + 1, rowY, x + W - 1, rowY + ROW_H, rowBg);
            drawRow(g, tr, skills[i], x, rowY);
        }
    }

    private void drawRow(GuiGraphicsExtractor g, ActiveTextCollector tr,
                         SkillType skill, int x, int rowY) {
        int count  = menu.getSkillCount(skill);
        int level  = menu.getSkillLevel(skill);
        int textY  = rowY + (ROW_H - 8) / 2;

        // İsim
        tr.accept(TextAlignment.LEFT, x + COL_NAME, textY, styled(skill.getDisplayName(), NAME_C));

        // Yıldızlar
        int sx = x + COL_STARS;
        for (int i = 0; i < 3; i++) {
            String s = i < level ? "★" : "☆";
            int c    = i < level ? STAR_ON : STAR_OFF;
            tr.accept(TextAlignment.LEFT, sx, textY, styled(s, c));
            sx += this.font.width(s) + 2;
        }

        // Progress bar
        int barX = x + COL_BAR;
        int barY = rowY + (ROW_H - BAR_H) / 2;
        g.fill(barX, barY, barX + BAR_W, barY + BAR_H, BAR_BG);
        float pct  = computeProgress(skill, count, level);
        int   fill = (int)(BAR_W * pct);
        if (fill > 0) {
            g.fill(barX, barY, barX + fill, barY + BAR_H, level >= 3 ? BAR_MAX : BAR_FILL);
        }

        // Progress text — bar'ın sağında
        String pt      = level >= 3 ? "MAX" : count + "/" + skill.getNextThreshold(count);
        int    ptColor = level >= 3 ? MAX_C : PROG_C;
        int rightEdge = x + W - PAD;
        tr.accept(TextAlignment.LEFT, rightEdge - this.font.width(pt), textY, styled(pt, ptColor));
    }

    private FormattedCharSequence styled(String text, int color) {
        return Component.literal(text)
                .withStyle(Style.EMPTY.withColor(color))
                .getVisualOrderText();
    }

    private float computeProgress(SkillType skill, int count, int level) {
        if (level >= 3) return 1f;
        int prev  = level > 0 ? skill.getThreshold(level) : 0;
        int next  = skill.getNextThreshold(count);
        int range = next - prev;
        if (range <= 0) return 0f;
        return Math.max(0f, Math.min(1f, (float)(count - prev) / range));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}