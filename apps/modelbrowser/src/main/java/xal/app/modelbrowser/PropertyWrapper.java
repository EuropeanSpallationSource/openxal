/*
 * Copyright (C) 2017 European Spallation Source ERIC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xal.app.modelbrowser;


import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.adapter.ReadOnlyJavaBeanObjectPropertyBuilder;
import org.apache.commons.lang3.StringUtils;


/**
 * Class wrapping java bean properties name and value to be displayed into a JavaFX table.
 *
 * @author claudiorosati
 */
@SuppressWarnings( "ClassWithoutLogger" )
public class PropertyWrapper extends BaseWrapper {

    @SuppressWarnings( value = { "UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch" } )
    public PropertyWrapper( Object bean, String name ) throws NoSuchMethodException {
        this(null, name, ReadOnlyJavaBeanObjectPropertyBuilder.create().bean(bean).name(name).build());
    }

    @SuppressWarnings( value = { "UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch" } )
    public PropertyWrapper( String prefix, Object bean, String name ) throws NoSuchMethodException {
        this(prefix, name, ReadOnlyJavaBeanObjectPropertyBuilder.create().bean(bean).name(name).build());
    }

    @SuppressWarnings( value = { "UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch" } )
    public PropertyWrapper( Object bean, String name, String getter ) throws NoSuchMethodException {
        this(null, name, ReadOnlyJavaBeanObjectPropertyBuilder.create().bean(bean).name(name).getter(getter).build());
    }

    @SuppressWarnings( value = { "UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch" } )
    public PropertyWrapper( String prefix, Object bean, String name, String getter ) throws NoSuchMethodException {
        this(prefix, name, ReadOnlyJavaBeanObjectPropertyBuilder.create().bean(bean).name(name).getter(getter).build());
    }

    @SuppressWarnings( value = { "UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch" } )
    public PropertyWrapper( String prefix, String name, ReadOnlyObjectProperty<Object> property ) {

        setName(StringUtils.isBlank(prefix) ? name : prefix + name);

        try {

            StringBinding binding = Bindings.when(property.isNotNull()).then(property.asString()).otherwise("–");

            tooltipProperty().bind(binding);
            valueProperty().bind(binding);

        } catch ( Exception ex ) {

            setAlarm(Alarm.INVALID);

            StringBuilder builder = new StringBuilder(200);
            Throwable t = ex;

            while ( t != null ) {

                StackTraceElement[] st = t.getStackTrace();

                builder.append(st[0].toString()).append(": ").append(ex.getClass().getSimpleName());

                String m = t.getMessage();

                if ( m != null ) {
                    builder.append(" [").append(m).append("]");
                }

                t = t.getCause();

                if ( t != null ) {
                    builder.append("\n");
                }

            }

            setTooltip(builder.toString());

        }

    }

    @Override
    public void dispose() {
    }

}
