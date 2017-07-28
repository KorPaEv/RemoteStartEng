package online.pins24.remotestartengine;

public final class InputSms
{
    String number;
    String message;

    public InputSms (String number, String message) {
        this.number = number;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getNumber() {
        return number;
    }
}