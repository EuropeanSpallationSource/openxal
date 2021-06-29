/*
 * DoubleTransformAdaptor.java
 *
 * Created on March 13, 2003, 10:34 AM
 */

package xal.tools.transforms;

import xal.tools.ArrayValue;

/**
 * An abstract class that provides a convenient base for classes implementing DoubleTransform.
 * The developer need only implement the non-inherited methods of the DoubleTransform interface.
 * Even if you do not want to subclass this class, it provides a static helper method from 
 * implementing the valueTransform() method required by DataTransform.
 *
 * @author  tap
 * @see DataTransformFactory
 */
public abstract class DoubleTransformAdaptor implements DoubleTransform {
    /** DoubleTransform interface */
    @Override
    public abstract double convertFromRaw(double rawValue);
    
    /** DoubleTransform interface */
    @Override
    public abstract double convertToRaw(double physicalValue);
    
    /** Implement DataTransform interface */
    @Override
    public final ValueTransform valueTransform() {
        return implementValueTransform(this);
    }
    
    
    /** 
     * Helper method for implementing the valueTransform() DataTransform method 
     * for a DoubleTransform implementation.
     */
    public static ValueTransform implementValueTransform(final DoubleTransform transform) {
        return new ValueTransform() {
            @Override
            public final ArrayValue convertFromRaw(ArrayValue rawArray) {
                double rawValue = rawArray.doubleValue();
                double value = transform.convertFromRaw(rawValue);
                
                return ArrayValue.doubleStore(new double[] {value});
            }
            
            @Override
            public final ArrayValue convertToRaw(ArrayValue physicalArray) {
                double value = physicalArray.doubleValue();
                double rawValue = transform.convertToRaw(value);
                
                return ArrayValue.doubleStore(new double[] {rawValue});
            }
        };
    }
}
