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
public class HubFlashSession extends MeteringSession{
    protected byte[] packets=null;
    protected int packetsCounter=0;
    protected int bytesCounter=0;
    protected boolean resultReaded=false;
    protected int packetSize;

    HubFlashSession(HubConnection hc, Timestamp time) throws MeteringSessionException {
        super(hc);
        long timeInt=Utils.timestamp2int(time);
        byte[] timeBytes=Utils.long2bytes(timeInt, 4);
        hc.sendCommand(Utils.startHubFlashSessionReq, timeBytes);
        byte data[]=hc.receiveAck(Utils.startHubFlashSessionRes);
        packetSize=(data[0]+1)*128;
        ComResp.setResSize(Utils.getNextHubFlashSessionReq, packetSize); //TODO: generalize to many hubs
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
                bytesCounter=0;
            }
            while(dp==null && packets!=null){
                if (bytesCounter+4>=this.packetSize){
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
                    packetsCounter++;
                }
                bytesCounter+=frameSize;
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
    
    
}
