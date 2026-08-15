package cm.univ.library.common.exception;

/** Thrown when a request is well-formed but violates a library policy/business rule
 *  (e.g. borrow limit reached, book already reserved by someone else). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
