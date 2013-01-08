package meteringcomreader;

/**
 * Wyjątek zgłaszany w przypadku niepowodzenia wykonania operacji
 * na koncentratorze.
 * @author Juliusz Jezierski
 */
public class MeteringSessionException extends Exception {
    
    public MeteringSessionException(String msg){
        super(msg);
    }
        public MeteringSessionException(Throwable ex){
        super(ex);
    }
}
