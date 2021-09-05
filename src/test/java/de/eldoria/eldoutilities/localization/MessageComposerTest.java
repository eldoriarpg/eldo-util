package de.eldoria.eldoutilities.localization;

import de.eldoria.eldoutilities.utils.TextUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MessageComposerTest {

    @Test
    public void fillTest() {
        String composer = MessageComposer.create().text("Hello").fillLines(10).build();
        Assertions.assertEquals(9, TextUtil.countChars(composer, '\n'));

        composer = MessageComposer.create().text("Hello").fillLines(1).build();
        Assertions.assertEquals(0, TextUtil.countChars(composer, '\n'));
    }

    @Test
    public void spacesText() {
        Assertions.assertEquals(5, MessageComposer.create().space(5).build().length());
    }
}
