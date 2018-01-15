package xal.extension.widgets.beaneditor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xal.tools.annotation.AProperty.NoEdit;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;

/** base class for a container of editable properties */
public class EditablePropertyContainer extends EditableProperty {
	/** target for child properties */
	final protected Object CHILD_TARGET;

	/** set of ancestors to reference to prevent cycles */
	final private Set<Object> ANCESTORS;
	
	/** list of child primitive properties */
	protected List<EditablePrimitiveProperty> _childPrimitiveProperties;

	/** list of child property containers */
	protected List<EditablePropertyContainer> _childPropertyContainers;


	/** Primary Constructor */
	protected EditablePropertyContainer( final String pathPrefix, final String name, final Object target, final PropertyDescriptor descriptor, final Object childTarget, final Set<Object> ancestors ) {
		super( pathPrefix, name, target, descriptor );

		CHILD_TARGET = childTarget;
		ANCESTORS = ancestors;
	}


	/** Constructor */
	protected EditablePropertyContainer( final String pathPrefix, final Object target, final PropertyDescriptor descriptor, final Object childTarget, final Set<Object> ancestors ) {
		this( pathPrefix, descriptor.getName(), target, descriptor, childTarget, ancestors );
	}


	/** Constructor */
	protected EditablePropertyContainer( final String pathPrefix, final Object target, final PropertyDescriptor descriptor, final Set<Object> ancestors ) {
		this( pathPrefix, target, descriptor, generateChildTarget( target, descriptor ), ancestors );
	}


	/** Create an instance with the specified root Object */
	static public EditablePropertyContainer getInstanceWithRoot( final String name, final Object rootObject ) {
		final Set<Object> ancestors = new HashSet<Object>();
		return new EditablePropertyContainer( "", name, null, null, rootObject, ancestors );
	}


	/** Generat the child target from the target and descriptor */
	static private Object generateChildTarget( final Object target, final PropertyDescriptor descriptor ) {
		try {
			final Method readMethod = descriptor.getReadMethod();
			return readMethod.invoke( target );
		}
		catch( Exception exception ) {
			return null;
		}
	}

	
	/** determine whether the property is a container */
	public boolean isContainer() {
		return true;
	}


	/** determine whether the property is a primitive */
	public boolean isPrimitive() {
		return false;
	}


	/** set the value */
	public void setValue( final Object value ) {
		throw new RuntimeException( "Usupported operation attempting to set the value of the editable property container: " + getPath() + " with value " + value );
	}


	/** determine whether this container has any child properties */
	public boolean isEmpty() {
		return getChildCount() == 0;
	}


	/** get the number of child properties */
	public int getChildCount() {
		generateChildPropertiesIfNeeded();
		return _childPrimitiveProperties.size() + _childPropertyContainers.size();
	}


	/** Get the child properties */
	public List<EditableProperty> getChildProperties() {
		generateChildPropertiesIfNeeded();
		final List<EditableProperty> properties = new ArrayList<>();
		properties.addAll( _childPrimitiveProperties );
		properties.addAll( _childPropertyContainers );
		return properties;
	}


	/** Get the list of child primitive properties */
	public List<EditablePrimitiveProperty> getChildPrimitiveProperties() {
		generateChildPropertiesIfNeeded();
		return _childPrimitiveProperties;
	}


	/** Get the list of child property containers */
	public List<EditablePropertyContainer> getChildPropertyContainers() {
		generateChildPropertiesIfNeeded();
		return _childPropertyContainers;
	}


	/** generate the child properties if needed */
	protected void generateChildPropertiesIfNeeded() {
		if ( _childPrimitiveProperties == null ) {
			generateChildProperties();
		}
	}


	/** Generate the child properties this container's child target */
	protected void generateChildProperties() {
		_childPrimitiveProperties = new ArrayList<>();
		_childPropertyContainers = new ArrayList<>();

		final PropertyDescriptor[] descriptors = getPropertyDescriptors( CHILD_TARGET );
		if ( descriptors != null ) { 
			for ( final PropertyDescriptor descriptor : descriptors ) {
				if ( descriptor.getPropertyType() != Class.class ) {
					generateChildPropertyForDescriptor( descriptor );
				}
			}
		}
	}


	/** Generate the child properties starting at the specified descriptor for this container's child target */
	protected void generateChildPropertyForDescriptor( final PropertyDescriptor descriptor ) {
		final Method getter = descriptor.getReadMethod();

		// include only properties if the getter exists and is not deprecated and not marked hidden
		if ( getter != null && getter.getAnnotation( Deprecated.class ) == null && getter.getAnnotation( NoEdit.class ) == null ) {
			final Class<?> propertyType = descriptor.getPropertyType();

			boolean primitive = false;
			
			if ( EDITABLE_PROPERTY_TYPES.contains( propertyType ) || propertyType.isEnum()) {
				// if the property is an editable primitive with both a getter and setter then return the primitive property instance otherwise null
				final Method setter = descriptor.getWriteMethod();
				// include only properties if the setter exists and is not deprecated (getter was already filtered in an enclosing block) and not marked hidden
				if ( setter != null && setter.getAnnotation( Deprecated.class ) == null && setter.getAnnotation( NoEdit.class ) == null ) {
					_childPrimitiveProperties.add( new EditablePrimitiveProperty( PATH, CHILD_TARGET, descriptor ) );
				}
				return;		// reached end of branch so we are done
			}
			else if ( propertyType == null ) {
				return;
			}
			else if ( propertyType.isArray() ) {
				// property is an array
				//			System.out.println( "Property type is array for target: " + CHILD_TARGET + " with descriptor: " + descriptor.getName() );
				return;
			}
			else {
				Object target = generateChildTarget( CHILD_TARGET, descriptor );
				
				if ( propertyType.equals(CovarianceMatrix.class) )
				{
					target =  new TwissCovarianceMatrixBridge((CovarianceMatrix)target);
				}
				
				// property is a plain container
				if ( !ANCESTORS.contains( target ) ) {	// only propagate down the branch if the targets are unique (avoid cycles)
					final Set<Object> ancestors = new HashSet<Object>( ANCESTORS );
					ancestors.add( target );
					final EditablePropertyContainer container = new EditablePropertyContainer( PATH, CHILD_TARGET, descriptor, target, ancestors );
					if ( container.getChildCount() > 0 ) {	// only care about containers that lead to editable properties
						_childPropertyContainers.add( container );
					}
				}
			
				return;
			}
		}
	}


	/** Get a string represenation of this property */
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append( getPath() + ":\n" );
		for ( final EditableProperty property : getChildProperties() ) {
			buffer.append( "\t" + property.toString() + "\n" );
		}
		return buffer.toString();
	}
}


class TwissCovarianceMatrixBridge {
	Twiss[] twiss;
	PhaseVector mean;
	CovarianceMatrix m;
	
	public TwissCovarianceMatrixBridge(CovarianceMatrix m) {
		twiss = m.computeTwiss();
		mean = m.getMean();
		this.m = m;
	}

	protected void update()
	{
		m.setMatrix( CovarianceMatrix.buildCovariance(twiss[0], twiss[1], twiss[2], mean).getArrayCopy() );
	}
	
	public void setXOffset(double X) {
		mean.setx(X);
		update();
	}

	public double getXOffset() {
		return mean.getx();
	}

	public void setYOffset(double Y) {
		mean.sety(Y);
		update();
	}

	public double getYOffset() {
		return mean.gety();
	}
	
	public double getAlphaX() {
		return twiss[0].getAlpha();
	}
	
	public double getAlphaY() {
		return twiss[1].getAlpha();
	}
	
	public double getAlphaZ() {
		return twiss[2].getAlpha();
	}
	
	public void setAlphaX(double alpha) {
		twiss[0].setTwiss(alpha, twiss[0].getBeta(), twiss[0].getEmittance());
		update();
	}
	
	public void setAlphaY(double alpha) {
		twiss[1].setTwiss(alpha, twiss[1].getBeta(), twiss[1].getEmittance());
		update();
	}

	public void setAlphaZ(double alpha) {
		twiss[2].setTwiss(alpha, twiss[2].getBeta(), twiss[2].getEmittance());
		update();
	}
	
	public double getBetaX() {
		return twiss[0].getBeta();
	}
	
	public double getBetaY() {
		return twiss[1].getBeta();
	}
	
	public double getBetaZ() {
		return twiss[2].getBeta();
	}
	
	public void setBetaX(double beta) {
		twiss[0].setTwiss(twiss[0].getAlpha(), beta, twiss[0].getEmittance());
		update();
	}
	
	public void setBetaY(double beta) {
		twiss[1].setTwiss(twiss[1].getAlpha(), beta, twiss[1].getEmittance());
		update();
	}

	public void setBetaZ(double beta) {
		twiss[2].setTwiss(twiss[2].getAlpha(), beta, twiss[2].getEmittance());
		update();
	}
	
	public double getEmitX() {
		return twiss[0].getEmittance();
	}
	
	public double getEmitY() {
		return twiss[1].getEmittance();
	}
	
	public double getEmitZ() {
		return twiss[2].getEmittance();
	}
	
	public void setEmitX(double emit) {
		twiss[0].setTwiss(twiss[0].getAlpha(), twiss[0].getBeta(), emit);
		update();
	}
	
	public void setEmitY(double emit) {
		twiss[1].setTwiss(twiss[1].getAlpha(), twiss[1].getBeta(), emit);
		update();
	}

	public void setEmitZ(double emit) {
		twiss[2].setTwiss(twiss[2].getAlpha(), twiss[2].getBeta(), emit);
		update();
	}	
}
