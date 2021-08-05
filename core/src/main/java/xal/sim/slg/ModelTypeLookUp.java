/*
 * ModelTypeLookUp.java
 *
 * Created on April 2, 2003, 4:57 PM
 */
package xal.sim.slg;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A lookup table that maps element ids as used by this lattice generator to
 * element ids as used by the on-line model for its xml presentation.
 *
 * @author wdklotz
 */
public class ModelTypeLookUp implements Map<String, String> {

    private static final Map<String, String> map;

    static {
        map = new HashMap<>();
        map.put("marker", "Marker");
        map.put("pmarker", "Marker");
        map.put("VIW", "Marker");
        map.put("Harp", "Marker");
        map.put("Foil", "Marker");
        map.put("Tgt", "Marker");
        map.put("drift", "IdealDrift");
        map.put("dipole", "ThickDipole");
        map.put("quadrupole", "IdealMagQuad");
        map.put("sextupole", "IdealMagSextupole");
        map.put("octupole", "IdealMagOct");
        map.put("hsteerer", "IdealMagSteeringDipole");
        map.put("vsteerer", "IdealMagSteeringDipole");
        map.put("skewquadrupole", "SkewMagQuad");
        map.put("skewsextupole", "SkewMagSex");
        map.put("beampositionmonitor", "Marker");
        map.put("beamcurrentmonitor", "Marker");
        map.put("beamlossmonitor", "Marker");
        map.put("rfgap", "IdealRfGap");
        map.put("wirescanner", "Marker");
    }


    @Override
    public void clear() {
        // Do nothing
    }

    @Override
    public boolean containsKey(Object obj) {
        return map.containsKey(obj);
    }

    @Override
    public boolean containsValue(Object obj) {
        return map.containsValue(obj);
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String get(final Object key) {
        return map.get(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public String put(String obj, String obj1) {
        return null;
    }

    /**
     *
     * @param map
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        // Do nothing
    }

    @Override
    public String remove(Object obj) {
        return null;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<String> values() {
        return map.values();
    }

    public String valueForKey(String key) {
        return get(key);
    }

}
