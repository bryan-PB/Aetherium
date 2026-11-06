package com.aetherium.aetherium.client;

import com.aetherium.aetherium.registry.RegistryHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kryzorae's lightweight client-only codex.
 * No custom shaders, textures, networking, ticking, or integration dependencies.
 * All chapters reuse the existing translated guide text.
 */
@OnlyIn(Dist.CLIENT)
public final class GuideBookScreen extends Screen {
    private static final int W = 454;
    private static final int H = 280;
    private static final int CHAPTERS = 8;
    private static final int INK = 0xE8E1EF;
    private static final int QUIET = 0xB7A5C5;
    private static final int GOLD = 0xD7B87D;
    private static final int PURPLE = 0xB47AFF;
    private static final int CRIMSON = 0xEA6680;

    private int chapter;
    private float panelScale;
    private int panelX;
    private int panelY;
    private int cachedChapter = -1;
    private List<String> wrapped = Collections.emptyList();

    public GuideBookScreen() {
        super(new StringTextComponent("Kryzorae"));
    }

    private int accent() {
        return chapter == 3 || chapter == 5 ? CRIMSON : chapter == 0 || chapter == 6 ? 0x67D7DD : PURPLE;
    }

    private void setChapter(int next) {
        chapter = Math.max(0, Math.min(CHAPTERS - 1, next));
        cachedChapter = -1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);
        panelScale = Math.min(1.0F, Math.min((this.width - 24.0F) / W, (this.height - 24.0F) / H));
        panelScale = Math.max(0.1F, panelScale);
        panelX = (this.width - Math.round(W * panelScale)) / 2;
        panelY = (this.height - Math.round(H * panelScale)) / 2;
        float localX = (mouseX - panelX) / panelScale;
        float localY = (mouseY - panelY) / panelScale;
        ensureContent();

        matrix.pushPose();
        matrix.translate(panelX, panelY, 0);
        matrix.scale(panelScale, panelScale, 1);
        renderCover(matrix);
        renderSidebar(matrix, localX, localY);
        renderPage(matrix, localX, localY);
        matrix.popPose();

        // Render the actual item sprite from the mod when there is room to display it.
        // ItemRenderer uses GUI screen coordinates, so it runs outside the scaled panel.
        if (panelScale >= 0.9F) {
            this.itemRenderer.renderAndDecorateItem(chapterIcon(),
                    panelX + Math.round(173 * panelScale),
                    panelY + Math.round(83 * panelScale));
        }
        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    private void renderCover(MatrixStack matrix) {
        // Obsidian cover, two-page seam, brass filigree and inner illuminated edges.
        fill(matrix, 3, 5, W + 2, H + 4, 0xAA020208);
        fillGradient(matrix, 0, 0, W, H, 0xFF100E17, 0xFF2A172E);
        fill(matrix, 3, 3, W - 3, H - 3, 0xFF8B6A48);
        fill(matrix, 6, 6, W - 6, H - 6, 0xFF231828);
        fillGradient(matrix, 12, 12, 157, H - 12, 0xFF272033, 0xFF15111F);
        fillGradient(matrix, 163, 12, W - 12, H - 12, 0xFF302639, 0xFF19131F);
        fill(matrix, 158, 10, 162, H - 10, 0xFF0F0C16);
        fill(matrix, 160, 15, 161, H - 15, 0xFF9C739B);

        // Gilded page outline and corner gems.
        outline(matrix, 169, 18, W - 17, H - 17, 0xFF8D6D55);
        fill(matrix, 171, 20, W - 19, 22, 0xFF513653);
        fill(matrix, 171, H - 22, W - 19, H - 20, 0xFF513653);
        for (int x : new int[]{21, 149, 173, W - 22}) {
            diamond(matrix, x, 21, 3, 0xFFBB8A62);
            diamond(matrix, x, H - 21, 3, 0xFFBB8A62);
        }
        fill(matrix, 12, 12, 15, H - 12, 0xFF3C2347);
        fill(matrix, 16, 16, 17, H - 16, 0xFFA765BF);
    }

    private void renderSidebar(MatrixStack matrix, float mouseX, float mouseY) {
        drawCenteredString(matrix, font, "K R Y Z O R A E", 85, 33, GOLD);
        fill(matrix, 33, 49, 136, 50, 0xFF74567D);
        font.draw(matrix, I18n.get("guide.aetherium.ui.codex"), 31, 61, QUIET);
        diamond(matrix, 137, 65, 4, 0xFFAB83D8);
        for (int i = 0; i < CHAPTERS; i++) {
            int y = 85 + 20 * i;
            boolean selected = chapter == i;
            boolean hover = bounds(mouseX, mouseY, 25, y, 147, y + 18);
            if (selected) {
                fillGradient(matrix, 24, y, 148, y + 18, 0xFF624170, 0xFF2A213B);
                fill(matrix, 24, y, 27, y + 18, accent() | 0xFF000000);
            } else if (hover) {
                fill(matrix, 24, y, 148, y + 18, 0xFF393145);
            }
            int color = selected ? INK : hover ? GOLD : QUIET;
            font.draw(matrix, String.format("%02d", i + 1), 31, y + 5, selected ? GOLD : 0xFF987F9C);
            String name = I18n.get("guide.aetherium.chapter." + (i + 1));
            font.draw(matrix, font.plainSubstrByWidth(name, 94), 53, y + 5, color);
        }
        font.draw(matrix, I18n.get("guide.aetherium.ui.hint"), 26, 258, 0xFF8A7999);
    }

    private void renderPage(MatrixStack matrix, float mouseX, float mouseY) {
        int tint = accent();
        // Chapter ribbon, rune and luminous divider.
        fillGradient(matrix, 176, 28, W - 25, 74, 0xFF382540, 0xFF20172A);
        fill(matrix, 176, 28, W - 25, 30, tint | 0xFF000000);
        font.draw(matrix, I18n.get("guide.aetherium.ui.chapter") + " " + String.format("%02d", chapter + 1),
                183, 35, GOLD);
        String heading = I18n.get("guide.aetherium.chapter." + (chapter + 1));
        font.drawShadow(matrix, font.plainSubstrByWidth(heading, 231), 183, 51, INK);
        diamond(matrix, W - 39, 55, 7, tint | 0xFF000000);
        diamond(matrix, W - 39, 55, 3, GOLD | 0xFF000000);

        fill(matrix, 176, 106, W - 27, 107, 0xFF6B536E);
        fill(matrix, 176, 106, 207, 107, tint | 0xFF000000);
        fill(matrix, 172, 80, 202, 103, 0xFF14111D);

        int y = 114;
        for (String line : wrapped) {
            if (y > 220) break;
            if (line.isEmpty()) {
                y += 5;
            } else {
                font.draw(matrix, line, 179, y, INK);
                y += 12;
            }
        }

        // Footer contains page progress and interactive page corners.
        fill(matrix, 176, 229, W - 26, 230, 0xFF57415C);
        fill(matrix, 203, 244, 401, 247, 0xFF35283E);
        int barEnd = 203 + (198 * (chapter + 1) / CHAPTERS);
        fill(matrix, 203, 244, barEnd, 247, tint | 0xFF000000);

        boolean previousHover = bounds(mouseX, mouseY, 176, 235, 198, 264);
        boolean nextHover = bounds(mouseX, mouseY, 410, 235, 434, 264);
        font.draw(matrix, "<", 183, 241, chapter > 0 ? (previousHover ? GOLD : INK) : 0xFF5F5369);
        font.draw(matrix, ">", 418, 241, chapter < CHAPTERS - 1 ? (nextHover ? GOLD : INK) : 0xFF5F5369);
        drawCenteredString(matrix, font,
                (chapter + 1) + " / " + CHAPTERS, 302, 255, QUIET);
        if (bounds(mouseX, mouseY, 426, 6, 449, 26)) {
            fill(matrix, 425, 7, 449, 25, 0xFF5B334F);
        }
        font.draw(matrix, "x", 434, 12, GOLD);
    }

    private void ensureContent() {
        if (cachedChapter == chapter) return;
        cachedChapter = chapter;
        List<String> result = new ArrayList<>();
        String raw = I18n.get("guide.aetherium.page." + (chapter + 1));
        String clean = TextFormatting.stripFormatting(raw);
        if (clean == null) clean = raw;
        String[] sections = clean.split("\\n\\n", 2);
        String body = sections.length == 2 ? sections[1] : clean;
        for (String para : body.split("\\n\\n")) {
            if (!result.isEmpty()) result.add("");
            wrapParagraph(para.replace('\n', ' ').trim(), 240, result);
        }
        wrapped = result;
    }

    private void wrapParagraph(String paragraph, int maxWidth, List<String> lines) {
        StringBuilder row = new StringBuilder();
        for (String word : paragraph.split(" ")) {
            if (word.isEmpty()) continue;
            String candidate = row.length() == 0 ? word : row + " " + word;
            if (font.width(candidate) > maxWidth && row.length() > 0) {
                lines.add(row.toString());
                row.setLength(0);
                row.append(word);
            } else {
                row.setLength(0);
                row.append(candidate);
            }
        }
        if (row.length() > 0) lines.add(row.toString());
    }

    private ItemStack chapterIcon() {
        switch (chapter) {
            case 1: return new ItemStack(RegistryHandler.AETHERIUM_ORE_ITEM.get());
            case 2: return new ItemStack(RegistryHandler.AETHERIUM_SWORD.get());
            case 3: return new ItemStack(RegistryHandler.ASTRAL_SWORD.get());
            case 4: return new ItemStack(RegistryHandler.RIFT_SIGIL.get());
            case 5: return new ItemStack(RegistryHandler.ASTRAL_SEAL.get());
            case 6: return new ItemStack(RegistryHandler.AETHERIUM_FLARE.get());
            case 7: return new ItemStack(RegistryHandler.ASTRAL_INGOT.get());
            default: return new ItemStack(RegistryHandler.GUIDE_BOOK.get());
        }
    }

    private static void outline(MatrixStack matrix, int x1, int y1, int x2, int y2, int color) {
        fill(matrix, x1, y1, x2, y1 + 1, color);
        fill(matrix, x1, y2 - 1, x2, y2, color);
        fill(matrix, x1, y1, x1 + 1, y2, color);
        fill(matrix, x2 - 1, y1, x2, y2, color);
    }

    private static void diamond(MatrixStack matrix, int cx, int cy, int size, int color) {
        for (int n = -size; n <= size; n++) {
            int halfWidth = size - Math.abs(n);
            fill(matrix, cx - halfWidth, cy + n, cx + halfWidth + 1, cy + n + 1, color);
        }
    }

    private static boolean bounds(double x, double y, int x1, int y1, int x2, int y2) {
        return x >= x1 && x < x2 && y >= y1 && y < y2;
    }

    private boolean localClick(double x, double y) {
        if (bounds(x, y, 426, 6, 449, 26)) {
            onClose();
            return true;
        }
        for (int i = 0; i < CHAPTERS; i++) {
            int y1 = 85 + 20 * i;
            if (bounds(x, y, 24, y1, 148, y1 + 18)) {
                setChapter(i);
                return true;
            }
        }
        if (bounds(x, y, 176, 235, 198, 264)) {
            setChapter(chapter - 1);
            return true;
        }
        if (bounds(x, y, 410, 235, 434, 264)) {
            setChapter(chapter + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && localClick((mouseX - panelX) / panelScale,
                (mouseY - panelY) / panelScale)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (scroll > 0) setChapter(chapter - 1);
        else if (scroll < 0) setChapter(chapter + 1);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_D) {
            setChapter(chapter + 1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_A) {
            setChapter(chapter - 1);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
}
