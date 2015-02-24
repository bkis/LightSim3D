package de.bkiss.lightsim3d;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

/**
 * Main class for LightSim3D
 * @author bkiss
 */
public class Main extends SimpleApplication {
    
    private Geometry sphere;
    private float sphereShininess = 30;
    
    private boolean autoCamEnabled = true;
    private boolean manualCamMovement = false;
    
    private boolean isOnScreenMsg = false;
    private float onScreenMsgTime;
    
    private CameraNode camNode;
    private Node camera;
    
    private static boolean hudEnabled = true;
    private static boolean isDisplayFps = false;
    private static boolean isDisplayStats = false;
    
    private DirectionalLightShadowRenderer dlsr;
    private LoopList<EdgeFilteringMode> shadowQualities;
    private AmbientLight ambient;
    private ColorRGBA ambColor = ColorRGBA.White.mult(3f);
    private DirectionalLight sun;
    private ColorRGBA sunColor = new ColorRGBA(1f,1f,0.85f,1f).mult(1.1f);
    
    private float camFrustumFar;
    private float camFrustumNear;
    private float camFrustumLeft;
    private float camFrustumRight;
    private float camFrustumBottom;
    private float camFrustumTop;
    
    
    public static void main(String[] args) {
        Main app = new Main();
        
        //configure settings
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        settings.setMinResolution(1024, 768);
        settings.setBitsPerPixel(24);
        settings.setVSync(false);
        settings.setFullscreen(false);
        settings.setTitle("LightSim3D - simulation for light"
                + "and material in 3D graphics");
        
        app.showSettings = true;
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
        
        //load ground plane
        Quad quad = new Quad(200, 200);
        quad.scaleTextureCoordinates(new Vector2f(20, 20));
        Geometry ground = new Geometry("ground", quad);
        ground.setShadowMode(RenderQueue.ShadowMode.Receive);
        Texture groundTex = assetManager.loadTexture("Textures/grass.jpg");
        groundTex.setWrap(Texture.WrapMode.Repeat);
        ground.setMaterial(new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"));
        ground.getMaterial().setTexture("DiffuseMap", groundTex);
        ground.rotate(FastMath.DEG_TO_RAD*-90, 0, 0);
        ground.setLocalTranslation(-100, 0, 100);
        rootNode.attachChild(ground);

        //load scene
        Node scene = (Node) assetManager.loadModel("Scenes/scene.j3o");
        scene.scale(0.05f);
        rootNode.attachChild(scene);
        
        //generate sphere
        Sphere sphereMesh = new Sphere(32,32, 1f);
        sphere = new Geometry("Shiny rock", sphereMesh);
        sphere.setName("sphere");
        sphere.move(1, 3.5f, 0);
        sphere.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        Material shinyMat = new Material( assetManager, "Common/MatDefs/Light/Lighting.j3md");
        shinyMat.setBoolean("UseMaterialColors", true);
        shinyMat.setColor("Specular", ColorRGBA.White);
        shinyMat.setColor("Diffuse",  ColorRGBA.Red);
        shinyMat.setColor("Ambient",  ColorRGBA.Red);
        shinyMat.setFloat("Shininess", sphereShininess);
        sphere.setMaterial(shinyMat);
        rootNode.attachChild(sphere);
        
        //ambient light
        ambient = new AmbientLight();
        ambient.setColor(ambColor);
        rootNode.addLight(ambient); 
        
        //directional light
        sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.4f, -0.5f)).normalizeLocal());
        sun.setColor(sunColor);
        rootNode.addLight(sun); 
        
        //shadow renderer
        shadowQualities = new LoopList<EdgeFilteringMode>();
        shadowQualities.add(EdgeFilteringMode.Dither);
        shadowQualities.add(EdgeFilteringMode.PCF4);
        shadowQualities.add(EdgeFilteringMode.PCF8);
        shadowQualities.add(EdgeFilteringMode.Nearest);
        dlsr = new DirectionalLightShadowRenderer(assetManager, 2048, 4);
        dlsr.setLight(sun);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
        viewPort.addProcessor(dlsr); 
        
        //camera node
        camera = new Node();
        flyCam.setEnabled(false);
        camNode = new CameraNode("Camera Node", cam);
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        camera.attachChild(camNode);
        camNode.setLocalTranslation(new Vector3f(0, 6, -9));
        camNode.lookAt(new Vector3f(0,1,0), Vector3f.UNIT_Y);
        rootNode.attachChild(camera);
        
        //load HUD
        setHUD(true);
        
        //load inputs
        initInputs();
    }

    @Override
    public void simpleUpdate(float tpf) {
        //camera rotation
        if (autoCamEnabled && !manualCamMovement)
            camera.rotate(0, FastMath.DEG_TO_RAD*20*tpf, 0);
        manualCamMovement = false;
        
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
        inputManager.addMapping("TOGGLE_HUD", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("TOGGLE_FPS", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addMapping("TOGGLE_STATS", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addMapping("TOGGLE_AUTOCAM", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addMapping("TOGGLE_SHADOWS", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("SHADOW_MODE", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("TOGGLE_DIRLIGHT", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("TOGGLE_AMBLIGHT", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("TOGGLE_PARALLELP", new KeyTrigger(KeyInput.KEY_P));
        
        inputManager.addListener(actionListener, "SELECT_OBJECT",
                                                 "SELECT_ALL",
                                                 "TOGGLE_HUD",
                                                 "TOGGLE_FPS",
                                                 "TOGGLE_STATS",
                                                 "TOGGLE_AUTOCAM",
                                                 "TOGGLE_SHADOWS",
                                                 "TOGGLE_DIRLIGHT",
                                                 "TOGGLE_AMBLIGHT",
                                                 "TOGGLE_PARALLELP",
                                                 "SHADOW_MODE");
        
        inputManager.addMapping("CAM_LEFT", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("CAM_RIGHT", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("SHINY_PLUS", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("SHINY_MINUS", new KeyTrigger(KeyInput.KEY_DOWN));
        
        inputManager.addListener(analogListener, "CAM_LEFT",
                                                 "CAM_RIGHT",
                                                 "SHINY_PLUS",
                                                 "SHINY_MINUS"); 
    }
    
    private ActionListener actionListener = new ActionListener(){
        public void onAction(String name, boolean pressed, float tpf){
            if (name.equals("TOGGLE_HUD") && pressed){
                setHUD(hudEnabled = !hudEnabled);
                displayOnScreenMsg("Controls display " + (hudEnabled ? "enabled" : "disabled"));
            } else if (name.equals("TOGGLE_FPS") && pressed){
                setDisplayFps(isDisplayFps = !isDisplayFps);
                displayOnScreenMsg("FPS display " + (isDisplayFps ? "enabled" : "disabled"));
            } else if (name.equals("TOGGLE_STATS") && pressed){
                setDisplayStatView(isDisplayStats = !isDisplayStats);
                displayOnScreenMsg("Stats display " + (isDisplayStats ? "enabled" : "disabled"));
            } else if (name.equals("TOGGLE_AUTOCAM") && pressed){
                autoCamEnabled = !autoCamEnabled;
                displayOnScreenMsg("Automatic camera " + (autoCamEnabled ? "enabled" : "disabled"));
            } else if (name.equals("TOGGLE_SHADOWS") && pressed){
                if (viewPort.getProcessors().contains(dlsr)){
                    viewPort.removeProcessor(dlsr);
                } else {
                    viewPort.addProcessor(dlsr);
                }
                displayOnScreenMsg("Shadow Processor " + (viewPort.getProcessors().contains(dlsr) ? "enabled" : "disabled"));
            } else if (name.equals("SHADOW_MODE") && pressed){
                cycleShadowQuality();
                displayOnScreenMsg("Shadow Mode: " + dlsr.getEdgeFilteringMode().name());
            } else if (name.equals("TOGGLE_DIRLIGHT") && pressed){
                if (!sun.getColor().equals(ColorRGBA.BlackNoAlpha)){
                    sun.setColor(ColorRGBA.BlackNoAlpha);
                    if (viewPort.getProcessors().contains(dlsr)) viewPort.removeProcessor(dlsr);
                    displayOnScreenMsg("Directional light and shadow processor disabled");
                } else {
                    sun.setColor(sunColor);
                    if (!viewPort.getProcessors().contains(dlsr)) viewPort.addProcessor(dlsr);
                    displayOnScreenMsg("Directional light and shadow processor enabled");
                }
            } else if (name.equals("TOGGLE_AMBLIGHT") && pressed){
                if (!ambient.getColor().equals(ColorRGBA.BlackNoAlpha)){
                    ambient.setColor(ColorRGBA.BlackNoAlpha);
                    displayOnScreenMsg("Ambient light disabled");
                } else {
                    ambient.setColor(ambColor);
                    displayOnScreenMsg("Ambient light enabled");
                }
            } else if (name.equals("TOGGLE_PARALLELP") && pressed){
                if (!cam.isParallelProjection()){
                    camFrustumNear = cam.getFrustumNear();
                    camFrustumFar = cam.getFrustumFar();
                    camFrustumLeft = cam.getFrustumLeft();
                    camFrustumRight = cam.getFrustumRight();
                    camFrustumBottom = cam.getFrustumBottom();
                    camFrustumTop = cam.getFrustumTop();
                    cam.setParallelProjection(true);
                    cam.setFrustum(-100, 1000, -5, 5, 3.5f, -3.5f);
                    displayOnScreenMsg("Parallel projection enabled");
                } else {
                    cam.setParallelProjection(false);
                    cam.setFrustum(camFrustumNear, camFrustumFar, camFrustumLeft, camFrustumRight, camFrustumTop, camFrustumBottom);
                    displayOnScreenMsg("Parallel projection disabled");
                }
            }
        }
    };
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("CAM_LEFT")){
                manualCamMovement = true;
                camera.rotate(0, FastMath.DEG_TO_RAD*50*tpf, 0);
            } else if (name.equals("CAM_RIGHT")){
                manualCamMovement = true;
                camera.rotate(0, FastMath.DEG_TO_RAD*-50*tpf, 0);
            } else if (name.equals("SHINY_PLUS")){
                if (sphereShininess < 128 - 20*tpf) sphereShininess += 20*tpf;
                sphere.getMaterial().setFloat("Shininess", sphereShininess);
                displayOnScreenMsg("Sphere Shininess: " + (int)sphereShininess);
            } else if (name.equals("SHINY_MINUS")){
                if (sphereShininess > 0 + 20*tpf) sphereShininess -= 20*tpf;
                sphere.getMaterial().setFloat("Shininess", sphereShininess);
                displayOnScreenMsg("Sphere Shininess: " + (int)sphereShininess);
            }
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
    
    private void cycleShadowQuality(){
        dlsr.setEdgeFilteringMode(shadowQualities.next());
    }
    
}
