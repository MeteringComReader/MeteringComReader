package meteringcomreader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Reprezentuje kontener połączeń do koncentratorów.  
 * @author Juliusz Jezierski
 */
public class HubsSessions extends HashMap<String, HubConnection>{

    /**
     * Konstruuje pusty kontener połączeń do koncentratorów o początkowej objętości 16.
     */
    public HubsSessions() {
        super();
    }

    /**
     * Konstruuje pusty kontener koncentratorów o wskazanej początkowej liczebności.
     * @param i początkowa objętość
     */
    public HubsSessions(int i) {
        super(i);
    }

    /**
     * Zwraca obiekt połączenia do koncentratora na podstawie heksadecymalnego 
     * identyfikatora koncentratora.
     * @param hubNo heksadecymalny identyfikator koncentratora
     * @return obiekt połączenia do koncentratora 
     * @throws MeteringSessionException zgłaszany w przypadku nie znalezienia 
     */
    public HubConnection getHubConnection(String hubNo) throws MeteringSessionException {
        HubConnection hc = get(hubNo);
        if (hc==null) 
            throw new MeteringSessionException("Hub Connection for hub no "+hubNo+" no found");
        return hc;
    }
    
    /**  
     * Sprawdza czy w kontenerze znajduje się połączenie do koncentratora 
     * wykorzystujące wskazany port komunikacyjny, co oznacza, że port jest
     * wykorzystywany
     * @param serialPortName nazwa portu komunikacyjnego
     * @return true jeżeli port jest wykorzystywany przez połączenie do dowolnego
     * koncentratora
     */
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
