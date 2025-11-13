package a1.StarBeat.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitBuilder {
    fun buildJamendoService(baseUrl: String = "https://api.jamendo.com/v3.0/"): JamendoApiService {
        val clientId = "31dfeabd" //JAMENDO_CLIENT_ID

        if (clientId.isEmpty()) {
            throw IllegalStateException("JAMENDO_CLIENT_ID não configurado corretamente. Verifique o arquivo local.properties ou as variáveis de ambiente.")
        }

        val clientIdInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url()

            val newUrl = originalUrl.newBuilder()
                .addQueryParameter("client_id", clientId)
                .build()

            val newRequest = originalRequest.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(clientIdInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())

        return retrofit.create(JamendoApiService::class.java)
    }
}