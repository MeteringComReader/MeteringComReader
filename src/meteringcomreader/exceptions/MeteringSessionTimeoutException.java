/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 * Wyjątek zgłaszany w przypadku przekroczenia czasu oczekiwania 
 * {@link Utils#TIMEOUT} na odczyt ze strumienia, służącego do odbierania danych 
 * z połączenia z koncentratorem. 
 * Zgłaszany również w przypadku gdy logger nie odpowiada w zadanym czasie po IR, na razie 1s
 * @author Juliusz Jezierski
 */
public class MeteringSessionTimeoutException extends MeteringSessionException{
    
    public MeteringSessionTimeoutException(){
        this (I18nHelper.getI18nMessage("Timeout"));            
    }
    public MeteringSessionTimeoutException(String msg){
        super(msg);
    }
    
}
