cmake_minimum_required(VERSION 3.22.1)
project("agrima")

add_library(agrima SHARED native-lib.cpp)

set(OpenCV_DIR /home/agrima/Android/OpenCV-android-sdk/OpenCV-android-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)

target_link_libraries(
        agrima
        ${OpenCV_LIBS}
        android
        log)
