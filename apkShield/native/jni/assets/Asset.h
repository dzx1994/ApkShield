#ifndef ASSET_H
#define ASSET_H

#include <stdio.h>
#include <sys/types.h>

namespace android {

class Asset {
public:
    virtual ~Asset(void) = default;

    typedef enum AccessMode {
        ACCESS_UNKNOWN = 0,

        ACCESS_RANDOM,

        ACCESS_STREAMING,

        ACCESS_BUFFER,
    } AccessMode;

    virtual ssize_t read(void* buf, size_t count) = 0;

    virtual off64_t seek(off64_t offset, int whence) = 0;

    virtual void close(void) = 0;

    virtual const void* getBuffer(bool wordAligned) = 0;

    virtual off64_t getLength(void) const = 0;

    virtual off64_t getRemainingLength(void) const = 0;

    virtual int openFileDescriptor(off64_t* outStart, off64_t* outLength) const = 0;

protected:
    Asset(void);        // constructor; only invoked indirectly

    /* handle common seek() housekeeping */
    off64_t handleSeek(off64_t offset, int whence, off64_t curPosn, off64_t maxPosn);

private:
    Asset(const Asset& src);
    Asset& operator=(const Asset& src);

    friend class AssetManager;
    
    Asset*		mNext;				// linked list.
    Asset*		mPrev;
};

}; // namespace android

#endif // ASSET_H
