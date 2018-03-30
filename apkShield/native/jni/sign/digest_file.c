#include <stdio.h>
#include "base64.h"
#include "sha1.h"
#include "sha256.h"
#include "dalvik/common.h"

#define DIG_VALUE "-Digest: "

int cn=0;
int count = 0;

//获取文件的一行内容
int read_line(char *ch,char **buffer)
{
    char *buf1 = (char*)malloc(1024);
    int i = 0,k = 1; //标记目前读到几个字节。
    while(1)
    {
        *(buf1+i)=*(ch+cn);
        if(*(ch+cn) == '\0'){
            k=0;
            break;
        }
        if(*(ch+cn) == '\r'){
            cn++;
            i++;
            *(buf1+i)=*(ch+cn);
			if(*(ch+cn) == '\n'){
                cn++;
                i++;
                *(buf1+i)='\0';
                break;
            }
		}
        cn ++;//读到的字符增加一个。
        i++;
    }

	*buffer = (char*)malloc(i+1);
	memcpy(*buffer,buf1,i);
	*(*buffer+i)='\0';
    free(buf1);

    return k;
}

char* getfiledigest(char *filecontent,char *alg, int filelen){
    char* b64;
    if(strcmp(alg, "SHA-256") == 0){
        //call sha-256
        uint8_t hash[SHA256_BYTES];

        sha256((const char*)filecontent, filelen, hash);
        b64 = base64_encode((char*)hash, 32);
    } else {
        char hashout[21];

        SHA1(hashout, (const char*)filecontent, filelen);
        b64 = base64_encode(hashout, 20);
    }
    return b64;
}

struct Lcode
{
	char *name;
	char *digest;
	char *alg;
	struct Lcode *next;
};

struct Lcode* creat_mflink(){
	struct Lcode *p1;
	p1=(struct Lcode *)malloc(sizeof(struct Lcode));
	
	if(!p1)
	{
#ifdef DEBUG
        LOGI("链表创建失败");
#endif
        return NULL;
    }
    p1->name=NULL;
    p1->digest=NULL;
    p1->alg=NULL;
    p1->next=NULL; /* 指针域为空 */
	
	return p1;
}

//删除链表
struct Lcode* deletecode(struct Lcode* head,struct Lcode* ptr)
{
   struct Lcode* previous;                 // * 指向前一节点          */

   if ( ptr == head )             // * 是否是串列开始        */
      // * 第一种情况: 删除第一个节点 */
      head = head->next;          // * 传回第二节点指标      */
   else
   {
      previous = head;
      while ( previous->next != ptr ) // * 找节点ptr的前节点 */
         previous = previous->next;

      if ( ptr->next == NULL )    // * 是否是串列结束        */
         // * 第二种情况: 删除最後一个节点 */
         previous->next = NULL;   // * 最後一个节点          */
      else
         // * 第三种情况: 删除中间节点 */
         previous->next = ptr->next; // * 中间节点           */
   }
   free(ptr->name);
   free(ptr->digest);
   free(ptr->alg);
   free(ptr);                     // * 释回节点记忆体        */
   return head;
}

//创建链表，增加节点
struct Lcode* getmflink(char *manifile){

    char *line=NULL,*digestorname=NULL,*digestline=NULL;
    char *name_1=NULL,*name_2=NULL,*digest=NULL,*end=NULL;
    char *new_name1=NULL,*new_name2=NULL,*catname=NULL,*new_digest1=NULL,*new_digest2=NULL,*new_alg1=NULL,*new_alg2=NULL;

    char *ptr= NULL;
    struct Lcode *head=NULL,*tail=NULL;
    struct Lcode *q;
    char start_char[10];

    while(read_line(manifile, &line)==1){
        memset(start_char, '\0', sizeof(start_char));
        memcpy(start_char, line, strlen("Name: "));

        if(strcmp(start_char,"Name: ")!=0){
            free(line);
            continue;
        }
        name_1 = line;

        q=creat_mflink();
        if(!q){
#ifdef DEBUG
            LOGI("创建链表失败!!");
#endif
            return NULL;
        } else{
            count++;
        }
        q->next=NULL;
        if(head==NULL){
            head=q;
        }
        if(tail){
            tail->next=q;
        }
        tail=q;

        name_1 = name_1+strlen("Name: ");
        int len = strlen(name_1)-2;

        new_name1=(char*)malloc(len+1);
        if (!new_name1){
#ifdef DEBUG
            LOGI("申请内存new_name1失败");
#endif
            return NULL;
        }
        memcpy(new_name1,name_1,len);
        *(new_name1+len)='\0';
        free(line);
        q->name = new_name1;

        read_line(manifile,&digestorname);
        if((ptr=strstr(digestorname,DIG_VALUE))==NULL){
            name_2 = digestorname+1;
            int len = strlen(name_2)-2;

            new_name2=(char*)malloc(len+1);
            if (!new_name2){
#ifdef DEBUG
                LOGI("申请内存new_name2失败");
#endif
                return NULL;
            }

            memcpy(new_name2,name_2,len);
            *(new_name2+len)='\0';

            catname = (char*)malloc(strlen(new_name1)+strlen(new_name2)+1);
            strcpy(catname,new_name1);
            strcat(catname, new_name2);
            *(catname+strlen(new_name1)+strlen(new_name2))='\0';
            free(new_name1);
            free(new_name2);
            q->name = catname;

            read_line(manifile,&digestline);
            if((ptr=strstr(digestline,DIG_VALUE))==NULL){
#ifdef DEBUG
                LOGI("error:this is not Digest:");
#endif
                return NULL;
            }else{
                digest = ptr+strlen(DIG_VALUE);
                int len = strlen(digest)-2;

                new_digest1=(char*)malloc(len+1);
                if (!new_digest1){
#ifdef DEBUG
                    LOGI("申请内存new_digest失败");
#endif
                    return NULL;
                }

                memcpy(new_digest1,digest,len);
                *(new_digest1+len)='\0';
                q->digest = new_digest1;

                *ptr= '\0';
                int len1 = strlen(digestline);
                new_alg1 = (char*)malloc(len1+1);
                if (!new_alg1){
#ifdef DEBUG
                    LOGI("申请内存new_alg失败");
#endif
                    return NULL;
                }

                memcpy(new_alg1,digestline,len1);
                *(new_alg1+len1)='\0';
                q->alg = new_alg1;
            }
            free(digestline);
        }else{
            digest = ptr+strlen(DIG_VALUE);
            int len = strlen(digest)-2;

            new_digest2=(char*)malloc(len+1);
            if (!new_digest2){
#ifdef DEBUG
                LOGI("申请内存new_digest失败");
#endif
                return NULL;
            }

            memcpy(new_digest2,digest,len);
            *(new_digest2+len)='\0';
            q->digest = new_digest2;

            *ptr= '\0';
            int len1 = strlen(digestorname);
            new_alg2 = (char*)malloc(len1+1);
            if (!new_alg2){
#ifdef DEBUG
                LOGI("申请内存new_alg失败");
#endif
                return NULL;
            }

            memcpy(new_alg2,digestorname,len1);
            *(new_alg2+len1)='\0';
            q->alg = new_alg2;
        }
        free(digestorname);
        read_line(manifile,&end);
        free(end);
    }
#ifdef DEBUG
    LOGI("in create linkList, count:%d", count);
#endif
    return head;
}
