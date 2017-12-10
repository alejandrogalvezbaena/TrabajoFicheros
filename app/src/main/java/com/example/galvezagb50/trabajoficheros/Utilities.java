package com.example.galvezagb50.trabajoficheros;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by galvezagb50.
 */

public class Utilities {
    public static String readExternal(String fileName, String codification){
        String result = "";
        if (canReadExternal()){
            File file, externalCardName;
            externalCardName = Environment.getExternalStorageDirectory();
            file = new File(externalCardName.getAbsolutePath(), fileName);
            result = read(file, codification);
        }
        return result;
    }

    public static boolean canReadExternal(){
        boolean canRead = false;
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
                || state.equals(Environment.MEDIA_MOUNTED))
            canRead = true;
        return canRead;
    }

    private static String read(File file, String codification){
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuilder result = new StringBuilder();
        int n;
        try {
            fileInputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(fileInputStream, codification);
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((n = bufferedReader.read()) != -1)
                result.append((char) n);
        } catch (IOException e) {
            Log.e("Error", e.getMessage());
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                Log.e("Error al cerrar", e.getMessage());
            }
        }
        return result.toString();
    }

    public static void leerRaw(Context context) {
        try {
            InputStreamReader isr = new InputStreamReader(context.getResources().openRawResource(R.raw.tiempo));
            BufferedReader br = new BufferedReader(isr);

            String linea;

            StringBuilder texto = new StringBuilder();

            while((linea = br.readLine()) != null)
            {
                texto.append(linea);
                texto.append("\n");
            }
            br.close();
            isr.close();
            //tiempo=Long.valueOf(texto.toString()); NO PUEDO HACER LA CONVERSION DE STRING A LONG

        } catch(IOException e)
        { e.printStackTrace(); }
    }
}
