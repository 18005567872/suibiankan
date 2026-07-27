# ── Jsoup (uses reflection for CSS selectors) ──
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# ── OkHttp + Okio ──
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# ── Retrofit ──
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ── Gson ──
-keep class com.google.gson.** { *; }
-keep class com.suibiankan.tv.data.remote.dto.** { *; }

# ── Room ──
-keep class com.suibiankan.tv.data.local.** { *; }

# ── Koin ──
-keep class org.koin.** { *; }

# ── Kotlin Coroutines ──
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── Keep app model classes used in serialization ──
-keep class com.suibiankan.tv.domain.model.** { *; }
-keep class com.suibiankan.tv.data.remote.dto.** { *; }
