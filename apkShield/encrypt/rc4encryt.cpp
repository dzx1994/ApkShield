#include <stdio.h>
#include <string.h>
#include <memory.h>
#include <stdlib.h>
#include <iostream>
#include <fstream>

using namespace std;

typedef unsigned long ULONG;

unsigned char key[256] = {0};
unsigned char s[256] = {0};      //s-box

unsigned long getFileSize (unsigned char * filename) {
    ifstream file ((const char *)filename, ios::in|ios::binary|ios::ate);
    unsigned long size = file.tellg();
    file.close();
    return size;
}

void readFile (unsigned char * filename, char *  buffer, unsigned long size) {
    ifstream file ((const char *)filename, ios::in|ios::binary|ios::ate);
    file.seekg (0, ios::beg);
    file.read (buffer, size);
    file.close();
}

void writeFile(unsigned char * filename, char * buffer, unsigned long size){
    ofstream file ((const char *)filename, ios::out|ios::binary);
    file.write((const char *)buffer, size);
    file.close();
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

void rc4_init(unsigned char *s, unsigned char *key, unsigned long Len) 
{

	int i =0, j = 0;
	unsigned char k[256] = {0};
	unsigned char tmp = 0;
	for(i=0; i<256; i++)
	{
		s[i]=i;
		k[i]=key[i%Len];
	}
	for(i=0; i<256; i++)
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

int main(int argc, char ** argv){
    if (argc < 3){
         printf("Error input!\nUsages:encrypt input output\n");
         exit(1); 
    }

    char *input = argv[1];
    char *output = argv[2];
    char * buffer;
    unsigned long size;
    size = getFileSize((unsigned char *)input);
    buffer = new char [size];
    
    readFile((unsigned char*)input, buffer, size);
    getKey(key, size);
    rc4_init(s, key, sizeof(key));
    rc4_crypt(s, (unsigned char *)buffer, size);

    writeFile((unsigned char *)output, buffer, size);
    
    delete[] buffer;
}
