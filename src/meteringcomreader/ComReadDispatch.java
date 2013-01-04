/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juliusz
 */
public class ComReadDispatch implements SerialPortEventListener{
    
    protected InputStream inputStream;
    protected BlockingQueue<DataPacket> rsData=new LinkedBlockingQueue<DataPacket>();
    protected BlockingQueue<ComResp> resp=new LinkedBlockingQueue<ComResp>();
    
    protected MeteringSessionException rsException=null;
    protected MeteringSessionException resException=null;
    
    
    public ComReadDispatch(InputStream in){
        this.inputStream=in;
    }
    
    

    @Override
    public void serialEvent(SerialPortEvent spe) {
System.out.println("Time:"+System.nanoTime()+", serialEvent: "+ spe.toString()+","+spe.getEventType()+", thread: "+Thread.currentThread().getName());           
        if(spe.getEventType()!=SerialPortEvent.DATA_AVAILABLE){ 
            return;
        }
        
        int loopNo=0;
        int res;
        byte[]data=null;
        int size;
        int len;
        int frameSize;
        while (true){
            loopNo++;
System.out.println("Time:"+System.nanoTime()+" serialEvent loopNo:"+loopNo);
            try {
                res = _receiveRes();
                if (res == Utils.radioSessionRes) 
                    try {
                        data =_receiveData(1);
                        frameSize = 0xFF & data[0];
                        if (frameSize==DataPacket.LEN){                           
                            data = _receiveData(DataPacket.LEN);
System.out.println("Time:"+System.nanoTime()+", [renew DP ");                            
                            DataPacket dp = new DataPacket(data);
                            rsData.add(dp);
System.out.println("Time:"+System.nanoTime()+", new DP inserted into queue");                            
                        }
                        else{
                            data =_receiveData(frameSize); //unknown frame format
 System.out.println("Time:"+System.nanoTime()+", unknown frame format, len:"+frameSize);                          
                        }
                    } catch (MeteringSessionTimeoutException ex) {
                        return;  //if timeout to get next data then exit
                    } catch (MeteringSessionException ex) {
System.out.println("Time:"+System.nanoTime()+","+" exeption dedected in serialEvent"+ex);                                   
                        setRSException(ex);
                        return;
                    }
                else {
                    size=ComResp.getResDataSize(res);
                    if((size&0x0FFF)==0x0FFF){ //next byte detemines data size
                        len=((int)Utils.bytes2long(_receiveData(1), 1))
                                *(1+size>>>12); // older 4 bits = len multiplier
                        data=_receiveData(len);
                    }
                    else if(size>0){
                        data=_receiveData(size);
                    }
                    else{
                        data=null;
                    }
                    resp.add(new ComResp(res, data));
                }
            } catch (MeteringSessionTimeoutException ex) {
                return;  //if timeout to get next data then exit
            } catch (MeteringSessionException ex) {
                setResException(ex);
                return;
            }
        }
    }
    
   public DataPacket getNextRSPacket() throws MeteringSessionException{
       MeteringSessionException e= getRSException();
       if (e!=null){
System.out.println("Time:"+System.nanoTime()+","+"getNextRSPacket exeption dedected in getNextRSPacket "+e);           
           throw e;
       }
       DataPacket dp;
        try {
            dp = rsData.take();
        } catch (InterruptedException ex) {
            throw new MeteringSessionException("Interrupted while waiting for data packet");
        }
       return dp;
   }
   
   public ComResp getNextResp() throws MeteringSessionException{
       MeteringSessionException e= getResException();
       if (e!=null){
System.out.println("Time:"+System.nanoTime()+","+"exeption dedected in getNextResp "+e);           
           throw e;
       }       
       ComResp ret=null;
        try {
            ret=resp.poll(Utils.TIMEOUT*10, TimeUnit.MILLISECONDS);
            if (ret==null){
System.out.println("Time:"+System.nanoTime()+","+Thread.currentThread().getName()+" throws exeption  in getNextResp "+e);           
                throw new MeteringSessionTimeoutException("Timeout during waiting for response");
            }
        } catch (InterruptedException ex) {
            //
        }
       return ret;
   }
    
   protected int _readBytes(byte[]buf, int size) throws MeteringSessionException{
        byte ret[] = new byte[1];
        int len;
        
        
//System.out.print("Time:"+System.nanoTime()+", ");                
        for(int i=0; i<size; i++){           
            try {
                len=this.inputStream.read(ret);
                if(len==-1) 
                    throw new MeteringSessionException("Serial EOF");
                if(len==0) {
//System.out.println("Time:"+System.nanoTime()+","+"Thread:"+Thread.currentThread().getName()+" Serial port read timeout in _readBytes size"+size);                
                    throw new MeteringSessionTimeoutException("Serial port read timeout");
                }
System.out.print(String.format("%0#2X", ret[0])+',');               
                buf[i]=ret[0];
            } catch (IOException ex) {
System.out.println("Time:"+System.nanoTime()+","+"IOException dedected in _readBytes"+ex);                
                new MeteringSessionSPException(ex);
            }
        }
System.out.println();        
System.out.println("Time:"+System.nanoTime()+","+" _readBytes size:"+size);                

        return size;
    }
   
   synchronized protected void setRSException(MeteringSessionException e){
       this.rsException=e;
   }
   
   synchronized protected MeteringSessionException getRSException(){
       MeteringSessionException e = this.rsException;
       this.rsException=null;
       return e;
   }
   
   synchronized protected void setResException(MeteringSessionException e){
       this.resException=e;
   }  
   
   
   synchronized protected MeteringSessionException getResException(){
       MeteringSessionException e = this.resException;
       this.resException=null;
       return e;
   }
   
   protected byte[] _receiveData(int size) throws MeteringSessionException{
        byte[] ret=new byte[size];
        _readBytes(ret, size);
        return ret;
    }
    
    protected int _receiveRes()throws MeteringSessionException{
        byte[] ret = new byte[2];
        _readBytes(ret, 2);
        return (int)Utils.bytes2long(ret, (byte)2);
    }
    
}
