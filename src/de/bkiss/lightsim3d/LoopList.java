package de.bkiss.lightsim3d;

import java.util.ArrayList;

/**
 *
 * @author bkiss
 */
public class LoopList<T> extends ArrayList<T>{
    
    private int pointer;
    
    public LoopList(){
        super();
        pointer = 0;
    }
    
    public T next(){
        if (size() == 0) return null;
        if (pointer > size() - 1) pointer = 0;
        pointer++;
        return get(pointer-1);
    }
    
}
