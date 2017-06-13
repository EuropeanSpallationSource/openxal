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


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 * Basic class wrapping name and value to be displayed into a JavaFX table.
 *
 * @author claudiorosati
 */
@SuppressWarnings( value = "PublicInnerClass" )
public abstract class BaseWrapper {

    private ObjectProperty<Alarm> alarm = null;
    private StringProperty name = null;
    private StringProperty tooltip = null;
    private StringProperty value = null;

    @SuppressWarnings( value = "FinalMethod" )
    public final ObjectProperty<Alarm> alarmProperty() {

        if ( alarm == null ) {
            alarm = new SimpleObjectProperty<>(Alarm.NONE);
        }

        return alarm;

    }

    public abstract void dispose();

    @SuppressWarnings( value = "FinalMethod" )
    public final Alarm getAlarm() {
        return ( alarm == null ) ? Alarm.NONE : alarm.getValue();
    }

    @SuppressWarnings( value = "FinalMethod" )
    public final String getName() {
        return ( name == null ) ? "–" : name.getValueSafe();
    }

    @SuppressWarnings( value = "FinalMethod" )
    public final String getTooltip() {
        return ( tooltip == null ) ? "–" : tooltip.getValueSafe();
    }

    @SuppressWarnings( value = "FinalMethod" )
    public final String getValue() {
        return ( value == null ) ? "–" : value.getValueSafe();
    }

    @SuppressWarnings( value = "FinalMethod" )
    public final StringProperty nameProperty() {

        if ( name == null ) {
            name = new SimpleStringProperty("–");
        }

        return name;

    }

    @SuppressWarnings( value = "FinalMethod" )
    public final void setAlarm( Alarm alarm ) {
        alarmProperty().set(alarm);
    }

    @SuppressWarnings( value = "FinalMethod" )
    public final void setName( String name ) {
        nameProperty().set(name);
    }

    @SuppressWarnings( value = "FinalMethod" )
    public final void setTooltip( String name ) {
        tooltipProperty().set(name);
    }

    @SuppressWarnings( value = "FinalMethod" )
    public final void setValue( String value ) {
        valueProperty().set(value);
    }

    @SuppressWarnings( value = "FinalMethod" )
    public final StringProperty tooltipProperty() {

        if ( tooltip == null ) {
            tooltip = new SimpleStringProperty("–");
        }

        return tooltip;

    }

    @SuppressWarnings( value = "FinalMethod" )
    public final StringProperty valueProperty() {

        if ( value == null ) {
            value = new SimpleStringProperty("–");
        }

        return value;

    }

}
