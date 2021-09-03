import de.eldoria.eldoutilities.localization.Replacement;
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
        Assertions.assertEquals("2.20", Replacement.create("NUM", 2.2000).invoke("%NUM%").replace(",", "."));
        Assertions.assertEquals("2.00", Replacement.create("NUM", 2.0).invoke("%NUM%").replace(",", "."));
        Assertions.assertEquals("2", Replacement.create("NUM", 2).invoke("%NUM%"));
    }
}
