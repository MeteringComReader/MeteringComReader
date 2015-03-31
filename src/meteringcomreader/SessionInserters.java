/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.util.HashMap;
import java.util.Map;
import meteringcomreader.SessionInserter;

/**
 *
 * @author Juliusz
 */
public class SessionInserters extends HashMap<String, SessionInserter> {
    
    public  void addInserter(String hubId, SessionInserter ins){
        put(hubId, ins);
    }
    public  SessionInserter getInserter(String hubId){
        return get(hubId);
    }
    public  SessionInserter removeInserter(String hubId){
        return remove(hubId);
    }
    public SessionInserters(Map<? extends String, ? extends SessionInserter> map) {
        super(map);
    }

    public SessionInserters() {
    }

    public SessionInserters(int i) {
        super(i);
    }

    public SessionInserters(int i, float f) {
        super(i, f);
    }
    
    
}
