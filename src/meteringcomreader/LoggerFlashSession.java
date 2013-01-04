/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.sql.Timestamp;

/**
 *
 * @author Juliusz
 */
public class LoggerFlashSession  extends MeteringSession{
    protected final long loggerId;
    
    protected byte[] packets=null;
    protected int bytesCounter=0;
    protected int bitCounter=0;
    protected boolean resultReaded=false;
    protected int packetSize;
    protected long time;
    protected long period;
    int[] temperatures;
    

    
    public LoggerFlashSession(HubConnection hc, Timestamp time) throws MeteringSessionException {
        super(hc);
        long timeInt=Utils.timestamp2int(time);
        byte[] timeBytes=Utils.long2bytes(timeInt, 4);
        hc.sendCommand(Utils.startLoggerFlashSessionReq, timeBytes);
        byte data[]=hc.receiveAck(Utils.startLoggerFlashRes);
        loggerId=Utils.bytes2long(data, 4);
        packetSize=(data[3]+1)*128;
        ComResp.setResSize(Utils.getNextLoggerFlashSessionReq, packetSize);
        temperatures=new int[packetSize/12];
    }
        
    public int getDataRecordingPeriod(){
        return 0;
    }
    
    public void  setDataRecordingPeriod(int period){
        
    }
    


    @Override
    public void close() throws MeteringSessionException {
        hc.sendCommand(Utils.closeLoggerFlashSessionReq);
        hc.receiveAck(Utils.closeLoggerFlashSessionRes);    
    }

    @Override
    public DataPacket getNextPacket() throws MeteringSessionException {
        DataPacket dp=null;
        if (resultReaded)
            throw new MeteringSessionException("All data already readed in flash hub session");
        if(packets==null){
            hc.sendCommand(Utils.getNextLoggerFlashSessionReq);
            ComResp rs = hc.crd.getNextResp();
            int errCode = rs.receiveAckWithErrCode(Utils.getNextLoggerFlashSessionRes);
            if (errCode==0xF){
                resultReaded=true;
                return null;                
            }
            else if(errCode!=0){
                throw new MeteringSessionException("Exception number:"+errCode+" for request:"+Utils.getNextLoggerFlashSessionRes); 
            }
            packets = rs.receiveData();
            bytesCounter=0;
            time=Utils.bytes2long(packets, 4);
            bytesCounter+=4;
            period=Utils.bytes2long(packets, bytesCounter, 2);
            bytesCounter+=2;
            bitCounter=bytesCounter*8;
        }
        dp=new DataPacket(loggerId, time, period);
        int temp;
        int tempCount=0;
        while (bitCounter<packetSize*8){
            temp=getWord(bitCounter, 12); //wyliczyć temp
            bitCounter+=12;
            if (temp==2010){ //koniec strony
                packets=null;
                break;
            } else if (temp==2020){
                if(bitCounter+24<packetSize*8){
                    period=getWord(bitCounter, 16);
                    bitCounter+=24;
                }else{                   //koniec strony
                    packets=null;
                }                    
                break;
            }
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

    @Override
    public DataPacket regetPrevPacket() {
        throw new UnsupportedOperationException("Not supported yet.");
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
