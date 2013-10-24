/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.exceptions;

/**
 * Urządzenie zajęte, nie można wykonać operacji, np. gdy chcemy rozpocząć sesję danych a koncentrator 
akurat w tym samym czasie po naciśnięciu przycisku użytkownika próbuje dopisać sprzętowo 
logera, należy spróbować jeszcze raz po kilku sekundach, kod 2

 * @author Juliusz Juliuz Jezierski
 */
public class MeteringSessionDeviceBusyException extends MeteringSessionException{
    public MeteringSessionDeviceBusyException(){
        this (I18nHelper.getI18nMessage("DeviceBusy"));        
    }
        public MeteringSessionDeviceBusyException(String msg){
        super(msg);
    }
        public MeteringSessionDeviceBusyException(Throwable ex){
        super(ex);
    }
}
