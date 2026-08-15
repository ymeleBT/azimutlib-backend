package cm.univ.library.notification;

/** Abstraction over the SMS provider so a real gateway (e.g. a local Cameroonian
 *  aggregator) can be plugged in later without touching call sites. */
public interface SmsGateway {
    void send(String phoneNumber, String message);
}
