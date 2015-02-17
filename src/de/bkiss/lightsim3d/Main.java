package de.bkiss.lightsim3d;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.shadow.DirectionalLightShadowRenderer;

/**
 * Main class for LightSim3D
 * @author Börge Kiss
 */
public class Main extends SimpleApplication {
    
    private CameraNode camNode;
    private Node camera;
    
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    

    @Override
    public void simpleInitApp() {
        
        //setup
        flyCam.setMoveSpeed(10f);
        flyCam.setEnabled(true);
        
        //table
        Spatial table = assetManager.loadModel("Models/Table/table.obj");
        Material matTable = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matTable.setTexture("DiffuseMap", assetManager.loadTexture("Models/Table/diffuse.tga"));
        matTable.setTexture("NormalMap", assetManager.loadTexture("Models/Table/normal.tga"));
        matTable.setTexture("SpecularMap", assetManager.loadTexture("Models/Table/specular.tga"));
        table.setMaterial(matTable);
        table.scale(0.05f);
        table.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(table);
        
        //bench
        Spatial bench = assetManager.loadModel("Models/Bench/bench.obj");
        Material matBench = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matBench.setTexture("DiffuseMap", assetManager.loadTexture("Models/Bench/diffuse.tga"));
        matBench.setTexture("NormalMap", assetManager.loadTexture("Models/Bench/normal.tga"));
        matBench.setTexture("SpecularMap", assetManager.loadTexture("Models/Bench/specular.tga"));
        bench.setMaterial(matBench);
        bench.scale(0.05f);
        bench.move(-1, 0, 4);
        bench.rotate(0, FastMath.DEG_TO_RAD*9, 0);
        bench.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(bench);
        
        //chair
        Spatial chair = assetManager.loadModel("Models/Chair/chair.obj");
        Material matChair = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matChair.setTexture("DiffuseMap", assetManager.loadTexture("Models/Chair/diffuse.tga"));
        matChair.setTexture("NormalMap", assetManager.loadTexture("Models/Chair/normal.tga"));
        matChair.setTexture("SpecularMap", assetManager.loadTexture("Models/Chair/specular.tga"));
        chair.setMaterial(matChair);
        chair.scale(0.16f);
        chair.move(2, 1.8f, -4);
        chair.rotate(0, FastMath.DEG_TO_RAD*9, 0);
        chair.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        rootNode.attachChild(chair);
        
        //ambient light
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient); 
        
        //sunlight
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.3f, -0.5f)).normalizeLocal());
        sun.setColor(new ColorRGBA(1f,1f,0.95f,1f));
        rootNode.addLight(sun); 
        
        //shadow renderer
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 2048, 4);
        dlsr.setLight(sun);
        viewPort.addProcessor(dlsr); 
        
        //camera node
//        camera = new Node();
//        flyCam.setEnabled(false);
//        camNode = new CameraNode("Camera Node", cam);
//        camNode.setControlDir(ControlDirection.SpatialToCamera);
//        camera.attachChild(camNode);
//        camNode.setLocalTranslation(new Vector3f(0, 6, -8));
//        camNode.lookAt(table.getLocalTranslation().add(0, 2, 0), Vector3f.UNIT_Y);
//        rootNode.attachChild(camera);
    }

    @Override
    public void simpleUpdate(float tpf) {
//        camera.rotate(0, FastMath.DEG_TO_RAD*20*tpf, 0);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
