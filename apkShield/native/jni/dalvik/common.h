#ifndef COMMON_H_
#define COMMON_H_

#include <android/log.h>
#include <stdlib.h>


#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "WKnightShield", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "WKnightShield", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "WKnightShield", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "WKnightShield", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "WKnightShield", __VA_ARGS__)

#define CHECK_VALID(V) 				\
	if(V == NULL){					\
		LOGE("%s is null.", #V);	\
		exit(-1);					\
	}else{							\
		LOGI("%s is %p.", #V, V);	\
	}								\

#endif /* COMMON_H_ */
