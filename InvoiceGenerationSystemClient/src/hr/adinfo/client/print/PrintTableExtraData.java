/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.print;

import java.util.ArrayList;
import hr.adinfo.utils.Pair;

/**
 *
 * @author Matej
 */
public class PrintTableExtraData {
	public ArrayList<Pair<String, String>> headerList = new ArrayList<Pair<String, String>>();
	public ArrayList<Pair<String, String>> footerList = new ArrayList<Pair<String, String>>();
}
