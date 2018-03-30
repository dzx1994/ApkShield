#ifndef TESTDEMO_DECOMPRESS_H
#define TESTDEMO_DECOMPRESS_H
#ifdef __cplusplus
extern "C" {
#endif
int uncompress(unsigned char *input, size_t inLen, unsigned char**output);
#ifdef __cplusplus
}
#endif
#endif //TESTDEMO_DECOMPRESS_H
