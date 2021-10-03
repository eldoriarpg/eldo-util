package de.eldoria.eldoutilities.localization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReplacementTest {
    Replacement repCaps = Replacement.create("ENTITY", "name");
    Replacement repLower = Replacement.create("entity", "name");
    Replacement replacement2 = Replacement.create("ENTITY", "name", '6');

    @Test
    public void test1() {
       Assertions.assertEquals("name", repCaps.invoke("%ENTITY%"));
       Assertions.assertEquals("name", repLower.invoke("%ENTITY%"));
    }
}
