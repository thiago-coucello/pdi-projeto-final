package com.example.appfiltros.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.appfiltros.R;

public class LoginActivity extends AppCompatActivity {
  
  private Button btnComecar;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    
    inicializarComponentes();
    
    btnComecar.setOnClickListener(view -> {
      //Obtendo as permissões do usuário para salvar e acessar a galeria.
      ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    });
  }
  
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    
    if (requestCode == 1) {
      for (int i = 1; i < permissions.length; i++) {
        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
          Toast.makeText(
              LoginActivity.this,
              "Não é possível continuar sem essas permissões!",
              Toast.LENGTH_SHORT
          ).show();
          finish();
        }
      }
      
      startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }
  }
  
  private void inicializarComponentes() {
    btnComecar = findViewById(R.id.btnComecar);
  }
}