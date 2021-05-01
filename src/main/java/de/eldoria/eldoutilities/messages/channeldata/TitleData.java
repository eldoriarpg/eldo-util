package de.eldoria.eldoutilities.messages.channeldata;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageChannel;
import de.eldoria.eldoutilities.messages.MessageType;

public final class TitleData implements ChannelData {
    public static final TitleData DEFAULT = new TitleData(10, 50, 20, "");
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;
    private String otherLine;

    private TitleData(int fadeIn, int stay, int fadeOut, String otherLine) {
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
        this.otherLine = otherLine;
    }

    public static TitleData forTime(int fadeIn, int stay, int fadeOut) {
        return new TitleData(fadeIn, stay, fadeOut, "");
    }

    public static TitleData forOtherLine(String otherLine) {
        return new TitleData(10, 50, 20, otherLine);
    }

    public static TitleData forFadeAndTime(int fadeIn, int stay, int fadeOut, String otherLine) {
        return new TitleData(fadeIn, stay, fadeOut, otherLine);
    }

    @Override
    public void localized(ILocalizer localizer, Replacement... replacements) {
        otherLine = localizer.localize(otherLine, replacements);
    }

    @Override
    public void formatText(MessageType type, MessageChannel<? extends ChannelData> channel, String prefix) {
        type.forceColor(otherLine);
        otherLine = channel.addPrefix(otherLine, prefix);
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public String getOtherLine() {
        return otherLine;
    }
}
