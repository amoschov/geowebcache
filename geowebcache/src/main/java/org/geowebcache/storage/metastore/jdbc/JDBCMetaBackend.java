/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Arne Kepp / The Open Planning Project 2009
 *  
 */
package org.geowebcache.storage.metastore.jdbc;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.storage.MetaStore;
import org.geowebcache.storage.StorageException;
import org.geowebcache.storage.StorageObject;
import org.geowebcache.storage.TileObject;
import org.geowebcache.storage.WFSObject;
import org.h2.tools.DeleteDbFiles;

public class JDBCMetaBackend implements MetaStore {
    private static Log log = LogFactory.getLog(org.geowebcache.storage.metastore.jdbc.JDBCMetaBackend.class);
    
    /** Wrapper that sets everything up */
    private final JDBCMBWrapper wrpr;
    
    /** Cache for translating layers and parameter strings to ids */
    private final JDBCMBIdCache idCache;
    
    public JDBCMetaBackend(String driverClass, String jdbcString, String username, String password) throws StorageException {
        try {
            wrpr = new JDBCMBWrapper(driverClass, jdbcString, username, password);
        } catch(SQLException se) {
            throw new StorageException(se.getMessage());
        }
        
        idCache = new JDBCMBIdCache(wrpr);
    }

    public boolean get(StorageObject stObj) throws StorageException {
        if(stObj instanceof TileObject) {
            return getTile((TileObject) stObj);
        } else if(stObj instanceof WFSObject) {
            return getWFS((WFSObject) stObj);
        } else {
            throw new StorageException(
                    this.getClass().getCanonicalName() 
                    + " cannot handle " 
                    + stObj.getClass().getCanonicalName() );
        }
    }
    
    public void put(StorageObject stObj) throws StorageException {
        if(stObj instanceof TileObject) {
            putTile((TileObject) stObj);
        } else if(stObj instanceof WFSObject) {
            putWFS((WFSObject) stObj);
        } else {
            throw new StorageException(
                    this.getClass().getCanonicalName() 
                    + " cannot handle " 
                    + stObj.getClass().getCanonicalName() );
        }
    }
    
    public void remove(StorageObject stObj) throws StorageException {
        if(stObj instanceof TileObject) {
            removeTile((TileObject) stObj);
        } else if(stObj instanceof WFSObject) {
            removeWFS((WFSObject) stObj);
        } else {
            throw new StorageException(
                    this.getClass().getCanonicalName() 
                    + " cannot handle " 
                    + stObj.getClass().getCanonicalName() );
        }
    }

    private boolean getTile(TileObject stObj) throws StorageException {
        long[] xyz = stObj.getXYZ();
        Integer layer_id = idCache.getLayerId(stObj.getLayerName());
        Integer format_id = idCache.getFormatId(stObj.getBlobFormat());
        Integer parameters_id = null;
        if(stObj.getParameters() != null) {
            parameters_id = idCache.getParametersId(stObj.getParameters());
        }
        
        try {
            return wrpr.getTile(layer_id,xyz,format_id,parameters_id, stObj);
        } catch (SQLException se) {
            log.error("Failed to get tile: " + se.getMessage());
        }
        
        return false;
    }
    
    private boolean getWFS(WFSObject stObj) throws StorageException {
        Integer parameters_id = null;
        
        if(stObj.getParameters() != null) {
            parameters_id = idCache.getParametersId(stObj.getParameters());
        } 
        //else {
        //    throw new StorageException(
        //            "Unable to deal with WFS requests that do not use a parameter string.");
        //}
        
        try {
            return wrpr.getWFS(parameters_id, stObj);
        } catch (SQLException se) {
            log.error("Failed to get WFS object: " + se.getMessage());
        }
        
        return false;
    }



    private void putTile(TileObject stObj) throws StorageException {
        long[] xyz = stObj.getXYZ();
        Integer layer_id = idCache.getLayerId(stObj.getLayerName());
        Integer format_id = idCache.getFormatId(stObj.getBlobFormat());
        Integer parameters_id = null;
        if(stObj.getParameters() != null) {
            parameters_id = idCache.getParametersId(stObj.getParameters());
        }
        
        try {
            wrpr.putTile(layer_id,xyz,format_id,parameters_id, stObj);
        } catch (SQLException se) {
            log.error("Failed to put tile: " + se.getMessage());
        }
    }
    

    private void putWFS(WFSObject stObj) throws StorageException {
        Integer parameters_id = null;

        if (stObj.getParameters() != null) {
            parameters_id = idCache.getParametersId(stObj.getParameters());
        }

        try {
            wrpr.putWFS(parameters_id, stObj);
        } catch (SQLException se) {
            log.error("Failed to put WFS object: " + se.getMessage());
        }

    }
    
    private void removeWFS(WFSObject stObj) {
        // TODO Auto-generated method stub
        
    }

    private void removeTile(TileObject stObj) {
        // TODO Auto-generated method stub
        
    }

    public void clear() throws StorageException {
       if(wrpr.driverClass.equals("org.h2.Driver")) {
           //TODO 
       //    wrpr.getConnection().getMetaData().get
       //    DeleteDbFiles.execute(findTempDir(), TESTDB_NAME, true);
       //} else {
           throw new StorageException(
                   "clear() has not been implemented for "+wrpr.driverClass);
       }
        
    }

}
