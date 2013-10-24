/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Timestamp;

/**
 *
 * @author Juliusz Jezierski
 */
public class HubFlashSessionWithPacketNo extends MeteringSession{
    protected byte[] packets=null;
    protected int packetsNo;
    protected int packetsCounter=0;
    protected int bytesCounter=0;
    protected boolean resultReaded=false;

    HubFlashSessionWithPacketNo(HubConnection hc, Timestamp time) throws MeteringSessionException {
        super(hc);
        long timeInt=Utils.timestamp2int(time);
        byte[] timeBytes=Utils.long2bytes(timeInt, 4);
        hc.sendCommand(Utils.startHubFlashSessionReq, timeBytes);
        byte data[]=hc.receiveAck(Utils.startHubFlashSessionRes);
        int packetSize=(data[0]+1)*128;
        ComResp.setResSize(Utils.getNextHubFlashSessionReq, packetSize);
    }

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
                packetsNo=packets[0];
                if (packetsNo==0){
                    packets = null;
                    resultReaded=true;
                    return null;                
                }
                bytesCounter=0;
                packetsCounter=0;        
            }
            while(dp==null && packets!=null){
                int packetSize=packets[bytesCounter];
                bytesCounter++;
                if (packetSize==DataPacket.LEN){
                    dp=new DataPacket(packets, bytesCounter);
                }
                bytesCounter+=packetSize;
                packetsCounter++;
                if ((packetsCounter)==packetsNo)
                    packets=null;
            }
        }
        return dp;
    }

    @Override
    public DataPacket regetPrevPacket() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws MeteringSessionException {
        hc.sendCommand(Utils.closeHubFlashSessionReq);
        hc.receiveAck(Utils.closeHubFlashSessionRes);
    }

    @Override
    public DataPacket getNextPacket(int maxRetries) throws MeteringSessionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
