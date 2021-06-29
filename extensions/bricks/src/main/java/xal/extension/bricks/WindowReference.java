//
//  WindowReference.java
//  xal
//
//  Created by Thomas Pelaia on 7/17/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import xal.tools.xml.XmlDataAdaptor;
import xal.tools.data.*;

import java.lang.reflect.*;
import java.beans.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Component;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;


/** loads resources for a single window instance */
public class WindowReference {
    private static final Logger LOGGER = Logger.getLogger(WindowReference.class.getName());

	/** context in which this window reference was made */
	private final BricksContext context;
	
	/** table of views tagged by their tag */
	private final Map<String,List<Object>> viewTable;
	
	/** the window reference */
	private final Window window;
	
	
	/** Constructor */
	public WindowReference( final URL url, final String tag, Object... windowParameters ) {
		context = new BricksContext( url );
		viewTable = new HashMap<>();
		window = loadWindow( url, tag, windowParameters );
	}
	
	
	/** Get the default window reference using the default window constructor */
	public static WindowReference getDefaultInstance( final URL url, final String tag ) {
		return new WindowReference( url, tag );
	}
	
	
	/** get the window */
	public Window getWindow() {
		return window;
	}
	
	
	/** get the views with the associated tag */
	public List<Object> getViews( final String tag ) {
		return viewTable.get( tag );
	}
	
	
	/** get a view with the associated tag */
	public Object getView( final String tag ) {
		final List<Object> views = getViews( tag );
		return views != null && views.size() > 0 ? views.get( 0 ) : null;
	}
	
	
	/** register the view with the table */
	protected void registerView( final Object view, final String tag ) {
		final List<Object> views;
		if ( viewTable.containsKey( tag ) ) {
			views = viewTable.get( tag );
		}
		else {
			views = new ArrayList<>();
			viewTable.put( tag, views );
		}
		views.add( view );
	}
	
	
	/** load the window from the resource */
	protected Window loadWindow( final URL url, final String tag, final Object[] windowParameters ) {
		final DataAdaptor windowAdaptor = getWindowAdaptor( url, tag );
		if ( windowAdaptor != null ) {
			final Window window = (Window)getView( windowAdaptor, windowParameters );
			
			if ( windowAdaptor.hasAttribute( "width" ) ) {
				final int width = windowAdaptor.intValue( "width" );
				final int height = windowAdaptor.intValue( "height" );
				window.setSize( width, height );
			}
			
			registerView( window, tag );
			
			return window;
		}
		else {
			return null;
		}
	}
	
	
	/** load the resources */
	protected DataAdaptor getWindowAdaptor( final URL url, final String tag ) {
        if ( url != null ) {
			final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
			final DataAdaptor mainAdaptor = documentAdaptor.childAdaptor( "BricksDocument" );
			final DataAdaptor rootAdaptor = mainAdaptor.childAdaptor( RootBrick.DATA_LABEL );
			final List<DataAdaptor> windowAdaptors = rootAdaptor.childAdaptors(ViewNode.DATA_LABEL );
			for ( final DataAdaptor windowAdaptor : windowAdaptors ) {
				final String windowTag = windowAdaptor.stringValue( "tag" );
				if ( windowTag.equals( tag ) )  return windowAdaptor;
			}
		}
		return null;
	}
	
	
	/** process adaptors to get components */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
	protected Component getView( final DataAdaptor adaptor, final Object... viewParameters ) {
		final DataAdaptor proxyAdaptor = adaptor.childAdaptor( ViewProxy.DATA_LABEL );
		final ViewProxy viewProxy = ViewProxy.getInstance( proxyAdaptor );
		final String tag = adaptor.stringValue( "tag" );
		
		Class<?> viewClass = viewProxy.getPrototypeClass();
		if ( adaptor.hasAttribute( "customBeanClass" ) ) {
			try {
				final String customClassName = adaptor.stringValue( "customBeanClass" );
				viewClass = Class.forName( customClassName );
			}
			catch( ClassNotFoundException exception ) {
				LOGGER.log(Level.SEVERE, null, exception);
			}
		}
		
		Component view;
		if ( viewParameters != null && viewParameters.length > 0 ) {
			try {
				final Constructor viewConstructor = findConstructor( viewClass, viewParameters );
				if ( viewConstructor == null ) {
					throw new RuntimeException( "Can't find constructor for:  " + viewClass.toString() + " matching parameters." );
				}
				view = (Component)viewProxy.getBeanInstance( viewClass, viewConstructor, viewParameters );
			}
			catch ( RuntimeException exception ) {
				throw new RuntimeException( "Can't instantiate class:  " + viewClass.toString() );
			}
		}
		else {
			view = (Component)viewProxy.getBeanInstance( viewClass );
		}
		
		registerView( view, tag );
		
		if ( viewProxy.isContainer() ) {
			final List<DataAdaptor> viewAdaptors = adaptor.childAdaptors(ViewNode.DATA_LABEL );
			for ( final DataAdaptor viewAdaptor : viewAdaptors ) {
				final Component subView = getView( viewAdaptor );
				viewProxy.getContainer( view ).add( subView );
			}
		}
		
		final DataAdaptor borderAdaptor = adaptor.childAdaptor(BorderNode.DATA_LABEL );
		if ( view instanceof JComponent && borderAdaptor != null ) {
			final Border border = getBorder( borderAdaptor );
			((JComponent)view).setBorder( border );
		}
		
		final List<DataAdaptor> beanAdaptors = adaptor.childAdaptors( BeanNode.BEAN_DATA_LABEL );
		for ( final DataAdaptor beanAdaptor : beanAdaptors ) {
			beanAdaptor.setValue( "contextURL", context.getSourceURL().toString() );
		}
		applyBeanPropertiesTo( view, beanAdaptors );
		
		return view;
	}
	
	
	/** Find a constructor that matches the specified parameters */
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static <ClassType> Constructor<ClassType> findConstructor( final Class<ClassType> theClass, final Object[] parameters ) {
		final Class<?>[] parameterTypes = new Class[parameters.length];
		for ( int index = 0 ; index < parameters.length ; index++ ) {
			parameterTypes[index] = parameters[index].getClass();
		}
		
		try {
			final Constructor<ClassType> constructor = theClass.getConstructor( parameterTypes );
			constructor.setAccessible( true );
			return constructor;
		}
		catch ( NoSuchMethodException | SecurityException exception ) {
			final Constructor[] constructors = theClass.getConstructors();
			for ( final Constructor constructor : constructors ) {
				if ( constructorCanOperateOn( constructor, parameters ) ) {
					constructor.setAccessible( true );
					return constructor;
				}
			}
			return null;
		}
	}
	
	
	/** determine if the constructor can take the specified parameters */
	private static boolean constructorCanOperateOn( final Constructor<?> constructor, final Object[] parameters ) {
		final Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
		if ( parameters.length != constructorParameterTypes.length ) {
			return false;
		}
		else {
			for ( int index = 0 ; index < parameters.length ; index++ ) {
				if ( !constructorParameterTypes[index].isInstance( parameters[index] ) )  return false;
			}
		}
		return true;
	}
	
	
	/** process adaptors to get borders */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
	protected Border getBorder( final DataAdaptor adaptor ) {
		final DataAdaptor proxyAdaptor = adaptor.childAdaptor(BorderProxy.DATA_LABEL );
		final BorderProxy borderProxy = BorderProxy.getInstance( proxyAdaptor );
		final String tag = adaptor.stringValue( "tag" );
		
		Class<?> borderClass = borderProxy.getPrototypeClass();
		if ( adaptor.hasAttribute( "customBeanClass" ) ) {
			try {
				final String customClassName = adaptor.stringValue( "customBeanClass" );
				borderClass = Class.forName( customClassName );
			}
			catch( ClassNotFoundException exception ) {
				LOGGER.log(Level.SEVERE, null, exception);
			}
		}
		
		final Border border = (Border)borderProxy.getBeanInstance( borderClass );
		registerView( border, tag );
		
		final List<DataAdaptor> beanAdaptors = adaptor.childAdaptors( BeanNode.BEAN_DATA_LABEL );
		applyBeanPropertiesTo( border, beanAdaptors );
		
		return border;
	}
	
	
	/** apply property settings to the bean object */
	protected static void applyBeanPropertiesTo( final Object object, final List<DataAdaptor> beanAdaptors ) {
		final Map<String,PropertyDescriptor>descriptorTable = getProperyDescriptorTable( object );
		
		for ( final DataAdaptor beanAdaptor : beanAdaptors ) {
			applyBeanPropertyTo( object, beanAdaptor, descriptorTable );
		}
	}
	
	
	/** Apply the property settings to the specified bean object */
	protected static void applyBeanPropertyTo( final Object object, final DataAdaptor beanAdaptor, final Map<String,PropertyDescriptor>descriptorTable ) {
		try {
			final PropertyValueEditorManager editorManager = PropertyValueEditorManager.getDefaultManager();
			final String name = beanAdaptor.stringValue( "name" );
			final PropertyDescriptor propertyDescriptor = descriptorTable.get( name );
			final Class<?> propertyType = propertyDescriptor.getPropertyType();
			final PropertyValueEditor<?> propertyEditor = editorManager.getEditor( propertyType );
			final Object value = propertyEditor.readValue( beanAdaptor );
			
			final Method method = propertyDescriptor.getWriteMethod();
			method.invoke( object, value );
		}
		catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException exception ) {
			LOGGER.log(Level.SEVERE, null, exception);
		}
	}
	
	
	/** Get the property descriptor table keyed by property name */
	protected static Map<String,PropertyDescriptor> getProperyDescriptorTable( final Object object ) {
		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo( object.getClass() );;
			final PropertyDescriptor[] descriptors = beanInfo != null ? beanInfo.getPropertyDescriptors() : new PropertyDescriptor[0];
			final Map<String,PropertyDescriptor> descriptorTable = new HashMap<>();
			for ( final PropertyDescriptor descriptor : descriptors ) {
				descriptorTable.put( descriptor.getName(), descriptor );
			}
			return descriptorTable;
		}
		catch( IntrospectionException exception ) {
			return null;
		}
	}
}
