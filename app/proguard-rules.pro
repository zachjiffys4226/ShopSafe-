# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# --------------------------------------------------
# General Attributes & Annotations
# --------------------------------------------------
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# --------------------------------------------------
# ShopSafe Application Data Models & Architecture
# --------------------------------------------------
-keep class com.example.shopsafe.data.models.** { *; }
-keep class com.example.shopsafe.data.local.** { *; }
-keep class com.example.shopsafe.data.repository.** { *; }
-keep class com.example.shopsafe.data.ml.** { *; }
-keep class com.example.shopsafe.data.services.** { *; }
-keep class com.example.shopsafe.ui.ShopSafeViewModel** { *; }
-keep class com.example.shopsafe.ui.** { *; }
-keep class com.example.shopsafe.util.** { *; }

# --------------------------------------------------
# Kotlinx Serialization
# --------------------------------------------------
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# --------------------------------------------------
# Room Database
# --------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-dontwarn androidx.room.**

# --------------------------------------------------
# Retrofit 2, OkHttp 3 & Moshi
# --------------------------------------------------
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Moshi
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class * extends com.squareup.moshi.JsonAdapter {
    public <init>(...);
}

# --------------------------------------------------
# Google Play Services, Google Maps & Location
# --------------------------------------------------
-dontwarn com.google.android.gms.**
-dontwarn com.google.maps.android.**
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-keep class com.google.android.gms.location.** { *; }
-keep interface com.google.android.gms.location.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.maps.android.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# --------------------------------------------------
# Firebase (Firestore, Auth, Messaging, AppCheck, Vertex/AI) & Credential Manager
# --------------------------------------------------
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.internal.firebase**
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**
-keep class com.google.firebase.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }

# --------------------------------------------------
# Kotlin Coroutines
# --------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# --------------------------------------------------
# Jetpack Compose & UI / CameraX / Coil
# --------------------------------------------------
-keep class androidx.compose.** { *; }
-dontwarn coil.**
-keep class coil.** { *; }
-dontwarn androidx.camera.**
-keep class androidx.camera.** { *; }

# --------------------------------------------------
# Android App Bundle & Google Play SDK Metadata
# --------------------------------------------------
# Keep Google Play Services and Play Core metadata
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**
-keep class com.google.android.gms.common.annotation.KeepName { *; }
-keep @interface com.google.android.gms.common.annotation.KeepName
-keepclassmembers class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# Preserve Native and JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelables and CREATOR fields
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Enum values and valueOf
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


