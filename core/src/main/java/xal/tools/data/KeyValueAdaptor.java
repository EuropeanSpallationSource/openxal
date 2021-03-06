//
//  KeyValueAdaptor.java
//  xal
//
//  Created by Tom Pelaia on 1/28/09.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;

import java.lang.reflect.*;
import java.util.*;


/** Provides methods to set and get an object's values for its named properties */
public class KeyValueAdaptor {
	/** table of keyed getters */
	final private Map<String,KeyedAccessing> GETTER_TABLE;

	/** table of keyed setters */
	final private Map<String,KeyedSetting> SETTER_TABLE;
	
	
	/** Constructor */
	public KeyValueAdaptor() {
		GETTER_TABLE = new HashMap<String,KeyedAccessing>();
		SETTER_TABLE = new HashMap<String,KeyedSetting>();
	}
	
	
	/** 
	 * Get the target's value for the specified accessor key. Accessor methods are searched by first looking for a method with the name specified by the key.
	 * If no such method is found, then a method name is generated by raising the case of the first character of the key and then prepending "get". For example, if
	 * the key is "betaX" it will first look for a method called "betaX" and if none is found then look for a method of name "getBetaX". If no such method can be found, then
	 * if the target implements the java.util.Map interface, the get method is called on the target with the specified key passed as an argument.
	 * @param target object from which to get the value
	 * @param key string indicating an accessor method (or Map key)
	 */
	public Object valueForKey( final Object target, final String key ) {
		try {
			final KeyedAccessing accessor = accessorForKey( target, key );
			return accessor != null ? accessor.valueForTarget( target ) : valueForInvalidAccessor( target, key );
		}
		catch ( InvalidKeyException exception ) {
			return valueForInvalidAccessor( target, key );
		}
	}
	
	
	/** 
	 * Get the target's value for the specified key path, but return null if any object along the key path is null.
	 * @param target object from which to get the value
	 * @param keyPath series of keys joined by "." in between.
	 */
	public Object valueForKeyPath( final Object target, final String keyPath ) {
		if ( target == null )  return null;		// if the target is null there is no point attempting to traverse the key path
		
		final String[] keyParts = keyPath.split( "\\.", 2 );
		switch ( keyParts.length ) {
			case 1:
				return valueForKey( target, keyParts[0] );
			case 2:
				final Object intermediate = valueForKey( target, keyParts[0] );	// get the next target
				return valueForKeyPath( intermediate, keyParts[1] );	// advance the intermediate
			default:
				return null;
		}
	}
	
	
	/** 
	 * Set the target's value for the specified accessor key. Setter methods are searched by looking for a method with the name specified by generating a name
	 * raising the case of the first character of the key and then prepending "set". For example, if the key is "betaX" it will look for a method called "setBetaX". If no match
	 * is found and the target implements the java.util.Map interface, then put method is called on the target with the specified key and value passed as arguments.
	 * @param target object from which to get the value
	 * @param key string indicating a setter method (or Map key)
	 * @param value the value to set
	 */
	public void setValueForKey( final Object target, final String key, final Object value ) {
		try {
			final Class<?> argumentClass = value != null ? value.getClass() : java.lang.Object.class;
			final KeyedSetting setter = setterForKey( target, key, argumentClass );
			if ( setter != null ) {
				setter.setValueForTarget( target, value );
			}
			else {
				setValueForInvalidSetter( target, key, value );
			}
		}
		catch ( InvalidKeyException exception ) {
			setValueForInvalidSetter( target, key, value );
		}
	}
	
	
	/** 
	 * Set the target's value for the specified key path.
	 * @param target object from which to get the value
	 * @param keyPath series of keys joined by "." in between.
	 * @param value the value to set
	 */
	public void setValueForKeyPath( final Object target, final String keyPath, final Object value ) {
		final String[] keyParts = keyPath.split( "\\.", 2 );
		switch ( keyParts.length ) {
			case 1:
				setValueForKey( target, keyParts[0], value );
				break;
			case 2:
				final Object intermediate = valueForKey( target, keyParts[0] );	// get the next target
				setValueForKeyPath( intermediate, keyParts[1], value );	// advance the intermediate
				break;
			default:
				break;
		}
	}
	
	
	/** 
	 * Hook to allow subclasses to provide a value for an invalid key. The current implementation simply throws an exception.
	 * @param target object from which to get the value
	 * @param key string indicating the accessor which could not be found
	 */
	protected Object valueForInvalidAccessor( final Object target, final String key ) {
		throw new InvalidAccessorException( target, key );
	}
	
	
	/** 
	 * Hook to allow subclasses to set a value for an invalid key. The current implementation simply throws an exception.
	 * @param target object from which to get the value
	 * @param key string indicating the setter which could not be found
	 * @param value the value to set
	 */
	protected Object setValueForInvalidSetter( final Object target, final String key, Object value ) {
		throw new InvalidSetterException( target, key, value );
	}
	
	
	/** 
	 * Get the target's accessor corresponding to the specified key.
	 * @param target object from which to get the accessor
	 * @param key accessor property
	 */
	private KeyedAccessing accessorForKey( final Object target, final String key ) {
		final Class<?> targetClass = target.getClass();
		final String accessorID = getAccessorID( targetClass, key );	// generate a unique ID for the target class/key pair
		
		synchronized ( GETTER_TABLE ) {
			if ( !GETTER_TABLE.containsKey( accessorID ) ) {	// check whether we have the selector cached and if not find the method and cache it
				// first try to find a method accessor corresponding to the key
				final KeyedAccessing methodAccessor = KeyedMethodAccessor.getInstance( targetClass, key );
				if ( methodAccessor != null ) {
					GETTER_TABLE.put( accessorID, methodAccessor );
				}
				else {	// no method accessor was found for the key
					// if the target implements the Map interface then use the Map's get method for access
					final KeyedAccessing mapAccessor = KeyedMapAccessor.getInstance( targetClass, key );
                    if ( mapAccessor != null ) {
                        GETTER_TABLE.put( accessorID, mapAccessor );
                    }
                    else {
                        final KeyedAccessing arrayItemAccessor = KeyedArrayItemAccessor.getInstance( targetClass, key );
                        GETTER_TABLE.put( accessorID, arrayItemAccessor );
                    }
				}
			}
			
			return GETTER_TABLE.get( accessorID );	// get the method from the cache
		}
	}
	
	
	/** 
	 * Get the target's public setter method corresponding to the specified key.
	 * @param target object from which to get the setter
	 * @param key setter property
	 * @param argumentClass class of the argument
	 */
	private KeyedSetting setterForKey( final Object target, final String key, final Class<?> argumentClass ) {
		final Class<?> targetClass = target.getClass();
		final String setterID = getSetterID( targetClass, key, argumentClass );	// generate a unique ID for the target class/key/argument class group
		
		synchronized ( SETTER_TABLE ) {
			if ( !SETTER_TABLE.containsKey( setterID ) ) {	// check whether we have the selector cached and if not find the method and cache it
				// first try to find a method setter corresponding to the key
				final KeyedSetting methodSetter = KeyedMethodSetter.getInstance( targetClass, key, argumentClass );
				if ( methodSetter != null ) {
					SETTER_TABLE.put( setterID, methodSetter );
				}
				else {	// no method setter was found for the key
					// if the target implements the Map interface then use the Map's put method for setting values
					final KeyedSetting mapSetter = KeyedMapSetter.getInstance( targetClass, key, argumentClass );
                    if ( mapSetter != null ) {
                        SETTER_TABLE.put( setterID, mapSetter );
                    }
                    else {
                        final KeyedSetting arrayItemSetter = KeyedArrayItemSetter.getInstance( targetClass, key, argumentClass );
                        SETTER_TABLE.put( setterID, arrayItemSetter );
                    }
				}
			}
			
			return SETTER_TABLE.get( setterID );	// get the method from the cache
		}
	}
	
	
	/** generate the accessor ID for the target class/key pair */
	static private String getAccessorID( final Class<?> targetClass, final String key ) {
		return targetClass.toString() + "#" + key;
	}
	
	
	/** generate the setter ID for the target class/key/argument class group */
	static private String getSetterID( final Class<?> targetClass, final String key, final Class<?> argumentClass ) {
		return targetClass.toString() + "#" + key + "#" + argumentClass.toString();
	}	
	
	
	
	/** Exception thrown to indicate that an accessor cannot be found for the specified target/key pair. */
	public class InvalidAccessorException extends RuntimeException {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
		/** key which identifies the desired accessor method */
		final private String KEY;
		
		/** object on which we are trying to get a value */
		final private Object TARGET;
		
		
		/** Constructor */
		public InvalidAccessorException( final Object target, final String key ) {
			super( "Could not find a suitable accessible method named: \"" + key + "\" or \"" + KeyedMethodAccessor.toGetMethodName( key ) + "\" for target: \"" + target + "\", nor does the target implement the java.util.Map interface." );
			KEY = key;
			TARGET = target;
		}
		
		
		/** get the invalid key */
		public String getKey() {
			return KEY;
		}
		
		
		/** get the object on which we attempted to get the accessor */
		public Object getTarget() {
			return TARGET;
		}
	}
	
	
	
	/** Exception thrown to indicate that a setter cannot be found for the specified target/key/value group. */
	public class InvalidSetterException extends RuntimeException {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
		/** key which identifies the desired accessor method */
		final private String KEY;
		
		/** object on which we are trying to get a value */
		final private Object TARGET;
		
		/** value to pass to the setter method */
		final private Object VALUE;
		
		
		/** Constructor */
		public InvalidSetterException( final Object target, final String key, final Object value ) {
			super( "Could not find a suitable setter method named: \"" + KeyedMethodSetter.toSetMethodName( key ) + "\" for target: \"" + target + "\"" + " with value: \"" + value + "\", nor does the target implement the java.util.Map interface." );
			KEY = key;
			TARGET = target;
			VALUE = value;
		}
		
		
		/** get the invalid key */
		public String getKey() {
			return KEY;
		}
		
		
		/** get the object on which we attempted to get the accessor */
		public Object getTarget() {
			return TARGET;
		}
		
		
		/** get the value to pass to the setter method */
		public Object getValue() {
			return VALUE;
		}
	}
}



/** Exception for a bad accessor or setter attempt */
class InvalidKeyException extends Exception {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	public InvalidKeyException( final Throwable cause ) {
		super( cause );
	}
}



/** interface for the source of keyed access */
interface KeyedAccessing {
	/** 
	 * Get the target's value for the specified accessor key. Accessor methods are searched by first looking for a method with the name specified by the key.
	 * If no such method is found, then a method name is generated by raising the case of the first character of the key and then prepending "get". For example, if
	 * the key is "betaX" it will first look for a method called "betaX" and if none is found then look for a method of name "getBetaX". 
	 * @param target object from which to get the value
	 */
	public Object valueForTarget( final Object target ) throws InvalidKeyException;
}



/** interface for the source of keyed setting */
interface KeyedSetting {
	/** 
	 * Set the target's value for the specified accessor key. Accessor methods are searched by looking for a method with the name specified by generating a name
	 * raising the case of the first character of the key and then prepending "set". For example, if the key is "betaX" it will look for a method called "setBetaX".
	 * @param target object from which to get the value
	 * @param value the value to set
	 */
	public void setValueForTarget( final Object target, final Object value ) throws InvalidKeyException;
}



/** Keyed access using methods */
class KeyedMethodAccessor implements KeyedAccessing {
	/** method for accessing a keyed value */
	final private Method ACCESS_METHOD;
	
	/** key for the method */
	final private String METHOD_KEY;
	
	
	/** Constructor */
	protected KeyedMethodAccessor( final Method method, final String key ) {
		method.setAccessible( true );
		ACCESS_METHOD = method;
		METHOD_KEY = key;
	}
	
	
	/** attempt to get an instance for the class and a method matching the key */
	static public KeyedMethodAccessor getInstance( final Class<?> targetClass, final String methodName ) {
		final Method method = findAccessorMethodForKey( targetClass, methodName );
		return method != null ? new KeyedMethodAccessor( method, methodName ) : null;
	}
	
	
	/** 
	 * Get the target's value for the specified accessor key. Accessor methods are searched by first looking for a method with the name specified by the key.
	 * If no such method is found, then a method name is generated by raising the case of the first character of the key and then prepending "get". For example, if
	 * the key is "betaX" it will first look for a method called "betaX" and if none is found then look for a method of name "getBetaX". 
	 * @param target object from which to get the value
	 */
	public Object valueForTarget( final Object target ) throws InvalidKeyException {
		try {
			return ACCESS_METHOD.invoke( target );
		}
		catch ( IllegalAccessException exception ) {
			throw new InvalidKeyException( exception );
		}
		catch ( IllegalArgumentException exception ) {
			throw new InvalidKeyException( exception );
		}
		catch ( InvocationTargetException exception ) {
			throw new RuntimeException( "Exception during evaluation of the accessor: " + METHOD_KEY + " on the target: " + target, exception );
		}		
	}
	
	
	/** 
	 * Get the target's public accessor method corresponding to the specified key.
	 * @param targetClass class of the target object from which to get the accessor method
	 * @param keyPath series of keys joined by "." in between.
	 */
	static private Method findAccessorMethodForKey( final Class<?> targetClass, final String key ) {
		try {
			return targetClass.getMethod( key );
		}
		catch ( NoSuchMethodException exception ) {
			return getterForKey( targetClass, key );
		}
		catch ( SecurityException exception ) {
			return null;
		}
	}
	
	
	/** 
	 * Get the target's public accessor method corresponding to the specified key.
	 * @param targetClass class of the target object from which to get the accessor method
	 * @param keyPath series of keys joined by "." in between.
	 */
	static private Method getterForKey( final Class<?> targetClass, final String key ) {
		try {
			final String methodName = toGetMethodName( key );
			return targetClass.getMethod( methodName );
		}
		catch ( NoSuchMethodException exception ) {
			return null;
		}
		catch ( SecurityException exception ) {
			return null;
		}
	}
	
	
	/**
	 * Generate an accessor method name for the specified key by raising the case of the first character and prepending "get"
	 * @param key parameter name
	 */
	static String toGetMethodName( final String key ) {
		return ( key != null && key.length() > 0 ) ? "get" + key.substring( 0, 1 ).toUpperCase() + key.substring( 1 ) : key;
	}
}



/** Keyed access using methods */
class KeyedMethodSetter implements KeyedSetting {
	/** method for setting a keyed value */
	final private Method SET_METHOD;
	
	/** key for the method */
	final private String METHOD_KEY;
	
	
	/** Constructor */
	protected KeyedMethodSetter( final Method method, final String key ) {
		method.setAccessible( true );
		SET_METHOD = method;
		METHOD_KEY = key;
	}
	
	
	/** attempt to get an instance for the class and a method matching the key */
	static public KeyedMethodSetter getInstance( final Class<?> targetClass, final String methodName, final Class<?> argumentClass ) {
		final Method method = findSetterMethodForKey( targetClass, methodName, argumentClass );
		return method != null ? new KeyedMethodSetter( method, methodName ) : null;
	}
	
	
	/** 
	 * Set the target's value for the specified accessor key. Accessor methods are searched by looking for a method with the name specified by generating a name
	 * raising the case of the first character of the key and then prepending "set". For example, if the key is "betaX" it will look for a method called "setBetaX".
	 * @param target object from which to get the value
	 * @param value the value to set
	 */
	public void setValueForTarget( final Object target, final Object value ) throws InvalidKeyException {
		try {
			SET_METHOD.invoke( target, value );
		}
		catch ( IllegalAccessException exception ) {
			throw new InvalidKeyException( exception );
		}
		catch ( IllegalArgumentException exception ) {
			throw new InvalidKeyException( exception );
		}
		catch ( InvocationTargetException exception ) {
			throw new RuntimeException( "Exception during evaluation of the setter: " + METHOD_KEY + " on the target: " + target, exception );
		}		
	}	
	
	
	/** 
	 * Get the target's public setter method corresponding to the specified key.
	 * @param targetClass class of the target object from which to get the accessor method
	 * @param keyPath series of keys joined by "." in between.
	 * @param argumentClass class of the argument
	 */
	static private Method findSetterMethodForKey( final Class<?> targetClass, final String key, final Class<?> argumentClass ) {
		final String methodName = toSetMethodName( key );
		try {
			return targetClass.getMethod( methodName, argumentClass );
		}
		catch ( NoSuchMethodException exception ) {
			try {
				// test to see if the argument is a primitive type
				if ( argumentClass.isAssignableFrom( Boolean.class ) ) {
					return targetClass.getMethod( methodName, Boolean.TYPE );
				}
				else if ( argumentClass.isAssignableFrom( Double.class ) ) {
					return targetClass.getMethod( methodName, Double.TYPE );
				}
				else if ( argumentClass.isAssignableFrom( Integer.class ) ) {
					return targetClass.getMethod( methodName, Integer.TYPE );
				}
				else if ( argumentClass.isAssignableFrom( Long.class ) ) {
					return targetClass.getMethod( methodName, Long.TYPE );
				}
				else if ( argumentClass.isAssignableFrom( Float.class ) ) {
					return targetClass.getMethod( methodName, Float.TYPE );
				}
				else if ( argumentClass.isAssignableFrom( Short.class ) ) {
					return targetClass.getMethod( methodName, Short.TYPE );
				}
				else if ( argumentClass.isAssignableFrom( Byte.class ) ) {
					return targetClass.getMethod( methodName, Byte.TYPE );
				}
				else {
					return targetClass.getMethod( methodName, Object.class );
				}
			}
			catch ( NoSuchMethodException subException ) {
				return null;
			}
			catch ( SecurityException subException ) {
				return null;
			}
		}
		catch ( SecurityException exception ) {
			return null;
		}
	}
	
	
	/**
	 * Generate a setter method name for the specified key by raising the case of the first character and prepending "set"
	 * @param key parameter name
	 */
	static String toSetMethodName( final String key ) {
		return ( key != null && key.length() > 0 ) ? "set" + key.substring( 0, 1 ).toUpperCase() + key.substring( 1 ) : key;
	}
}



/** Keyed access using maps */
class KeyedMapAccessor implements KeyedAccessing {
	/** key for the get method call */
	final private String VALUE_KEY;
	
	
	/** Constructor */
	protected KeyedMapAccessor( final String key ) {
		VALUE_KEY = key;
	}
	
	
	/** attempt to get an instance for the class if the target implements the java.util.Map interface */
	static public KeyedMapAccessor getInstance( final Class<?> targetClass, final String key ) {
		return Map.class.isAssignableFrom( targetClass ) ? new KeyedMapAccessor( key ) : null;
	}
	
	
	/** 
	 * Get the target's value for the specified accessor key by simply calling the target's get method and passing the key as the argument. 
	 * @param target object from which to get the value
	 */
    @SuppressWarnings( "unchecked" )    // no way to predetermine the target class, so we must suppress this warning
	public Object valueForTarget( final Object target ) throws InvalidKeyException {
		try {
			final Map<String,Object> targetMap = (Map<String,Object>)target;
			return targetMap.get( VALUE_KEY );
		}
		catch ( ClassCastException exception ) {
			throw new InvalidKeyException( exception );
		}		
	}
}



/** Keyed access using maps */
class KeyedMapSetter implements KeyedSetting {
	/** key for the put method call */
	final private String VALUE_KEY;
	
	
	/** Constructor */
	protected KeyedMapSetter( final String key ) {
		VALUE_KEY = key;
	}
	
	
	/** attempt to get an instance for the class if the target implements the java.util.Map interface */
	static public KeyedMapSetter getInstance( final Class<?> targetClass, final String key, final Class<?> argumentClass ) {
		return Map.class.isAssignableFrom( targetClass ) ? new KeyedMapSetter( key ) : null;
	}
	
	
	/** 
	 * Set the target's value for the specified accessor key by simply calling the target's put method and passing the key and value as arguments.
	 * @param target object from which to get the value
	 * @param value the value to set
	 */
    @SuppressWarnings( "unchecked" )    // no way to predetermine the target class, so we must suppress this warning
	public void setValueForTarget( final Object target, final Object value ) throws InvalidKeyException {
		try {
			final Map<String,Object> targetMap = (Map<String,Object>)target;
			targetMap.put( VALUE_KEY, value );
		}
		catch ( ClassCastException exception ) {
			throw new InvalidKeyException( exception );
		}		
	}	
}



/** Keyed access for an item of an Array */
class KeyedArrayItemAccessor implements KeyedAccessing {
    /** index of the array item to access */
    final private int ITEM_INDEX;
    
    
    /** Constructor */
    protected KeyedArrayItemAccessor( final int itemIndex ) {
        ITEM_INDEX = itemIndex;
    }
    
    
    /** attempt to get an instance for the class if the target is a primitive array */
    static public KeyedArrayItemAccessor getInstance( final Class<?> targetClass, final String key ) {
        try {
            final int itemIndex = Integer.parseInt( key );
            return targetClass.isArray() ? new KeyedArrayItemAccessor( itemIndex ) : null;
        }
        catch ( NumberFormatException exception ) {
            return null;
        }
    }
    
    
    /** 
     * Get the target's array item. 
     * @param target object from which to get the value
     */
    public Object valueForTarget( final Object target ) throws InvalidKeyException {
        try {
            return java.lang.reflect.Array.get( target, ITEM_INDEX );
        }
        catch ( ArrayIndexOutOfBoundsException exception ) {
            throw new InvalidKeyException( exception );
        }		
    }
}



/** Keyed setter for an item of an Array */
class KeyedArrayItemSetter implements KeyedSetting {
    /** index of the array item to set */
    final private int ITEM_INDEX;
    
    
    /** Constructor */
    protected KeyedArrayItemSetter( final int itemIndex ) {
        ITEM_INDEX = itemIndex;
    }
    
    
    /** attempt to get an instance for the class if the target is a primitive array */
    static public KeyedArrayItemSetter getInstance( final Class<?> targetClass, final String key, final Class<?> argumentClass ) {
        try {
            final int itemIndex = Integer.parseInt( key );
            return targetClass.isArray() ? new KeyedArrayItemSetter( itemIndex ) : null;
        }
        catch ( NumberFormatException exception ) {
            return null;
        }
    }
    
    
    /** 
     * Set the target's array item.
     * @param target object from which to get the value
     * @param value the value to set
     */
    public void setValueForTarget( final Object target, final Object value ) throws InvalidKeyException {
        try {
            java.lang.reflect.Array.set( target, ITEM_INDEX, value );
        }
        catch ( ArrayIndexOutOfBoundsException exception ) {
            throw new InvalidKeyException( exception );
        }		
        catch ( IllegalArgumentException exception ) {
            throw new InvalidKeyException( exception );
        }		
    }
}
