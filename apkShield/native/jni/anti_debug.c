#include "anti_debug.h"
//using namespace std;
/*
#ifdef __arm__
char dynamic_ccode[] = {
0x1f,0xb4, //push {r0-r4}
0x01,0xde, //breakpoint
0x1f,0xbc, //pop {r0-r4}
0xf7,0x46
};//mov pc,lr

char *g_addr = 0;

void my_sigtrap(int sig){
char change_bkp[] = {0x00,0x46}; //mov r0,r0
memcpy(g_addr+2,change_bkp,2);
__clear_cache((void*)g_addr,(void*)(g_addr+8)); // need to clear cache
}
#endif

void anti_sigtrap(){//SIGTRAP
#ifdef __arm__
int ret,size;
char *addr,*tmpaddr;

signal(SIGTRAP,my_sigtrap);

addr = (char*)malloc(PAGESIZE*2);

memset(addr,0,PAGESIZE*2);
g_addr = (char *)(((int) addr + PAGESIZE-1) & ~(PAGESIZE-1));

LOGI("addr: %p ,g_addr : %p",addr,g_addr);

ret = mprotect(g_addr,PAGESIZE,PROT_READ|PROT_WRITE|PROT_EXEC);
if(ret!=0)
{
LOGI("mprotect error");
return ;
}

size = 8;
memcpy(g_addr,dynamic_ccode,size);

__clear_cache((void*)g_addr,(void*)(g_addr+size)); // need to clear cache

__asm__("push {r0-r4,lr}\n\t"
"mov r0,pc\n"  //此时pc指向后两条指令
"add r0,r0,#4\n"//+4 是的lr 地址为 pop{r0-r5}
"mov lr,r0\n"
"mov pc,%0\n"
"pop {r0-r5}\n"
"mov lr,r5\n" //恢复lr
:
:"r"(g_addr)
:);

LOGI("hi, i'm here");
free(addr);
#endif
}
 */
void printChar(const char *msg, char * src, int len);

void callback(){
#ifdef DEBUG
    LOGI("in callback");
#endif
    int pid = getpid();
    int ret = kill(pid, SIGKILL);
}

void anti_watcher(int sdkVersion) {
	pipe_t *fpipes = (pipe_t *)malloc(sizeof(pipe_t));
	pipe(fpipes->fdpipes2r);
	pipe(fpipes->fdpiper2s);

	pipe_t *fds = fpipes;
	Signal *sign = (Signal *)malloc(sizeof(Signal));
	sign->basetimeout = 2;
	sign->pid = getpid();
	sign->fds = fds;
	sign->callback = callback;

	pid_t child = fork();

	if (child == -1) {
		LOGE("fork child error: [error: %d, desc: %s.", errno, strerror(errno));
	} else if (child == 0) {
		ptrace(PTRACE_TRACEME, 0, 0, 0);

		// add watches
		FileWatcher *fileWatcher = init_watcher(getppid());
		char *pathes[LEN];
		pathes[0] = "maps";
		pathes[1] = "status";
		pathes[2] = NULL;
		fileWatcher->pathes = pathes;
		fileWatcher->sinfo = sign;
		fileWatcher->sdkVersion = sdkVersion;

		while (!start(fileWatcher)) {
			int pid = fileWatcher->pid;

#ifndef DEBUG
			if (pid == getppid()) {
				LOGW("%d being traced, terminating application...", pid);
				kill(pid, SIGKILL);
			}
#else
			if (pid == getppid())
				LOGI("%d being traced, terminating application...", pid);
#endif
			remove_watchers(fileWatcher);
			_exit(-1);
		}

	} else {
		pthread_t checkthread;
		if (pthread_create(&checkthread, NULL, (void*(*)(void*)) receive_notification, sign) < 0) {
			LOGE("error on creating thread [error:%d, desc:%s].", errno, strerror(errno));
		}
	}
}

int getDynSymSize(){
	int linker_fd = open("/system/bin/linker", O_RDONLY);
	int length = lseek(linker_fd,0,SEEK_END);
	int size = -1;

	lseek(linker_fd,0,SEEK_SET);
	void* handle = mmap(NULL, length, PROT_READ, MAP_PRIVATE, linker_fd,0);

	Elf32_Ehdr *ehdr = (Elf32_Ehdr *) handle;
	Elf32_Shdr *shdr = (Elf32_Shdr *)(ehdr->e_shoff + (int)handle);
#ifdef DEBUG
	printChar("ehdr", (char*)ehdr, 32);
	LOGI("e_shoff:%08x", ehdr->e_shoff);
	printChar("shdr", (char*)shdr, 64);
#endif
	for (int i = 0; i < ehdr->e_shnum; i++) {
		if (shdr[i].sh_type == SHT_DYNSYM) {
#ifdef DEBUG
			LOGI("sh_offset:%08x, sh_size:%d", shdr[i].sh_offset, shdr[i].sh_size);
#endif
			size = shdr[i].sh_size;
			break;
		}
	}

	munmap(handle, length);
	close(linker_fd);

	return size;
}

void* get_Address(const char*  libname)
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

int anti_break(const char* soname) {
	void *handle = get_Address(soname);

	Elf32_Ehdr *ehdr = (Elf32_Ehdr *) handle;
	void *base = handle;

	Elf32_Phdr *phdr = (Elf32_Phdr *) (ehdr + 1);
	for (int i = 0; i < ehdr->e_phnum; i++) {
		if (phdr[i].p_type == PT_LOAD) {
			base = (void *) ((char *) base - phdr[i].p_vaddr);
#ifdef DEBUG
			LOGI("base: %08x", base);
#endif
			break;
		}
	}

	int sym_size = getDynSymSize();

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
	unsigned entry = 0;

	for(d = (unsigned*)dynamic; *d; d++){
		if(*d == DT_SYMTAB){
			d++;
			symtab = (Elf32_Sym*)(*d + (char*)base);
		}else if(*d == DT_STRTAB){
			d++;
			strtab = (char*)(*d + (char*)base);
		}else if(*d == DT_SYMENT){
			d++;
			entry = *d;
		}
		else d++;
	}
	void* addr;
	for(int i=0; i<sym_size/entry; i++){
		char *sym_name = (char *) (strtab + symtab[i].st_name);
		if(strcmp(sym_name, "rtld_db_dlactivity") == 0){
			addr = (void*)(symtab[i].st_value + (int)handle);
			break;
		}
	}
#ifdef DEBUG
	printChar("rtld_db_dlactivity", (char*)addr-1, 8);
#endif
	char a = *(((char*)addr) - 1);
	char b = *(char*)addr;

	if(a == 0x10 && b == 0xde){
#ifdef DEBUG
		LOGI("a:%d b%d", (int)a, (int)b);
#endif
		callback();
		return -1;
	}
	return 0;
}

int anti_port(){
	char buff[256];
	FILE* file;
	file = fopen("/proc/net/tcp", "rt");
	if (file == NULL)
		return -1;

	while (fgets(buff, sizeof buff, file) != NULL) {
		int  len = strlen(buff);
		if (len > 0 && buff[len-1] == '\n') {
			buff[--len] = '\0';
		}

		char s_local_addr[20], s_remote_addr[20], s_state[20];
		char flags[4];
		int n = sscanf(buff, "%*s%s%s%s", s_local_addr, s_remote_addr, s_state);
#ifdef DEBUG
		LOGI("line:%s", buff);
		LOGI("s_local_addr:%s", s_local_addr);
		LOGI("s_remote_addr:%s", s_remote_addr);
		LOGI("s_state:%s", s_state);
#endif
		char* c = strstr((char*)s_local_addr, "5D8A");

		if(c != NULL){
#ifdef DEBUG
			LOGI("tcp port:%s your app is being debug", c);
#endif
			callback();
		}

	}

	fclose(file);

	return 0;
}


