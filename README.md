#Apk加固项目（支持4.0-8.1，8.0+部分机型存在crash，持续跟进中）


----------
##使用方法

> 1.  clone 项目至本地

> 2.  在 ApkShield\apkShield\native 进行 ndk-build ,修改 jni/Android.mk 中的 LOCAL_CFLAGS    += -DDEBUG，可控住 debug 版本和 release 版本的生成，同时也支持 ollvm 混淆

> 3.  把生成的 lib 下的文件全部拷贝到 ApkShield\apkShield\files\lib-debug（lib-release）\lib 下，ndk-build 生成的是 debug 版就放在 lib-debug\lib ，若下生成的是 release 版就放在 lib-release\lib 下

> 4.  把项目导入 Idea 或 AndroidStudio  ，然后对 apkShield（不是ApkShield）进行 assemble 项目构建，修改apkShield\build.gradle 文件可以项目名称和构建版本的控制（debug 或 release）

> 5.  在 apkShield\build\distributions 找到构建的 zip 文件，解压。

> 6.  执行 Java -jar  apkShield.jar  youSinged.apk ,加固之后的 apk 会输出到 out 文件夹，然后重新签名你的 apk 。注意：加固时你的 apk 一定要是签过名的

##注意 

    仅用于学习，请勿商用