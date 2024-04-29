/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.utils;

import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class Pair<K,V> implements Serializable {
    private K key;
    private V value;

    public K getKey() { return key; }
    public V getValue() { return value; }
	
	public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
