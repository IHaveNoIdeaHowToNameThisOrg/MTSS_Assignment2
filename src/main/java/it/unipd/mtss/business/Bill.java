////////////////////////////////////////////////////////////////////
// Augusto Zanellato 2000555
// Stefano Zanovello 2008459
////////////////////////////////////////////////////////////////////
package it.unipd.mtss.business;

import it.unipd.mtss.model.EItem;
import it.unipd.mtss.model.User;

import java.time.LocalTime;
import java.util.List;

public interface Bill {
    double getOrderPrice(List<EItem> itemsOrdered, User user, LocalTime purchaseTime);
}
