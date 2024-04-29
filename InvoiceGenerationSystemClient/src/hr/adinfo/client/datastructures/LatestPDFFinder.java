/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.adinfo.client.datastructures;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author dell_
 */
public class LatestPDFFinder {
     public static void main(String[] args) {
        File pdfFolder = new File("PDF"); // Assuming "PDF" folder is located in the same directory as the Java application
        File latestPDF = getLatestPDF(pdfFolder);
        if (latestPDF != null) {
            System.out.println("Latest PDF file: " + latestPDF.getAbsolutePath());
            // Now you can use 'latestPDF' as needed (e.g., read, process, etc.)
        } else {
            System.out.println("No PDF files found in the 'PDF' folder.");
        }
    }

    private static File getLatestPDF(File folder) {
        File[] pdfFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pdf");
            }
        });

        if (pdfFiles != null && pdfFiles.length > 0) {
            Arrays.sort(pdfFiles, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return Long.compare(f2.lastModified(), f1.lastModified());
                }
            });
            return pdfFiles[0]; // The first element is the latest PDF file
        } else {
            return null; // No PDF files found in the folder
        }
    }
}
