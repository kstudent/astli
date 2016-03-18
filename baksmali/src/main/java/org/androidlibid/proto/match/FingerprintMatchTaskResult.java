/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.match;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public enum FingerprintMatchTaskResult {
    OK,
    NOT_PERFECT,
    NO_MATCH_BY_NAME,
    NO_MATCH_BY_DISTANCE,
    CLASS_LENGTH_0;
}
