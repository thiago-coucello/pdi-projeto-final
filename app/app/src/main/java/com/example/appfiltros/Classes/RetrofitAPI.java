package com.example.appfiltros.Classes;

import com.example.appfiltros.Interfaces.ProccessImage;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitAPI {
  private static Retrofit api;
  
  private static void createApi() {
    if (api == null) {
      OkHttpClient client = new OkHttpClient.Builder()
          .connectTimeout(300, TimeUnit.SECONDS)
          .build();
      
      api = new Retrofit.Builder()
          .baseUrl("http://192.168.0.9:3333/")
          .addConverterFactory(GsonConverterFactory.create())
          .client(client)
          .build();
    }
  }
  
  public static ProccessImage getProcessImageService() {
    if (api == null) {
      createApi();
    }
  
    return api.create(ProccessImage.class);
  }
}
