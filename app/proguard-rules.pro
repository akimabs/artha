# Keep Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Coil
-keep class coil.** { *; }

# OkHttp (hindari warning)
-dontwarn okhttp3.**

# Gson - Keep type signatures and annotations
-keepattributes Signature, RuntimeVisibleAnnotations

# Keep Gson TypeToken
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken

# Keep models used by Gson (prevent crash on release build)
-keep class com.example.artha.model.PocketData { *; }
-keep class com.example.artha.model.HistoryItemData { *; }

# (Optional) If using @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class com.example.artha.model.* {
    <fields>;
    <init>();
}

-keepclassmembers class com.example.artha.util.LocalStorageManager {
    public static *** loadHistory(android.content.Context);
    public static *** saveHistory(android.content.Context, java.util.List);
}
-keepclassmembers class com.example.artha.model.HistoryItemData { *; }
