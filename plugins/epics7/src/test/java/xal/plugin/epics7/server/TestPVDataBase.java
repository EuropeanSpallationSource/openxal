/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.plugin.epics7.server;

import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVRecord;

/**
 *
 * @author juanfestebanmuller
 */
public class TestPVDataBase implements PVDatabase {
    @Override
    public void destroy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PVRecord findRecord(String recordName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addRecord(PVRecord record) {
        return true;
    }

    @Override
    public boolean removeRecord(PVRecord record) {
        return true;
    }

    @Override
    public String[] getRecordNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
