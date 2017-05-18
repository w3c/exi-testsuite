
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/time.h>
#include <com_sun_japex_jdsl_nativecode_JapexNativeDriver.h>

/*
 * These functions are defined towards the bottom of this file
 */
jobject userDataToObject(JNIEnv *env, void *userData, int size);
void* objectToUserData(JNIEnv *env, jobject object);
const char* getParam(JNIEnv *env, jobject this, const char *name);
void setLongParam(JNIEnv *env, jobject this, const char *name, long value);
long getLongParam(JNIEnv *env, jobject this, const char *name);

/*
 * Sample UserData struct
 */
struct UserData {
    char *s;
    int n;
};

/*
 * Class:     com_sun_japex_jdsl_nativecode_JapexNativeDriver
 * Method:    initializeDriver
 * Signature: (Ljava/lang/Object;)V
 *
 * Default value of userData is null when this function is called.
 */
JNIEXPORT jobject JNICALL Java_com_sun_japex_jdsl_nativecode_JapexNativeDriver_initializeDriver
  (JNIEnv *env, jobject this, jobject userData) 
{
    printf("JapexNativeDriverTwo: initializeDriver()\n");

    /*
     * Create and initialize an instance of UserData. Use 
     * userDataToObject() to return it as a Java object.
     */
    struct UserData *ud = (struct UserData*) malloc(sizeof(struct UserData));
    ud->n = 5;
    ud->s = (char *) malloc(ud->n + 1);
    strcpy(ud->s, "Hello");
    return userDataToObject(env, ud, sizeof(struct UserData));
}

/*
 * Class:     com_sun_japex_jdsl_nativecode_JapexNativeDriver
 * Method:    prepare
 * Signature: (Lcom/sun/japex/TestCase;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_sun_japex_jdsl_nativecode_JapexNativeDriver_prepare
  (JNIEnv *env, jobject this, jobject testCase, jobject userData) 
{
    printf("JapexNativeDriverTwo: prepare()\n");

    /*
     * Convert Java object to UserData instance and access
     * members set in initializeDriver() function.
     */
    struct UserData *ud = objectToUserData(env, userData);
    printf("userdata = (%s, %d)\n", ud->s, ud->n);
}

/*
 * Class:     com_sun_japex_jdsl_nativecode_JapexNativeDriver
 * Method:    warmup
 * Signature: (Lcom/sun/japex/TestCase;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_sun_japex_jdsl_nativecode_JapexNativeDriver_warmup
  (JNIEnv *env, jobject this, jobject testCase, jobject userData) 
{
    printf("JapexNativeDriverTwo: warmup()\n");
}

/*
 * Class:     com_sun_japex_jdsl_nativecode_JapexNativeDriver
 * Method:    run
 * Signature: (Lcom/sun/japex/TestCase;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_sun_japex_jdsl_nativecode_JapexNativeDriver_run
  (JNIEnv *env, jobject this, jobject testCase, jobject userData) 
{
    printf("JapexNativeDriverTwo: run()\n");

    /* --- THE FOLLOWING TWO LINES SHOW HOW TO THROW A RUNTIME EXCEPTION --- 
    jclass exceptionClass = (*env)->FindClass(env, "java/lang/RuntimeException");
    (*env)->ThrowNew(env, exceptionClass, "Error found!"); */
}

/*
 * Class:     com_sun_japex_jdsl_nativecode_JapexNativeDriver
 * Method:    finish
 * Signature: (Lcom/sun/japex/TestCase;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_sun_japex_jdsl_nativecode_JapexNativeDriver_finish
  (JNIEnv *env, jobject this, jobject testCase, jobject userData) 
{
    printf("JapexNativeDriverTwo: finish()\n");
}

/*
 * Class:     com_sun_japex_jdsl_nativecode_JapexNativeDriver
 * Method:    terminateDriver
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_sun_japex_jdsl_nativecode_JapexNativeDriver_terminateDriver
  (JNIEnv *env, jobject this, jobject userData) 
{
    printf("JapexNativeDriverTwo: terminateDriver()\n");

    /*
     * Free memory allocated for the UserData instance.
     */
    struct UserData *ud = objectToUserData(env, userData);
    free(ud);
}

/* ---- THE FOLLOWING TWO METHODS SHOW HOW TO ACCESS DRIVER PARAMS ----- */

const char* getParam(JNIEnv *env, jobject this, const char *name)
{
    jclass cls;
    jmethodID mid;
    jstring value;

    cls = (*env)->GetObjectClass(env, this);
    mid = (*env)->GetMethodID(env, cls, "getParam", "(Ljava/lang/String;)Ljava/lang/String;");
    value = (jstring) (*env)->CallObjectMethod(env, this, mid, (*env)->NewStringUTF(env, name));
    return value == NULL ? (const char*) value : (*env)->GetStringUTFChars(env, value, NULL);
}

void setLongParam(JNIEnv *env, jobject this, const char *name, long value) 
{
    jclass cls;
    jmethodID mid;

    cls = (*env)->GetObjectClass(env, this);
    mid = (*env)->GetMethodID(env, cls, "setLongParam", "(Ljava/lang/String;J)V");
    (*env)->CallVoidMethod(env, this, mid, (*env)->NewStringUTF(env, name), value);
}

long getLongParam(JNIEnv *env, jobject this, const char *name) 
{
    jclass cls;
    jmethodID mid;

    cls = (*env)->GetObjectClass(env, this);
    mid = (*env)->GetMethodID(env, cls, "getLongParam", "(Ljava/lang/String;)J");
    return (*env)->CallLongMethod(env, this, mid, (*env)->NewStringUTF(env, name));
}

/* ------- UTILITY METHODS TO CONVERT DIRECT BUFFERS INTO OBJECTS ------ */

jobject userDataToObject(JNIEnv *env, void *userData, int size) {
    return (*env)->NewDirectByteBuffer(env, userData, size);
}

void* objectToUserData(JNIEnv *env, jobject object) {
    return (*env)->GetDirectBufferAddress(env, object);
}

/* --------------------- DO NOT EDIT BELOW THIS LINE ------------------- */

jdouble timeMillis() {
    struct timeval t;
    gettimeofday(&t, 0);
    return t.tv_sec * 1000 + t.tv_usec / 1000.0;
}

/*
 * Class:     japexjni_NativeDriver
 * Method:    runLoopDuration
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_sun_japex_jdsl_nativecode_JapexNativeDriver_runLoopDuration
  (JNIEnv *env, jobject this, jdouble duration, jobject userData) 
{
    jclass cls;
    jfieldID fid;
    jobject _testCase;

    /* Get a reference to _testCase */
    cls = (*env)->GetObjectClass(env, this);
    fid = (*env)->GetFieldID(env, cls, "_testCase", "Lcom/sun/japex/TestCaseImpl;");
    _testCase = (*env)->GetObjectField(env, this, fid);
    
    jdouble startTime = timeMillis();
    jdouble endTime = startTime + duration;

    jdouble currentTime = 0;
    jint iterations = 0;
    do {
        Java_com_sun_japex_jdsl_nativecode_JapexNativeDriver_run(env, this, _testCase, userData);
        iterations++;
        currentTime = timeMillis();
    } while (endTime >= currentTime);

    return iterations;
}

/*
 * Class:     japexjni_NativeDriver
 * Method:    runLoopIterations
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_sun_japex_jdsl_nativecode_JapexNativeDriver_runLoopIterations
  (JNIEnv *env, jobject this, jint iterations, jobject userData) 
{
    jclass cls;
    jfieldID fid;
    jobject _testCase;

    /* Get a reference to _testCase */
    cls = (*env)->GetObjectClass(env, this);
    fid = (*env)->GetFieldID(env, cls, "_testCase", "Lcom/sun/japex/TestCaseImpl;");
    _testCase = (*env)->GetObjectField(env, this, fid);

    int i;
    for (i = 0; i < iterations; i++) {
        Java_com_sun_japex_jdsl_nativecode_JapexNativeDriver_run(env, this, _testCase, userData);
    }
}
