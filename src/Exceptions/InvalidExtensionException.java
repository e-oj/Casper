package Exceptions;

/**
 * @author Emmanuel Olaojo
 * @since 6/1/16
 */
public class InvalidExtensionException extends Exception {
    public InvalidExtensionException(String fileName){
        super(fileName + " has an unsupported extension");
    }
}
