package core;

import java.util.HashMap;

import misc.Globals;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TmxMapLoader.Parameters;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public final class TileMap {

	public enum TileMapLayerType {
		FOREGROUND,
		NORMAL,
		ENCLOSING,
		BACKGROUND
	}
	
	private final OrthogonalTiledMapRenderer RENDERER;	
	private final TiledMap TILE_MAP;
	private final HashMap<String, TiledMapTileLayer> LAYER_MAP = new HashMap<String, TiledMapTileLayer>();
	
	public TileMap(String tileMapName) {
		Parameters tileMapParams = new Parameters();
		tileMapParams.flipY = false;
		TILE_MAP = new TmxMapLoader().load(tileMapName, tileMapParams);
		RENDERER = new OrthogonalTiledMapRenderer(TILE_MAP, Globals.getCamera().getTileMapScale());

		for(MapLayer layer : TILE_MAP.getLayers()) {
			if(!(layer instanceof TiledMapTileLayer)) {
				continue;
			}
			
			String layerName = layer.getName();
			LAYER_MAP.put(layerName, (TiledMapTileLayer)layer);
		}
	}
	
	public void setView(OrthographicCamera camera) {
		RENDERER.setView(camera);
	}
	
	public void render(TileMapLayerType layerType, SpriteBatch spriteBatch) {
		String layerName = getLayerName(layerType);
		TiledMapTileLayer layer = LAYER_MAP.get(layerName);
		
		getBatch().begin();
		RENDERER.renderTileLayer(layer);
		getBatch().end();
	}
	
	public void render(TileMapLayerType[] layerTypes, SpriteBatch spriteBatch) {
		getBatch().begin();
		for(TileMapLayerType layerType : layerTypes) {
			String layerName = getLayerName(layerType);
			TiledMapTileLayer layer = LAYER_MAP.get(layerName);
			RENDERER.renderTileLayer(layer);
		}
		getBatch().end();
	}
	
	public TiledMap getRawTileMap() {
		return TILE_MAP;
	}
	
	public Batch getBatch() {
		return RENDERER.getBatch();
	}
	
	private String getLayerName(TileMapLayerType layerType) {
		switch(layerType) {
			case FOREGROUND:
				return "Foreground";
			case NORMAL:
				return "Normal";
			case ENCLOSING:
				return "Enclosing";
			case BACKGROUND:
				return "Background";
			default:
				return null;
		}
	}
}
