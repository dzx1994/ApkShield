#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <android/log.h>
#include <elf.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <dlfcn.h>

#include "dalvik/common.h"

typedef struct link_map_t {
	uintptr_t l_addr;
	char* l_name;
	uintptr_t l_ld;
	struct link_map_t* l_next;
	struct link_map_t* l_prev;
} link_map_t;

typedef void (*linker_function_t)();

#define SOINFO_NAME_LEN 128
typedef struct soinfo {
	char name[SOINFO_NAME_LEN];
//	const Elf32_Phdr* phdr;
	const unsigned *phdr;
//	const Elf_Phdr* phdr;
	size_t phnum;
//	Elf_Addr entry;
//	Elf_Addr base;

	unsigned entry;
	unsigned base;

	unsigned size;

	uint32_t unused1; // DO NOT USE, maintained for compatibility.

//	Elf_Dyn* dynamic;
	unsigned *dynamic;

	uint32_t unused2; // DO NOT USE, maintained for compatibility
	uint32_t unused3; // DO NOT USE, maintained for compatibility

	struct soinfo* next;

	unsigned flags;

	const char* strtab;
//	Elf_Sym* symtab;
	Elf32_Sym *symtab;
	size_t nbucket;
	size_t nchain;
	unsigned* bucket;
	unsigned* chain;

	//------------------

	// This is only used by 32-bit MIPS, but needs to be here for
	// all 32-bit architectures to preserve binary compatibility.
	unsigned* plt_got;

//	Elf_Rel* plt_rel;
	Elf32_Rel *plt_rel;
	size_t plt_rel_count;

//	Elf_Rel* rel;
	unsigned *rel;
	size_t rel_count;

	linker_function_t* preinit_array;
	size_t preinit_array_count;

	linker_function_t* init_array;
	size_t init_array_count;
	linker_function_t* fini_array;
	size_t fini_array_count;

	linker_function_t init_func;
	linker_function_t fini_func;

	// ARM EABI section used for stack unwinding.
	unsigned* ARM_exidx;
	size_t ARM_exidx_count;

	size_t ref_count;
	link_map_t link_map;

	int constructors_called;

	// When you read a virtual address from the ELF file, add this
	// value to get the corresponding address in the process' address space.
//	Elf_Addr load_bias;
	unsigned load_bias;
} soinfo;

typedef struct MySoinfo{
	void* base;
	Elf32_Ehdr *ehdr;
	Elf32_Phdr *phdr;
	Elf32_Dyn *dynamic;
	Elf32_Sym *symtab;
	const char* strtab;
	Elf32_Rel *plt_rel;
	unsigned plt_rel_count;
}soinfo2;

#ifdef __cplusplus
extern "C" {
#endif
void* getTargetSI_6(const char* soname);
void* getTargetSI_7(const char* soname);
void replaceFunc_6(void *handle, const char *name, void* pNewFun,	void** pOldFun, int sdkVersion);
void replaceFunc_7(void *handle, const char *name, void* pNewFun,	void** pOldFun, int sdkVersion);
void* getSoBase(const char* soname);
void* get_library_address(const char*  libname);
void* get_dex_in_mem(const char*  libname, uint32_t *len,const char*  packageName);
void* get_dex_in_mem_4(const char*  libname, uint32_t *len);
#ifdef __cplusplus
}
#endif