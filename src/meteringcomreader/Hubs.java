/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Juliusz
 */
public class Hubs extends HashMap<String, Hub>{

    public Hubs(Map<? extends String, ? extends Hub> map) {
        super(map);
    }

    public Hubs() {
    }

    public Hubs(int i) {
        super(i);
    }

    public Hubs(int i, float f) {
        super(i, f);
    }

    public Hub getHub(String hubNo) throws MeteringSessionException {
        Hub h = super.get(hubNo);
        if (h==null) throw new MeteringSessionException("Hub number:"+hubNo+" no found");
        return h;
     
    }
    

    
    

}
