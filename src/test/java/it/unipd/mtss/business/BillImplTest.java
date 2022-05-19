////////////////////////////////////////////////////////////////////
// Augusto Zanellato 2000555
// Stefano Zanovello 2008459
////////////////////////////////////////////////////////////////////
package it.unipd.mtss.business;

import it.unipd.mtss.model.EItem;
import it.unipd.mtss.model.ItemType;
import it.unipd.mtss.model.User;
import it.unipd.mtss.model.exception.BillException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BillImplTest {

    Bill bill;
    User user;

    @BeforeEach
    void setUp() {
        bill = new BillImpl();
        user = new User();
    }

    @DisplayName("Empty order is rejected")
    @Test
    void testEmptyOrderIsRejected() {
        assertThrows(BillException.class, () -> bill.getOrderPrice(List.of(), user));
    }

    @DisplayName("Simple total is calculated correctly")
    @ParameterizedTest
    @MethodSource("generateSimpleTotalData")
    void testSimpleTotal(int total, List<Integer> prices) {
        final var types = ItemType.values();
        var items = IntStream.range(0, prices.size())
                .mapToObj(i -> new EItem(types[i % types.length], "foo", prices.get(i)))
                .toList();


        assertEquals(total, bill.getOrderPrice(items, user));
    }

    static Stream<Arguments> generateSimpleTotalData() {
        return Stream.of(
                Arguments.of(10, Arrays.asList(6, 4)),
                Arguments.of(15, Arrays.asList(7, 5, 3)),
                Arguments.of(60, Arrays.asList(6, 12, 24, 8, 5, 3, 2)),
                Arguments.of(110, Arrays.asList(6, 4, 19, 11, 8, 12, 3, 7, 16, 17, 7))
        );
    }

}
