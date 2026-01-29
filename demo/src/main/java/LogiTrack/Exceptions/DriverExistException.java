package LogiTrack.Exceptions;

public class DriverExistException extends  RuntimeException{
    public DriverExistException(String message){
        super(message);
    }
}
