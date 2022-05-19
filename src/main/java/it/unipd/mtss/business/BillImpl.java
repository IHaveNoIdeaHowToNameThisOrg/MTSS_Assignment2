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

    private static final int MIN_PROCESSORS_FOR_DISCOUNT = 5;

    private static Stream<EItem> filteredItems(List<EItem> itemsOrdered, ItemType type) {
        return itemsOrdered.stream().filter(item -> item.itemType() == type);
    }

    @Override
    public double getOrderPrice(List<EItem> itemsOrdered, User user) throws BillException {
        if(itemsOrdered.isEmpty()){
            throw new BillException("Order can't be empty");
        }
        var orderTotal = itemsOrdered.stream().mapToDouble(EItem::price).sum();

        if (filteredItems(itemsOrdered, ItemType.PROCESSOR).count() > MIN_PROCESSORS_FOR_DISCOUNT) {
            // this can't throw because we already checked that there are more than 5 processors.
            orderTotal -= filteredItems(itemsOrdered, ItemType.PROCESSOR)
                    .mapToDouble(EItem::price)
                    .min().orElseThrow() / 2;
        }
        final var mouseCount = filteredItems(itemsOrdered, ItemType.MOUSE).count();
        if (mouseCount > 10) {
            // this can't throw because we already checked that there are more than 10 mouses.
            orderTotal -= filteredItems(itemsOrdered, ItemType.MOUSE)
                    .mapToDouble(EItem::price)
                    .min().orElseThrow();
        }
        return orderTotal;
    }


}
