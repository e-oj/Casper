package Exceptions;

/**
 * @author Emmanuel Olaojo
 * @since 6/2/16
 */
public class InvalidFormatException extends Exception{
    public InvalidFormatException (String fileName) {
        super(fileName + " has an invalid css format");
    }
}
