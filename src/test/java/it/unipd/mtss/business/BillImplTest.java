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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.params.DoubleRangeSource;
import org.junitpioneer.jupiter.params.IntRangeSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BillImplTest {

    LocalTime orderTime = LocalTime.of(12, 0);
    LocalTime underageGiftOrderTime = LocalTime.of(18, 30);

    Bill bill;
    User adultUser, underageUser;
    Random mockedRandom;

    @BeforeEach
    void setUp(@Mock Random mockedRandom) {
        this.mockedRandom = mockedRandom;
        bill = new BillImpl(mockedRandom);
        adultUser = new User(false);
        underageUser = new User(true);
        BillImpl.resetUnderageGift();
    }

    @DisplayName("Empty order is rejected")
    @Test
    void testEmptyOrderIsRejected() {
        assertThrows(BillException.class, () -> bill.getOrderPrice(List.of(), adultUser, orderTime));
    }

    @DisplayName("Simple total is calculated correctly")
    @ParameterizedTest
    @MethodSource("generateSimpleTotalData")
    void testSimpleTotal(int total, List<Integer> prices) {
        final var types = ItemType.values();
        var items = IntStream.range(0, prices.size())
                .mapToObj(i -> new EItem(types[i % types.length], "foo", prices.get(i)))
                .toList();


        assertEquals(total, bill.getOrderPrice(items, adultUser, orderTime));
    }

    @DisplayName(">10 mouses gift is not applied with less than 11 mouses")
    @ParameterizedTest
    @IntRangeSource(from = 1, to = 11)
    void testTenMousesGiftNotApplying(int mousesCount) {
        var items = generateItems(ItemType.MOUSE, mousesCount, 10).toList();
        var expectedTotal = items.stream().mapToDouble(EItem::price).sum();
        assertEquals(expectedTotal, bill.getOrderPrice(items, adultUser, orderTime));
    }

    @DisplayName(">10 mouses gift is applied with at least 11 mouses")
    @ParameterizedTest
    @IntRangeSource(from = 11, to = 15)
    void testTenMousesGiftApplying(int mousesCount) {
        var items = generateItems(ItemType.MOUSE, mousesCount, 5).toList();
        var expectedTotal = items.stream().mapToDouble(EItem::price).sum()
                - items.stream().mapToDouble(EItem::price).min().orElseThrow();
        assertEquals(expectedTotal, bill.getOrderPrice(items, adultUser, orderTime));
    }


    static Stream<Arguments> generateSimpleTotalData() {
        return Stream.of(
                Arguments.of(10, Arrays.asList(6, 4)),
                Arguments.of(15, Arrays.asList(7, 5, 3)),
                Arguments.of(60, Arrays.asList(6, 12, 24, 8, 5, 3, 2)),
                Arguments.of(110, Arrays.asList(6, 4, 19, 11, 8, 12, 3, 7, 16, 17, 7))
        );
    }

    private static Stream<EItem> generateItems(ItemType type, int quantity, int priceMultiplier) {
        return IntStream.range(0, quantity)
                .map(v -> (v + 1) * priceMultiplier)
                .mapToObj(price -> new EItem(type, "foo", price));
    }

    @DisplayName(">5 processors discount is not applied with less than 6 processors")
    @ParameterizedTest
    @IntRangeSource(from = 1, to = 6)
    void testFiveProcessorsDiscountNotApplying(int processorCount) {
        var items = generateItems(ItemType.PROCESSOR, processorCount, 10).toList();
        var expectedTotal = items.stream().mapToDouble(EItem::price).sum();
        assertEquals(expectedTotal, bill.getOrderPrice(items, adultUser, orderTime));
    }

    @DisplayName(">5 processors discount is applied with at least 6 processors")
    @ParameterizedTest
    @IntRangeSource(from = 6, to = 15)
    void testFiveProcessorsDiscountApplying(int processorCount) {
        var items = generateItems(ItemType.PROCESSOR, processorCount, 5).toList();
        var expectedTotal = items.stream().mapToDouble(EItem::price).sum()
                - (items.stream().mapToDouble(EItem::price).min().orElseThrow() / 2);
        assertEquals(expectedTotal, bill.getOrderPrice(items, adultUser, orderTime));
    }

    @DisplayName("mouse/keyboard combo gift is not applied with different number of m/k")
    @ParameterizedTest
    @CsvSource(value = {
            "1,2",
            "2,3",
            "3,2",
            "2,1"
    })
    void testMKComboGiftNotApplying(int mousesCount, int keyboardsCount) {
        var items = Stream.concat(generateItems(ItemType.MOUSE, mousesCount, 5),
                generateItems(ItemType.KEYBOARD, keyboardsCount, 10)).toList();
        var expectedTotal = items.stream().mapToDouble(EItem::price).sum();
        assertEquals(expectedTotal, bill.getOrderPrice(items, adultUser, orderTime));
    }

    @DisplayName("mouse/keyboard combo gift is applied with same number of m/k and no mouse gift")
    @ParameterizedTest
    @IntRangeSource(from = 1, to = 11)
    void testMKComboGiftApplyingWithoutMouseGift(int count) {
        final var mousePriceMultiplier = 5;
        final var keyboardPriceMultiplier = 10;
        var items = Stream.of(generateItems(ItemType.MOUSE, count, mousePriceMultiplier),
                generateItems(ItemType.KEYBOARD, count, keyboardPriceMultiplier),
                generateItems(ItemType.PROCESSOR, 2, 15)).flatMap(s -> s).toList();
        var expectedTotal = items.stream().mapToDouble(EItem::price).sum()
                - items.stream().mapToDouble(EItem::price).min().orElseThrow();
        assertEquals(expectedTotal, bill.getOrderPrice(items, adultUser, orderTime));
    }

    @DisplayName("mouse/keyboard combo gift applied with mouse gift, gifting 2 mouses")
    @ParameterizedTest
    @IntRangeSource(from = 11, to = 15)
    void testMKComboGiftApplyingWithDoubleMouseGift(int count) {
        final var mousePriceMultiplier = 2;
        final var keyboardPriceMultiplier = 5;
        var items = Stream.concat(generateItems(ItemType.MOUSE, count, mousePriceMultiplier),
                generateItems(ItemType.KEYBOARD, count, keyboardPriceMultiplier)).toList();
        var expectedTotal = items.stream().mapToDouble(EItem::price).sum()
                - generateItems(ItemType.MOUSE, count, mousePriceMultiplier)
                .mapToDouble(EItem::price).sorted().limit(2).sum();
        assertEquals(expectedTotal, bill.getOrderPrice(items, adultUser, orderTime));
    }

    @DisplayName("mouse/keyboard combo gift applied with mouse gift, gifting a mouse < keyboard")
    @ParameterizedTest
    @IntRangeSource(from = 11, to = 15)
    void testMKComboGiftApplyingWithMouseAndKeyboardGiftCheaperMouse(int count) {
        final var mousePriceMultiplier = 3;
        final var keyboardPriceMultiplier = 5;
        var items = Stream.concat(generateItems(ItemType.MOUSE, count, mousePriceMultiplier),
                generateItems(ItemType.KEYBOARD, count, keyboardPriceMultiplier)).toList();
        var expectedTotal = items.stream().mapToDouble(EItem::price).sum()
                - generateItems(ItemType.MOUSE, count, mousePriceMultiplier)
                .mapToDouble(EItem::price).min().orElseThrow()
                - generateItems(ItemType.KEYBOARD, count, keyboardPriceMultiplier)
                .mapToDouble(EItem::price).min().orElseThrow();
        assertEquals(expectedTotal, bill.getOrderPrice(items, adultUser, orderTime));
    }

    @DisplayName("mouse/keyboard combo gift applied with mouse gift, gifting a mouse > keyboard")
    @ParameterizedTest
    @IntRangeSource(from = 11, to = 15)
    void testMKComboGiftApplyingWithMouseAndKeyboardGiftCheaperKeyboard(int count) {
        final var mousePriceMultiplier = 5;
        final var keyboardPriceMultiplier = 3;
        var items = Stream.concat(generateItems(ItemType.MOUSE, count, mousePriceMultiplier),
                generateItems(ItemType.KEYBOARD, count, keyboardPriceMultiplier)).toList();
        var expectedTotal = items.stream().mapToDouble(EItem::price).sum()
                - generateItems(ItemType.MOUSE, count, mousePriceMultiplier)
                .mapToDouble(EItem::price).min().orElseThrow()
                - generateItems(ItemType.KEYBOARD, count, keyboardPriceMultiplier)
                .mapToDouble(EItem::price).min().orElseThrow();
        assertEquals(expectedTotal, bill.getOrderPrice(items, adultUser, orderTime));
    }
    @DisplayName("order total <= 1000 doesn't get discounted")
    @ParameterizedTest
    @DoubleRangeSource(from=100, to=1001, step = 100)
    void testNoTotalDiscount(double itemPrice) {
        assertEquals(itemPrice, bill.getOrderPrice(
            List.of(new EItem(ItemType.KEYBOARD, "foo", itemPrice)),
            adultUser,
            orderTime
        ));
    }

    @DisplayName("order total > 1000 gets discounted")
    @ParameterizedTest
    @DoubleRangeSource(from=1001, to=2000, step=100)
    void testTotalDiscount(double itemPrice) {
        assertEquals(itemPrice * 0.9, bill.getOrderPrice(
            List.of(new EItem(ItemType.KEYBOARD, "foo", itemPrice)),
            adultUser,
            orderTime
        ));
    }

    @DisplayName("order with <= 30 item gets accepted")
    @ParameterizedTest
    @IntRangeSource(from = 1, to = 30)
    void testItemLimitAccepted(int itemCount) {
        bill.getOrderPrice(generateItems(ItemType.KEYBOARD, itemCount, 1).toList(), adultUser, orderTime);
    }

    @DisplayName("order with <= 30 item gets rejected")
    @ParameterizedTest
    @IntRangeSource(from = 31, to = 40)
    void testItemLimitRejected(int itemCount) {
        assertThrows(BillException.class,
                () -> bill.getOrderPrice(generateItems(ItemType.KEYBOARD, itemCount, 1).toList(), adultUser, orderTime)
        );
    }

    @DisplayName("order total >= 10 doesn't get commission added")
    @ParameterizedTest
    @IntRangeSource(from=10, to=100, step = 2)
    void testNoCommissionOverTotal10(double itemPrice) {
        assertEquals(itemPrice, bill.getOrderPrice(
            List.of(new EItem(ItemType.KEYBOARD, "foo", itemPrice)),
            adultUser,
            orderTime
        ));
    }

    @DisplayName("order total < 10 gets commission added")
    @ParameterizedTest
    @IntRangeSource(from=1, to=10)
    void testCommissionUnderTotal10(double itemPrice) {
        assertEquals(itemPrice + 2, bill.getOrderPrice(
            List.of(new EItem(ItemType.KEYBOARD, "foo", itemPrice)),
            adultUser,
            orderTime
        ));
    }

    private void generateOrders(int underAgeOrders, int adultOrders, boolean sameUser) {
        List<EItem> items = List.of(new EItem(ItemType.MOTHERBOARD, "foo", 1));
        IntStream.range(0, underAgeOrders).forEach(
                i -> bill.getOrderPrice(items, sameUser ? underageUser : new User(true), underageGiftOrderTime)
        );
        IntStream.range(0, adultOrders).forEach(
                i -> bill.getOrderPrice(items, sameUser ? adultUser : new User(false), underageGiftOrderTime)
        );
    }
    @DisplayName("no orders are gifted if there aren't orders from underage users")
    @ParameterizedTest
    @IntRangeSource(from = 1, to = 10)
    void testNoUnderageGiftIfOnlyAdultUsers(int adultOrders) {
        generateOrders(0, adultOrders, false);
        verify(mockedRandom, Mockito.never()).nextBoolean();
        assertEquals(0, BillImpl.getUnderageGiftCount());
    }

    @DisplayName("no orders are gifted to underage users if outside time range")
    @ParameterizedTest
    @MethodSource("generateTimesOutsideRange")
    void testNoUnderageGiftOutsideTimeRange(LocalTime orderTime) {
        var items = generateItems(ItemType.MOTHERBOARD, 1, 1).toList();
        bill.getOrderPrice(items, underageUser, orderTime);
        verify(mockedRandom, Mockito.never()).nextBoolean();
        assertEquals(0, BillImpl.getUnderageGiftCount());
    }
    public static Stream<Arguments> generateTimesOutsideRange() {
        return Stream.of(
                Arguments.of(LocalTime.of(0, 0)),
                Arguments.of(LocalTime.of(10, 0)),
                Arguments.of(LocalTime.of(15, 0)),
                Arguments.of(LocalTime.of(17, 0)),
                Arguments.of(LocalTime.of(19, 0)),
                Arguments.of(LocalTime.of(21, 0)),
                Arguments.of(LocalTime.of(23, 30))
        );
    }

    @DisplayName("orders are gifted to underage users if inside time range")
    @ParameterizedTest
    @MethodSource("generateTimesInsideRange")
    void testUnderageGiftInsideTimeRange(LocalTime orderTime) {
        var items = generateItems(ItemType.MOTHERBOARD, 1, 1).toList();
        when(mockedRandom.nextBoolean()).thenReturn(true);
        bill.getOrderPrice(items, underageUser, orderTime);
        verify(mockedRandom, times(1)).nextBoolean();
        assertEquals(1, BillImpl.getUnderageGiftCount());
    }
    public static Stream<Arguments> generateTimesInsideRange() {
        return Stream.of(
                Arguments.of(LocalTime.of(18, 0)),
                Arguments.of(LocalTime.of(18, 0, 1)),
                Arguments.of(LocalTime.of(18, 10)),
                Arguments.of(LocalTime.of(18, 20)),
                Arguments.of(LocalTime.of(18, 30)),
                Arguments.of(LocalTime.of(18, 40)),
                Arguments.of(LocalTime.of(18, 50)),
                Arguments.of(LocalTime.of(18, 59, 59))
        );
    }

    @DisplayName("Same underage user can't be gifted twice in the same day")
    @Test
    void testSingleGiftSameUnderageUser() {
        when(mockedRandom.nextBoolean()).thenReturn(true);
        generateOrders(10, 0, true);
        verify(mockedRandom, times(1)).nextBoolean();
        assertEquals(1, BillImpl.getUnderageGiftCount());
    }

    @DisplayName("Allow less than 10 underage gifts per day")
    @ParameterizedTest
    @IntRangeSource(from = 1, to = 10)
    void testUnderageGiftsUnder10(int orderCount) {
        when(mockedRandom.nextBoolean()).thenReturn(true);
        generateOrders(orderCount, 0, false);
        verify(mockedRandom, times(orderCount)).nextBoolean();
        assertEquals(orderCount, BillImpl.getUnderageGiftCount());
    }

    @DisplayName("Limit underage gifts to 10 per day")
    @ParameterizedTest
    @IntRangeSource(from = 11, to = 20)
    void testUnderageGiftsLimit10(int orderCount) {
        when(mockedRandom.nextBoolean()).thenReturn(true);
        generateOrders(orderCount, 0, false);
        verify(mockedRandom, times(10)).nextBoolean();
        assertEquals(10, BillImpl.getUnderageGiftCount());
    }

    @DisplayName("No underage gift when nextBoolean returns false")
    @Test
    void testNoUnderageGiftOnRandFalse() {
        when(mockedRandom.nextBoolean()).thenReturn(false);
        generateOrders(1, 0, false);
        verify(mockedRandom, times(1)).nextBoolean();
        assertEquals(0, BillImpl.getUnderageGiftCount());
    }
}
