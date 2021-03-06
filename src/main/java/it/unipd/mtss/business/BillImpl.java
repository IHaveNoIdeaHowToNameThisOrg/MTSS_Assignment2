////////////////////////////////////////////////////////////////////
// Augusto Zanellato 2000555
// Stefano Zanovello 2008459
////////////////////////////////////////////////////////////////////
package it.unipd.mtss.business;

import it.unipd.mtss.model.EItem;
import it.unipd.mtss.model.ItemType;
import it.unipd.mtss.model.User;
import it.unipd.mtss.model.exception.BillException;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.OptionalDouble;
import java.util.stream.Stream;

public class BillImpl implements Bill{

    private static final int MAX_ITEM_COUNT = 30;
    private static final int MIN_PROCESSORS_FOR_DISCOUNT = 5;
    private static final double MIN_TOTAL_DISCOUNT = 1000;
    private static final double TOTAL_DISCOUNT_AMOUNT = 0.1;
    private static final int MIN_MOUSES_FOR_DISCOUNT = 10;
    private static final double MIN_TOTAL_WITHOUT_COMMISSION = 10;
    private static final double SMALL_ORDER_COMMISSION = 2;

    private static final int MAX_UNDERAGE_GIFTS_PER_DAY = 10;
    private static final LocalTime UNDERAGE_GIFT_START_TIME = LocalTime.of(18, 0);
    private static final LocalTime UNDERAGE_GIFT_END_TIME = LocalTime.of(19, 0);

    private static int underageGiftCount = 0;
    private static final Set<User> giftedUsers = new HashSet<>();

    private final Random rnd;

    public BillImpl(Random rnd) {
        this.rnd = rnd;
    }

    private static Stream<EItem> filteredItems(List<EItem> itemsOrdered, ItemType type) {
        return itemsOrdered.stream().filter(item -> item.itemType() == type);
    }

    public static int getUnderageGiftCount() {
        return underageGiftCount;
    }

    public static void resetUnderageGift() {
        underageGiftCount = 0;
    }

    private boolean checkUnderageGift(User user, LocalTime orderTime) {
        return user.isUnderage()
                && underageGiftCount < MAX_UNDERAGE_GIFTS_PER_DAY
                && !giftedUsers.contains(user)
                && (
                orderTime.isAfter(UNDERAGE_GIFT_START_TIME)
                        || orderTime.equals(UNDERAGE_GIFT_START_TIME)
        ) && orderTime.isBefore(UNDERAGE_GIFT_END_TIME)
                && rnd.nextBoolean();
    }

    @Override
    public double getOrderPrice(List<EItem> itemsOrdered, User user, LocalTime orderTime) throws BillException {
        if(itemsOrdered.isEmpty()){
            throw new BillException("Order can't be empty");
        }

        if (itemsOrdered.size() > MAX_ITEM_COUNT) {
            throw new BillException("Order can't contain more than " + MAX_ITEM_COUNT + " elements");
        }

        if (checkUnderageGift(user, orderTime)) {
            underageGiftCount++;
            giftedUsers.add(user);
            return 0;
        }

        var orderTotal = itemsOrdered.stream().mapToDouble(EItem::price).sum();

        if (filteredItems(itemsOrdered, ItemType.PROCESSOR).count() > MIN_PROCESSORS_FOR_DISCOUNT) {
            // this can't throw because we already checked that there are more than 5 processors.
            orderTotal -= filteredItems(itemsOrdered, ItemType.PROCESSOR)
                    .mapToDouble(EItem::price)
                    .min().orElseThrow() / 2;
        }

        OptionalDouble giftedMouse = OptionalDouble.empty();
        final var mouseCount = filteredItems(itemsOrdered, ItemType.MOUSE).count();
        if (mouseCount > MIN_MOUSES_FOR_DISCOUNT) {
            // this can't throw because we already checked that there are more than 10 mouses.
            giftedMouse = OptionalDouble.of(filteredItems(itemsOrdered, ItemType.MOUSE)
                    .mapToDouble(EItem::price)
                    .min().orElseThrow());
            orderTotal -= giftedMouse.getAsDouble();
        }
        if (mouseCount != 0 && mouseCount == filteredItems(itemsOrdered, ItemType.KEYBOARD).count()) {
            var giftPicks = itemsOrdered.stream()
                    .filter(item -> item.itemType() == ItemType.MOUSE || item.itemType() == ItemType.KEYBOARD)
                    .mapToDouble(EItem::price)
                    .sorted()
                    .limit(2)
                    .toArray();
            // gift second pick if a mouse was gifted and it is the first pick
            orderTotal -= giftPicks[giftedMouse.isPresent() && giftPicks[0] == giftedMouse.getAsDouble() ? 1 : 0];
        }
        if (orderTotal > MIN_TOTAL_DISCOUNT) {
            orderTotal *= 1 - TOTAL_DISCOUNT_AMOUNT;
        }

        if (orderTotal < MIN_TOTAL_WITHOUT_COMMISSION) {
            orderTotal += SMALL_ORDER_COMMISSION;
        }
        return orderTotal;
    }


}
