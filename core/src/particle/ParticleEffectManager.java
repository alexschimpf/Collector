package particle;

import java.util.HashMap;

public final class ParticleEffectManager {

	private static ParticleEffectManager instance;
	
	private final HashMap<String, ParticleEffect> _particleEffectMap = new HashMap<String, ParticleEffect>();
	
	public static ParticleEffectManager getInstance() {
		if(instance == null) {
			instance = new ParticleEffectManager();
		}
		
		return instance;
	}
	
	public ParticleEffectManager() {
		
	}
	
	public ParticleEffect getParticleEffect(String key, float x, float y) {
		ParticleEffect particleEffect = _particleEffectMap.get(key);
		ParticleEffect particleEffectClone = particleEffect.clone();
		particleEffectClone.position(x, y);
		
		return particleEffectClone;
	}
	
	public void startParticleEffect(String key, float x, float y) {
		ParticleEffect particleEffect = getParticleEffect(key, x, y);
		particleEffect.addToScreen();
	}
	
	public void addParticleEffect(String key, ParticleEffect particleEffect) {
		_particleEffectMap.put(key, particleEffect);
	}
}
