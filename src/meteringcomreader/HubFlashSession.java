package meteringcomreader;

import meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Timestamp;
import meteringcomreader.exceptions.MeteringSessionOperationAlreadyInProgressException;
import meteringcomreader.exceptions.MeteringSessionTimeoutException;

/**
 * Reprezentuje sesję odczytu danych z pamięci flash koncentratora.
 * @author Juliusz Jezierski
 */
public class HubFlashSession extends MeteringSession{
    
    /**
     * Tablica odebranych w sesji pakietów danych, 
     * przechowuje pakiety między kolejnymi wywołaniami
     * {@link #getNextPacket() }. 
     */
    protected byte[] packets=null;
    /*
     * Licznik pobranych pakietów przez {@link #getNextPacket() }
     * z tablice {@link #packets}.
     */
//    protected int packetsCounter=0;
    
    /**
     * Licznik pobranych bytów danych przez {@link #getNextPacket() }
     * z tablicy {@link #packets}.
     */
    protected int bytesCounter=0;
    /**
     * Czy odczytano wszystkie dane z koncentratora?
     * Informacja przekazywana między kolejnymi wywołaniami {@link #getNextPacket() }
     */
    protected boolean resultReaded=false;
    /**
     * Wielkość strony pamięci flash koncentratora. Strona jest jednostką odczytu
     * danych z koncentratora.
     */
    protected int flashPageSize;

    /**
     * Tworzy sesję odczytu danych z pamięci flash koncentratora, w ramach połączenia
     * z koncentratorem <code>hc</code>, odczytując dane zarejestrowane od czasu
     * <code>time</code>.
     * @param hc obiekt połączenia z koncentratorem, w ramach którego jest tworzona
     * sesja
     * @param time  czas, od którego zarejestrowane dane w koncentratorze mają
     * być odczytane w tworzonej sesji
     * @throws MeteringSessionException zgłaszany w przypadku błędu komunikacji 
     * z koncentratorem
     */
    HubFlashSession(HubConnection hc, Timestamp time) throws MeteringSessionException {
        super(hc);
        if (time==null)
            throw new MeteringSessionException("HubFlsahSession Starttime can't be null.");
        long timeInt=Utils.timestamp2int(time);
        byte[] timeBytes=Utils.long2bytes(timeInt, 4);
        hc.sendCommand(Utils.startHubFlashSessionReq, timeBytes);
        byte data[]=hc.receiveAck(Utils.startHubFlashSessionRes);
        flashPageSize=(data[0]+1)*128;
        ComResp.setResSize(Utils.getNextHubFlashSessionReq, flashPageSize); //TODO: generalize to many hubs
    }

    /**
     * Pobiera kolejny pakiet danych loggera z pamięci flash koncentratora.
     * @return kolejny pakiet danych loggera z pamięci flash koncentratora lub null 
     * w przypadku pobrania już wszystkich pakietów.
     * @throws MeteringSessionException zgłaszany w przypadku błędu  komunikacji 
     * z koncentratorem lub poprzednie wywołanie zwróciło null sygnalizując,
     * że wszystkie pakiety zostały wcześniej odczytane.
     */
    @Override
    public DataPacket getNextPacket() throws MeteringSessionException {
        DataPacket dp=null;
        while(dp==null){
            if (resultReaded)
                throw new MeteringSessionException("All data already readed in flash hub session");
            if(packets==null){
                hc.sendCommand(Utils.getNextHubFlashSessionReq);
                ComResp rs = hc.crd.getNextResp();
                int errCode = rs.receiveAckWithErrCode(Utils.getNextHubFlashSessionRes);
                if (errCode==0xF){
                    resultReaded=true;
                    return null;                
                }
                else if(errCode!=0){
                    throw new MeteringSessionException("Exception number:"+errCode+" for request:"+Utils.getNextHubFlashSessionRes); 
                }
                packets = rs.receiveData();
                bytesCounter=0;
            }
            while(dp==null && packets!=null){
                if (bytesCounter+4>=this.flashPageSize){
                    packets=null;
                    continue;                    
                }
                int frameTime= (int)Utils.bytes2long(packets, bytesCounter, 4);
                bytesCounter+=4;
                if (frameTime==0xFFFFFFFF){
                    packets=null;
                    continue;
                }
                int frameSize=packets[bytesCounter];
                bytesCounter++;
                if (frameSize==DataPacket.LEN){
                    dp=new DataPacket(packets, bytesCounter);
//                    packetsCounter++;
                }
                bytesCounter+=frameSize;
            }
        }
        return dp;
    }

    /**
     * Niezaimplementowana.
     * @return UnsupportedOperationException
     */
    @Override
    public DataPacket regetPrevPacket() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Zamyka sesję odczytu danych z pamięci flash koncentratora.
     * @throws MeteringSessionException 
     */
    @Override
    public void close() throws MeteringSessionException {
            hc.sendCommand(Utils.closeHubFlashSessionReq);
            hc.receiveAck(Utils.closeHubFlashSessionRes);
           }        
    
}
