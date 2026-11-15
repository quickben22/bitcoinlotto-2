# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Room creates generated RoomDatabase implementations via reflection. Room 2.2.5 (pulled in
# transitively by WorkManager 2.7.0) ships a consumer rule that keeps the generated class but
# not its constructor, so R8 strips it and the app crashes at startup in WorkManager's
# initializer. Keep the constructors explicitly until WorkManager is upgraded.
-keep class * extends androidx.room.RoomDatabase { <init>(...); }
