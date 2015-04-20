/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bkiss.lightsim3d;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author bkiss
 */
public class GUIController implements ScreenController{
    
    private Nifty nifty;
    private Screen screen;   
    private final NiftyJmeDisplay niftyDisplay;
    private final Main app;
    
    
    public GUIController(Application app){
        this.app = (Main) app;
        
        niftyDisplay = new NiftyJmeDisplay(
            app.getAssetManager(),
            app.getInputManager(),
            app.getAudioRenderer(),
            app.getViewPort());
        
        nifty = niftyDisplay.getNifty();
        nifty.setIgnoreKeyboardEvents(true);
        app.getGuiViewPort().addProcessor(niftyDisplay);
        
        //set logging level
//        Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE); 
//        Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE); 
    }

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
    
    public void loadScreen(String screenKey){
        nifty.fromXml("Interface/gui.xml", screenKey, this);
        this.screen = nifty.getCurrentScreen();
    }
    
    public void test(){
        System.out.println("HAHAHA");
    }
    
    @NiftyEventSubscriber(pattern="sl.*")
    public void onAllSliderChanged(final String id, final SliderChangedEvent event) {
        app.sliderEvent(event.getSlider().getId(), event.getSlider().getValue());
    } 
    
}
