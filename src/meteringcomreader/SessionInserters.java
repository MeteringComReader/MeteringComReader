/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.util.HashMap;
import java.util.Map;
import meteringcomreader.RadioSessionDBInserter;

/**
 *
 * @author Juliusz
 */
public class SessionInserters extends HashMap<String, RadioSessionDBInserter> {
    
    public  void addInserter(String hubId, RadioSessionDBInserter ins){
        put(hubId, ins);
    }
    public  RadioSessionDBInserter getInserter(String hubId){
        return get(hubId);
    }
    public  RadioSessionDBInserter removeInserter(String hubId){
        return remove(hubId);
    }
    public SessionInserters(Map<? extends String, ? extends RadioSessionDBInserter> map) {
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
