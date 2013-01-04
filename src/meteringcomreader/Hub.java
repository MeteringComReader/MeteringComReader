/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

/**
 *
 * @author Juliusz
 */
public class Hub {
    protected String hubId;
    protected String comPortName;
    

    public Hub(long hubId, String comPortName){
        this.hubId=convertHubId2Hex(hubId);
        this.comPortName=comPortName;
    }
    /**
     * @return the hubId
     */
/*    
    public long getHubId() {
        return hubId;
    }

    **
     * @param hubId the hubId to set
     *
    public void setHubId(long hubId) {
        this.hubId = hubId;
    }
*/
    /**
     * @return the comPortName
     */
    public String getComPortName() {
        return comPortName;
    }

    /**
     * @param comPortName the comPortName to set
     */
    public void setComPortName(String comPortName) {
        this.comPortName = comPortName;
    }

    static String convertHubId2Hex(long hubId){
           long fullLoggerId= hubId | 0x4D4500000000L; //"454D" 
           String hexHubId=
                   String.format("%12X", fullLoggerId);           
           return hexHubId;        
    }
    
    String getHubHexId() {
        return hubId;
    }
}
