#include <stdio.h>
#include <string.h>

#include <memory.h>
#include <stdlib.h>
#include "malloc.h"

typedef unsigned long ULONG;

void rc4_init(unsigned char *s, unsigned char *key, unsigned long Len) 
{

	int i =0, j = 0;
	unsigned char k[256] = {0};
	unsigned char tmp = 0;
	for(i=0;i<256;i++)
	{
		s[i]=i;
		k[i]=key[i%Len];
	}
	for (i=0; i<256; i++)
	{
		j=(j+s[i]+k[i])%256;
		tmp = s[i];
		s[i] = s[j];
		s[j] = tmp;
	}

}

void  rc4_crypt(unsigned char *s, unsigned char *Data, unsigned long Len) 
{

	int i = 0, j = 0, t = 0;
	unsigned long k = 0;
	unsigned char tmp;
	for(k=0;k<Len;k++)
	{
		i=(i+1)%256;
		j=(j+s[i])%256;
		tmp = s[i];
		s[i] = s[j];
		s[j] = tmp;
		t=(s[i]+s[j])%256;
		Data[k] ^= s[t];
	}
}

void  getKey(unsigned char *key, unsigned long dataLen)
{
	unsigned char tmpChar[128] = {	'q', 'F', 'h', '~', 'g', '+', 'f', '?',\
									'\"', 'c', 'e', 't', 'j', '`', '^', '2',\
									'@', '%', 'b', '<', '>', ';', ':', '{',\
									'\\', '\n', '}', 'y', 'i', '(', 'O', '7',\
									'9', 'l', 'A', '$', 'h', 'd', 'r', 'o',\
									'p', 'h', '&', 'S', 'x', '|', ')', 'k',\
									'e', 'l', ' ', 'i', 'z', 'W', ':', '.',\
									'x', 'z', 'N', 'u', 'r', 'M', ',', 'v',\
									'g', 'w', 'b', '4', '!', '-', 'f', '_',\
									'c', '\t', 'p', 'd', 'h', '#', 'g', '0',\
									'.', '+', 'x', '~', '`', 'c', 'A', 'F',\
									'A', 'K', ']', '[', '*', 'Y', 'I', 'P',\
									'1', '>', '\"', '-', '*', 'E', '$', 't',\
									'L', '3', 'S', 'V', '4', '\\', 'm', 'n',\
									'x', 'Q', 'R', 'q', 'c', 't', ',', 'w',\
									']', '/', 's', 'T', 'x', 'b', '5', '.'	};
	for(int i=0; i<256; ++i)
	{
		if(i%2 != 0)
		{
			key[i] = dataLen%(i+1);
		}
		else
		{
			key[i] = tmpChar[i/2];
		}
	}
}


