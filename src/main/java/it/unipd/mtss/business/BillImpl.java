////////////////////////////////////////////////////////////////////
// Augusto Zanellato 2000555
// Stefano Zanovello 2008459
////////////////////////////////////////////////////////////////////
package it.unipd.mtss.business;

import it.unipd.mtss.model.EItem;
import it.unipd.mtss.model.User;
import it.unipd.mtss.model.exception.BillException;

import java.util.List;

public class BillImpl implements Bill{

    @Override
    public double getOrderPrice(List<EItem> itemsOrdered, User user) throws BillException {
        if(itemsOrdered.isEmpty()){
            throw new BillException("Order can't be empty");
        }
        return itemsOrdered.stream().mapToDouble(EItem::price).sum();
    }


}
