package Exceptions;

/**
 * @author Emmanuel Olaojo
 * @since 6/2/16
 */
public class InvalidSyntaxException extends Exception{
    public InvalidSyntaxException(String fileName) {
        super(fileName + " has an invalid css syntax");
    }
}
