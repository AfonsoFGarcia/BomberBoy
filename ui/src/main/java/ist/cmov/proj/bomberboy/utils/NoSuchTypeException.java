package ist.cmov.proj.bomberboy.utils;

/**
 * Created by agfrg on 26/04/14.
 */
public class NoSuchTypeException extends Exception {
    private char type;

    public NoSuchTypeException(char type) {
        this.type = type;
    }

    public String getMessage() {
        return "The type " + type + " does not exist!";
    }
}
