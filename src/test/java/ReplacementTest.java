import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.utils.Parser;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

public class ReplacementTest {
    Replacement repCaps = Replacement.create("ENTITY", "name");
    Replacement repLower = Replacement.create("entity", "name");
    Replacement replacement2 = Replacement.create("ENTITY", "name", '6');

    @Test
    public void test1() {
        Assertions.assertEquals("name§r", repCaps.invoke("%ENTITY%"));
        Assertions.assertEquals("name§r", repLower.invoke("%ENTITY%"));
    }
}
