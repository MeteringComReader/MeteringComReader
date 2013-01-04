/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Juliusz
 */
public class HubsSessions extends HashMap<String, HubConnection>{

    public HubsSessions(Map<? extends String, ? extends HubConnection> map) {
        super(map);
    }

    public HubsSessions() {
    }

    public HubsSessions(int i) {
        super(i);
    }

    public HubsSessions(int i, float f) {
        super(i, f);
    }

    public HubConnection getHubConnection(String hubNo) throws MeteringSessionException {
        HubConnection hc = get(hubNo);
        if (hc==null) 
            throw new MeteringSessionException("Hub Connection for hub no "+hubNo+" no found");
        return hc;
    }
    
    public boolean isPortUsed(String serialPortName){
        boolean ret=false;
        Set<Map.Entry<String, HubConnection>> connectionSet= this.entrySet();
        Iterator<Map.Entry<String, HubConnection>> it = connectionSet.iterator();
        while(it.hasNext()){
            Map.Entry<String, HubConnection> pair= it.next();
            HubConnection hc= pair.getValue();
            if (hc.hub.comPortName.equals(serialPortName))
                ret = true;
        }        
        return ret;
    }
        
}
