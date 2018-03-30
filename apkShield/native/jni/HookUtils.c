#include "HookUtils.h"
#include <sys/mman.h>
#include <errno.h>
#include <dirent.h>
#include <sys/stat.h>
void printChar(const char *msg, char * src, int len);

int findDex(void* start, void* end, void **addr){
    LOGI("in findDex start");
    uint32_t *magic;
    magic = (uint32_t*)start;
    int isFound = 0;
    while(magic < end){
        if(*magic == 0x5f736b77){
#ifdef DEBUG
            LOGI("magic:%08x", *magic);
#endif
            isFound = 1;
            break;
        }else {
            magic++;
        }
    }
    int number = 0;
    if (isFound){
        magic++;
        number = *magic;
#ifdef DEBUG
        LOGI("dexCount:%x", number);
#endif
        magic++;
        *addr = (void*)magic;
#ifdef DEBUG
        LOGI("addr:%08x", *addr);
#endif
    } else {
        number = -1;
    }

    return number;
}
/**
void re_mmap(const char*  packageName){
        int fd ;
        struct stat f_stat;
        char temp_filename [4096];
        DIR * dir;
        struct dirent *filename;//readdir 的返回类型
        dir =opendir("/data/app");
            if(dir == NULL)
            {
            return ;
            }
                while((filename = readdir(dir)) != NULL)
                {
                strcpy(temp_filename,filename->d_name);
                if(memcmp(temp_filename,packageName,sizeof packageName)){
                   break ;
                }
                }
                closedir(dir);
         strcat (temp_filename,"/oat/arm/base.vdex");

        fd = open(temp_filename, O_RDWR);
             if(fstat(fd, &f_stat) < 0){
             return ;
        	}
        void * mmapAddr= mmap(NULL,f_stat.st_size, PROT_READ|PROT_WRITE,
                        MAP_SHARED, fd, 0);
}

void* get_dex_in_mem(const char*  libname, uint32_t *length,const char*  packageName)
{
    char path[256];
    char buff[4096];
    char temp[4096];
    int len_libname = strlen(libname);
    FILE* file;
    snprintf(path, sizeof(path), "/proc/%d/maps", getpid());
    LOGI("path is %s",path);
    if(path==NULL){
    LOGE(" error, errorno:%s", strerror(errno));
       return 0;
    }
    file = fopen(path, "rt");
    if (file == NULL){
        LOGE(" error, errorno:%s", strerror(errno));
            return 0;
    }
    #ifdef DEBUG
        while (fgets(temp, sizeof temp, file) != NULL) {
        LOGE(" error, errorno:%s", strerror(errno));
              LOGI(" cat buff content is %s",temp);
        }
    #endif

    void * dexAddr = NULL;
    int temp1=0;
    while (fgets(buff, sizeof buff, file) != NULL) {
              LOGI("buff content is %s",buff);
        int  len = strlen(buff);
            LOGI("buff len is %d ",len);
        if (len > 0 && buff[len-1] == '\n') {
            temp1++;
            LOGI("this is at %d line",temp1);
            buff[--len] = '\0';
        }
          LOGI("now buff len is %d ,libname len is %d ",len,len_libname);
        if (len <= len_libname || memcmp(buff + len - len_libname, libname, len_libname)) {
            LOGI("find libname");
            continue;
        }
        size_t start, end, offset;
        char flags[4];
        if (sscanf(buff, "%zx-%zx %c%c%c%c %zx", &start, &end,
                   &flags[0], &flags[1], &flags[2], &flags[3], &offset) != 7) {
                       LOGI("format erron");
            continue;
        }
#ifdef DEBUG
        LOGI("line:%s", buff);
#endif
        if (flags[0] != 'r') {
            continue;
        }

        *length = findDex((void*)start, (void*)end, &dexAddr);

        if(((*length) != -1) && (dexAddr != NULL)){
            break;
        }
    }
    if(dexAddr==NULL){
    re_mmap(packageName);
    }
        while (fgets(buff, sizeof buff, file) != NULL) {
                  LOGI("buff content is %s",buff);
            int  len = strlen(buff);
                LOGI("buff len is %d ",len);
            if (len > 0 && buff[len-1] == '\n') {
                temp1++;
                LOGI("this is at %d line",temp1);
                buff[--len] = '\0';
            }
              LOGI("now buff len is %d ,libname len is %d ",len,len_libname);
            if (len <= len_libname || memcmp(buff + len - len_libname, libname, len_libname)) {
                LOGI("find libname");
                continue;
            }
            size_t start, end, offset;
            char flags[4];
            if (sscanf(buff, "%zx-%zx %c%c%c%c %zx", &start, &end,
                       &flags[0], &flags[1], &flags[2], &flags[3], &offset) != 7) {
                           LOGI("format erron");
                continue;
            }
    #ifdef DEBUG
            LOGI("line:%s", buff);
    #endif
            if (flags[0] != 'r') {
                continue;
            }

            *length = findDex((void*)start, (void*)end, &dexAddr);

            if(((*length) != -1) && (dexAddr != NULL)){
                break;
            }
        }
#ifdef DEBUG
    printChar("dex", (char*)dexAddr, 64);
#endif
    fclose(file);
    return (void*)dexAddr;
}
***/
void* get_dex_in_mem_4(const char*  libname, uint32_t *length)
{
    char path[256];
    char buff[256];
    int len_libname = strlen(libname);
    FILE* file;
    snprintf(path, sizeof(path), "/proc/%d/smaps", getpid());
    file = fopen(path, "rt");
    if (file == NULL)
        return 0;

    void * dexAddr = NULL;
    while (fgets(buff, sizeof buff, file) != NULL) {
        int  len = strlen(buff);
        if (len > 0 && buff[len-1] == '\n') {
            buff[--len] = '\0';
        }

        if (len <= len_libname || memcmp(buff + len - len_libname, libname, len_libname)) {
            continue;
        }
        size_t start, end, offset;
        char flags[4];
        if (sscanf(buff, "%zx-%zx %c%c%c%c %zx", &start, &end,
                   &flags[0], &flags[1], &flags[2], &flags[3], &offset) != 7) {
            continue;
        }
#ifdef DEBUG
        LOGI("line:%s", buff);
#endif
        if (flags[0] != 'r') {
            continue;
        }

        *length = findDex((void*)start, (void*)end, &dexAddr);

        if(((*length) != -1) && (dexAddr != NULL)){
            break;
        }
    }
#ifdef DEBUG
    printChar("dex", (char*)dexAddr, 64);
#endif
    fclose(file);
    return (void*)dexAddr;
}

void* get_library_address(const char*  libname)
{
    char path[256];
    char buff[256];
    int len_libname = strlen(libname);
    FILE* file;
    size_t  addr = 0;

    snprintf(path, sizeof(path), "/proc/%d/smaps", getpid());
    file = fopen(path, "rt");
    if (file == NULL)
        return 0;

    while (fgets(buff, sizeof buff, file) != NULL) {
        int  len = strlen(buff);
        if (len > 0 && buff[len-1] == '\n') {
            buff[--len] = '\0';
        }

        if (len <= len_libname || memcmp(buff + len - len_libname, libname, len_libname)) {
            continue;
        }
        size_t start, end, offset;
        char flags[4];
        if (sscanf(buff, "%zx-%zx %c%c%c%c %zx", &start, &end,
                   &flags[0], &flags[1], &flags[2], &flags[3], &offset) != 7) {
            continue;
        }
#ifdef DEBUG
        LOGI("line:%s", buff);
#endif
        if (flags[0] != 'r' || flags[2] != 'x') {
            continue;
        }
        addr = start;
        break;
    }

    fclose(file);
    return (void*)addr;
}

void* getTargetSI_6(const char* soname) {
	soinfo *pLibdl = NULL;
	soinfo *psiTmp = NULL;
	soinfo *psiRet = NULL;

	pLibdl = (soinfo *) dlopen("libdl.so", RTLD_NOW);
	if (NULL == pLibdl) {
#ifdef DEBUG
		LOGI("dlopen libdl.so NULL");
#endif
		goto EXIT0;
	}

#ifdef DEBUG
	LOGI("soname:%s nameLen:%d", soname, strlen(soname));
#endif
	for (psiTmp = pLibdl; psiTmp != 0; psiTmp = psiTmp->next) {
		if(psiTmp == pLibdl) continue;

		if (0 == memcmp(soname, psiTmp->name, strlen(soname))) {
			psiRet = psiTmp;
			break;
		}
	}
	EXIT0:

	if (NULL != pLibdl) {
		dlclose(pLibdl);
		pLibdl = NULL;
	}

	if(NULL == psiRet && 0 == strcmp(soname, "libart.so"))
	{
		psiRet = (soinfo *) dlopen("libart.so", RTLD_NOW);
	}
	return (void *)psiRet;
}

void* getTargetSI_7(const char* soname) {
    void *handle = get_library_address(soname);

    Elf32_Ehdr *ehdr = (Elf32_Ehdr *) handle;
    void *base = handle;

    Elf32_Phdr *phdr = (Elf32_Phdr *) (ehdr + 1);
    for (int i = 0; i < ehdr->e_phnum; i++) {
        if (phdr[i].p_type == PT_LOAD) {
            base = (void *) ((char *) base - phdr[i].p_vaddr);
            break;
        }
    }

    void *dynamicAddr;
    for (int i = 0; i < ehdr->e_phnum; i++) {
    	if (phdr[i].p_type == PT_DYNAMIC) {
            dynamicAddr = (char *) base + phdr[i].p_vaddr;
#ifdef DEBUG
            LOGI("dynamicAddr: %08p", dynamicAddr);
#endif
            break;
        }
    }
	unsigned *d;
    Elf32_Dyn *dynamic = (Elf32_Dyn*) dynamicAddr;

    Elf32_Sym *symtab;
    const char *strtab;
    Elf32_Rel *plt_rel;
    size_t plt_rel_count;
    for(d = (unsigned*)dynamic; *d; d++){
    	if(*d == DT_SYMTAB){
    		d++;
    		symtab = (Elf32_Sym*)(*d + (char*)base);
    	}else if(*d == DT_STRTAB){
    		d++;
    		strtab = (char*)(*d + (char*)base);
    	}else if(*d == DT_JMPREL){
    		d++;
    		plt_rel = (Elf32_Rel*)(*d + (char*)base);
    	}else if(*d == DT_PLTRELSZ){
    		d++;
    		plt_rel_count = *d/8;
    	}else d++;
    }

//    soinfo2* si = new soinfo2;
    soinfo2* si = (soinfo2*)malloc(sizeof(soinfo2));
    si->base = base;
    si->ehdr = ehdr;
    si->phdr = phdr;
    si->dynamic = dynamic;
    si->symtab = symtab;
    si->strtab = strtab;
    si->plt_rel = plt_rel;
    si->plt_rel_count = plt_rel_count;

    return (void*)si;
}

void* getSoBase(const char* soname){
    soinfo *si = NULL;
    si = (soinfo *) dlopen(soname, RTLD_NOW);
    return (void*)si->base;
}

void replaceFunc_6(void *handle, const char *name, void* pNewFun,	void** pOldFun, int sdkVersion) {
#ifdef REPLACE
	LOGI("in replaceFunc start, soHandle:%08x funcName:%s", handle, name);
#endif
	if (!handle)
		return;
#ifdef REPLACE
	printChar("art2", (char*)handle, 32);
	LOGI("%s", (char*)handle);
#endif
	soinfo *si = (soinfo*) handle;
	Elf32_Sym *symtab = si->symtab;
	const char *strtab = si->strtab;

	Elf32_Rel *rel = si->plt_rel;
	size_t count = si->plt_rel_count;
#ifdef REPLACE
	LOGI("in replaceFunc start, symtab:%08x\nstrtab:%08x\nplt_rel:%08x\nrel_count:%d", si->symtab, si->strtab, si->plt_rel, si->plt_rel_count);
#endif
	unsigned idx;
	int fit = 0;
	unsigned base = 0;


	if(sdkVersion >= 23) {
		base = (unsigned) si->base;
		unsigned *p1 = (unsigned*) ((unsigned)si->base + 0x34);
		while(1) {
			if(p1[0] == 0x01) {
				break;
			}
			p1 += 0x8;
		}
		unsigned fixbase = p1[2];
		base = base - fixbase;
	} else {
		base = (unsigned)si->base;
	}
#ifdef REPLACE
	LOGI("in replaceFunc start, base:%08x symtab:%08x strtab:%08x rel:%08x rel-count:%d", base, symtab, strtab, rel, count);
#endif
	for (idx = 0; idx < count; idx++) {
		unsigned int type = ELF32_R_TYPE(rel->r_info);
		unsigned int sym = ELF32_R_SYM(rel->r_info);
		unsigned int reloc = (unsigned) (rel->r_offset + base);
		char *sym_name = (char *) (strtab + symtab[sym].st_name);
		if (strcmp(sym_name, name) == 0) {
            LOGI("replace %s\n",name);
			uint32_t page_size = getpagesize();
			uint32_t entry_page_start = reloc & (~(page_size - 1));
			mprotect((uint32_t *) entry_page_start, page_size,
					PROT_READ | PROT_WRITE);

			*pOldFun = (void *) *((unsigned int*) reloc); //pOldFun = &g_OriginalFunc *pOldFun = g_OriginalFunc
			*((unsigned int*) reloc) = (unsigned int) pNewFun;
			fit = 1;
			break;
		}
		rel++;
	}

	if (!fit) {
#ifdef REPLACE
		LOGD("not find\n");
#endif
	}
}

void replaceFunc_7(void *handle, const char *name, void* pNewFun,	void** pOldFun, int sdkVersion) {

	soinfo2* si = (soinfo2*) handle;

	Elf32_Sym *symtab = si->symtab;
    const char *strtab = si->strtab;
    Elf32_Rel *rel = si->plt_rel;
    size_t count = si->plt_rel_count;

	unsigned idx;
    int fit = 0;
	unsigned base = 0;

    base = (unsigned)si->base;
#ifdef REPLACE
	LOGI("in replaceFunc start, base:%08x symtab:%08x strtab:%08x rel:%08x rel-count:%d", base, symtab, strtab, rel, count);
	LOGI("func name:%s", name);
#endif
    for (idx = 0; idx < count; idx++) {
    	unsigned int type = ELF32_R_TYPE(rel->r_info);
    	unsigned int sym = ELF32_R_SYM(rel->r_info);
    	unsigned int reloc = (unsigned) (rel->r_offset + base);
    	char *sym_name = (char *) (strtab + symtab[sym].st_name);
    	if (strcmp(sym_name, name) == 0) {
        LOGI("replace %s\n",name);
    		uint32_t page_size = getpagesize();
    		uint32_t entry_page_start = reloc & (~(page_size - 1));
    		mprotect((uint32_t *) entry_page_start, page_size,
    				PROT_READ | PROT_WRITE);
        //pOldFun = &g_OriginalFunc *pOldFun = g_OriginalFunc
    		*pOldFun = (void *) *((unsigned int*) reloc);
    		*((unsigned int*) reloc) = (unsigned int) pNewFun;
    		fit = 1;
    		break;
    	}
    	rel++;
    }

    if (!fit) {
#ifdef REPLACE
    	LOGD("not find\n");
#endif
    }
}