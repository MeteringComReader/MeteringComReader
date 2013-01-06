/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

/**
 * Reprezentuje abstrakcyjny obiekt sesji w bieżącym połączeniu 
 * z koncentratorem.
 * @author Juliusz Jezierski
 */

abstract public class MeteringSession {
    
    /**
     * Obiekt połączenia z koncentratorem.
     */
    protected HubConnection hc;

    /**
     * Inicjuje abstrakcyjny obiekt sesji ustawiając połączenie z koncentratorem.
     * i wysyłając polecenie rozpoczynające abstrakcyjną sesję
     * @param hc bieżące połączenie z koncentratorem
     * @param command  polecenie rozpoczynające abstrakcyjną sesję
     * @throws MeteringSessionException zgłaszany w przypadku niepowodzenia
     * wysłania polecenia lub odebranie potwierdzenia rozpoczęcia abstrakcyjnej
     * sesji
     */
    public MeteringSession(HubConnection hc, int command) throws MeteringSessionException{
            this.hc=hc;
            hc.sendCommand(command);
            hc.receiveAck(command);
    }
    
    /**
     * Inicjuje abstrakcyjny obiekt sesji ustawiając połączenie z koncentratorem.
     * @param hc ustawia bieżące połączenie z koncentratorem
     */
    public MeteringSession(HubConnection hc) {
            this.hc=hc;
    }
    
    /**
     * Zamyka połączenie abstrakcyjnej sesji.
     * @throws MeteringSessionException zgłaszany w przypadku niepowodzenia
     * wysłania polecenia lub odebrania potwierdzenia zakończenia sesję 
     */
    abstract public void close() throws MeteringSessionException;
    
    /**
     * Pobiera kolejny pakiet danych w abstrakcyjnej sesji.
     * @return pobrany pakiet danych
     * @throws MeteringSessionException zwracany w przypadku niepowodzenia
     * pobrania pakietu danych
     */
    abstract public DataPacket getNextPacket() throws MeteringSessionException;
    
    /**
     * Ponownie pobiera kolejny pakiet danych w abstrakcyjnej sesji.
     * @return pobrany pakiet danych
     */
    abstract public DataPacket regetPrevPacket();
     
    
}
