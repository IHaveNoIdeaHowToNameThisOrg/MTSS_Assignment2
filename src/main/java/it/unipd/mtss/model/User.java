////////////////////////////////////////////////////////////////////
// Augusto Zanellato 2000555
// Stefano Zanovello 2008459
////////////////////////////////////////////////////////////////////
package it.unipd.mtss.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class User {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    private final int id = ID_GENERATOR.incrementAndGet();
    private final boolean isUnderage;

    public User(boolean isUnderage) {
        this.isUnderage = isUnderage;
    }

    public boolean isUnderage() {
        return isUnderage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
