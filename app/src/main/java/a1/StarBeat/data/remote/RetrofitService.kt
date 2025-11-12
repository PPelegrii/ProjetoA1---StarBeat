package a1.StarBeat.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitBuilder {
    /**
     * Cria o serviço Retrofit para interagir com a API Jamendo.
     */
    fun buildJamendoService(baseUrl: String = "https://api.jamendo.com/v3.0/"): JamendoApiService {
        val clientId = "31dfeabd" //JAMENDO_CLIENT_ID

        // Verifica se o clientId está presente
        if (clientId.isEmpty()) {
            throw IllegalStateException("JAMENDO_CLIENT_ID não configurado corretamente. Verifique o arquivo local.properties ou as variáveis de ambiente.")
        }

        // Cria um interceptor para adicionar o client_id em cada requisição
        val clientIdInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url()

            // Adiciona o client_id à URL da requisição
            val newUrl = originalUrl.newBuilder()
                .addQueryParameter("client_id", clientId)
                .build()

            val newRequest = originalRequest.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        }

        // Configura o OkHttpClient com o interceptor
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(clientIdInterceptor)
            .build()

        // Cria o Retrofit com o OkHttpClient e GsonConverter
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Usa o GsonConverter para converter as respostas
            .build()

        // Retorna o serviço Retrofit para fazer as requisições
        return retrofit.create(JamendoApiService::class.java)
    }
}