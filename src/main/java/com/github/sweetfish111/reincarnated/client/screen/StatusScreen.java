package com.github.sweetfish111.reincarnated.client.screen;

import com.github.sweetfish111.reincarnated.network.payload.SyncStatusPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatusScreen extends Screen {
    private final SyncStatusPayload status;
    private static final double EVOLUTION_THRESHOLD = 100.0; // 表示用。実値はBalanceConfig側が権威

    public StatusScreen(SyncStatusPayload status) {
        super(Component.translatable("screen.reincarnated.status.title"));
        this.status = status;
    }

    /** langキー＋引数からローカライズ済み文字列を得る小さなヘルパー */
    private static String tr(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int x = this.width / 2 - 140;
        int y = 20;
        int lineHeight = 14;

        graphics.centeredText(this.font, this.title.getString(), this.width / 2, y, 0xFFFFFFFF);
        y += lineHeight;

        // --- 魔素 ---
        String stageDisplay = tr("name.reincarnated.masoStage." + status.masoStageName());
        graphics.text(this.font, tr("screen.reincarnated.status.maso_economy") + "  " + tr("screen.reincarnated.status.stage", stageDisplay), x, y, 0xFFFFFFFF);
        y += lineHeight;
        drawBar(graphics, x, y, 260, status.currentMaso() / Math.max(1f, status.maxMaso()),
                tr("screen.reincarnated.status.maso_bar",
                        String.format("%.1f", status.currentMaso()),
                        String.format("%.1f", status.maxMaso())),
                0xFF55FFFF);
        y += lineHeight;
        graphics.text(this.font, tr("screen.reincarnated.status.regen_rate", String.format("%.2f", status.masoRegenRate())), x, y, 0xFFAAAAAA);
        y += lineHeight;
        drawStyleBar(graphics, x, y, 260, status.masoStylePreference(),
                tr("screen.reincarnated.status.sustain_type"), tr("screen.reincarnated.status.burst_type"));
        y += lineHeight * 2;

        // --- バリア ---
        graphics.text(this.font, tr("screen.reincarnated.status.barrier"), x, y, 0xFFFFFFFF);
        y += lineHeight;
        drawBar(graphics, x, y, 260, status.barrierPoint() / Math.max(1f, status.maxBarrierPoint()),
                tr("screen.reincarnated.status.barrier_bar",
                        String.format("%.1f", status.barrierPoint()),
                        String.format("%.1f", status.maxBarrierPoint())),
                0xFF77CCFF);
        y += lineHeight;
        graphics.text(this.font, tr("screen.reincarnated.status.damage_reduction", String.format("%.0f", status.barrierDamageReduction() * 100)), x, y, 0xFFAAAAAA);
        y += lineHeight;
        drawStyleBar(graphics, x, y, 260, status.barrierStylePreference(),
                tr("screen.reincarnated.status.chip_type"), tr("screen.reincarnated.status.heavy_type"));
        y += lineHeight * 2;

        //---- 演算能力 ----
        graphics.text(this.font, tr("screen.reincarnated.status.compute"), x, y, 0xFFFFFFFF);
        y += lineHeight;
        drawBar(graphics, x, y, 260,
                (float) (status.maxComputeCapacity() > 0 ? status.computeUsage() / status.maxComputeCapacity() : 0f),
                tr("screen.reincarnated.status.compute_bar",
                        String.format("%.1f", status.computeUsage()),
                        String.format("%.1f", status.maxComputeCapacity())),
                0xFFCC88FF);
        y += lineHeight * 2;

        // --- ユニークスキル ---
        String skillLine = tr("screen.reincarnated.status.unique_skill") + "  "
                + tr("screen.reincarnated.status.current_skill", status.currentUniqueSkill())
                + (status.completeGreedy() ? "" : tr("screen.reincarnated.status.greedy_not_established"));
        graphics.text(this.font, skillLine, x, y, 0xFFFFFFFF);
        y += lineHeight;

        if (status.completeGreedy()) {
            drawAnonymizedFactors(graphics, x, y);
        }
    }

    private int drawAnonymizedFactors(GuiGraphicsExtractor graphics, int x, int y) {
        int lineHeight = 14;

        List<Integer> tiers = new ArrayList<>();
        tiers.add(computeFactorTier(status.predatorScore()));
        tiers.add(computeFactorTier(status.scavengerScore()));
        tiers.add(computeFactorTier(status.hoarderScore()));
        tiers.add(computeFactorTier(status.usurperScore()));
        tiers.removeIf(t -> t <= 0 || t >= 4);
        tiers.sort(Collections.reverseOrder());

        if (tiers.isEmpty() && status.evolvableUniqueSkills().isEmpty()) {
            graphics.text(this.font, tr("screen.reincarnated.status.nothing_felt"), x, y, 0xFF888888);
            y += lineHeight;
        } else {
            for (int tier : tiers) {
                int color = switch (tier) {
                    case 1 -> 0xFFAAAAAA;
                    case 2 -> 0xFFDDDD55;
                    default -> 0xFFFFAA55;
                };
                String hint = tr("message.reincarnated.factor_tier." + tier);
                graphics.text(this.font, tr("screen.reincarnated.status.factor_detected", hint), x, y, color);
                y += lineHeight;
            }
        }

        y += lineHeight / 2;
        if (!status.evolvableUniqueSkills().isEmpty()) {
            List<String> translatedNames = new ArrayList<>();
            for (String skillId : status.evolvableUniqueSkills()) {
                translatedNames.add(tr("name.reincarnated.uniqueSkill." + skillId));
            }
            graphics.text(this.font, "§a" + tr("screen.reincarnated.status.awakening_available", String.join(", ", translatedNames)) + "§r", x, y, 0xFFFFFFFF);
            y += lineHeight;
        }

        return y;
    }

    private static int computeFactorTier(double score) {
        if (score >= EVOLUTION_THRESHOLD) return 4;
        double ratio = score / EVOLUTION_THRESHOLD;
        if (ratio < 0.1)  return 0;
        if (ratio < 0.35) return 1;
        if (ratio < 0.65) return 2;
        return 3;
    }

    private void drawBar(GuiGraphicsExtractor graphics, int x, int y, int width, float ratio, String label, int fillColor) {
        ratio = Math.max(0f, Math.min(1f, ratio));
        int height = 10;
        graphics.fill(x, y, x + width, y + height, 0xFF333333);
        graphics.fill(x, y, x + (int)(width * ratio), y + height, fillColor);
        graphics.centeredText(this.font, label, x + width / 2, y + 1, 0xFFFFFFFF);
    }

    private void drawStyleBar(GuiGraphicsExtractor graphics, int x, int y, int width, double r, String leftLabel, String rightLabel) {
        int height = 6;
        graphics.fill(x, y, x + width, y + height, 0xFF333333);
        int markerX = x + (int) (width * Math.max(0.0, Math.min(1.0, r)));
        graphics.fill(markerX - 2, y - 2, markerX + 2, y + height + 2, 0xFFFFAA00);
        graphics.text(this.font, leftLabel, x, y + height + 2, 0xFFAAAAAA);
        graphics.text(this.font, rightLabel, x + width - this.font.width(rightLabel), y + height + 2, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}