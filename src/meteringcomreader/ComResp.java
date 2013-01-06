
package meteringcomreader;
import java.util.HashMap;

/**
 *
 * @author Juliusz Jezierski
 */



public class ComResp {
    
    /**
     * Opisuje obiekt odpowiedź z koncentratora za pomocą pól resp i data, zawiera
     * również rozmiar parametrów odpowiedzi w resSizes.
     */
    static  protected HashMap 
        <Integer, Integer> resSizes = new HashMap<Integer, Integer>(50);


    /**
     * Ustawia rozmiar parametrów odpowiedzi.
     * @param res kod odpowiedzi
     * @param packetSize rozmiar parametrów odpowiedzi
     */
    static void setResSize(int res, int packetSize) {
        resSizes.remove(res);
        resSizes.put(res, packetSize)  ;      
    }
        
    /**
     * Kod odpowiedzi.
     */
    protected int resp;
    
    /**
     * Parametry odpowiedzi.
     */
    protected  byte[]data;
        
    /**
     * Konstruuje obiekt odpowiedzi.
     * @param resp kod odpowiedzi
     * @param data parametry odpowiedzi
     */
    public ComResp(int resp, byte[]data){
       this.resp=resp;
       this.data=data;
    }
   
    /**
     * Zwraca rozmiar parametrów odpowiedzi dla danego kodu odpowiedzi.
     * @param res kod odpowiedzi
     * @return rozmiar parametrów odpowiedzi
     * @throws MeteringSessionException zgłaszany w przypadku nie znalezienia kodu odpowiedzi
     */
    static int getResDataSize(int res) throws MeteringSessionException{        
        Integer sizeInt=resSizes.get(res&0x0FFF);  //ignore error code
        if (sizeInt==null){
            sizeInt=resSizes.get(res);             //if oldest part is not error code
            if (sizeInt==null)
                throw new MeteringSessionException("Unexpected response:"+res);
        }
        else if(res>0x0FFF)                 //if oldest part is error code
             return 0;       
        
        return sizeInt.intValue()
                &0xFFFF;   //double checking
    }

    
static{    
    resSizes.put(Utils.hubIdentifictionAck, 4); //start hub session res
    resSizes.put(Utils.startRadioSessionRes, 0); //start radio session res
    resSizes.put(Utils.startLoggerFlashRes, 5); //start logger flash session res
    resSizes.put(Utils.startHubFlashSessionRes, 1); //start hub flash session res
    resSizes.put(Utils.closeAllSessionRes, 0); //stop hub session res
    resSizes.put(Utils.closeRadioSessionRes, 0); //stop radio session res
    resSizes.put(Utils.closeLoggerFlashSessionRes, 0); //stop logger flash session res
    resSizes.put(Utils.closeHubFlashSessionRes, 0); //stop hub flash session res
    resSizes.put(Utils.isHubPoweredAfterSessionRes, 1); //is hub powered after session res
    resSizes.put(Utils.setHubPoweredAfterSessionFalseRes, 0); //unset powered hub after session res
    resSizes.put(Utils.setHubPoweredAfterSessionTrueRes, 0); //set powered hub after session res
    resSizes.put(Utils.radioSessionRes, DataPacket.LEN); //receive next data packet in radio session
//    resSizes.put(0x010A, ???); //receive next data packet in logger flash session
    //jeżeli bład to zero
//    resSizes.put(0x020A, ???); //receive next data packet in hub flash session
//    resSizes.put(0x010B, ???); //receive again next data packet in logger flash session
    //jeżeli bład to zero
//    resSizes.put(0x020B, ???); //receive agin next data packet in hub flash session
//    resSizes.put(0x0005, ???); //do ustalenia
    resSizes.put(Utils.enableIntervalHubFlashMemModeAck,0);
    resSizes.put(Utils.enableOverwriteHubFlashMemModeAck,0);
    resSizes.put(Utils.disableIntervalHubFlashMemModeAck,0);
    resSizes.put(Utils.disableOverwriteHubFlashMemModeAck,0);
    resSizes.put(Utils.getPeriodIntervalHubFlashMemModeRes,8);
    
    resSizes.put(Utils.getloggersRes, 0x3FFF); // rozmiar determinuje kolejny bajt, współczynnik 3+1=4  //get registred loggers res
    resSizes.put(Utils.unregisterLoggerAck, 4); //unregister logger ack
    resSizes.put(Utils.registerLoggerAck, 4); //register logger ack
    resSizes.put(Utils.getChargeHubBatteryLevelRes, 1); //get hub charge batery level res
    resSizes.put(Utils.getHubTimeRes, 4); //get hub time res
    resSizes.put(Utils.setHubTimeAck, 0); //set hub time ack
    resSizes.put(Utils.getLoggerTimeRes, 4); //get logger time res
 //   resSizes.put(0x0009, ??); //TODO time offset
    resSizes.put(Utils.getFreqLoggingRes, 1); //get freqency of temprature logging res
    resSizes.put(Utils.getLoggerIdRes, 4);  //get IR session logger ID
    resSizes.put(Utils.enableLoggerRadioAck, 0);  //set enable logger radio ack
    resSizes.put(Utils.getNextHubFlashSessionRes, 0); //set by HubFlasSession class
    resSizes.put(Utils.getNextLoggerFlashSessionRes, 0); //set by LoggerFlasSession class
}

    /**
     * Testuje poprawność obiektu odpowiedzi ze spodziewanym kodem odpowiedzi.
     * @param ack spodziewany kod odpowiedzi
     * @throws MeteringSessionException w przypadku niezgodności obiektu odpowiedzi
     * ze spodziewanym kodem odpowiedzi lub w przypadku ustawienia bitów błędu w odpowiedzi
     */
    void receiveAck(int ack) throws MeteringSessionException {
        if((0x00FF & resp)!= (0x00FF & ack)) //młodszy
            throw new MeteringSessionException("Expected command ack:"+(0x00FF & ack)+" found:"+(0x00FF & resp));
        if((0x0F00 & resp)!= (0x0F00 & ack)) //młodsza połówka starszego
            throw new MeteringSessionException("Expected session ack:"+(0x00FF & (ack>>>8))+" found:"+(0x00FF & (resp>>>8)));        
        if(ack>0x0FFF & ack!=Utils.hubIdentifictionAck) //starszy
            throw new MeteringSessionException("Exception number:"+(resp>>>12)+" for request:"+ack); 

    }

    /**
     * Zwraca parametry obiektu odpowiedzi.
     * @return parametry obiektu odpowiedzi
     */
    byte[] receiveData() {
        return this.data;
    }
            
    /**
     * Zwraca ustawione bity błędu odpowiedzi w stosunku do spodziewanego kodu odpowiedzi.
     * @param ack spodziewany kod odpowiedzi
     * @return ustawione bity błędu
     * @throws MeteringSessionException w przypadku niezgodności obiektu odpowiedzi
     * ze spodziewanym kodem odpowiedzi
     */
    int receiveAckWithErrCode(int ack) throws MeteringSessionException {
        if((0x00FF & resp)!= (0x00FF & ack)) //młodszy
            throw new MeteringSessionException("Expected command ack:"+(0x00FF & ack)+" found:"+(0x00FF & resp));
        if((0x0F00 & resp)!= (0x0F00 & ack)) //młodsza połówka starszego
            throw new MeteringSessionException("Expected session ack:"+(0x00FF & (ack>>>8))+" found:"+(0x00FF & (resp>>>8)));        
        return resp>>>12;
    }
    
    
}
