package xal.sim.scenario;

import xal.extension.jels.model.elem.JElsElementMapping;

public class PluginElementMapping {

    /**
     * This method is used to replace default element mapping with JELS element
     * mapping.
     *
     * @return JElsElementMapping
     */
    public static ElementMapping getInstance() {
        return new JElsElementMapping();
    }
}
