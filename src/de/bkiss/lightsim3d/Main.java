package de.bkiss.lightsim3d;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.CartoonEdgeFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for LightSim3D
 * @author Börge Kiss
 */
public class Main extends SimpleApplication {
    
    private CameraNode camNode;
    private Node camera;
    private List<Spatial> spatials;
    
    
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
        app.setSettings(settings);
        app.start();
    }
    

    @Override
    public void simpleInitApp() {
        
        //setup
        flyCam.setMoveSpeed(10f);
        flyCam.setEnabled(false);

        //load scene
        Node scene = (Node) assetManager.loadModel("Scenes/scene.j3o");
        scene.scale(0.05f);
        rootNode.attachChild(scene);
        
        //generate scene objects list
        spatials = new ArrayList<Spatial>(scene.getChildren());
        spatials.add(scene); //add the scene itself to list
        
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
        
        
        //TEST: HUD Text
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Calibri.fnt");
        BitmapText text = new BitmapText(guiFont, false);
        text.setSize(guiFont.getCharSet().getRenderedSize());
        text.setLineWrapMode(LineWrapMode.Word);
        text.setText("[SPACE] toggle this text overlay\n[A] Something\n[B] Something else");
        text.setLocalTranslation(10, settings.getHeight() - 10, 0);
        guiNode.attachChild(text);
    }

    @Override
    public void simpleUpdate(float tpf) {
        camera.rotate(0, FastMath.DEG_TO_RAD*20*tpf, 0);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    private Geometry getGeometry(String name){
        for (Spatial s : spatials)
            if (s.getName().equals(name))
                return (Geometry) s;
        return null;
    }
    
    private void initInputs(){
        //TODO
    }
}
