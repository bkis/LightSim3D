package de.bkiss.lightsim3d;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import java.util.HashSet;
import java.util.Set;

/**
 * Main class for LightSim3D
 * @author Börge Kiss
 */
public class Main extends SimpleApplication {
    
    private Set<Geometry> selected;
    private boolean highlight;
    private float highlightTime;
    
    private boolean isOnScreenMsg;
    private float onScreenMsgTime;
    
    private CameraNode camNode;
    private Node camera;
    private LoopList<Geometry> geoms;
    
    private static boolean hudEnabled = true;
    private static boolean isDisplayFps = true;
    private static boolean isDisplayStats = false;
    
    public static void main(String[] args) {
        Main app = new Main();
        
        //configure settings
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        settings.setBitsPerPixel(24);
        settings.setVSync(true);
        settings.setFullscreen(false);
        settings.setTitle("LightSim3D - simulation for light and material in 3D graphics");
        
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
        
        //register loaders
        assetManager.registerLoader(TextLoader.class, "txt");

        //load scene
        Node scene = (Node) assetManager.loadModel("Scenes/scene.j3o");
        scene.scale(0.05f);
        rootNode.attachChild(scene);
        
        //generate scene objects list
        geoms = new LoopList<Geometry>();
        for (Spatial s : scene.getChildren()){
            if (s.getUserData("obj")){
                geoms.add((Geometry) s);
                s.setUserData("mat", ((Geometry)s).getMaterial().getAssetName());
            }
        }
        
        //ambient light
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(2f));
        rootNode.addLight(ambient); 
        
        //sunlight
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.3f, -0.5f)).normalizeLocal());
        sun.setColor(new ColorRGBA(1f,1f,0.85f,1f).mult(1.3f));
        rootNode.addLight(sun); 
        
        //shadow renderer
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 2048, 4);
        dlsr.setLight(sun);
        viewPort.addProcessor(dlsr); 
        
        //camera node
        camera = new Node();
        flyCam.setEnabled(false);
        camNode = new CameraNode("Camera Node", cam);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        camera.attachChild(camNode);
        camNode.setLocalTranslation(new Vector3f(0, 6, -8));
        camNode.lookAt(new Vector3f(0,2,0), Vector3f.UNIT_Y);
        rootNode.attachChild(camera);
        
        //TEST: manipulate materials
        //getGeometry("table").getMaterial().setTexture("DiffuseMap", null);
        //getGeometry("table").getMaterial().setTexture("SpecularMap", null);
        //getGeometry("table").getMaterial().setTexture("NormalMap", null);
        
        //load HUD
        setHUD(true);
        
        //init selection set
        selected = new HashSet<Geometry>();
        
        //load inputs
        initInputs();
    }

    @Override
    public void simpleUpdate(float tpf) {
        //camera rotation
        camera.rotate(0, FastMath.DEG_TO_RAD*20*tpf, 0);
        
        //highlight timing
        if (highlight && (highlightTime+=tpf) > 0.5f){
            unhighlightGeom();
        }
        
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
    
    private Geometry getGeometry(String name){
        for (Spatial s : geoms)
            if (s.getName().equals(name))
                return (Geometry) s;
        return null;
    }
    
    private void highlightGeom(Geometry geom){
        selected.add(geom);
        geom.setMaterial(assetManager.loadMaterial("Materials/highlightMat.j3m"));
        highlight = true;
    }
    
    private void unhighlightGeom(){
        highlightTime = 0;
        highlight = false;
        if (selected == null) return;
        for(Geometry g : selected)
            g.setMaterial(assetManager.loadMaterial((String)g.getUserData("mat")));
    }
    
    private void selectNextObject(){
        unhighlightGeom();
        selected.clear();
        highlightGeom((Geometry) geoms.next());
        displayOnScreenMsg("Selected: Single Object");
    }
    
    private void selectAllObjects(){
        selected.clear();
        unhighlightGeom();
        for (Spatial s : geoms){
            highlightGeom((Geometry) s);
        }
        displayOnScreenMsg("Selected: All Objects");
    }
    
    private void initInputs(){
        inputManager.addMapping("TOGGLE_HUD", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("SELECT_OBJECT", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addMapping("SELECT_ALL", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("TOGGLE_FPS", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("TOGGLE_STATS", new KeyTrigger(KeyInput.KEY_S));
        
        inputManager.addListener(actionListener, "SELECT_OBJECT",
                                                 "SELECT_ALL",
                                                 "TOGGLE_HUD",
                                                 "TOGGLE_FPS",
                                                 "TOGGLE_STATS");
        //inputManager.addListener(analogListener, "My Action"); 
    }
    
    private ActionListener actionListener = new ActionListener(){
        public void onAction(String name, boolean pressed, float tpf){
            if (name.equals("SELECT_OBJECT") && pressed){
                selectNextObject();
            } else if (name.equals("SELECT_ALL") && pressed){
                selectAllObjects();
            } else if (name.equals("TOGGLE_HUD") && pressed){
                setHUD(hudEnabled = !hudEnabled);
            } else if (name.equals("TOGGLE_FPS") && pressed){
                setDisplayFps(isDisplayFps = !isDisplayFps);
            } else if (name.equals("TOGGLE_STATS") && pressed){
                setDisplayStatView(isDisplayStats = !isDisplayStats);
            }
        }
    };
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            System.out.println(name + " = " + value);
        }
    };

    private void setHUD(boolean show) {
        if (show){
            guiFont = assetManager.loadFont("Interface/Fonts/Consolas.fnt");
            BitmapText text = new BitmapText(guiFont, false);
            text.setSize(guiFont.getCharSet().getRenderedSize());
            text.setText(assetManager.loadAsset("Interface/hud.txt") + "");
            text.setLocalTranslation(10, cam.getHeight() - 10, 0);
            text.setName("hud");
            guiNode.attachChild(text);
        } else {
            guiNode.detachChildNamed("hud");
        }
    }
    
    private void displayOnScreenMsg(String msg){
        onScreenMsgTime = 0;
        guiNode.detachChildNamed("msg");
        guiFont = assetManager.loadFont("Interface/Fonts/Calibri.fnt");
        BitmapText text = new BitmapText(guiFont, false);
        text.setSize(guiFont.getCharSet().getRenderedSize());
        text.setText(msg);
        text.setLocalTranslation((cam.getWidth()/2)-(text.getLineWidth()/2), 50, 0);
        text.setName("msg");
        guiNode.attachChild(text);
        isOnScreenMsg = true;
    }
    
}
