//
//  JSONCoder.java
//  xal
//
//  Created by Tom Pelaia on 6/16/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.coding.json;

import xal.tools.coding.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.*;
import java.io.*;


/** encode and decode objects with JSON */
public class JSONCoder implements Coder {
    /** default coder */
    static JSONCoder DEFAULT_CODER;
    
    /** adaptors between all custom types and representation JSON types */
    final MutableConversionAdaptorStore CONVERSION_ADAPTOR_STORE;
    
    
    // static initializer
    static {
        DEFAULT_CODER = new JSONCoder( true );
    }
    
    
    /** get a new JSON Coder only if you need to customize it, otherwise use the static methods to encode/decode */
    static public JSONCoder getInstance() {
        return new JSONCoder( false );
    }
    
    
    /** Constructor to be called for the default coder */
    private JSONCoder( final boolean isDefault ) {
        CONVERSION_ADAPTOR_STORE = isDefault ? new MutableConversionAdaptorStore( true ) : new MutableConversionAdaptorStore( DEFAULT_CODER.CONVERSION_ADAPTOR_STORE );
    }
    
    
    /** Get a list of types (including JSON standard types plus default extensions) which are supported for coding and decoding */
    static public List<String> getDefaultTypes() {
        return DEFAULT_CODER.getSupportedTypes();
    }
    
    
    /** Get a list of the standard types encoded directly into JSON */
    static public List<String> getStandardTypes() {
        return ConversionAdaptorStore.getStandardTypes();
    }
    
    
    /** Determine whether the specified type is a standard JSON type */
    static public boolean isStandardType( final String type ) {
        return ConversionAdaptorStore.isStandardType( type );
    }
    
    
    /** Get a list of all types which are supported for coding and decoding */
    public List<String> getSupportedTypes() {
        return CONVERSION_ADAPTOR_STORE.getSupportedTypes();
    }
    
    
    /** Get a list of types which extend beyond the JSON standard types */
    public List<String> getExtendedTypes() {
        return CONVERSION_ADAPTOR_STORE.getExtendedTypes();
    }
    
    
    /** 
     * Register the custom type by class and its associated adaptor 
     * @param type type to identify and process for encoding and decoding
     * @param adaptor translator between the custom type and representation JSON constructs
     */
    public <CustomType,RepresentationType> void registerType( final Class<CustomType> type, final ConversionAdaptor<CustomType,RepresentationType> adaptor ) {
        CONVERSION_ADAPTOR_STORE.registerType( type, adaptor );
    }
    
    
    /** Get the conversion adaptor for the given value */
    protected ConversionAdaptor<?,?> getConversionAdaptor( final String valueType ) {
        return CONVERSION_ADAPTOR_STORE.getConversionAdaptor( valueType );
    }
    
    
	/** 
	 * Decode the JSON string using the default coder
	 * @param archive JSON string representation of an object
	 * @return an object with the data described in the archive
	 */
	static public Object defaultDecode( final String archive ) {
        return DEFAULT_CODER.decode( archive );
	}
    
    
	/** 
	 * Decode the JSON string
	 * @param archive JSON string representation of an object
	 * @return an object with the data described in the archive
	 */
    public Object decode( final String archive ) {
        final AbstractDecoder<?> decoder = AbstractDecoder.getInstance( archive, new ConversionAdaptorStore( CONVERSION_ADAPTOR_STORE ) );
		return decoder != null ? decoder.decode() : null;
    }
	
	
	/** 
     * Encode the object as a JSON string using the default encoder
     * @param value the object to encode
     * @return a JSON string representing the value
     */
    static public String defaultEncode( final Object value ) {
        return DEFAULT_CODER.encode( value );
	}
	
	
	/** 
     * Encode the object as a JSON string 
     * @param value the object to encode
     * @return a JSON string representing the value
     */
    public String encode( final Object value ) {
        return AbstractEncoder.encode( value, new ConversionAdaptorStore( CONVERSION_ADAPTOR_STORE ) );
	}
}



/** Base class of encoders */
abstract class AbstractEncoder {
    /** encode the value using the coder */
    static public String encode( final Object value, final ConversionAdaptorStore conversionAdaptorStore ) {
        return record( value, conversionAdaptorStore ).encode();
    }
    
    /** encode the archived value to JSON */
    abstract public String encode();
    
    
    /** Mark that this encoder is a member of a collection of the specified type. Does nothing by default, but subclasses may have a custom behavior. */
    public void setIsMemberOfCollectionWithType( final String collectionType ) {}

    
    /** record the value for encoding later */
    static public AbstractEncoder record( final Object value, final ConversionAdaptorStore conversionAdaptorStore ) {
        return record( value, conversionAdaptorStore, new ReferenceStore() );
    }
    
    
    /** record a value for encoding later and store references in the supplied store */
    @SuppressWarnings( "unchecked" )    // no way to guarantee at compile time conversion types
    static protected AbstractEncoder record( final Object value, final ConversionAdaptorStore conversionAdaptorStore, final ReferenceStore referenceStore ) {
        final Class<?> valueClass = value != null ? value.getClass() : null;
        
		if ( valueClass == null ) {
			return NullEncoder.getInstance();
		}
        else if ( valueClass.equals( Boolean.class ) ) {
            return new BooleanEncoder( (Boolean)value );
        }
		// handle each immediate concrete subclass of Number
		else if ( valueClass.equals( JSONNumber.class ) ) {
			return new NumberEncoder( (JSONNumber)value );
		}
        else if ( valueClass.equals( String.class ) ) {
            final String stringValue = (String)value;
            final IdentityReference<?> reference = StringEncoder.allowsReference( stringValue ) ? referenceStore.store( value ) : null;
            return reference != null && reference.hasMultiple() ? new ReferenceEncoder( reference.getID() ) : new StringEncoder( stringValue, reference );
        }
        else {      // these are the ones that support references
            final IdentityReference<?> reference = referenceStore.store( value );
            if ( reference.hasMultiple() ) {
                return new ReferenceEncoder( reference.getID() );
            }
            else if ( valueClass.equals( HashMap.class ) ) {  // no way to check at compile time that the key type is string
                return new DictionaryEncoder( (HashMap<String,Object>)value, conversionAdaptorStore, reference, referenceStore );
            }
            else if ( valueClass.isArray() ) {
                return ArrayEncoder.getInstance( value, conversionAdaptorStore, reference, referenceStore );
            }
            else if ( conversionAdaptorStore.isExtendedClass( valueClass ) ) {  // if the type is not among the standard ones then look to extensions
                return new ExtensionEncoder( value, conversionAdaptorStore, reference, referenceStore );
            }
            else if ( value instanceof Serializable ) {
                return new SerializationEncoder( value, conversionAdaptorStore, reference, referenceStore );
            }
            else {
                throw new RuntimeException( "No JSON support for the object of type: " + valueClass );
            }
        }
    }
}



/** Base class of encoders for hard objects (not references) */
abstract class HardEncoder<DataType> extends AbstractEncoder {
    /** value to encode */
    final protected DataType VALUE;
    
    
    /** Constructor */
    public HardEncoder( final DataType value ) {
        VALUE = value;
    }
}



/** Base class of encoders for objects that support references */
abstract class SoftValueEncoder extends AbstractEncoder {
    /** key identifies an object that is referenced */
    static final public String OBJECT_ID_KEY = "__XALID";
    
    /** key for the referenced value */
    static final public String VALUE_KEY = "value";

    /** reference to this value */
    final private IdentityReference<?> REFERENCE;
    
    
    /** Constructor */
    public SoftValueEncoder( final IdentityReference<?> reference ) {
        REFERENCE = reference;
    }
    
    
    /** encode an object ID and value if there are multiple references to the value, otherwise just encode the value */
    public String encode() {
        if ( REFERENCE.hasMultiple() ) {
            final String valueEncoding = encodeValueForReference();
            return DictionaryEncoder.encodeKeyValueStringPairs( new KeyValueStringPair( OBJECT_ID_KEY, NumberEncoder.encode( REFERENCE.getID() ) ), new KeyValueStringPair( VALUE_KEY, valueEncoding ) );
        }
        else {
            return encodeValue();
        }
    }
    
    
    /** Encode the value for a reference which by default just encodes the value. Subclasses may override for custom behavior. */
    protected String encodeValueForReference() {
        return encodeValue();
    }
    
    
    /** encode just the value */
    abstract String encodeValue();
}


/** encoder for references */
class ReferenceEncoder extends AbstractEncoder {
    /** key to indicate a reference */
    static final public String REFERENCE_KEY = "__XALREF";
    
    /** ID of referenced object */
    final private long REFERENCE_ID;
    
    
    /** Constructor */
    public ReferenceEncoder( final long referenceID ) {
        REFERENCE_ID = referenceID;
    }
    
    
    /** encode the reference to JSON */
    public String encode() {
        return DictionaryEncoder.encodeKeyValueStringPairs( new KeyValueStringPair( REFERENCE_KEY, NumberEncoder.encode( REFERENCE_ID ) ) );
    }
}



/** encode a null to JSON */
class NullEncoder extends AbstractEncoder {
    /** encoder singleton */
    static final private NullEncoder SHARED_ENCODER;
    
    
    // static initializer
    static {
        SHARED_ENCODER = new NullEncoder();
    }
    
    
    /** get the shared instance */
    static public NullEncoder getInstance() {
        return SHARED_ENCODER;
    }
    
    
    /** Constructor */
    private NullEncoder() {}
    
    
    /** encode the archived value to JSON */
    public String encode() {
        return "null";
    }
}



/** encode a string to JSON */
class StringEncoder extends SoftValueEncoder {
    /** value to encode */
    final private String VALUE;
    
    
    /** Constructor */
    public StringEncoder( final String value, final IdentityReference<?> reference ) {
        super( reference );
        VALUE = value;
    }
    
    
    /** determine whether the string allows referencing */
    static public boolean allowsReference( final String value ) {
        return value.length() > 20;     // don't bother using references unless the string is long enough to warrant the overhead
    }
    
    
    /** encode an object ID and value if there are multiple references to the value, otherwise just encode the value */
    public String encode() {
        return allowsReference( VALUE ) ? super.encode() : encodeValue();
    }
    
    
    /** encode the archived value to JSON */
    public String encodeValue() {
        return encode( VALUE );
    }
    
    
    /** encode a string */
    static public String encode( final String value ) {
        return "\"" + value.replace( "\\", "\\\\" ).replace( "\"", "\\\"" ) + "\"";
    }
}



/** encode a boolean to JSON */
class BooleanEncoder extends HardEncoder<Boolean> {
    /** Constructor */
    public BooleanEncoder( final Boolean value ) {
        super( value );
    }
    
    
    /** encode the archived value to JSON */
    public String encode() {
		return VALUE.booleanValue() ? "true" : "false";
    }
}



/** encode a number to JSON */
class NumberEncoder extends HardEncoder<JSONNumber> {
    /** Constructor */
    public NumberEncoder( final JSONNumber value ) {
        super( value );
    }
    
    
    /** encode the archived value to JSON */
    public String encode() {
		return encode( VALUE );
    }
    
    
    /** encode a numeric value */
    static public String encode( final Number value ) {		// we will encode any number not just a JSONNumber
        return value.toString();
    }
}



/** encode a hash map to JSON */
class DictionaryEncoder extends SoftValueEncoder {
    /** map of item encoders keyed by the corresponding orignal map keys */
    final private Map<String,AbstractEncoder> ENCODER_MAP;
    
    
    /** Constructor */
    public DictionaryEncoder( final HashMap<String,Object> map, final ConversionAdaptorStore conversionAdaptorStore, final IdentityReference<?> reference, final ReferenceStore referenceStore ) {
        super( reference );
        
        ENCODER_MAP = new HashMap<String,AbstractEncoder>( map.size() );
        final Set<Map.Entry<String,Object>> entries = map.entrySet();
        for ( final Map.Entry<String,Object> entry : entries ) {
            final AbstractEncoder itemEncoder = AbstractEncoder.record( entry.getValue(), conversionAdaptorStore, referenceStore );
            ENCODER_MAP.put( entry.getKey(), itemEncoder );
        }
    }

    
    /** encode key value string pairs where the value is already encoded */
    static public String encodeKeyValueStringPairs( final KeyValueStringPair ... keyValuePairs ) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append( "{" );
        
        int index = 0;
        for ( final KeyValueStringPair keyValuePair : keyValuePairs ) {
            switch ( index ) {
                case 0:
                    break;
                default:
                    buffer.append( ", " );
                    break;
            }
            buffer.append( StringEncoder.encode( keyValuePair.KEY ) );
            buffer.append( ": " );
            buffer.append( keyValuePair.VALUE );
            ++index;
        }
        
        buffer.append( "}" );
        return buffer.toString();
    }
    
    
    /** encode the archived value to JSON */
    public String encodeValue() {
        final Set<Map.Entry<String,AbstractEncoder>> entries = ENCODER_MAP.entrySet();
        final KeyValueStringPair[] keyValuePairs = new KeyValueStringPair[ entries.size() ];
        int index = 0;
        for ( final Map.Entry<String,AbstractEncoder> entry : entries ) {
            final String key = entry.getKey();
            final AbstractEncoder itemEncoder = entry.getValue();
            final String value = itemEncoder.encode();
            keyValuePairs[index] = new KeyValueStringPair( key, value );
            ++index;
        }
        return encodeKeyValueStringPairs( keyValuePairs );
    }
    
    
    /** Get the item encoder for the specified key */
    protected AbstractEncoder getItemEncoderForKey( final String key ) {
        return ENCODER_MAP.get( key );
    }
}


/** encoder for extensions which piggybacks on the dictionary encoder */
class ExtensionEncoder extends DictionaryEncoder {
    /** custom key identifying a custom type translated in terms of JSON representations */
    static final public String EXTENDED_TYPE_KEY = "__XALTYPE";
    
    /** custom key identifying a custom value to translate in terms of JSON representations */
    static final public String EXTENDED_VALUE_KEY = "value";
    
    /** extended type of the value */
    final private String _extensionType;
    
    /** indicates whether this encoder represents a typed collection item matching the batch type. */
    private boolean _isTypedCollectionItem;
    
    
    /** Constructor */
    public ExtensionEncoder( final Object value, final ConversionAdaptorStore conversionAdaptorStore, final IdentityReference<?> reference, final ReferenceStore referenceStore ) {
        super( getValueRep( value, conversionAdaptorStore ), conversionAdaptorStore, reference, referenceStore );
        _extensionType = getValueType( value );
    }
    
    
    /** Mark that this encoder is a member of a collection of the specified type. If the type matches the extension type we may encode just the representation value (unless it is a reference). */
    public void setIsMemberOfCollectionWithType( final String collectionType ) {
        _isTypedCollectionItem = collectionType.equals( _extensionType );
    }
    
    
    /** get the value representation as a dictionary keyed for the extended type and value */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    static private HashMap<String,Object> getValueRep( final Object value, final ConversionAdaptorStore conversionAdaptorStore ) {
        final String valueType = getValueType( value );
        final ConversionAdaptor adaptor = conversionAdaptorStore.getConversionAdaptor( valueType );
        if ( adaptor != null ) {
            final HashMap<String,Object> valueRep = new HashMap<String,Object>();
            final Object representationValue = adaptor.toRepresentation( value );
            valueRep.put( EXTENDED_TYPE_KEY, valueType );
            valueRep.put( EXTENDED_VALUE_KEY, representationValue );
            return valueRep;
        }
        else {
            throw new RuntimeException( "No coder for encoding objects of type: " + valueType );
        }
    }
    
    
    /** get the value type for the specified value */
    static private String getValueType( final Object value ) {
        return value.getClass().getName();
    }
    
    
    /** get the extention type */
    public String getExtensionType() {
        return _extensionType;
    }
    
    
    /** get the representation encoder */
    public AbstractEncoder getRepresentationEncoder() {
        return getItemEncoderForKey( EXTENDED_VALUE_KEY );
    }
    
    
    /** Always encode the full extension (not just representation) when encoding a reference. */
    protected String encodeValueForReference() {
        return super.encodeValue();
    }
    
    
    /** encode just the representation value if it is part of a batched type, otherwise encode the full extension */
    public String encodeValue() {
        return _isTypedCollectionItem ? getRepresentationEncoder().encode() : super.encodeValue();
    }
}



/** encoder for serialized objects which piggybacks on the dictionary encoder */
class SerializationEncoder extends DictionaryEncoder {
    /** custom key identifying the serialization byte array */
    static final public String SERIALIZATION_VALUE_KEY = "__XALSERIALIZATION";
    
    
    /** Constructor */
    public SerializationEncoder( final Object value, final ConversionAdaptorStore conversionAdaptorStore, final IdentityReference<?> reference, final ReferenceStore referenceStore ) {
        super( getValueRep( value ), conversionAdaptorStore, reference, referenceStore );
    }
    
    
    /** get the serialization byte array */
    @SuppressWarnings( "unchecked" )
    static private HashMap<String,Object> getValueRep( final Object value ) {
        try {
            final HashMap<String,Object> valueRep = new HashMap<String,Object>();
            final ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutStream = new ObjectOutputStream( byteOutStream );
            objectOutStream.writeObject( value );
            objectOutStream.flush();
                        
            valueRep.put( SERIALIZATION_VALUE_KEY, byteOutStream.toByteArray() );
            
            objectOutStream.close();
            byteOutStream.close();
                        
            return valueRep;
        }
        catch ( IOException exception ) {
            throw new RuntimeException( "Exception serializing object: " + value , exception );
        }
    }
}



/** encoder for an array of items of a common extended type which piggybacks on the dictionary encoder */
class TypedArrayEncoder extends DictionaryEncoder {
    /** primitive type wrappers keyed by type */
    final static private Map<Class<?>,Class<?>> PRIMITIVE_TYPE_WRAPPERS;

    /** key for identifying the array data of an extended type */
    static final public String ARRAY_ITEM_TYPE_KEY = "__XALITEMTYPE";
    
    /** key for identifying the array data of an extended type */
    static final public String ARRAY_KEY = "array";
    
    /** primitive classes keyed by type name */
    static final private Map<String,Class<?>> PRIMITIVE_CLASSES;
    
    
    // static initializer
    static {
        PRIMITIVE_CLASSES = generatePrimitiveClassMap();
        PRIMITIVE_TYPE_WRAPPERS = populatePrimitiveTypeWrappers();
    }
    
    
    /** Constructor */
    public TypedArrayEncoder( final Object array, final ConversionAdaptorStore conversionAdaptorStore, final IdentityReference<?> reference, final ReferenceStore referenceStore ) {
        super( getArrayRep( array, conversionAdaptorStore ), conversionAdaptorStore, reference, referenceStore );
        final String componentType = getComponentObjectType( array );
        final ArrayEncoder arrayEncoder = (ArrayEncoder)getItemEncoderForKey( ARRAY_KEY );
        arrayEncoder.setComponentType( componentType );
    }
    
    
    /** get the value representation as a dictionary keyed for the array item type and generic object array */
    @SuppressWarnings( "unchecked" )
    static private HashMap<String,Object> getArrayRep( final Object array, final ConversionAdaptorStore conversionAdaptorStore ) {
        final String itemType = getComponentType( array );
        final HashMap<String,Object> arrayRep = new HashMap<String,Object>();
        final int arrayLength = Array.getLength( array );
        final Object[] objectArray = new Object[ arrayLength ];    // encode as a generic object array
        for ( int index = 0 ; index < arrayLength ; index++ ) {
            objectArray[index] = Array.get( array, index );
        }
        arrayRep.put( ARRAY_ITEM_TYPE_KEY, itemType );
        arrayRep.put( ARRAY_KEY, objectArray );
        return arrayRep;
    }
    
    
    /** Get the component type appropriate for an Object (e.g. wrapper for a primitive) */
    private static String getComponentObjectType( final Object array ) {
        final Class<?> componentClass = array.getClass().getComponentType();
        return getObjectTypeForClass( componentClass );
    }
    
    
    /** Get the type appropriate for an Object (e.g. wrapper for a primitive) of the specified raw type */
    public static String getObjectTypeForClass( final Class<?> rawClass ) {
        final Class<?> wrapperClass = PRIMITIVE_TYPE_WRAPPERS.get( rawClass );
        final Class<?> objectClass = wrapperClass != null ? wrapperClass : rawClass;
        return objectClass.getName();
    }
    
    
    /** get the raw component type for the specified array */
    static private String getComponentType( final Object array ) {
        return array.getClass().getComponentType().getName();
    }
    
    
    /** get the primitive type for the specified type name */
    static public Class<?> getPrimitiveType( final String typeName ) {
        return PRIMITIVE_CLASSES.get( typeName );
    }
    
    
    /** populate the table of primitive type wrappers */
    private static Map<Class<?>,Class<?>> populatePrimitiveTypeWrappers() {
        final Map<Class<?>,Class<?>> table = new Hashtable<Class<?>,Class<?>>();
        
        table.put( Integer.TYPE, Integer.class );
        table.put( Long.TYPE, Long.class );
        table.put( Short.TYPE, Short.class );
        table.put( Byte.TYPE, Byte.class );
        table.put( Character.TYPE, Character.class );
        table.put( Float.TYPE, Float.class );
        table.put( Double.TYPE, Double.class );
        table.put( Boolean.TYPE, Boolean.class );
        
        return table;
    }

    
    /** generate the table of primitive classes keyed by name */
    static private Map<String,Class<?>> generatePrimitiveClassMap() {
        final Map<String,Class<?>> classTable = new Hashtable<String,Class<?>>();
        registerPrimitiveType( classTable, Float.TYPE );
        registerPrimitiveType( classTable, Double.TYPE );
        registerPrimitiveType( classTable, Byte.TYPE );
        registerPrimitiveType( classTable, Character.TYPE );
        registerPrimitiveType( classTable, Short.TYPE );
        registerPrimitiveType( classTable, Integer.TYPE );
        registerPrimitiveType( classTable, Long.TYPE );
        return classTable;
    }
    
    
    /** register the primitive type in the table */
    static private void registerPrimitiveType( final Map<String,Class<?>> table, final Class<?> type ) {
        table.put( type.getName(), type );
    }
}



/** encode an array to JSON */
class ArrayEncoder extends SoftValueEncoder {
    /** array of encoders each of which corresponds to an item in the original array */
    final private AbstractEncoder[] ITEM_ENCODERS;
    
    
    /** Constructor */
    public ArrayEncoder( final Object array, final ConversionAdaptorStore conversionAdaptorStore, final IdentityReference<?> reference, final ReferenceStore referenceStore ) {
        super( reference );
        
        final int arrayLength = Array.getLength( array );
        ITEM_ENCODERS = new AbstractEncoder[ arrayLength ];
        for ( int index = 0 ; index < arrayLength ; index++ ) {
            ITEM_ENCODERS[index] = AbstractEncoder.record( Array.get( array, index ), conversionAdaptorStore, referenceStore );
        }
    }
    
    
    /** Set the component type */
    public void setComponentType( final String componentType ) {
        for ( final AbstractEncoder itemEncoder : ITEM_ENCODERS ) {
            itemEncoder.setIsMemberOfCollectionWithType( componentType );
        }
    }
    
    
    /** Get an instance that can encode arrays and efficiently arrays of extended types */
    static public SoftValueEncoder getInstance( final Object array, final ConversionAdaptorStore conversionAdaptorStore, final IdentityReference<?> reference, final ReferenceStore referenceStore ) {
        return isTypedArray( array ) ? new TypedArrayEncoder( array, conversionAdaptorStore, reference, referenceStore ) : new ArrayEncoder( array, conversionAdaptorStore, reference, referenceStore );
    }
    
    
    /** Determine whether the array is of a common extended type */
    static private boolean isTypedArray( final Object array ) {
        final Class<?> itemClass = array.getClass().getComponentType();
        return itemClass != null && itemClass != Object.class;
    }
    
    
    /** encode the archived value to JSON */
    public String encodeValue() {
        final int count = ITEM_ENCODERS.length;
        final StringBuffer buffer = new StringBuffer();
        buffer.append( "[" );
        for ( int index = 0 ; index < count ; index++ ) {
            switch ( index ) {
                case 0:
                    break;
                default:
                    buffer.append( ", " );
                    break;
            }
            final AbstractEncoder itemEncoder = ITEM_ENCODERS[index];
            buffer.append( itemEncoder.encode() );
        }
        buffer.append( "]" );
        return buffer.toString();
    }
}



/** Base class of decoders */
abstract class AbstractDecoder<DataType> {
	/** archive to parse */
	final protected String ARCHIVE;
	
	/** unparsed remainder of the source string after parsing */
	protected String _remainder;
	
	
	/** Constructor */
	protected AbstractDecoder( final String archive ) {
		ARCHIVE = archive;
	}
	
	
	/** decode the source to extract the next object */	
	abstract protected DataType decode();
	
	
	/** get the unparsed remainder of the source string */
	protected String getRemainder() {
		return _remainder;
	}
	
	
	/** check for a match of the archive against the specified pattern, update the remainder and return the matching string */
	protected String processMatch( final Pattern pattern ) {
		final Matcher matcher = pattern.matcher( ARCHIVE );
		matcher.find();
		final int nextIndex = matcher.end();
		_remainder = ARCHIVE.length() > nextIndex ? ARCHIVE.substring( nextIndex ) : null;
		return matcher.group();
	}
	
	
	/** Get a decoder for the archive */
	public static AbstractDecoder<?> getInstance( final String archive, final ConversionAdaptorStore conversionAdaptorStore ) {
        return AbstractDecoder.getInstance( archive, conversionAdaptorStore, new KeyedReferenceStore() );
	}	
	
	
	/** Get a decoder for the archive */
	protected static AbstractDecoder<?> getInstance( final String archive, final ConversionAdaptorStore conversionAdaptorStore, final KeyedReferenceStore referenceStore ) {
		final String source = archive.trim();
		if ( source.length() > 0 ) {
			final char firstChar = source.charAt( 0 );
			switch ( firstChar ) {
				case '+': case '-': case '.':
				case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
					return new NumberDecoder( source );
				case 't': case 'f':
					return new BooleanDecoder( source );
				case 'n':
					return new NullDecoder( source );
				case '\"':
					return new StringDecoder( source );
				case '[':
					return new ArrayDecoder( source, conversionAdaptorStore, referenceStore );
				case '{':
					return new DictionaryDecoder( source, conversionAdaptorStore, referenceStore );
				default:
					return null;
			}
		}
		else {
			return null;
		}
	}	
}



/** decode a number from a source string */
class NumberDecoder extends AbstractDecoder<JSONNumber> {
	/** pattern for matching a number */
	static final Pattern NUMBER_PATTERN;
	
	
	// static initializer
	static {
		NUMBER_PATTERN = Pattern.compile( "[+-]?((\\d+\\.?\\d*)|(\\.?\\d+))([eE][+-]?\\d+)?" );
	}
	
	
	/** Constructor */
	protected NumberDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected JSONNumber decode() {
		final String match = processMatch( NUMBER_PATTERN );
        // doubles always have a decimal point even if the fraction is zero, so the absence of a period indicates a long integer
        if ( match != null ) {
			return JSONNumber.valueOf( match );
        }
        else {
            return null;
        }
	}
}



/** decode a boolean from a source string */
class BooleanDecoder extends AbstractDecoder<Boolean> {
	/** pattern for matching booleans */
	static final Pattern BOOLEAN_PATTERN;
	
	
	// static initializer
	static {
		BOOLEAN_PATTERN = Pattern.compile( "(true)|(false)" );
	}
	
	
	/** Constructor */
	protected BooleanDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected Boolean decode() {
		final String match = processMatch( BOOLEAN_PATTERN );
		return match != null ? Boolean.valueOf( match ) : null;
	}
}



/** decode a null identifier from a source string */
class NullDecoder extends AbstractDecoder<Object> {
	/** pattern for matching the null identifier */
	static final Pattern NULL_PATTERN;
	
	
	// static initializer
	static {
		NULL_PATTERN = Pattern.compile( "null" );
	}
	
	
	/** Constructor */
	protected NullDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected Object decode() {
		final String match = processMatch( NULL_PATTERN );
		return null;
	}
}



/** decode a string from a source string */
class StringDecoder extends AbstractDecoder<String> {
	/** pattern for matching a string */
	static final Pattern STRING_PATTERN;
	
	
	// static initializer
	static {
		// a string begins and ends with a quotation mark and no unescaped quotation marks in between them
		STRING_PATTERN = Pattern.compile( "\\\"(((\\\\)+\")|[^\"])*\\\"" );
	}
	
	
	/** Constructor */
	protected StringDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected String decode() {
		final String match = processMatch( STRING_PATTERN );
		if ( match != null ) {
			final int length = match.length();
			return length > 0 ? unescape( match.substring( 1, length-1 ) ) : "";
		}
		else {
			return null;
		}
	}
	
	
	/** unescape (replace occurences of a backslash and a character by the character itself) the input and return the resulting string */
	static private String unescape( final String input ) {
		final StringBuffer buffer = new StringBuffer();
		unescapeToBuffer( buffer, input );
		return buffer.toString();
	}
	
	
	/** unescape the input and append the text to the buffer */
	static private void unescapeToBuffer( final StringBuffer buffer, final String input ) {
		if ( input != null && input.length() > 0 ) {
			final int location = input.indexOf( '\\' );
			if ( location < 0 ) {
				buffer.append( input );
			}
			else {
				buffer.append( input.substring( 0, location ) );
				final int inputLength = input.length();
				if ( inputLength > location ) {
					buffer.append( input.charAt( location + 1 ) );
					final String remainder = inputLength > location + 1 ? input.substring( location + 2 ) : null;
					unescapeToBuffer( buffer, remainder );
				}
			}
		}
	}
}



/** decode an array from a source string */
class ArrayDecoder extends AbstractDecoder<Object[]> {
    /** custom type adaptors */
    final private ConversionAdaptorStore CONVERSION_ADAPTOR_STORE;
    
    /** reference store */
    final private KeyedReferenceStore REFERENCE_STORE;
    
    
	/** Constructor */
	protected ArrayDecoder( final String archive, final ConversionAdaptorStore conversionAdaptorStore, final KeyedReferenceStore referenceStore ) {
		super( archive );
        CONVERSION_ADAPTOR_STORE = conversionAdaptorStore;
        REFERENCE_STORE = referenceStore;
	}
	
	
	/** decode the source to extract the next object */	
	protected Object[] decode() {
		final String arrayString = ARCHIVE.substring( 1 ).trim();	// strip the leading bracket
		final List<Object> items = new ArrayList<Object>();
		appendItems( items, arrayString );
		return items.toArray();
	}
	
	
	/** append to the items the parsed items from the array string */
	private void appendItems( final List<Object> items, final String arrayString ) {
		if ( arrayString != null && arrayString.length() > 0 ) {
			try {
				if ( arrayString.charAt( 0 ) == ']' ) {
					_remainder = arrayString.substring( 1 ).trim();
					return;
				}
				else {
					final AbstractDecoder<?> itemDecoder = AbstractDecoder.getInstance( arrayString, CONVERSION_ADAPTOR_STORE, REFERENCE_STORE );
					items.add( itemDecoder.decode() );
					final String itemRemainder = itemDecoder.getRemainder().trim();
					final char closure = itemRemainder.charAt( 0 );
					final String archiveRemainder = itemRemainder.substring(1).trim();
					switch ( closure ) {
						case ',':
							appendItems( items, archiveRemainder );
							return;
						case ']':
							_remainder = archiveRemainder;
							return;
						default:
							throw new RuntimeException( "Invalid array closure mark: " + closure );
					}
				}
			}
			catch ( Exception exception ) {
				exception.printStackTrace();
			}
		}
		else {
			_remainder = null;
		}
	}
}



/** decode a dictionary from a source string */
class DictionaryDecoder extends AbstractDecoder<Object> {
    /** custom type adaptors */
    final private ConversionAdaptorStore CONVERSION_ADAPTOR_STORE;
    
    /** reference store */
    final private KeyedReferenceStore REFERENCE_STORE;

    
	/** Constructor */
	protected DictionaryDecoder( final String archive, final ConversionAdaptorStore conversionAdaptorStore, final KeyedReferenceStore referenceStore ) {
		super( archive );
        CONVERSION_ADAPTOR_STORE = conversionAdaptorStore;
        REFERENCE_STORE = referenceStore;
	}
	
	
	/** decode the source to extract the next object */	
    @SuppressWarnings( "unchecked" )    // no way to validate representation value and type at compile time
	protected Object decode() {
		final String dictionaryString = ARCHIVE.substring( 1 ).trim();	// strip the leading brace
		final Map<String,Object> dictionary = new HashMap<String,Object>();
		appendItems( dictionary, dictionaryString );
        
        if ( dictionary.containsKey( ExtensionEncoder.EXTENDED_TYPE_KEY ) && dictionary.containsKey( ExtensionEncoder.EXTENDED_VALUE_KEY ) ) {
            // decode object of extended type
            final String extendedType = (String)dictionary.get( ExtensionEncoder.EXTENDED_TYPE_KEY );
            final Object representationValue = dictionary.get( ExtensionEncoder.EXTENDED_VALUE_KEY );
            return toNative( representationValue, extendedType );
        }
        else if ( dictionary.containsKey( TypedArrayEncoder.ARRAY_ITEM_TYPE_KEY ) && dictionary.containsKey( TypedArrayEncoder.ARRAY_KEY ) ) {
            // decode array of with a specified component type from a generic object array
            final String componentType = (String)dictionary.get( TypedArrayEncoder.ARRAY_ITEM_TYPE_KEY );
            final Object[] objectArray = (Object[])dictionary.get( TypedArrayEncoder.ARRAY_KEY );
            
            try {
                final Class<?> primitiveClass = TypedArrayEncoder.getPrimitiveType( componentType );
                final Class<?> componentClass = primitiveClass != null ? primitiveClass : Class.forName( componentType );
                final String componentObjectType = TypedArrayEncoder.getObjectTypeForClass( componentClass );   // this allows us to handle primitive wrappers
                final Class<?> componentObjectClass = Class.forName( componentObjectType ); 
                final Object array = Array.newInstance( componentClass, objectArray.length );
                for ( int index = 0 ; index < objectArray.length ; index++ ) {
                    final Object rawItem = objectArray[index];
                    // if the raw item is an extended type it will automatically have been decoded unless it is of the common component type in which case we tranlate it
                    final Object item = componentObjectClass.isInstance( rawItem ) ? rawItem : toNative( rawItem, componentObjectType );
                    Array.set( array, index, item );
                }
                return array;
            }
            catch( Exception exception ) {
                exception.printStackTrace();
                throw new RuntimeException( "Exception decoding a typed array of type: " + componentType, exception );
            }
        }
        else if ( dictionary.containsKey( SerializationEncoder.SERIALIZATION_VALUE_KEY ) ) {
            final byte[] serializationData = (byte[])dictionary.get( SerializationEncoder.SERIALIZATION_VALUE_KEY );
            
            try {
                final ByteArrayInputStream byteInputStream = new ByteArrayInputStream( serializationData );
                final ObjectInputStream objectInputStream = new ObjectInputStream( byteInputStream );
                final Object value = objectInputStream.readObject();
                objectInputStream.close();
                byteInputStream.close();
                return value;
            }
            catch ( Exception exception ) {
                throw new RuntimeException( "Exception decoding serialized object from dictionary: " + dictionary, exception );
            }
        }
        else if ( dictionary.containsKey( SoftValueEncoder.OBJECT_ID_KEY ) && dictionary.containsKey( SoftValueEncoder.VALUE_KEY ) ) {
            // decode a referenced object definition and store it
            final JSONNumber itemID = (JSONNumber)dictionary.get( SoftValueEncoder.OBJECT_ID_KEY );
            final Object item = dictionary.get( SoftValueEncoder.VALUE_KEY );
            REFERENCE_STORE.store( itemID.longValue(), item );
            return item;
        }
        else if ( dictionary.containsKey( ReferenceEncoder.REFERENCE_KEY ) ) {
            // decode a reference to an object in the store
            final JSONNumber itemID = (JSONNumber)dictionary.get( ReferenceEncoder.REFERENCE_KEY );
            return REFERENCE_STORE.get( itemID.longValue() );
        }
        else {
            return dictionary;
        }
	}
    
    
    /** Convert the representation value to native using the specified extension type */
    @SuppressWarnings( {"unchecked", "rawtypes"} )
    private Object toNative( final Object representationValue, final String extendedType ) {
        final ConversionAdaptor adaptor = CONVERSION_ADAPTOR_STORE.getConversionAdaptor( extendedType );
        if ( adaptor == null )  throw new RuntimeException( "Missing JSON adaptor for type: " + extendedType );
        return adaptor.toNative( representationValue );
    }
	
	
	/** append to the items the parsed items from the array string */
	private void appendItems( final Map<String,Object> dictionary, final String dictionaryString ) {
		if ( dictionaryString != null && dictionaryString.length() > 0 ) {
			try {
				if ( dictionaryString.charAt( 0 ) == '}' ) {
					_remainder = dictionaryString.substring( 1 ).trim();
					return;
				}
				else {
					final StringDecoder keyDecoder = new StringDecoder( dictionaryString );
					final String key = keyDecoder.decode();
					final String keyRemainder = keyDecoder.getRemainder();
					final String valueBuffer = keyRemainder.trim().substring( 1 );	// trim spaces and strip the leading colon
					final AbstractDecoder<?> valueDecoder = AbstractDecoder.getInstance( valueBuffer, CONVERSION_ADAPTOR_STORE, REFERENCE_STORE );
					final Object value = valueDecoder.decode();
					dictionary.put( key, value );
					final String itemRemainder = valueDecoder.getRemainder().trim();
					final char closure = itemRemainder.charAt( 0 );
					final String archiveRemainder = itemRemainder.substring(1).trim();
					switch ( closure ) {
						case ',':
							appendItems( dictionary, archiveRemainder );
							return;
						case '}':
							_remainder = archiveRemainder;
							return;
						default:
							throw new RuntimeException( "Invalid dictionary closure mark: " + closure );
					}
				}
			}
			catch ( Exception exception ) {
				exception.printStackTrace();
			}
		}
		else {
			_remainder = null;
		}
	}
}



/** Stores referenced items keyed by ID */
class KeyedReferenceStore {
    /** references keyed by ID */
    final private Map<Long,Object> REFERENCES;
    
    /** Constructor */
    public KeyedReferenceStore() {
        REFERENCES = new HashMap<Long,Object>();
    }
    
    
    /** store the value associated with the key */
    public void store( final long key, final Object value ) {
        REFERENCES.put( key, value );
    }
    
    
    /** get the item associated with the key */
    public Object get( final long key ) {
        return REFERENCES.get( key );
    }
}



/** pair of strings representing the key and value */
class KeyValueStringPair {
    /** key */
    final public String KEY;
    
    /** value */
    final public String VALUE;
    
    
    /** Constructor */
    public KeyValueStringPair( final String key, final String value ) {
        KEY = key;
        VALUE = value;
    }
}



/** storage of possible references */
class ReferenceStore {
    /** set of objects with a common equality */
    final private Map<Object,EqualityReference<Object>> EQUALITY_REFERENCES;
    
    /** counter of unique objects */
    private long _objectCounter;
    
    
    /** Constructor */
    public ReferenceStore() {
        EQUALITY_REFERENCES = new HashMap<Object,EqualityReference<Object>>();
        _objectCounter = 0;
    }
    
    
    /** store the item */
    @SuppressWarnings( "unchecked" )    // no way to test type at compile time
    public <ItemType> IdentityReference<ItemType> store( final ItemType item ) {
        if ( !EQUALITY_REFERENCES.containsKey( item ) ) {
            EQUALITY_REFERENCES.put( item, new EqualityReference<Object>() );
        }
        final EqualityReference<ItemType> equalityReference = (EqualityReference<ItemType>)EQUALITY_REFERENCES.get( item );
        return equalityReference.add( item, ++_objectCounter );
    }
}



/** reference to a collection of objects which are equal among themselves */
class EqualityReference<ItemType> {
    /** list of identity references */
    final private List<IdentityReference<ItemType>> IDENTITY_REFERENCES;
    
    
    /** Constructor */
    public EqualityReference() {
        IDENTITY_REFERENCES = new ArrayList<IdentityReference<ItemType>>();
    }
    
    
    /** add the object to the set of equals */
    public IdentityReference<ItemType> add( final ItemType item, final long uniqueID ) {
        for ( final IdentityReference<ItemType> reference : IDENTITY_REFERENCES ) {
            if ( reference.getItem() == item ) {
                reference.setHasMultiple( true );
                return reference;
            }
        }
        
        final IdentityReference<ItemType> reference = new IdentityReference<ItemType>( item, uniqueID );
        IDENTITY_REFERENCES.add( reference );
        return reference;
    }
}



/** reference to an object along with the count */
class IdentityReference<ItemType> {
    /** referenced item */
    final private ItemType ITEM;
    
    /** unique ID for this object */
    final private long ID;
    
    /** indicates multiple references to the item */
    private boolean _hasMultiple;
    
    
    /** Constructor */
    public IdentityReference( final ItemType item, final long uniqueID ) {
        ITEM = item;
        ID = uniqueID;
        _hasMultiple = false;
    }
    
    
    /** get the item */
    public ItemType getItem() {
        return ITEM;
    }
    
    
    /** get the unique ID for the item */
    public long getID() {
        return ID;
    }
    
    
    /** indicates whether more than one reference exists to the referenced item */
    public boolean hasMultiple() {
        return _hasMultiple;
    }
    
    
    /** mark whether multiple (more than one) references are made to the item */
    public void setHasMultiple( final boolean hasMultiple ) {
        _hasMultiple = hasMultiple;
    }
}



/** conversion adaptors container whose contents cannot be changed */
class ConversionAdaptorStore {    
    /** adaptors between all custom types and representation JSON types */
    final protected Map<String,ConversionAdaptor<?,?>> TYPE_EXTENSION_ADAPTORS;
    
    /** set of standard types */
    static final private Set<String> STANDARD_TYPES;
    
    
    // static initializer
    static {
        STANDARD_TYPES = new HashSet<String>();
        populateStandardTypes();
    }
    
    
    /** Constructor */
    protected ConversionAdaptorStore() {
        TYPE_EXTENSION_ADAPTORS = new HashMap<String,ConversionAdaptor<?,?>>();
    }
    
    
    /** Unmodifiable Copy Constructor */
    public ConversionAdaptorStore( final ConversionAdaptorStore sourceAdaptors ) {
        TYPE_EXTENSION_ADAPTORS = Collections.unmodifiableMap( sourceAdaptors.TYPE_EXTENSION_ADAPTORS );
    }
    
    
    /** populate the set of standard types */
    static private void populateStandardTypes() {
        final Set<String> types = new HashSet<String>();
        
        types.add( Boolean.class.getName() );
        types.add( Double.class.getName() );
        types.add( Long.class.getName() );
        types.add( HashMap.class.getName() );
        types.add( Object[].class.getName() );
        types.add( String.class.getName() );
        
        STANDARD_TYPES.addAll( types );
    }
    
    
    /** Get a list of all types which are supported for coding and decoding */
    static public List<String> getStandardTypes() {
        final List<String> types = new ArrayList<String>( STANDARD_TYPES );
        Collections.sort( types );
        return types;
    }
    
    
    /** determine whether the specified type is a standard JSON type */
    static public boolean isStandardType( final String type ) {
        return STANDARD_TYPES.contains( type );
    }
    
    
    /** Get a list of all types which are supported for coding and decoding */
    public List<String> getSupportedTypes() {
        final List<String> types = new ArrayList<String>();
        
        types.addAll( getStandardTypes() );
        types.addAll( getExtendedTypes() );
        
        Collections.sort( types );
        
        return types;
    }
    
    
    /** Get a list of types which extend beyond the JSON standard types */
    public List<String> getExtendedTypes() {
        final List<String> types = new ArrayList<String>();
        
        for ( final String type : TYPE_EXTENSION_ADAPTORS.keySet() ) {
            types.add( type );
        }
        
        Collections.sort( types );
        
        return types;
    }
    
    
    /** Determine if the specified object is of an extended type */
    public boolean isOfExtendedType( final Object value ) {
        return isExtendedClass( value.getClass() );
    }
    
    
    /** Determine if the specified class corresponds to an extended type */
    public boolean isExtendedClass( final Class<?> valueClass ) {
        return isExtendedType( valueClass.getName() );
    }
    
    
    /** Determine if the specified type is an extended type */
    public boolean isExtendedType( final String valueType ) {
        return TYPE_EXTENSION_ADAPTORS.containsKey( valueType );
    }
    
    
    /** Get the conversion adaptor for the given value */
    public ConversionAdaptor<?,?> getConversionAdaptor( final String valueType ) {
        return TYPE_EXTENSION_ADAPTORS.get( valueType );
    }
}



/** conversion adaptors container whose contents can be changed */
class MutableConversionAdaptorStore extends ConversionAdaptorStore {    
    /** Constructor */
    public MutableConversionAdaptorStore( final boolean shouldRegisterStandardExtensions ) {
        super();
        if( shouldRegisterStandardExtensions )  registerStandardExtensions();
    }
    
    
    /** Copy Constructor */
    public MutableConversionAdaptorStore( final MutableConversionAdaptorStore sourceAdaptors ) {
        this( false );
        TYPE_EXTENSION_ADAPTORS.putAll( sourceAdaptors.TYPE_EXTENSION_ADAPTORS );
    }
    
    
    /** 
     * Register the custom type by class and its associated adaptor 
     * @param type type to identify and process for encoding and decoding
     * @param adaptor translator between the custom type and representation JSON constructs
	 * @param alternateKeys zero or more alternate names used to reference the adaptor (e.g. "double" for "java.lang.Double")
     */
    public <CustomType,RepresentationType> void registerType( final Class<?> type, final ConversionAdaptor<CustomType,RepresentationType> adaptor, final String ... alternateKeys ) {
        registerType( type.getName(), adaptor );

		if ( alternateKeys != null ) {
			for ( final String key : alternateKeys ) {
				registerType( key, adaptor );
			}
		}
    }


    /**
     * Register the custom type by name and its associated adaptor
     * @param type type to identify and process for encoding and decoding
     * @param adaptor translator between the custom type and representation JSON constructs
     */
    public <CustomType,RepresentationType> void registerType( final String type, final ConversionAdaptor<CustomType,RepresentationType> adaptor ) {
        TYPE_EXTENSION_ADAPTORS.put( type, adaptor );
    }
    
    
    /** register the standard type extensions (only needs to be done for the default coder) */
    private void registerStandardExtensions() {
        registerType( Character.class, new ConversionAdaptor<Character,String>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public String toRepresentation( final Character custom ) {
                return custom.toString();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Character toNative( final String representation ) {
                return representation.charAt( 0 );
            }
        });

		registerType( Byte.class, new ConversionAdaptor<Byte,JSONNumber>() {
			/** convert the custom type to a representation in terms of representation JSON constructs */
			public JSONNumber toRepresentation( final Byte custom ) {
				return new JSONNumber( custom );
			}


			/** convert the JSON representation construct into the custom type */
			public Byte toNative( final JSONNumber representation ) {
				return representation.byteValue();
			}
		}, "byte" );

        registerType( Short.class, new ConversionAdaptor<Short,JSONNumber>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public JSONNumber toRepresentation( final Short custom ) {
                return new JSONNumber( custom );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Short toNative( final JSONNumber representation ) {
                return representation.shortValue();
            }
        }, "short" );

        registerType( Integer.class, new ConversionAdaptor<Integer,JSONNumber>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public JSONNumber toRepresentation( final Integer custom ) {
                return new JSONNumber( custom );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Integer toNative( final JSONNumber representation ) {
                return representation.intValue();
            }
        }, "int" );

		registerType( Long.class, new ConversionAdaptor<Long,JSONNumber>() {
			/** convert the custom type to a representation in terms of representation JSON constructs */
			public JSONNumber toRepresentation( final Long custom ) {
				return new JSONNumber( custom );
			}


			/** convert the JSON representation construct into the custom type */
			public Long toNative( final JSONNumber representation ) {
				return representation.longValue();
			}
		}, "long" );

        registerType( Float.class, new ConversionAdaptor<Float,JSONNumber>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public JSONNumber toRepresentation( final Float custom ) {
                return new JSONNumber( custom );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Float toNative( final JSONNumber representation ) {
                return representation.floatValue();
            }
        }, "float" );

		registerType( Double.class, new ConversionAdaptor<Double,JSONNumber>() {
			/** convert the custom type to a representation in terms of representation JSON constructs */
			public JSONNumber toRepresentation( final Double custom ) {
				return new JSONNumber( custom );
			}


			/** convert the JSON representation construct into the custom type */
			public Double toNative( final JSONNumber representation ) {
				return representation.doubleValue();
			}
		}, "double" );

        registerType( Date.class, new ConversionAdaptor<Date,JSONNumber>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public JSONNumber toRepresentation( final Date timestamp ) {
                return new JSONNumber( timestamp.getTime() );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Date toNative( final JSONNumber msecFromEpoch ) {
                return new Date( msecFromEpoch.longValue() );
            }
        });
        
        this.<ArrayList<?>,Object[]>registerType( ArrayList.class, new ConversionAdaptor<ArrayList<?>,Object[]>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Object[] toRepresentation( final ArrayList<?> list ) {
                return list.toArray();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    // list can represent any type
            public ArrayList toNative( final Object[] array ) {
                final ArrayList list = new ArrayList( array.length );
                for ( final Object item : array ) {
                    list.add( item );
                }
                return list;
            }
        });
        
        this.<Vector<?>,Object[]>registerType( Vector.class, new ConversionAdaptor<Vector<?>,Object[]>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Object[] toRepresentation( final Vector<?> list ) {
                return list.toArray();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    // list can represent any type
            public Vector<?> toNative( final Object[] array ) {
                final Vector list = new Vector( array.length );
                for ( final Object item : array ) {
                    list.add( item );
                }
                return list;
            }
        });
        
        this.<Hashtable<String,?>,HashMap<String,?>>registerType( Hashtable.class, new ConversionAdaptor<Hashtable<String,?>,HashMap<String,?>>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            @SuppressWarnings( "unchecked" )
            public HashMap<String,?> toRepresentation( final Hashtable<String,?> table ) {
                return new HashMap( table );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )
            public Hashtable<String,?> toNative( final HashMap<String,?> map ) {
                return new Hashtable( map );
            }
        });
        
        registerType( StackTraceElement.class, new ConversionAdaptor<StackTraceElement,HashMap<String,?>>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            @SuppressWarnings( "unchecked" ) 
            public HashMap<String,?> toRepresentation( final StackTraceElement traceElement ) {
                final HashMap traceElementMap = new HashMap( 3 );
                traceElementMap.put( "className", traceElement.getClassName() );
                traceElementMap.put( "methodName", traceElement.getMethodName() );
                traceElementMap.put( "fileName", traceElement.getFileName() );
                traceElementMap.put( "lineNumber", traceElement.getLineNumber() );
                return traceElementMap;
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    
            public StackTraceElement toNative( final HashMap<String,?> traceElementMap ) {
                final String className = (String)traceElementMap.get( "className" );
                final String methodName = (String)traceElementMap.get( "methodName" );
                final String fileName = (String)traceElementMap.get( "fileName" );
                final int lineNumber = (Integer)traceElementMap.get( "lineNumber" );
                return new StackTraceElement( className, methodName, fileName, lineNumber );
            }
        });
        
        registerType( RuntimeException.class, new ConversionAdaptor<RuntimeException,HashMap<String,?>>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            @SuppressWarnings( "unchecked" )
            public HashMap<String,?> toRepresentation( final RuntimeException exception ) {
                final String rawMessage = exception.getMessage();
                final HashMap exceptionMap = new HashMap( 3 );
                exceptionMap.put( "message", rawMessage != null ? rawMessage : exception.toString() );
                exceptionMap.put( "stackTrace", exception.getStackTrace() );
                return exceptionMap;
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )
            public RuntimeException toNative( final HashMap<String,?> exceptionMap ) {
                final String message = (String)exceptionMap.get( "message" );
                final StackTraceElement[] stackTrace = (StackTraceElement[])exceptionMap.get( "stackTrace" );
                final RuntimeException exception = new RuntimeException( message );
                exception.setStackTrace( stackTrace );
                return exception;
            }
        });
    }
}



/** Concrete class to hold a generic JSON number. JSON and JavaScript don't distinguish between floats and ints and the various numeric sizes */
class JSONNumber extends Number {
	/** class variable required for serializable classes */
	private static final long serialVersionUID = 1L;


	/** actual number with data */
	final private Number WRAPPED_NUMBER;


	/** Constructor */
	public JSONNumber( final Number wrappedNumber ) {
		WRAPPED_NUMBER = wrappedNumber;
	}


	/** get this number as a byte value */
	public byte byteValue() {
		return WRAPPED_NUMBER.byteValue();
	}


	/** get this number as a double value */
	public double doubleValue() {
		return WRAPPED_NUMBER.doubleValue();
	}


	/** get this number as a float value */
	public float floatValue() {
		return WRAPPED_NUMBER.floatValue();
	}


	/** get this number as a int value */
	public int intValue() {
		return WRAPPED_NUMBER.intValue();
	}


	/** get this number as a long value */
	public long longValue() {
		return WRAPPED_NUMBER.longValue();
	}


	/** get this number as a short value */
	public short shortValue() {
		return WRAPPED_NUMBER.shortValue();
	}


	/** Generate the string representation of the wrapped number */
	public String toString() {
		return WRAPPED_NUMBER.toString();
	}


	/** convert the specified string to a number */
	public static JSONNumber valueOf( final String numstr ) {
		if ( numstr.contains( "." ) ) {
			return new JSONNumber( Double.valueOf( numstr ) );
		}
		else {
			return new JSONNumber( Long.valueOf( numstr ) );
		}
	}
}



