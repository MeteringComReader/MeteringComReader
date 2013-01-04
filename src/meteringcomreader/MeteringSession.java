/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

/**
 *
 * @author Juliusz
 */
abstract public class MeteringSession {
    protected HubConnection hc;

    
    public MeteringSession(HubConnection hc, int command) throws MeteringSessionException{
            this.hc=hc;
            hc.sendCommand(command);
            hc.receiveAck(command);
    }
    
    public MeteringSession(HubConnection hc) throws MeteringSessionException{
            this.hc=hc;
    }
    
    abstract public void close() throws MeteringSessionException;
    
    abstract public DataPacket getNextPacket() throws MeteringSessionException;
    
    abstract public DataPacket regetPrevPacket();
     
    
}
