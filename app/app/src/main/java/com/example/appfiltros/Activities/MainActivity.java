package com.example.appfiltros.Activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appfiltros.Classes.RetrofitAPI;
import com.example.appfiltros.Interfaces.ProccessImage;
import com.example.appfiltros.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import java.io.File;
import java.io.OutputStream;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button btnCelulas, btnDNA, btnCompleto;
    private EditText edtRaioRBC, edtRaioWBC;
    private FloatingActionButton btnReset, btnSalva, btnGaleria;
    private Slider sldRaioRBC, sldRaioWBC;
    private static final int PEGAR_IMAGEM = 100;
    private static int raioRBC = 10, raioWBC = 13;
    Uri imageUri;

    BitmapDrawable bmpOriginal, bmpProcessado; // usado para pegar o bitmap da imagem original.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        inicializarComponentes();
        
        // Definindo onChange dos Sliders
        sldRaioRBC.addOnChangeListener((slider, valor, fromUser) -> {
            if (fromUser) {
                raioRBC = Math.round(valor);
                edtRaioRBC.setText(String.valueOf(raioRBC));
            }
        });
    
        sldRaioWBC.addOnChangeListener((slider, valor, fromUser) -> {
            if (fromUser) {
                raioWBC = Math.round(valor);
                edtRaioWBC.setText(String.valueOf(raioWBC));
            }
        });
        
        // Definindo onChange dos EditTexts
        edtRaioRBC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        
            }
    
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int valor = 0;
                
                try {
                    valor = Integer.parseInt(charSequence.toString());
                } catch (NumberFormatException ignored) {
                }
                
                sldRaioRBC.setValue(valor);
            }
    
            @Override
            public void afterTextChanged(Editable editable) {
            
            }
        });
    
        edtRaioWBC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            
            }
        
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int valor = 0;
    
                try {
                    valor = Integer.parseInt(charSequence.toString());
                } catch (NumberFormatException ignored) {
                }
    
                sldRaioWBC.setValue(valor);
            }
        
            @Override
            public void afterTextChanged(Editable editable) {
            
            }
        });
        
        imageView.setOnClickListener(view -> escolherImagem());
        
        // Eventos dos botões flutuantes
        btnGaleria.setOnClickListener(v -> escolherImagem());

        btnSalva.setOnClickListener(v -> salvarPraGaleria());
        
        // Eventos dos botões de processamento
        
        btnCelulas.setOnClickListener(v -> realizarRequisicao("blood"));
        
        btnDNA.setOnClickListener(v -> realizarRequisicao("dna"));
        
        btnCompleto.setOnClickListener(v -> realizarRequisicao("all"));
    }
    
    //abrindo a galeria
    private void escolherImagem() {
        Intent galeria = new Intent(Intent.ACTION_PICK);
        galeria.setType("image/*");
        startActivityForResult(galeria, PEGAR_IMAGEM);
    }

    private void salvarPraGaleria(){
        Uri imagem;
        ContentResolver contentResolver = getContentResolver();
    
        imagem = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        //salvando a imagem
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg"); //formato da imagem
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*"); //local onde a imagem será salva
        Uri uri = contentResolver.insert(imagem, contentValues);

        try{
            bmpOriginal = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = bmpOriginal.getBitmap();

            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Objects.requireNonNull(outputStream);

            Toast.makeText(this, "Imagem salva com sucesso!", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            Toast.makeText(this, "Não foi possível salvar a imagem.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //colocando a imagem que foi selecionada na galeria no ImageView
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PEGAR_IMAGEM){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
            bmpOriginal = (BitmapDrawable) imageView.getDrawable();
            bmpProcessado = (BitmapDrawable) imageView.getDrawable();
        }
    }
    
    private static String recuperarCaminho(Context context, Uri uri ) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) {
            result = "Not found";
        }
        
        return result;
    }
    
    private void realizarRequisicao(String opcao) {
        if (imageUri == null) {
            Toast.makeText(this, "Selecione uma imagem!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String caminhoImagem = recuperarCaminho(MainActivity.this, imageUri);
        File imagemAProcessar = new File(caminhoImagem);
        RequestBody corpoReq = RequestBody.create(MediaType.parse("image/*"), imagemAProcessar);
        MultipartBody.Part envio = MultipartBody.Part.createFormData("originalImage", imagemAProcessar.getName(), corpoReq);
    
        ProccessImage servicoProcessarImagem = RetrofitAPI.getProcessImageService();
        Call<ResponseBody> servico = servicoProcessarImagem.processImage(envio, opcao, raioRBC, raioWBC);
        Log.d("RETROFIT URL", servico.request().url().toString());
        // Realizando requisição e pegando resposta
        Toast.makeText(this, "Realizando processamento...", Toast.LENGTH_LONG).show();
        servico.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // display the image data in a ImageView or save it
                        Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                        imageView.setImageBitmap(bmp);
                    } else {
                        Toast.makeText(MainActivity.this, "Algo deu errado ao recuperar a imagem processada!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Algo deu errado ao realizar a requisição!", Toast.LENGTH_SHORT).show();
                }
            }
        
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("RETROFIT", t.getCause() + ": " + t.getMessage());
                Toast.makeText(MainActivity.this, "Erro ao realizar a requisição!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void inicializarComponentes() {
        //iniciando variaveis da imagem
        imageView = findViewById(R.id.imageView);
        bmpOriginal = (BitmapDrawable) imageView.getDrawable();
        bmpProcessado = (BitmapDrawable) imageView.getDrawable();
        
        //botões para galeria
        btnGaleria = findViewById(R.id.btnGaleria);
        btnSalva = findViewById(R.id.btnSalva);
        
        // Botões de ação
        btnDNA = findViewById(R.id.btnDNA);
        btnCelulas = findViewById(R.id.btnCelulas);
        btnCompleto = findViewById(R.id.btnCompleto);
        
        // EditTexts
        edtRaioRBC = findViewById(R.id.edtRaioRBC);
        edtRaioWBC = findViewById(R.id.edtRaioWBC);
        
        // Sliders
        sldRaioRBC = findViewById(R.id.sldRaioRBC);
        sldRaioWBC = findViewById(R.id.sldRaioWBC);
    }
}