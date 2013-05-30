/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.sql.Timestamp;
import java.util.Arrays;
import meteringdatareader.Frame;

/**
 *
 * @author Juliusz
 */
public class DataPacket {
    static int LEN=31;
    static int START_DATA=13;
    static final int defTempCount=6;
     int appId;
     short fieldLength;
     long loggerNo;  //Logger identificator
     short encAlg;
     Timestamp measurmentTimeStart;
     Timestamp measurmentTimeEnd;
     int measurmentPeriod;
     int[] temperatures = new int[defTempCount];
     byte batteryVoltage;
     long loggerId; //logger datrabase id
     int rssi;
     int lqi;
     protected int tempCount=defTempCount;
     long endTime;
     byte[] encriptedData;
     byte[] decriptKey;
     

     DataPacket(long loggerNo, long time, long period){
         this.loggerNo=loggerNo;
         this.measurmentPeriod=(int)period;
         this.measurmentTimeStart =  Utils.time2Timestamp(time);
         this.endTime =  time;
        
    }
      
      
      public String getLoggerHexId(){
        /*
           String hexLoggerNo=
                   String.format("%02X",(byte)(0xFF&appId))
                   +String.format("%02X",(byte)(0xFF&(appId>>8)))
                   +String.format("%08X", loggerNo); //"454D" 
                   * 
                   */
           long fullLoggerId= loggerNo | ((0xFF&(long)appId)<<(8*5)) | ((0xFF00&(long)appId)<<(8*3));
           String hexLoggerNo=
                   String.format("%12X", fullLoggerId);           
           return hexLoggerNo;
    }
    
    DataPacket(byte[] data) throws MeteringSessionException {
        this(data,0);
    }
    
    boolean decriptData(){
        boolean ret=true;
        
        return false;
    }

    
    DataPacket(byte[] data, int start) throws MeteringSessionException {
        Frame frame = new Frame(data, start, LEN);
        int temp;
        
        appId = frame.getHeaderElement(Frame.frameTempLogger, "APPID");
        fieldLength = (short)frame.getHeaderElement(Frame.frameTempLogger, "LEN");
        loggerNo = //frame.getHeaderElement(Frame.frameTempLogger, "IDD");
              ((long)frame.getHeaderElement(Frame.frameTempLogger, "IDD"))&0x00000000FFFFFFFFL;
        int infb=frame.getHeaderElement(Frame.frameTempLogger, "INFB");
//        Frame infbFrame = new Frame(Utils.long2bytes(infb, 4), 0,32);
//      encAlg = (short)infbFrame.getHeaderElement(Frame.frameINFB, "ENCF");
        encAlg=0;
//        measurmentPeriod = frame.getHeaderElement(Frame.frameTempLoggerData, "PERIOD"); //TODO: pobrać okres
        measurmentPeriod = 180;
        rssi=((int)((byte)frame.getHeaderElement(Frame.frameTempLogger, "RSSI")))/2-74;
        lqi=frame.getHeaderElement(Frame.frameTempLogger, "LQI")&0x7F; //ignore b7
        if (encAlg!=0){
                encriptedData= Arrays.copyOfRange(data, START_DATA, START_DATA+16);
                throw new MeteringSessionException("Decription is not supported yet");
                        //System.arraycopy(LEN, lqi, LEN, LEN, LEN)
        }
        else{
            frame = new Frame(data, start+START_DATA, 16);
            endTime = ((long)frame.getHeaderElement(Frame.frameTempLoggerData, "TIME"))&0x00000000FFFFFFFFL;
            measurmentTimeEnd =  Utils.time2Timestamp(endTime);
            //        new Timestamp(Timestamp.valueOf("2011-01-01 00:00:00").getTime()+
            //                endTime*1000);
            measurmentTimeStart =Utils.time2Timestamp(endTime-(tempCount-1)*measurmentPeriod);               
    //               new Timestamp(Timestamp.valueOf("2011-01-01 00:00:00").getTime()+
    //                       (startTime-5*measurmentPeriod)*1000);  
            for (int i=0; i<tempCount; i++){

                temp=frame.getHeaderElement(Frame.frameTempLoggerData, "TEMPERATURE"+(i+1));
                if ((temp&0x0800)==0x0800){  //sign bit in 12bits number is set
                    temp=temp |0xFFFFF800;

                    //temp=(temp&0x07FF)|0x80000000;
                }
                temperatures[i]=temp;
            }
            batteryVoltage = (byte)frame.getHeaderElement(Frame.frameTempLoggerData, "BATTERY_VOLTAGE");        
        }
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("appId:");sb.append(appId);
        sb.append(", fieldLength:");sb.append(fieldLength);
        sb.append(", loggerId:");sb.append(String.format("%0#8X", loggerNo));
        sb.append(", encAlg:");sb.append(encAlg);
        sb.append(", measurmentTime:");sb.append(measurmentTimeStart);
        sb.append(", measurmentPeriod:");sb.append(measurmentPeriod);
        sb.append(", temperatures:(");
        for (int i=0; i<6; i++){
            sb.append(temperatures[i]);
            sb.append(" ,");
        }       
        sb.append("), batteryVoltage:");sb.append(batteryVoltage);
        
        return sb.toString();
        
    }

    long getRSSI() {
        return rssi;
    }

    void setTemperatures(int[] temperatures, int tempCount) {
       this.temperatures= new int [tempCount];
       this.tempCount=tempCount;
       for (int i=0; i<tempCount; i++){
           this.temperatures[i]=temperatures[i];
       }
       this.endTime=this.endTime+(tempCount-1)*this.measurmentPeriod;
       this.measurmentTimeEnd= Utils.time2Timestamp(endTime);
    }
    
    public static void main(String[] args){
        int i = 0xFF;
        byte b =(byte)i;
        System.out.println((int)(byte)i);
        System.out.println(i);
    }
}
