#ifdef __cplusplus
extern "C" {
#endif
jint filedigest(JNIEnv *env, jobject obj,jbyteArray fileByteArr,jint filelen,jint tab,jstring fileName,jint total);
int getLinkList(char* manifile);
#ifdef __cplusplus
}
#endif
