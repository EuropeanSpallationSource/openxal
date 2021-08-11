/*
 * Copyright (C) 2021 European Spallation Source ERIC.
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
package xal;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Juan F. Esteban Müller <JuanF.EstebanMuller@ess.eu>
 */
public class XalExceptionTest {

    public XalExceptionTest() {
    }

    @Test
    public void testNakedException() {
        Exception exception = assertThrows(XalException.class, () -> {
            throw new XalException();
        });

        String actualMessage = exception.getMessage();
        assertTrue(actualMessage == null);
    }

    @Test
    public void testExceptionMsg() {
        String expectedMessage = "Test";

        Exception exception = assertThrows(XalException.class, () -> {
            throw new XalException(expectedMessage);
        });

        String actualMessage = exception.getMessage();
        assertEquals(actualMessage, expectedMessage);
    }

    @Test
    public void testExceptionThrowable() {
        Throwable expectedCause = new RuntimeException();

        Exception exception = assertThrows(XalException.class, () -> {
            throw new XalException(expectedCause);
        });

        Throwable actualCause = exception.getCause();
        assertEquals(actualCause, expectedCause);
    }

    @Test
    public void testExceptionMsgAndThrowable() {
        String expectedMessage = "Test";
        Throwable expectedCause = new RuntimeException();

        Exception exception = assertThrows(XalException.class, () -> {
            throw new XalException(expectedMessage, expectedCause);
        });

        String actualMessage = exception.getMessage();
        assertEquals(actualMessage, expectedMessage);

        Throwable actualCause = exception.getCause();
        assertEquals(actualCause, expectedCause);
    }
}
