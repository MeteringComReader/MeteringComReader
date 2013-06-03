/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import meteringcomreader.exceptions.MeteringSessionTimeoutException;
import meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Timestamp;
import meteringcomreader.exceptions.MeteringSessionOperationAlreadyInProgressException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class LoggerFlashSession  extends MeteringSession{
    private static final Logger lgr = LoggerFactory.getLogger(LoggerFlashSession.class);
    static boolean skipLastPage = false;
    protected int pageCount;
    protected int pageCounter=0;

    protected final long loggerId;
    
    protected byte[] packets=null;
    protected int bytesCounter=0;
    protected int bitCounter=0;
    protected boolean resultReaded=false;
    protected int packetSize;
    protected long time;
    protected long period;
    int[] temperatures;
    protected int recordsPerPage;
    

    
    public LoggerFlashSession(HubConnection hc, Timestamp time) throws MeteringSessionException {
        super(hc);
        byte[] ret;

        
        try{close();} 
        catch(MeteringSessionException e){}
        
        try{
            hc.sendCommand(Utils.getIdLoggerFlashSessionReq);
            ret=hc.receiveAck(Utils.getIdLoggerFlashSessionRes);
            int logId=(int)Utils.bytes2long(ret, 4);
            lgr.debug("getIdLoggerFlashSessionRes="+Integer.toHexString(logId));
        }
        catch(MeteringSessionTimeoutException e){
            throw e; //TODO obsłużyć zaśnięcie logera oddzielnym wyjątkiem
        }
/*        
        hc.sendCommand(Utils.getLoggerHardwareVerReq);
        ret=hc.receiveAck(Utils.getLoggerHardwareVerRes);
        int logHardVer=(int)Utils.bytes2long(ret, 2);
        lgr.debug("getLoggerHardwareVerRes="+Integer.toHexString(logHardVer));        

        hc.sendCommand(Utils.getLoggerFirmwareVerReq);
        ret=hc.receiveAck(Utils.getLoggerFirmwareVerRes);
        int logFirmVer=(int)Utils.bytes2long(ret, 2);
        lgr.debug("getLoggerHardwareVerRes="+Integer.toHexString(logFirmVer));        
*/
        hc.sendCommand(Utils.readPeriodRecodTimeFlashSessionReq);
        ret=hc.receiveAck(Utils.readPeriodRecodTimeFlashSessionRes);
        int periodSeconds=(int)Utils.bytes2long(ret, 2);
        lgr.debug("periodSeconds="+Integer.toString(periodSeconds));
        
        hc.sendCommand(Utils.readFirstRecodTimeFlashSessionReq);
        ret=hc.receiveAck(Utils.readFirstRecodTimeFlashSessionRes);        
        int startTime=(int)Utils.bytes2long(ret, 4);
        lgr.debug("startTime="+Integer.toString(startTime));
        
        hc.sendCommand(Utils.readLastRecodTimeFlashSessionReq);
        ret=hc.receiveAck(Utils.readLastRecodTimeFlashSessionRes);        
        int endTime=(int)Utils.bytes2long(ret, 4);
        lgr.debug("endTime="+Integer.toString(endTime));
        
        hc.sendCommand(Utils.countRecordsPerPageLoggerFlashSessionReq);
        ret=hc.receiveAck(Utils.countRecordsPerPageLoggerFlashSessionReq);        
        recordsPerPage=(int)Utils.bytes2long(ret, 1);
        lgr.debug("recordsPerPage="+Integer.toString(recordsPerPage));
        
        pageCount=(endTime-startTime)/periodSeconds/recordsPerPage+1;
        int startPage=0;
        lgr.debug("pageCount="+Integer.toString(pageCount));
               
        byte[] pageBytes=Utils.long2bytes(startPage, 2);
        
        hc.sendCommand(Utils.startLoggerFlashSessionReq, pageBytes);
        ret=hc.receiveAck(Utils.startLoggerFlashRes);                
        loggerId=Utils.bytes2long(ret, 4);
        packetSize=((int)Utils.bytes2long(ret, 4, 1)+1)*128; //wycięte CRC
        lgr.debug("packetSize="+Integer.toString(packetSize));
        ComResp.setResSize(Utils.getNextLoggerFlashSessionRes, packetSize);
        ComResp.setResSize(Utils.regetPrevLoggerFlashSessionRes, packetSize);
        
        
        temperatures=new int[recordsPerPage];  
        
        
    }
        
    public int getDataRecordingPeriod(){
        return 0;
    }
    
    public void  setDataRecordingPeriod(int period){
        
    }
    

 
    @Override
    public void close() throws MeteringSessionException {
        try{
            hc.sendCommand(Utils.closeLoggerFlashSessionReq);
            hc.receiveAck(Utils.closeLoggerFlashSessionRes);    
           }catch (MeteringSessionOperationAlreadyInProgressException e){
               ; // TODO: obsługa buga Darka
           }        

    }

    @Override
    public DataPacket getNextPacket() throws MeteringSessionException {
        return getPacket(Utils.getNextLoggerFlashSessionReq, Utils.getNextLoggerFlashSessionRes);
    }

    @Override
    public DataPacket regetPrevPacket() throws MeteringSessionException{
        hc.sendCommand(Utils.getIdLoggerFlashSessionReq);
        byte[] ret=hc.receiveAck(Utils.getIdLoggerFlashSessionRes);
        int logId=(int)Utils.bytes2long(ret, 4);
        lgr.debug("getIdLoggerFlashSessionRes="+Integer.toString(logId));
        return getPacket(Utils.regetPrevLoggerFlashSessionReq, Utils.regetPrevLoggerFlashSessionRes);
    }
    
        
    protected DataPacket getPacket(int requestCommand, int responseCommand) throws MeteringSessionException{
        /* TODO: start: latka z pomijaniem ostatniej strony */
        lgr.debug("pageCounter="+Integer.toString(pageCounter));
        lgr.debug("pageCount="+Integer.toString(pageCount));
        if (pageCounter==pageCount)
            return null;
        pageCounter++;
        /* TODO: end: latka z pomijaniem ostatniej strony */
        
        DataPacket dp=null;
        if (resultReaded)
            throw new MeteringSessionException("All data already readed in logger flash session");
        hc.sendCommand(requestCommand);

        ComResp rss[]=new ComResp[1]; // = hc.crd.getNextResp();
        int errCode =  hc.receiveAckWithErrCodeAndSetCR(responseCommand, rss);
        ComResp rs = rss[0];
//            int errCode = rs.receiveAckWithErrCode(Utils.getNextLoggerFlashSessionRes);
        if (errCode==0xF){  //przeczytano wyszyskie dane
            resultReaded=true;
            return null;                
        }
        else if(errCode!=0){
            if (errCode==4)                
                throw new MeteringSessionTimeoutException("LoggerFlashSessionTimeout");
            else
                throw new MeteringSessionException("Exception number:"+errCode+" for request: 0x"+Integer.toHexString(Utils.getNextLoggerFlashSessionRes)); 
        }
        packets = rs.receiveData();
        bytesCounter=0;
        time=Utils.bytes2long(packets, 4);
        bytesCounter+=4;
        period=Utils.bytes2long(packets, bytesCounter, 2);
        bytesCounter+=2;
        bitCounter=bytesCounter*8;

        dp=new DataPacket(loggerId, time, period);
        int temp;
        int tempCount=0;
        
        for (int i=0; i<recordsPerPage; i++){
            temp=getWord(bitCounter, 8); //czy wypelnione 255
            if (temp==255){
                break;
            }
            temp=getWord(bitCounter, 12); //wyliczyć temp
            bitCounter+=12;
            if ((temp&0x0800)==0x0800)  //if sign bit in 12bits number is set
                temp=temp |0xFFFFF800;
            temperatures[tempCount]=temp;                        
            tempCount++; 
        }
        if (tempCount==0)
            return null;
        dp.setTemperatures(temperatures, tempCount);
        
        return dp;
    }

    
    public int getWord(int bitPos, int bits){
        int bitNo=8-(bitPos%8); //ile zostało
        int byteNo=bitPos/8; //skąd startujemy
        int mask= (0xFFFFFFFF>>>(32-bits));
        int res=((((int)packets[byteNo])&0xFF)>>>(8-bitNo)) & mask;
        int wordLen=bitNo;
        byteNo++;
        int shiftR=bitNo;
        while (wordLen<bits){
            mask= (0xFFFFFFFF>>>(32-bits)) >>> wordLen;
            res=res | (((((int)packets[byteNo])&0xFF) & mask)<<shiftR);
            shiftR+=8;
            byteNo++;
            wordLen+=8;      
        }
       return res;
    }


}
