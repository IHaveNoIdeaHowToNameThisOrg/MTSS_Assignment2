////////////////////////////////////////////////////////////////////
// Augusto Zanellato 2000555
// Stefano Zanovello 2008459
////////////////////////////////////////////////////////////////////
package it.unipd.mtss.model.exception;

/**
 * Base class for all the project exceptions
 */
public class EException extends RuntimeException {
    EException(String message) {
        super(message);
    }
}
