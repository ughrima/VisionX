#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_agrima_MainActivity_nativeProcessAndRender(
        JNIEnv *env,
        jobject /*thiz*/,
        jbyteArray data,
        jint width,
        jint height,
        jint mode) {

    if (data == nullptr) return nullptr;
    jbyte *input = env->GetByteArrayElements(data, nullptr);
    if (input == nullptr) return nullptr;

    cv::Mat src(height, width, CV_8UC4, reinterpret_cast<unsigned char *>(input));
    cv::Mat gray, edges, rgbaOut;

    switch (mode) {
        case 0:
            rgbaOut = src.clone();
            break;
        case 1:
            cv::cvtColor(src, gray, cv::COLOR_RGBA2GRAY);
            cv::cvtColor(gray, rgbaOut, cv::COLOR_GRAY2RGBA);
            break;
        case 2:
            cv::cvtColor(src, gray, cv::COLOR_RGBA2GRAY);
            cv::Canny(gray, edges, 100, 200);
            cv::cvtColor(edges, rgbaOut, cv::COLOR_GRAY2RGBA);
            break;
        case 3:
            cv::bitwise_not(src, rgbaOut);
            break;
        case 4:
            cv::GaussianBlur(src, rgbaOut, cv::Size(15, 15), 0);
            break;
        case 5:
            cv::cvtColor(src, gray, cv::COLOR_RGBA2GRAY);
            cv::threshold(gray, gray, 128, 255, cv::THRESH_BINARY);
            cv::cvtColor(gray, rgbaOut, cv::COLOR_GRAY2RGBA);
            break;
        default:
            rgbaOut = src.clone();
            break;
    }

    jsize outputSize = width * height * 4;
    jbyteArray output = env->NewByteArray(outputSize);
    if (output != nullptr) {
        env->SetByteArrayRegion(output, 0, outputSize, reinterpret_cast<jbyte *>(rgbaOut.data));
    }

    env->ReleaseByteArrayElements(data, input, JNI_ABORT);

    return output;
}

