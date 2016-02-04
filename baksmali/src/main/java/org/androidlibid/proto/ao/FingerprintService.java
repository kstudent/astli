/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

import java.sql.SQLException;
import net.java.ao.EntityManager;
import org.la4j.Vector;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintService {

    private EntityManager em; 

    public FingerprintService(EntityManager em) {
        this.em = em;
    }
    
    public FingerprintEntity saveFingerprint(Vector vector, String name) throws SQLException {
        FingerprintEntity print = em.create(FingerprintEntity.class);
        print.setName(name);
        print.setVector(vector.toBinary());
        print.save();
        return print;
    }
    
    
}
