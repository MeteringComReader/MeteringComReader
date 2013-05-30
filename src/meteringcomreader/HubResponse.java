/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

/**
 *
 * @author Juliusz
 */
public class HubResponse {

    public String getHexHubId() {
        return hexHubId;
    }

    public void setHexHubId(String hubId) {
        this.hexHubId = hubId;
    }


    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }
    
    protected String hexHubId;
    protected String parameters[]= new String[5];
    protected String errMsg;

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String err_msg) {
        this.errMsg = err_msg;
    }
    
}
