# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-allowaccessmodification
-reoptimizeclasses 3

-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod

# --- Firebase ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- Google Maps SDK ---
-keep class com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.maps.**
-keep class com.google.maps.android.** { *; }
-dontwarn com.google.maps.android.**

# --- Retrofit & OkHttp & Moshi ---
-keepattributes *Annotation*,Signature,Exception
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# --- Stripe Android SDK ---
-keep class com.stripe.** { *; }
-keep class com.stripe.android.** { *; }
-dontwarn com.stripe.**
-dontwarn com.stripe.android.**
-keepclassmembers class * extends androidx.lifecycle.ViewModel { *; }

# --- Room ---
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

