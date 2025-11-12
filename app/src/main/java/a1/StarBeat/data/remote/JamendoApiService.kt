package a1.StarBeat.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface JamendoApiService {
    // Exemplo de busca de tracks:
    // GET https://api.jamendo.com/v3.0/tracks/?client_id=XXX&format=json&limit=20&search=...
    @GET("tracks/")
    suspend fun searchTracks(
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String? = null,
        @Query("namesearch") namesearch: String? = null,
        @Query("artist_name") artistName: String? = null,
        @Query("fuzzytags") fuzzyTags: String? = null,
        @Query("audioformat") audioFormat: String? = null,   // mp31, mp32, ogg, flac etc.
        @Query("audiodlformat") audioDlFormat: String? = null, // mp31, mp32, ogg, flac
        @Query("clientId") clientId: String
    ): JamendoTracksResponse

    // Download streaming (use URL vindo do campo audiodownload ou audio)
    @GET
    @Streaming
    suspend fun downloadFile(@Url fileUrl: String): Response<ResponseBody>
}