package com.example.appfiltros.Interfaces;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ProccessImage {
  @POST("/")
  //@Streaming
  @Multipart
  Call<ResponseBody> processImage(
      @Part MultipartBody.Part imagem,
      @Query("task") String task,
      @Query("RBC") int rbcRadius,
      @Query("WBC") int wbcRadius
  );
}
