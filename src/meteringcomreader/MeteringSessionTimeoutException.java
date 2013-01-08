/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

/**
 * Wyjątek zgłaszany w przypadku przekroczenia czasu oczekiwania 
 * {@link Utils#TIMEOUT} na odczyt ze strumienia, służącego do odbierania danych 
 * z połączenia z koncentratorem.
 * @author Juliusz Jezierski
 */
public class MeteringSessionTimeoutException extends MeteringSessionException{
    public MeteringSessionTimeoutException(String msg){
        super(msg);
    }
    
}
