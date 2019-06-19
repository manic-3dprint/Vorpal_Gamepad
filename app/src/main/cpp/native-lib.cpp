#include <jni.h>
#include <string>
#include <errno.h>
#include "Vorpal-Hexapod-Gamepad_ino.h"
#include "SdFat.h"
#include <iterator>
extern SoftwareSerial BlueTooth;
extern byte TrimMode;
extern int SRecState;
extern int GRecState;

char internalFileDir[BUFFER_SIZE];
char errorMessage[BUFFER_SIZE];
SoftwareSerial Serial = SoftwareSerial();
long bluetoothSpeed;
bool isSDcard;

// https://stackoverflow.com/questions/11558899/passing-a-string-to-c-code-in-android-ndk
std::string ConvertJString(JNIEnv* env, jstring str)
{
    //if ( !str ) LString();

    const jsize len = env->GetStringUTFLength(str);
    const char* strChars = env->GetStringUTFChars(str, (jboolean *)nullptr);

    std::string Result(strChars, (unsigned long)len);

    env->ReleaseStringUTFChars(str, strChars);

    return Result;
}

std::vector<byte> jByteArrayToStdVector(JNIEnv *env, jbyteArray arr)
{
    std::vector<byte> ret;
    jsize len = env->GetArrayLength(arr);
    jbyte *body = env->GetByteArrayElements(arr, nullptr);
    for (int i=0; i<len; i++) {
        ret.push_back((byte)body[i]);
    }
    return ret;
}
jbyteArray stdVectorToJByteArray(JNIEnv *env, std::vector<byte> value)
{
    jsize arraySize = (jsize)value.size();
    jbyte jniValue[arraySize];
    std::copy(value.begin(),
              value.end(),
              jniValue);
    jbyteArray ret = env->NewByteArray(arraySize);
    env->SetByteArrayRegion (ret, 0, arraySize, jniValue);
    return ret;
}
jbyteArray charArrayToJByteArray(JNIEnv *env, char* buffer)
{
    jsize arraySize = strlen(buffer);
    jbyte jniValue[arraySize];
    std::copy(buffer, buffer+arraySize+1, jniValue);
    jbyteArray ret = env->NewByteArray(arraySize);
    env->SetByteArrayRegion (ret, 0, arraySize, jniValue);
    return ret;
}
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_vorpalrobotics_hexapod_MainActivity_arduinoSetup(JNIEnv *env, jobject) {
    setup();
    std::vector<byte> value = Serial.getOutput();
    Serial.clearOutput();
    return stdVectorToJByteArray(env, value);
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_vorpalrobotics_hexapod_ArduinoThread_arduinoLoop(JNIEnv *env, jobject, jbyteArray serialInput, jbyteArray bluetoothInput) {
    errorMessage[0] = '\0';
    Serial.setInput(jByteArrayToStdVector(env, serialInput));
    BlueTooth.setInput(jByteArrayToStdVector(env, bluetoothInput));
    loop();
    BlueTooth.clearInput();
    jobjectArray ret = (jobjectArray)env->NewObjectArray(4,env->FindClass("[B"),nullptr);
    std::vector<byte> indicators;
    indicators.push_back(TrimMode);
    indicators.push_back((byte)SRecState);
    indicators.push_back((byte)GRecState);
    env->SetObjectArrayElement(ret,0,stdVectorToJByteArray(env, Serial.getOutput()));
    env->SetObjectArrayElement(ret,1,stdVectorToJByteArray(env, BlueTooth.getOutput()));
    env->SetObjectArrayElement(ret,2,stdVectorToJByteArray(env, indicators));
    env->SetObjectArrayElement(ret,3,charArrayToJByteArray(env, errorMessage));
    Serial.clearOutput();
    BlueTooth.clearOutput();
    strcpy(errorMessage,"");
    return(ret);
}

extern "C" JNIEXPORT void JNICALL
Java_com_vorpalrobotics_hexapod_MainActivity_clickButton(JNIEnv * env, jobject, jstring buttonNameJava, jboolean isOnJava)
{
    std::string buttonNameC = ConvertJString( env, buttonNameJava );
    bool isOnC = isOnJava;
    clickGamepadButton(buttonNameC, isOnC);
}

extern "C" JNIEXPORT void JNICALL
Java_com_vorpalrobotics_hexapod_MainActivity_gamepadPowerOn(JNIEnv *env, jobject)
{
    gamepadPowerOn();
}

extern "C" JNIEXPORT void JNICALL
Java_com_vorpalrobotics_hexapod_MainActivity_gamepadPowerOff(JNIEnv *, jobject)
{
    gamepadPowerOff();
}

extern "C" JNIEXPORT void JNICALL
Java_com_vorpalrobotics_hexapod_MainActivity_setInternalFileDir(JNIEnv *env, jobject, jstring internalFileDir_)
{
    strcpy(internalFileDir, ConvertJString(env, internalFileDir_).c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_vorpalrobotics_hexapod_MainActivity_setSDcard(JNIEnv *env, jobject, jboolean isSDcard_)
{
    isSDcard = isSDcard_;
}

extern "C" JNIEXPORT void JNICALL
Java_com_vorpalrobotics_hexapod_MainActivity_setBluetoothSpeed(JNIEnv *env, jobject, jlong bluetoothSpeed_)
{
    bluetoothSpeed = bluetoothSpeed_;
}
