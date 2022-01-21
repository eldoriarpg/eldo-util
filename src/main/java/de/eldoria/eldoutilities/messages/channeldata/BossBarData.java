/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages.channeldata;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public final class BossBarData implements ChannelData {
    private static final BarStyle DEFAULT_STYLE = BarStyle.SOLID;
    private static final BarColor DEFAULT_COLOR = BarColor.WHITE;
    private static final int DEFAULT_DURATION = 20 * 10;
    /**
     * Default BossBar Data
     */
    public static final BossBarData DEFAULT = new BossBarData(DEFAULT_COLOR, DEFAULT_STYLE, DEFAULT_DURATION);

    private final BarColor color;
    private final BarStyle style;
    private final BarFlag[] flags;
    private final int duration;

    private BossBarData(BarColor color, BarStyle style, int duration, BarFlag... flags) {
        this.color = color;
        this.style = style;
        this.duration = duration;
        this.flags = flags;
    }

    /**
     * Get a new BossBar Builder
     *
     * @return return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a bossbar with a message and a key
     *
     * @param key     kay of message
     * @param message text of bossbar
     * @return BossBar instance
     */
    public BossBar create(NamespacedKey key, String message) {
        return Bukkit.createBossBar(key, message, color, style, flags);
    }

    public int getDuration() {
        return duration;
    }

    public static final class Builder {
        private BarColor color = DEFAULT_COLOR;
        private BarStyle style = DEFAULT_STYLE;
        private BarFlag[] barFlags = new BarFlag[0];
        private int duration = DEFAULT_DURATION;

        public Builder color(BarColor color) {
            this.color = color;
            return this;
        }

        public Builder style(BarStyle style) {
            this.style = style;
            return this;
        }

        public Builder flags(BarFlag... barFlags) {
            this.barFlags = barFlags;
            return this;
        }

        public Builder ofDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public BossBarData build() {
            return new BossBarData(color, style, duration, barFlags);
        }
    }
}
