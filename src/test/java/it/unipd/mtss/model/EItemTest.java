////////////////////////////////////////////////////////////////////
// Augusto Zanellato 2000555
// Stefano Zanovello 2008459
////////////////////////////////////////////////////////////////////
package it.unipd.mtss.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import it.unipd.mtss.model.exception.ItemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.params.IntRangeSource;

public class EItemTest {

    @DisplayName("Getters should return the value that was passed to the constructor")
    @CartesianTest
    void testGetters(
            @CartesianTest.Enum ItemType type,
            @CartesianTest.Values(strings = {"Foo", "Bar", "Baz"}) String name,
            @IntRangeSource(from=100, to=1000, step=100) int priceInCents) {
        var item = new EItem(type, name, priceInCents);
        assertEquals(type, item.itemType());
        assertEquals(name, item.name());
        assertEquals(priceInCents, item.price());
    }

    @DisplayName("Negative prices should be rejected")
    @ParameterizedTest
    @IntRangeSource(from=-1000, to=-100, step=100)
    void testNegativePrice(int priceInCents) {
        assertThrows(ItemException.class, () -> new EItem(ItemType.MOTHERBOARD, "foo", priceInCents));
    }

}
