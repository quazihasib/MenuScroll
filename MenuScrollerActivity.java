import java.util.ArrayList;
import java.util.List;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ClickDetector;
import org.anddev.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;


/**
 * 
 * @author Knoll Florian
 * @email myfknoll@gmail.com
 *
 */
public class MenuScrollerActivity extends BaseGameActivity implements IScrollDetectorListener, IOnSceneTouchListener, IClickDetectorListener {
       
        // ===========================================================
        // Constants
        // ===========================================================
        protected static int CAMERA_WIDTH = 480;
        protected static int CAMERA_HEIGHT = 320;
 
        protected static int FONT_SIZE = 24;
        protected static int PADDING = 50;
        
        protected static int MENUITEMS = 7;
        
 
        // ===========================================================
        // Fields
        // ===========================================================
        private Scene mScene;
        private Camera mCamera;
 
        private Font mFont; 
        private BitmapTextureAtlas mFontTexture;     
        
        private BitmapTextureAtlas mMenuTextureAtlas;        
        private TextureRegion mMenuLeftTextureRegion;
        private TextureRegion mMenuRightTextureRegion;
        
        private Sprite menuleft;
        private Sprite menuright;
 
        // Scrolling
        private SurfaceScrollDetector mScrollDetector;
        private ClickDetector mClickDetector;
 
        private float mMinX = 0;
        private float mMaxX = 0;
        private float mCurrentX = 0;
        private int iItemClicked = -1;
        
        private Rectangle scrollBar;        
        private List<TextureRegion> columns = new ArrayList<TextureRegion>();

        // ===========================================================
        // Constructors
        // ===========================================================
 
        // ===========================================================
        // Getter & Setter
        // ===========================================================
 
        // ===========================================================
        // Methods for/from SuperClass/Interfaces
        // ===========================================================
 
        @Override
        public void onLoadResources() {
                // Paths
                FontFactory.setAssetBasePath("font/");
                BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
                // Font
                this.mFontTexture = new BitmapTextureAtlas(256, 256);
                this.mFont = FontFactory.createFromAsset(this.mFontTexture, this, "Plok.TTF", FONT_SIZE, true, Color.BLACK);
                this.mEngine.getTextureManager().loadTextures(this.mFontTexture);
                this.mEngine.getFontManager().loadFonts(this.mFont);
                
                //Images for the menu
                for (int i = 0; i < MENUITEMS; i++) {				
                	BitmapTextureAtlas mMenuBitmapTextureAtlas = new BitmapTextureAtlas(256,256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
            		TextureRegion mMenuTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuBitmapTextureAtlas, this, "menu"+i+".png", 0, 0);
                	
                	this.mEngine.getTextureManager().loadTexture(mMenuBitmapTextureAtlas);
                	columns.add(mMenuTextureRegion);
                	
                	
                }
                //Textures for menu arrows
                this.mMenuTextureAtlas = new BitmapTextureAtlas(128,128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
                this.mMenuLeftTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuTextureAtlas, this, "menu_left.png", 0, 0);
                this.mMenuRightTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuTextureAtlas, this, "menu_right.png",64, 0);
                this.mEngine.getTextureManager().loadTexture(mMenuTextureAtlas);
          
        }
 
        @Override
        public Engine onLoadEngine() {
                this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 
                final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new FillResolutionPolicy(), this.mCamera);
                engineOptions.getTouchOptions().setRunOnUpdateThread(true);
 
                final Engine engine = new Engine(engineOptions);
                return engine;
        }
 
        @Override
        public Scene onLoadScene() {
                this.mEngine.registerUpdateHandler(new FPSLogger());
 
                this.mScene = new Scene();
                this.mScene.setBackground(new ColorBackground(0, 0, 0));
               
                this.mScrollDetector = new SurfaceScrollDetector(this);
                this.mClickDetector = new ClickDetector(this);
 
                this.mScene.setOnSceneTouchListener(this);
                this.mScene.setTouchAreaBindingEnabled(true);
                this.mScene.setOnSceneTouchListenerBindingEnabled(true);
 
                CreateMenuBoxes();
 
                return this.mScene;
 
        }
 
        @Override
        public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
                this.mClickDetector.onTouchEvent(pSceneTouchEvent);
                this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
                return true;
        }
 
        @Override
        public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent, final float pDistanceX, final float pDistanceY) {

        		//Disable the menu arrows left and right (15px padding)
	        	if(mCamera.getMinX()<=15)
	             	menuleft.setVisible(false);
	             else
	             	menuleft.setVisible(true);
	        	 
	        	 if(mCamera.getMinX()>mMaxX-15)
		             menuright.setVisible(false);
		         else
		        	 menuright.setVisible(true);
	             	
                //Return if ends are reached
                if ( ((mCurrentX - pDistanceX) < mMinX)  ){                	
                    return;
                }else if((mCurrentX - pDistanceX) > mMaxX){
                	
                	return;
                }
                
                //Center camera to the current point
                this.mCamera.offsetCenter(-pDistanceX,0 );
                mCurrentX -= pDistanceX;
                	
               
                //Set the scrollbar with the camera
                float tempX =mCamera.getCenterX()-CAMERA_WIDTH/2;
                // add the % part to the position
                tempX+= (tempX/(mMaxX+CAMERA_WIDTH))*CAMERA_WIDTH;      
                //set the position
                scrollBar.setPosition(tempX, scrollBar.getY());
                
                //set the arrows for left and right
                menuright.setPosition(mCamera.getCenterX()+CAMERA_WIDTH/2-menuright.getWidth(),menuright.getY());
                menuleft.setPosition(mCamera.getCenterX()-CAMERA_WIDTH/2,menuleft.getY());
                
              
                
                //Because Camera can have negativ X values, so set to 0
            	if(this.mCamera.getMinX()<0){
            		this.mCamera.offsetCenter(0,0 );
            		mCurrentX=0;
            	}
            	
 
        }
 
        @Override
        public void onClick(ClickDetector pClickDetector, TouchEvent pTouchEvent) {
                loadLevel(iItemClicked);
        };
 
        // ===========================================================
        // Methods
        // ===========================================================
        
        private void CreateMenuBoxes() {
        	
             int spriteX = PADDING;
        	 int spriteY = PADDING;
        	 
        	 //current item counter
             int iItem = 1;

        	 for (int x = 0; x < columns.size(); x++) {
        		 
        		 //On Touch, save the clicked item in case it's a click and not a scroll.
                 final int itemToLoad = iItem;
        		 
        		 Sprite sprite = new Sprite(spriteX,spriteY,columns.get(x)){
        			 
        			 public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                         iItemClicked = itemToLoad;
                         return false;
        			 }        			 
        		 };        		 
        		 iItem++;
        		
        		 
        		 this.mScene.attachChild(sprite);        		 
        		 this.mScene.registerTouchArea(sprite);        		 
   
        		 spriteX += 20 + PADDING+sprite.getWidth();
			}
        	
        	 mMaxX = spriteX - CAMERA_WIDTH;
        	 
        	 //set the size of the scrollbar
        	 float scrollbarsize = CAMERA_WIDTH/((mMaxX+CAMERA_WIDTH)/CAMERA_WIDTH);
        	 scrollBar = new Rectangle(0,CAMERA_HEIGHT-20,scrollbarsize, 20);
        	 scrollBar.setColor(1,0,0);
        	 this.mScene.attachChild(scrollBar);
        	 
        	 menuleft = new Sprite(0,CAMERA_HEIGHT/2-mMenuLeftTextureRegion.getHeight()/2,mMenuLeftTextureRegion);
        	 menuright = new Sprite(CAMERA_WIDTH-mMenuRightTextureRegion.getWidth(),CAMERA_HEIGHT/2-mMenuRightTextureRegion.getHeight()/2,mMenuRightTextureRegion);
        	 this.mScene.attachChild(menuright);
        	 menuleft.setVisible(false);
        	 this.mScene.attachChild(menuleft);
        }
        
        
 
        @Override
        public void onLoadComplete() {
 
        }
 
        //Here is where you call the item load.
        private void loadLevel(final int iLevel) {
                if (iLevel != -1) {
                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                			
                                        Toast.makeText(MenuScrollerActivity.this, "Load Item" + String.valueOf(iLevel), Toast.LENGTH_SHORT).show();
                                        iItemClicked = -1;
                                }
                        });
                }
        }
}
 