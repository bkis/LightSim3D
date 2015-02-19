package de.bkiss.lightsim3d;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
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
        
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        settings.setBitsPerPixel(24);
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

//        //table
//        Spatial table = assetManager.loadModel("Models/Table/table.j3o");
//        Material matTable = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        matTable.setTexture("DiffuseMap", assetManager.loadTexture("Models/Table/diffuse.tga"));
//        matTable.setTexture("NormalMap", assetManager.loadTexture("Models/Table/normal.tga"));
//        matTable.setTexture("SpecularMap", assetManager.loadTexture("Models/Table/specular.tga"));
//        table.setMaterial(matTable);
//        table.scale(0.05f);
//        table.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//        rootNode.attachChild(table);
//        
//        //bench
//        Spatial bench = assetManager.loadModel("Models/Bench/bench.j3o");
//        Material matBench = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        matBench.setTexture("DiffuseMap", assetManager.loadTexture("Models/Bench/diffuse.tga"));
//        matBench.setTexture("NormalMap", assetManager.loadTexture("Models/Bench/normal.tga"));
//        matBench.setTexture("SpecularMap", assetManager.loadTexture("Models/Bench/specular.tga"));
//        bench.setMaterial(matBench);
//        bench.scale(0.05f);
//        bench.move(0, 0, 4);
//        bench.rotate(0, FastMath.DEG_TO_RAD*9, 0);
//        bench.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//        rootNode.attachChild(bench);
//        
//        //chair
//        Spatial chair = assetManager.loadModel("Models/Chair/chair.j3o");
//        Material matChair = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        matChair.setTexture("DiffuseMap", assetManager.loadTexture("Models/Chair/diffuse.tga"));
//        matChair.setTexture("NormalMap", assetManager.loadTexture("Models/Chair/normal.tga"));
//        matChair.setTexture("SpecularMap", assetManager.loadTexture("Models/Chair/specular.tga"));
//        chair.setMaterial(matChair);
//        chair.scale(0.16f);
//        chair.move(3, 1.7f, -4);
//        chair.rotate(0, FastMath.DEG_TO_RAD*-12, 0);
//        chair.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//        rootNode.attachChild(chair);
//        
//        //candle
//        Spatial candle = assetManager.loadModel("Models/Candle/candle.j3o");
//        Material matCandle = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        matCandle.setTexture("DiffuseMap", assetManager.loadTexture("Models/Candle/diffuse.tga"));
//        matCandle.setTexture("NormalMap", assetManager.loadTexture("Models/Candle/normal.tga"));
//        matCandle.setTexture("SpecularMap", assetManager.loadTexture("Models/Candle/specular.tga"));
//        candle.setMaterial(matCandle);
//        candle.scale(0.003f);
//        candle.move(0, 2.59f, 0);
//        candle.rotate(0, FastMath.DEG_TO_RAD*-12, 0);
//        candle.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//        rootNode.attachChild(candle);
        
        //load scene
        Node scene = (Node) assetManager.loadModel("Scenes/scene.j3o");
        spatials = scene.getChildren();
        scene.scale(0.05f);
        rootNode.attachChild(scene);
        
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
        
        //TEST
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
}
