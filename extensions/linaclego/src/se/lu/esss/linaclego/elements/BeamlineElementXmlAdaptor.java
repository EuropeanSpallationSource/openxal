package se.lu.esss.linaclego.elements;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BeamlineElementXmlAdaptor extends XmlAdapter<BeamlineElement, BeamlineElement> {
	protected Map<String, Class<? extends BeamlineElement>> bles = new HashMap<>();
	
	public BeamlineElementXmlAdaptor()
	{
		add(Drift.class, Quad.class, ThinSteering.class, RfGap.class, DtlCell.class, Edge.class, Bend.class, FieldMap.class);
	}
	
	
	@SafeVarargs
	protected final void add(Class<? extends BeamlineElement>... bless) 
	{
		for (Class<? extends BeamlineElement> ble : bless) {
			bles.put(ble.getAnnotation(XmlType.class).name(), ble);
		}
	}
	
	@Override
	public BeamlineElement marshal(BeamlineElement v) throws Exception {
		v.setType(v.getClass().getAnnotation(XmlType.class).name());
		return v;
	}

	@Override
	public BeamlineElement unmarshal(BeamlineElement v) throws Exception {
		BeamlineElement v2 = bles.get(v.getType()).newInstance();
		v2.setId(v.getId());
		v2.setModel(v.getModel());
		v2.getParameters().addAll(v.getParameters());
		return v2;
	}
}
