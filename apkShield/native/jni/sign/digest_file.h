#ifndef _DIGEST_FILE_H_
#define _DIGEST_FILE_H_

#define DIG_VALUE "-Digest: "

#ifdef __cplusplus
extern "C" {
#endif
    int read_line(char *ch,char **buffer);

	char* getfiledigest(char *name,char *alg,int filelen);
	
	struct Lcode
	{
	char *name;
	char *digest;
	char *alg;
	struct Lcode *next;
	};

	struct Lcode* creat_mflink();
	
	struct Lcode* deletecode(struct Lcode* head,struct Lcode* ptr);

    struct Lcode* getmflink(char *manifile);

//    int mfverify(char *manifile);
	
#ifdef __cplusplus
	}
#endif

#endif