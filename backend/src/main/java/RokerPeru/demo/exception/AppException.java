package RokerPeru.demo.exception;

public abstract class AppException extends RuntimeException{
    protected AppException(String mensaje){
        super(mensaje);
    }
    protected AppException(String mensaje, Throwable causa){
        super(mensaje, causa);
    }
}
