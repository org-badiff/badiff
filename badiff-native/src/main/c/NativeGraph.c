/*
 * NativeGraph.c
 *
 *  Created on: Sep 7, 2013
 *      Author: robin
 */

#include <jni.h>

#include "org_badiff_nat_NativeGraph.h"

/*
 * Class:     org_badiff_nat_NativeGraph
 * Method:    compute0
 * Signature: ([B[B)V
 */
JNIEXPORT void JNICALL Java_org_badiff_nat_NativeGraph_compute0
  (JNIEnv *, jobject, jbyteArray, jbyteArray);

/*
 * Class:     org_badiff_nat_NativeGraph
 * Method:    walk0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_badiff_nat_NativeGraph_walk0
  (JNIEnv *, jobject);

/*
 * Class:     org_badiff_nat_NativeGraph
 * Method:    flag0
 * Signature: ()B
 */
JNIEXPORT jbyte JNICALL Java_org_badiff_nat_NativeGraph_flag0
  (JNIEnv *, jobject);

/*
 * Class:     org_badiff_nat_NativeGraph
 * Method:    val0
 * Signature: ()B
 */
JNIEXPORT jbyte JNICALL Java_org_badiff_nat_NativeGraph_val0
  (JNIEnv *, jobject);

/*
 * Class:     org_badiff_nat_NativeGraph
 * Method:    prev0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_badiff_nat_NativeGraph_prev0
  (JNIEnv *, jobject);

/*
 * Class:     org_badiff_nat_NativeGraph
 * Method:    free0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_badiff_nat_NativeGraph_free0
  (JNIEnv *, jobject);
