package cm.univ.library.loan.dto;

/** Outcome chosen by the librarian when processing a return at the desk.
 *  Not persisted directly — drives how {@code LoanService.finishReturn} updates
 *  the loan/copy status and whether a lost/damage fine is created. */
public enum ReturnOutcome {
    NORMAL,
    LOST,
    DAMAGED
}
