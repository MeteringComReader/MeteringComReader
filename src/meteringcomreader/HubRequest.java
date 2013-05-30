/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

/**
 *
 * @author Juliusz
 */
public class HubRequest {

    public String getHexHubId() {
        return hexHubId;
    }

    public void setHexHubId(String hexHubId) {
        this.hexHubId = hexHubId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }
    
    protected String hexHubId;
    protected String command;
    protected String parameters[]= new String[10];

}
