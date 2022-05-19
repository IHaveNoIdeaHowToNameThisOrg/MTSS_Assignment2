////////////////////////////////////////////////////////////////////
// Augusto Zanellato 2000555
// Stefano Zanovello 2008459
////////////////////////////////////////////////////////////////////
package it.unipd.mtss.business;

import it.unipd.mtss.model.EItem;
import it.unipd.mtss.model.ItemType;
import it.unipd.mtss.model.User;
import it.unipd.mtss.model.exception.BillException;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;

public class BillImpl implements Bill{

    private static final int MAX_ITEM_COUNT = 30;
    private static final int MIN_PROCESSORS_FOR_DISCOUNT = 5;
    private static final double MIN_TOTAL_DISCOUNT = 1000;
    private static final double TOTAL_DISCOUNT_AMOUNT = 0.1;

    private static final int MIN_MOUSES_FOR_DISCOUNT = 10;

    private static Stream<EItem> filteredItems(List<EItem> itemsOrdered, ItemType type) {
        return itemsOrdered.stream().filter(item -> item.itemType() == type);
    }

    @Override
    public double getOrderPrice(List<EItem> itemsOrdered, User user) throws BillException {
        if(itemsOrdered.isEmpty()){
            throw new BillException("Order can't be empty");
        }

        if (itemsOrdered.size() > MAX_ITEM_COUNT) {
            throw new BillException("Order can't contain more than " + MAX_ITEM_COUNT + " elements");
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
        return orderTotal;
    }


}
