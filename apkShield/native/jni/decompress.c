#include <malloc.h>
#include <string.h>

#include "decompress.h"
#include "lzma/LzmaLib.h"

#define FILE_SIZE 4

int uncompress(unsigned char *input, size_t inLen, unsigned char**output)
{
    unsigned char props[LZMA_PROPS_SIZE];
    memcpy(props, input, LZMA_PROPS_SIZE);
    size_t outputSize;
    memcpy(&outputSize, input + LZMA_PROPS_SIZE, FILE_SIZE);
    *output = (unsigned char *)malloc(outputSize);
    input += 8 + LZMA_PROPS_SIZE;
    LzmaUncompress(*output, &outputSize, input, &inLen, props, LZMA_PROPS_SIZE);
    return outputSize;
}
