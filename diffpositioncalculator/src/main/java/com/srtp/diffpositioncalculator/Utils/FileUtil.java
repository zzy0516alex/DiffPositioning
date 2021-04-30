package com.srtp.diffpositioncalculator.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileUtil {
    public static void clearInfoForFile(String fileName) {
        File file =new File(fileName);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void WriteTXT(String filename,String content,boolean append){
        File mk_txt=new File(filename);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mk_txt, append)));
            out.write(content+"\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert out != null;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void table_writer(StringBuilder builder,String format,String ...columns){
        for (String col:columns) {
            builder.append(String.format(format,col));
            builder.append("\t");
        }
        builder.append("\n");
    }
}
