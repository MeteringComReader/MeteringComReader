package meteringcomreader;

/**
 * Wyjątek zgłaszany w przypadku niepowodzenia wykonania operacji
 * wejścia-wyjścia na strumieniu  służącemu do odbierania danych 
 * z połączenia z koncentratorem (SP - serial port).
 * @author Juliusz Jezierski
 */
class MeteringSessionSPException extends MeteringSessionException{

    public MeteringSessionSPException(Throwable ex) {
        super(ex);
    }

    public MeteringSessionSPException(String msg) {
        super(msg);
    }


    
}
