package de.bkiss.lightsim3d;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.SpotLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.system.lwjgl.LwjglContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Main class for LightSim3D
 * @author bkiss
 */
public class Main extends SimpleApplication {
    
    private boolean isOnScreenMsg = false;
    private float onScreenMsgTime;
    
    private static boolean isDisplayFps = false;
    private static boolean isDisplayStats = false;
    
    private Geometry sphere;
    
    private Map<String, Float> v;
    
    private SpotLight spot;
    private SpotLightShadowRenderer slsr;
    private AmbientLight ambient;
    
    private GUIController gui;
    
  
    public static void main(String[] args) {
        Main app = new Main();
        
        //configure settings
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        settings.setMinResolution(1024, 768);
        settings.setVSync(true);
        settings.setFullscreen(false);
        settings.setTitle("LightSim3D");
        
        app.showSettings = false;
        app.setDisplayFps(isDisplayFps);
        app.setDisplayStatView(isDisplayStats);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //setup
        flyCam.setMoveSpeed(10f);
        flyCam.setEnabled(false);
        
        //camera
        cam.setLocation(new Vector3f(1.1161567f, 1.8043375f, -2.223234f));
        cam.setRotation(new Quaternion(0.24233484f, -0.16623776f, 0.042186935f, 0.95491314f));

        //register loaders
        assetManager.registerLoader(TextLoader.class, "txt");
        
        //load ground plane
        Quad quad = new Quad(200, 200);
        Geometry ground = new Geometry("ground", quad);
        ground.setShadowMode(RenderQueue.ShadowMode.Receive);
        Material groundMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        groundMat.setBoolean("UseMaterialColors", true);
        groundMat.setColor("Diffuse", ColorRGBA.Gray);
        groundMat.setColor("Ambient", ColorRGBA.Gray);
        groundMat.setColor("Specular", ColorRGBA.White);
        ground.setMaterial(groundMat);
        ground.rotate(FastMath.DEG_TO_RAD*-90, 0, 0);
        ground.setLocalTranslation(-5.5f, 0, 8);
        rootNode.attachChild(ground);
        
        //load object
        Sphere sphereMesh = new Sphere(32,32,20f);
        sphere = new Geometry("sphere", sphereMesh);
        sphere.scale(0.03f);
        sphere.move(0, 0.5f, 0f);
        sphere.setShadowMode(RenderQueue.ShadowMode.Cast);
        Material shinyMat = new Material( assetManager, "Common/MatDefs/Light/Lighting.j3md");
        shinyMat.setBoolean("UseMaterialColors", true);
        shinyMat.setColor("Specular", ColorRGBA.White);
        shinyMat.setColor("Diffuse",  ColorRGBA.Red);
        shinyMat.setColor("Ambient",  ColorRGBA.Red.mult(0.5f));
        shinyMat.setFloat("Shininess", 4f);
        sphere.setMaterial(shinyMat);
        rootNode.attachChild(sphere);
        
        //ambient light
        ambient = new AmbientLight();
        rootNode.addLight(ambient);
        
        //spotlight
        spot = new SpotLight();  
        spot.setPosition(new Vector3f(5,4,0));
        spot.setDirection(new Vector3f(-1.5f,-1,0));
        rootNode.addLight(spot);
        
        //shadow renderer
        slsr = new SpotLightShadowRenderer(assetManager, 1024);
        slsr.setLight(spot);
        viewPort.addProcessor(slsr); 
  
        //load inputs
        initInputs();
        
        //load GUI
        gui = new GUIController(this);
        gui.loadScreen("start");

        //init values
        initValues();
    }

    @Override
    public void simpleUpdate(float tpf) {
        
        //onscreen msg timing
        if (isOnScreenMsg && (onScreenMsgTime+=tpf) > 2){
            guiNode.detachChildNamed("msg");
            isOnScreenMsg = false;
        }

    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    private void initInputs(){
        inputManager.addMapping("TOGGLE_FPS", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addMapping("TOGGLE_STATS", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addMapping("TOGGLE_SHADOWS", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("TOGGLE_DIRLIGHT", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("TOGGLE_AMBLIGHT", new KeyTrigger(KeyInput.KEY_A));
        
        inputManager.addListener(actionListener, "TOGGLE_FPS",
                                                 "TOGGLE_STATS",
                                                 "TOGGLE_SHADOWS");
    }
    
    private ActionListener actionListener = new ActionListener(){
        public void onAction(String name, boolean pressed, float tpf){
            if (name.equals("TOGGLE_FPS") && pressed){
                setDisplayFps(isDisplayFps = !isDisplayFps);
                displayOnScreenMsg("FPS display " + (isDisplayFps ? "enabled" : "disabled"));
            } else if (name.equals("TOGGLE_STATS") && pressed){
                setDisplayStatView(isDisplayStats = !isDisplayStats);
                displayOnScreenMsg("Stats display " + (isDisplayStats ? "enabled" : "disabled"));
            } else if (name.equals("TOGGLE_SHADOWS") && pressed){
                if (viewPort.getProcessors().contains(slsr)){
                    viewPort.removeProcessor(slsr);
                } else {
                    viewPort.addProcessor(slsr);
                }
                displayOnScreenMsg("Shadow Processor " + (viewPort.getProcessors().contains(slsr) ? "enabled" : "disabled"));
            }
        }
    };
    
    private void displayOnScreenMsg(String msg){
        onScreenMsgTime = 0;
        guiNode.detachChildNamed("msg");
        guiFont = assetManager.loadFont("Interface/Fonts/CalibriBold.fnt");
        BitmapText text = new BitmapText(guiFont, false);
        text.setSize(guiFont.getCharSet().getRenderedSize());
        text.setText(msg);
        text.setLocalTranslation((cam.getWidth()/2)-(text.getLineWidth()/2), cam.getHeight(), 0);
        text.setName("msg");
        guiNode.attachChild(text);
        isOnScreenMsg = true;
    }
    
    public void sliderEvent(String id, float value){
        displayOnScreenMsg(id + ": " + value);
        v.put(id, value);
        if (id.equals("slMatShin")) {
            sphere.getMaterial().setFloat("Shininess", v.get(id)*127+1);
        } else if (id.equals("slSpotExp")) {
            spot.setSpotOuterAngle(v.get("slSpotExp") * FastMath.DEG_TO_RAD);
        } else if (id.equals("slSpotCut")) {
            spot.setSpotInnerAngle(v.get("slSpotCut") * FastMath.DEG_TO_RAD);
        } else if (id.equals("slSpotRange")) {
            spot.setSpotRange(v.get("slSpotRange"));
        } else {
            if (id.contains("MatDiff")){
                sphere.getMaterial().setColor("Diffuse", new ColorRGBA(
                        v.get("slMatDiffR"),
                        v.get("slMatDiffG"),
                        v.get("slMatDiffB"),
                        1));
            } else if (id.contains("MatAmb")){
                sphere.getMaterial().setColor("Ambient", new ColorRGBA(
                        v.get("slMatAmbR"),
                        v.get("slMatAmbG"),
                        v.get("slMatAmbB"),
                        1));
            } else if (id.contains("MatSpec")){
                sphere.getMaterial().setColor("Specular", new ColorRGBA(
                        v.get("slMatSpecR"),
                        v.get("slMatSpecG"),
                        v.get("slMatSpecB"),
                        1));
            } else if (id.contains("AmbLi")){
                ambient.setColor(new ColorRGBA(
                        v.get("slAmbLiR"),
                        v.get("slAmbLiG"),
                        v.get("slAmbLiB"),
                        1));
            } else if (id.contains("SpotLi")){
                spot.setColor(new ColorRGBA(
                        v.get("slSpotLiR"),
                        v.get("slSpotLiG"),
                        v.get("slSpotLiB"),
                        1));
            }
        }
    }
    
    private void initValues(){
        v = new HashMap<String, Float>();
        //Material Diffuse
        v.put("slMatDiffR", gui.getSliderValue("slMatDiffR"));
        v.put("slMatDiffG", gui.getSliderValue("slMatDiffG"));
        v.put("slMatDiffB", gui.getSliderValue("slMatDiffB"));
        //Material Ambient
        v.put("slMatAmbR", gui.getSliderValue("slMatAmbR"));
        v.put("slMatAmbG", gui.getSliderValue("slMatAmbG"));
        v.put("slMatAmbB", gui.getSliderValue("slMatAmbB"));
        //Material Specular
        v.put("slMatSpecR", gui.getSliderValue("slMatSpecR"));
        v.put("slMatSpecG", gui.getSliderValue("slMatSpecG"));
        v.put("slMatSpecB", gui.getSliderValue("slMatSpecB"));
        //Material Shininess
        v.put("slMatShin", gui.getSliderValue("slMatShin"));
        //Ambient Light
        v.put("slAmbLiR", gui.getSliderValue("slAmbLiR"));
        v.put("slAmbLiG", gui.getSliderValue("slAmbLiG"));
        v.put("slAmbLiB", gui.getSliderValue("slAmbLiB"));
        //SpotLight
        v.put("slSpotLiR", gui.getSliderValue("slSpotLiR"));
        v.put("slSpotLiG", gui.getSliderValue("slSpotLiG"));
        v.put("slSpotLiB", gui.getSliderValue("slSpotLiB"));
        //Spotlight Exponent + Cutoff
        v.put("slSpotExp", gui.getSliderValue("slSpotExp"));
        v.put("slSpotCut", gui.getSliderValue("slSpotCut"));
        v.put("slSpotRange", gui.getSliderValue("slSpotRange"));
        
        //set initial values
        for (Entry<String, Float> e : v.entrySet()){
            sliderEvent(e.getKey(), e.getValue());
        }
    }
    
}
