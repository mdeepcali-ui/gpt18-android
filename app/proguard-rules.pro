# ═══════════════════════════════════════════════════════════
# GptPlus18 — ProGuard Rules (احترافي شامل)
# ═══════════════════════════════════════════════════════════

# ─── 1) Data Models (Gson) ───
-keep class com.gptplus18.app.data.models.** { *; }
-keepclassmembers class com.gptplus18.app.data.models.** {
    <fields>;
    <init>(...);
}

# ─── 2) Gson ───
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ─── 3) Retrofit ───
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ─── 4) OkHttp + SSE ───
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class com.squareup.okhttp3.sse.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ─── 5) Hilt / Dagger ───
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <init>(...);
}
-keep,allowobfuscation @interface dagger.hilt.android.AndroidEntryPoint
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.* <fields>;
    @javax.inject.* <fields>;
    @javax.inject.* <init>(...);
}
-dontwarn dagger.hilt.**

# ─── 6) Room ───
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.paging.**

# ─── 7) Firebase (Analytics + Crashlytics + Messaging) ───
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Analytics
-keepattributes *Annotation*
-keep class com.google.firebase.analytics.** { *; }

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Firebase Messaging (FCM)
-keep class com.google.firebase.messaging.** { *; }
-keep class * extends com.google.firebase.messaging.FirebaseMessagingService { *; }

# ─── 8) Coil ───
-keep class coil.** { *; }
-dontwarn coil.**

# ─── 9) ZXing ───
-keep class com.google.zxing.** { *; }

# ─── 10) Jetpack Compose ───
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers class androidx.compose.runtime.** { *; }

# ─── 11) DataStore ───
-keep class androidx.datastore.** { *; }
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-dontwarn androidx.datastore.**

# ─── 12) Kotlin Coroutines ───
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.android.** { *; }
-dontwarn kotlinx.coroutines.**

# ─── 13) Kotlin ───
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ─── 14) ViewModel / Lifecycle ───
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─── 15) Hilt ViewModels ───
-keep class * extends androidx.lifecycle.ViewModel {
    @javax.inject.Inject <init>(...);
}

# ─── 16) General Android ───
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.content.ContentProvider

# ─── 17) Native Methods ───
-keepclasseswithmembernames class * {
    native <methods>;
}

# ─── 18) Parcelable ───
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# ─── 19) Enum ───
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ─── 20) Serialization ───
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
