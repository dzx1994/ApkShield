#ifndef RC4_JNI_H_
#define RC4_JNI_H_
#ifdef __cplusplus
extern "C" {
#endif
	void getKey(unsigned char *key, unsigned long dataLen);
	void rc4_crypt(unsigned char *s, unsigned char *Data, unsigned long Len); 
	void rc4_init(unsigned char *s, unsigned char *key, unsigned long Len);
#ifdef __cplusplus
}
#endif
#endif
