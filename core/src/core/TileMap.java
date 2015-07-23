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
	
	private final OrthogonalTiledMapRenderer _renderer;	
	private final TiledMap _tileMap;
	private final HashMap<String, TiledMapTileLayer> _layerMap = new HashMap<String, TiledMapTileLayer>();
	
	public TileMap(String tileMapName) {
		Parameters tileMapParams = new Parameters();
		tileMapParams.flipY = false;
		_tileMap = new TmxMapLoader().load(tileMapName, tileMapParams);
		_renderer = new OrthogonalTiledMapRenderer(_tileMap, Globals.getCamera().getTileMapScale());

		for(MapLayer layer : _tileMap.getLayers()) {
			if(!(layer instanceof TiledMapTileLayer)) {
				continue;
			}
			
			String layerName = layer.getName();
			_layerMap.put(layerName, (TiledMapTileLayer)layer);
		}
	}
	
	public void setView(OrthographicCamera camera) {
		_renderer.setView(camera);
	}
	
	public void render(TileMapLayerType layerType, SpriteBatch spriteBatch) {
		String layerName = _getLayerName(layerType);
		TiledMapTileLayer layer = _layerMap.get(layerName);
		
		getBatch().begin();
		_renderer.renderTileLayer(layer);
		getBatch().end();
	}
	
	public void render(TileMapLayerType[] layerTypes, SpriteBatch spriteBatch) {
		getBatch().begin();
		for(TileMapLayerType layerType : layerTypes) {
			String layerName = _getLayerName(layerType);
			TiledMapTileLayer layer = _layerMap.get(layerName);
			_renderer.renderTileLayer(layer);
		}
		getBatch().end();
	}
	
	public TiledMap getRawTileMap() {
		return _tileMap;
	}
	
	public Batch getBatch() {
		return _renderer.getBatch();
	}
	
	private String _getLayerName(TileMapLayerType layerType) {
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
