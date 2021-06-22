/*
 * ValueTransformFactory.java
 *
 * Created on September 23, 2002, 1:46 PM
 */

package xal.tools.transforms;

/**
 * Factory for instantiating DataTransforms implementing common transformations
 * such as linear, scale and offset transformations.
 *
 * @author  tap
 */
public class DataTransformFactory {    
    /** Creates a new instance of ValueTransformFactory */
    protected DataTransformFactory() {
    }    
    
    
    /** Transform that does nothing.  Suitable as a default transform. */
    public static DataTransform noOperationTransform() {
        return DataTransform.NO_OPERATION_TRANSFORM;
    }
    
        
    // ------ DoubleTransforms ----------------------------------------------------
    
    /** Convert double values with a simple scale conversion */
    public static DoubleTransform doubleScaleTransform(final double scale) {
        return new DoubleTransformAdaptor() {
            @Override
            public double convertFromRaw(double rawValue) {
                return scale * rawValue;
            }
            
            @Override
            public double convertToRaw(double physicalValue) {
                return physicalValue / scale;
            }
        };
    }
    
    
    /** Convert double values with a simple translation conversion */
    public static DoubleTransform doubleTranslationTransform(final double offset) {
        return new DoubleTransformAdaptor() {
            @Override
            public double convertFromRaw(double rawValue) {
                return rawValue + offset;
            }
            
            @Override
            public double convertToRaw(double physicalValue) {
                return physicalValue - offset;
            }
        };
    }
    
    
    /** Convert double values with a simple linear conversion */
    public static DoubleTransform doubleLinearTransform(final double scale, final double offset) {
        return new DoubleTransformAdaptor() {
            @Override
            public double convertFromRaw(double rawValue) {
                return scale * rawValue + offset;
            }
            
            @Override
            public double convertToRaw(double physicalValue) {
                return (physicalValue - offset) / scale;
            }
        };
    }
    
    // ------ End DoubleTransforms -----------------------------------------------------
    
    
    // ------ DoubleArrayTransforms ----------------------------------------------------
    
    /** Convert a double precision array with a simple scale conversion */
    public static DoubleArrayTransform doubleArrayScaleTransform(final double scale) {
        return new DoubleArrayTransformAdaptor() {
            @Override
            public double[] convertFromRaw(double[] rawArray) {
                double[] physicalArray = new double[rawArray.length];
                
                for( int index = 0 ; index < rawArray.length ; index++ ) {
                    physicalArray[index] = scale * rawArray[index];
                }
                
                return physicalArray;
            }
            
            @Override
            public double[] convertToRaw(double[] physicalArray) {
                double[] rawArray = new double[physicalArray.length];
                
                for( int index = 0 ; index < physicalArray.length ; index++ ) {
                    rawArray[index] = physicalArray[index] / scale;
                }
                
                return rawArray;
            }
        };
    }    

    
    /** Convert a double precision array with a simple translation conversion */
    public static DoubleArrayTransform doubleArrayTranslationTransform(final double offset) {
        return new DoubleArrayTransformAdaptor() {
            @Override
            public double[] convertFromRaw(double[] rawArray) {
                double[] physicalArray = new double[rawArray.length];
                
                for( int index = 0 ; index < rawArray.length ; index++ ) {
                    physicalArray[index] = offset + rawArray[index];
                }
                
                return physicalArray;
            }
            
            @Override
            public double[] convertToRaw(double[] physicalArray) {
                double[] rawArray = new double[physicalArray.length];
                
                for( int index = 0 ; index < physicalArray.length ; index++ ) {
                    rawArray[index] = physicalArray[index] - offset;
                }
                
                return rawArray;
            }
        };
    }    
    

    /** Convert a double precision array with a simple linear conversion */
    public static DoubleArrayTransform doubleArrayLinearTransform(final double scale, final double offset) {
        return new DoubleArrayTransformAdaptor() {
            @Override
            public double[] convertFromRaw(double[] rawArray) {
                double[] physicalArray = new double[rawArray.length];
                
                for( int index = 0 ; index < rawArray.length ; index++ ) {
                    physicalArray[index] = offset + scale * rawArray[index];
                }
                
                return physicalArray;
            }
            
            @Override
            public double[] convertToRaw(double[] physicalArray) {
                double[] rawArray = new double[physicalArray.length];
                
                for( int index = 0 ; index < physicalArray.length ; index++ ) {
                    rawArray[index] = (physicalArray[index] - offset) / scale;
                }
                
                return rawArray;
            }
        };
    }    
    

    // ------ End DoubleArrayTransforms ------------------------------------------------
}
