package com.suibiankan.tv.di

import com.suibiankan.tv.data.local.AppDatabase
import com.suibiankan.tv.data.local.SearchHistoryDao
import com.suibiankan.tv.data.parser.*
import com.suibiankan.tv.data.remote.SearchApi
import com.suibiankan.tv.data.remote.SearchEngine
import com.suibiankan.tv.data.repository.SearchRepository
import com.suibiankan.tv.data.repository.SearchRepositoryImpl
import com.suibiankan.tv.domain.usecase.ExtractVideoLinkUseCase
import com.suibiankan.tv.domain.usecase.GetSearchHistoryUseCase
import com.suibiankan.tv.domain.usecase.SearchVideosUseCase
import com.suibiankan.tv.ui.webview.WebViewViewModel
import com.suibiankan.tv.util.Constants
import com.suibiankan.tv.viewmodel.DetailViewModel
import com.suibiankan.tv.viewmodel.SearchViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import androidx.room.Room

val appModule = module {

    // ── OkHttpClient ──
    single<OkHttpClient> {
        val builder = OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        // Log network requests in debug builds
        if (com.suibiankan.tv.BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addNetworkInterceptor(loggingInterceptor)
        }
        builder.build()
    }

    // ── Retrofit + SearchApi ──
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://html.duckduckgo.com/")
            .client(get())
            .build()
    }

    single<SearchApi> {
        get<Retrofit>().create(SearchApi::class.java)
    }

    // ── HTML Parsers ──
    single<Map<SearchEngine, HtmlParser>> {
        mapOf(
            SearchEngine.DUCKDUCKGO to DuckDuckGoParser(),
            SearchEngine.BAIDU to BaiduParser(),
            SearchEngine.BING to BingParser()
        )
    }

    // ── Video Link Extractor ──
    single { VideoLinkExtractor() }

    // ── Room Database ──
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "suibiankan.db"
        ).build()
    }

    single<SearchHistoryDao> {
        get<AppDatabase>().searchHistoryDao()
    }

    // ── Repository ──
    single<SearchRepository> {
        SearchRepositoryImpl(
            api = get(),
            parsers = get(),
            videoLinkExtractor = get(),
            searchHistoryDao = get()
        )
    }

    // ── Use Cases ──
    factory { SearchVideosUseCase(repository = get()) }
    factory { ExtractVideoLinkUseCase(repository = get()) }
    factory { GetSearchHistoryUseCase(repository = get()) }

    // ── ViewModels ──
    viewModel { SearchViewModel(searchVideosUseCase = get(), getSearchHistoryUseCase = get()) }
    viewModel { DetailViewModel(extractVideoLinkUseCase = get()) }
    viewModel { WebViewViewModel() }
}
