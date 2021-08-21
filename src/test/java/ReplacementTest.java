import de.eldoria.eldoutilities.localization.Replacement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
