////////////////////////////////////////////////////////////////////
// Augusto Zanellato 2000555
// Stefano Zanovello 2008459
////////////////////////////////////////////////////////////////////
package it.unipd.mtss.model;

import it.unipd.mtss.model.exception.ItemException;

public record EItem(ItemType itemType, String name, double price) {
    public EItem {
        if (price <= 0) {
            throw new ItemException("Item price must be >0");
        }
    }
}
