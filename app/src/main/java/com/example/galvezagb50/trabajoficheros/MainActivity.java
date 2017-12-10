package com.example.galvezagb50.trabajoficheros;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    Button btDownload;
    ImageView ivImage;
    EditText edtUrlImagenes, edtUrlFrases;
    TextView txvFrase;
    ArrayList<String> urls;
    int urlActual;
    Handler handler;
    Timer timer;
    String frases;
    String[] frase;
    int contador;
    long tiempo;

    String fileRoute;
    String fileName = "frases.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUrlImagenes = (EditText) findViewById(R.id.edtUrlImagenes);
        edtUrlFrases = (EditText) findViewById(R.id.edtUrlFrases);
        txvFrase=(TextView)findViewById(R.id.txvFrase) ;
        btDownload = (Button) findViewById(R.id.btnDescargar);
        ivImage = (ImageView) findViewById(R.id.imgImagen);
        handler=new Handler();
        contador=0;
        fileRoute=edtUrlFrases.getText().toString();
        tiempo=5000;

        btDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = edtUrlImagenes.getText().toString();
                if (!TextUtils.isEmpty(url)){
                    loadUrls(url);
                    frases=Utilities.readExternal(fileName, "UTF-8");
                    frase=frases.split("\n");
                    Utilities.leerRaw(getApplicationContext());
                    pasarFoto();
                }
            }
        });

        if (isNetworkAvailable()) {
            try {
                new Download().execute();

            } catch (Exception e) {
                e.getMessage();
            }
        } else {
            Toast.makeText(this, "No hay Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private void loadUrls(String url) {
        final ProgressDialog dialog = new ProgressDialog(this);
        RestClient.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                dialog.setCancelable(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Conectando...");
                dialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                urls = new ArrayList<String>();
                CharSequence seq2 = new String(responseBody, Charset.forName("UTF-8"));
                String pattern = "(http(s?):/)(/[^/]+)+" + ".(?:jpg|gif|png)";
                boolean matches;
                for (String q : seq2.toString().split("\n| ")) {
                    matches = q.matches(pattern);
                    if (matches)
                        urls.add(q);
                }
                if (urls != null && urls.size() > 0){
                    urlActual = 0;
                    setImage();

                    Toast.makeText(MainActivity.this, "Se ha descargado correctamente", Toast.LENGTH_SHORT).show();
                } else {

                    ivImage.setImageDrawable(null);

                    Toast.makeText(MainActivity.this, "Se ha descargado correctamente, pero no había ninguna imagen válida", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                ivImage.setImageDrawable(null);
                Toast.makeText(MainActivity.this, "Error:" + error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void changeImage() {
        if (urlActual == urls.size() - 1)
            urlActual = 0;
        else
            urlActual ++;
        txvFrase.setText(frase[contador]);
        if (contador == frase.length - 1)
            contador = 0;
        else
            contador ++;
        setImage();
    }

    private void setImage() {
        Picasso.with(getApplicationContext())
                .load(urls.get(urlActual))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(ivImage);
    }

    public void pasarFoto(){

        TimerTask tarea = new TimerTask(){
            @Override
            public void run() {

                handler.post(new Runnable(){
                    public void run() {
                        changeImage();
                    };
                });

            }        };

        timer = new Timer();
        timer.schedule(tarea, tiempo,tiempo);
    }

    class Download extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                URL url = new URL(fileRoute);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                InputStream is = url.openStream();
                File file = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
                if (!file.exists())
                    file.createNewFile();

                FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/"+fileName);
                byte data[] = new byte[1024];
                long total = 0;
                int count = 0;
                while ((count = is.read(data)) != -1) {
                    total += count;
                    int progress_temp = (int) total * 100 / lenghtOfFile;
                    fos.write(data, 0, count);
                }
                Handler handler =  new Handler(MainActivity.this.getMainLooper());
                handler.post( new Runnable(){
                    public void run(){
                        Toast.makeText(MainActivity.this,"Archivo frases.txt actualizado correctamente", Toast.LENGTH_SHORT).show();
                    }
                });
                is.close();
                fos.close();
            } catch (final Exception e) {
                Handler handler =  new Handler(MainActivity.this.getMainLooper());
                handler.post( new Runnable(){
                    public void run(){
                        Toast.makeText(MainActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
    }

}


