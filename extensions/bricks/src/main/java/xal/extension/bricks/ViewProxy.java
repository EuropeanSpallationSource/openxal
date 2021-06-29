//
//  ViewProxy.java
//  xal
//
//  Created by Thomas Pelaia on 7/3/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.*;
import java.awt.Window;
import javax.swing.*;
import java.beans.*;

import xal.tools.data.*;


/** interface for providing view node behavior */
public abstract class ViewProxy<ViewType extends Component> extends BeanProxy<ViewType> {
	/** data label */
	public static final String DATA_LABEL = "ViewProxy";
	
	/** indicates whether the component should accept components */
	protected final boolean isContainer;
	
	/** indicates whether to display a prototype icon */
	protected final boolean makeIcon;
	
	
	/** Constructor */
	public ViewProxy( final Class<ViewType> prototypeClass, final boolean isContainer, final boolean makeIcon ) {
		super( prototypeClass );
		this.isContainer = isContainer;
		this.makeIcon = makeIcon;
	}
	
	
	/** generator */
	public static ViewProxy<?> getInstance( final DataAdaptor adaptor ) {
		return ViewProxyFactory.getViewProxy( adaptor.stringValue( "type" ) );
	}
	
	
	/** Determine whether the view should be treated as a container */
	public boolean isContainer() {
		return isContainer;
	}
	
	
	/** determine if the view is a window */
	public boolean isWindow() {
		return Window.class.isAssignableFrom(prototypeClass );
	}
	
	
	/**
	 * Get the container to which sub views should be added
	 * @param view the view whose container is to be gotten
	 * @return the view's container
	 */
	public Container getContainer( final ViewType view ) {
		return view instanceof RootPaneContainer ? ((RootPaneContainer)view).getContentPane() : view instanceof Container ? (Container)view : null;
	}
	
	
	/** handle child node property change */
	public void handleChildNodePropertyChange( final ViewNode node, final BeanNode<?> beanNode, final PropertyDescriptor propertyDescriptor, final Object value ) {}
	
	
	/** Get an icon representation for the view */
        @Override
	public Icon getIcon()  {
		if ( makeIcon && JComponent.class.isAssignableFrom(prototypeClass ) ) {
			return new ImageIcon( getIconImage() );
		}
		else {
			return null;
		}
	}
	
	
	/** Get an image representation for the view */
	public Image getIconImage()  {
		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(prototypeClass );
			final Image image = beanInfo.getIcon( BeanInfo.ICON_COLOR_16x16 );
			return image != null ? image : makeImage();
		}
		catch ( IntrospectionException exception ) {
			return makeImage();
		}
	}
	
	
	/** make the image from the component itself */
	private Image makeImage() {
		final JComponent view = (JComponent)getPrototype();
		final Dimension imageSize = view.getPreferredSize();
		final int width = imageSize.width <= 0 ? 60 : imageSize.width;
		final int height = imageSize.height <= 0 ? 40 : imageSize.height;
		view.setSize( width, height );
		final BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );
		view.paint( image.createGraphics() );
		return image;		
	}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
        @Override
    public String dataLabel() {
		return DATA_LABEL;
	}
}
