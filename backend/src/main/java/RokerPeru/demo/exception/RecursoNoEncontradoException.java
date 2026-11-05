package RokerPeru.demo.exception;

public class RecursoNoEncontradoException extends AppException {
    public RecursoNoEncontradoException(String recurso, Object id){
        super(recurso + "con id " + id + " no existe ");
    }
}
