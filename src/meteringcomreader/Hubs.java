package meteringcomreader;

import meteringcomreader.exceptions.MeteringSessionException;
import java.util.HashMap;
import java.util.Map;

/**
 * Reprezentuje kontener koncentratorów.
 * @author Juliusz Jezierski
 */
public class Hubs extends HashMap<String, Hub>{


    /**
     * Konstruuje pusty kontener koncentratorów o początkowej objętości 16. 
     */
    public Hubs() {
        super();
    }
    
    /**
     * Konstruuje pusty kontener koncentratorów o wskazanej początkowej liczebności.
     * @param i początkowa objętość
     */
    public Hubs(int i) {
        super(i);
    }


    /**
     * Zwraca obiekt koncentratora na postawie jego heksadecymalnego identyfikatora.
     * @param hubNo heksadecymalny identyfikator koncentratora
     * @return obiekt koncentratora
     * @throws MeteringSessionException zgłaszany w przypadku nie znalezienia koncentratora
     */
    public Hub getHub(String hubNo) throws MeteringSessionException {
        Hub h = super.get(hubNo);
        if (h==null) throw new MeteringSessionException("Hub number:"+hubNo+" no found");
        return h;
     
    }
    

    
    

}
