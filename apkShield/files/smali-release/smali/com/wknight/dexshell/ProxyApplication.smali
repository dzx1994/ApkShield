.class public Lcom/wknight/dexshell/ProxyApplication;
.super Landroid/app/Application;
.source "ProxyApplication.java"


# annotations
.annotation build Landroid/annotation/SuppressLint;
    value = {
        "NewApi"
    }
.end annotation


# static fields
.field private static final COPY_BYTE_LEN:I = 0x4000

.field private static logTag:Ljava/lang/String;

.field private static shieldSoName:Ljava/lang/String;


# instance fields
.field private appClassName:Ljava/lang/String;


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .prologue
    .line 33
    const-string v0, "ProxyApplication"

    sput-object v0, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    .line 44
    const-string v0, "libWKnightShield"

    sput-object v0, Lcom/wknight/dexshell/ProxyApplication;->shieldSoName:Ljava/lang/String;

    .line 45
    return-void
.end method

.method public constructor <init>()V
    .registers 2

    .prologue
    .line 31
    invoke-direct {p0}, Landroid/app/Application;-><init>()V

    .line 32
    const-string v0, "<APK_APPLICATION_NAME>"

    iput-object v0, p0, Lcom/wknight/dexshell/ProxyApplication;->appClassName:Ljava/lang/String;

    return-void
.end method

.method private copyFromAssetsToPayload(Ljava/lang/String;Ljava/lang/String;)Z
    .registers 19
    .param p1, "srcFileName"    # Ljava/lang/String;
    .param p2, "dstFullPath"    # Ljava/lang/String;

    .prologue
    .line 92
    const/4 v10, 0x0

    .line 93
    .local v10, "is":Ljava/io/InputStream;
    const/4 v8, 0x0

    .line 94
    .local v8, "fos":Ljava/io/FileOutputStream;
    const/4 v6, 0x0

    .line 95
    .local v6, "error":Z
    const/4 v12, 0x1

    .line 96
    .local v12, "isMkdir":Z
    new-instance v3, Ljava/io/File;

    move-object/from16 v0, p2

    invoke-direct {v3, v0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 98
    .local v3, "desFile":Ljava/io/File;
    :try_start_b
    invoke-virtual {v3}, Ljava/io/File;->exists()Z

    move-result v13

    if-nez v13, :cond_ec

    .line 100
    new-instance v4, Ljava/io/File;

    const/4 v13, 0x0

    const-string v14, "/"

    move-object/from16 v0, p2

    invoke-virtual {v0, v14}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v14

    move-object/from16 v0, p2

    invoke-virtual {v0, v13, v14}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v13

    invoke-direct {v4, v13}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 101
    .local v4, "dir":Ljava/io/File;
    invoke-virtual {v4}, Ljava/io/File;->exists()Z

    move-result v13

    if-nez v13, :cond_2f

    .line 102
    invoke-virtual {v4}, Ljava/io/File;->mkdir()Z
    :try_end_2e
    .catch Ljava/io/IOException; {:try_start_b .. :try_end_2e} :catch_1d4
    .catchall {:try_start_b .. :try_end_2e} :catchall_1d2

    move-result v12

    .line 104
    :cond_2f
    if-nez v12, :cond_6b

    .line 105
    const/4 v13, 0x0

    .line 140
    if-eqz v10, :cond_37

    .line 141
    :try_start_34
    invoke-virtual {v10}, Ljava/io/InputStream;->close()V
    :try_end_37
    .catch Ljava/io/IOException; {:try_start_34 .. :try_end_37} :catch_59

    .line 147
    :cond_37
    :goto_37
    if-eqz v8, :cond_3c

    .line 148
    :try_start_39
    invoke-virtual {v8}, Ljava/io/FileOutputStream;->close()V
    :try_end_3c
    .catch Ljava/io/IOException; {:try_start_39 .. :try_end_3c} :catch_62

    .line 153
    :cond_3c
    :goto_3c
    if-eqz v6, :cond_58

    .line 154
    new-instance v7, Ljava/io/File;

    move-object/from16 v0, p2

    invoke-direct {v7, v0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 155
    .local v7, "file":Ljava/io/File;
    invoke-virtual {v7}, Ljava/io/File;->exists()Z

    move-result v14

    if-eqz v14, :cond_58

    .line 156
    invoke-virtual {v7}, Ljava/io/File;->delete()Z

    move-result v11

    .line 157
    .local v11, "isDelete":Z
    if-nez v11, :cond_58

    .line 158
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload File Delete Failed"

    invoke-static {v14, v15}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 137
    .end local v4    # "dir":Ljava/io/File;
    .end local v7    # "file":Ljava/io/File;
    .end local v11    # "isDelete":Z
    :cond_58
    :goto_58
    return v13

    .line 143
    .restart local v4    # "dir":Ljava/io/File;
    :catch_59
    move-exception v5

    .line 144
    .local v5, "e":Ljava/io/IOException;
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload FOS IOException"

    invoke-static {v14, v15, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_37

    .line 150
    .end local v5    # "e":Ljava/io/IOException;
    :catch_62
    move-exception v5

    .line 151
    .restart local v5    # "e":Ljava/io/IOException;
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload IOException"

    invoke-static {v14, v15, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_3c

    .line 108
    .end local v5    # "e":Ljava/io/IOException;
    :cond_6b
    :try_start_6b
    invoke-virtual/range {p0 .. p0}, Lcom/wknight/dexshell/ProxyApplication;->getResources()Landroid/content/res/Resources;

    move-result-object v13

    invoke-virtual {v13}, Landroid/content/res/Resources;->getAssets()Landroid/content/res/AssetManager;

    move-result-object v13

    move-object/from16 v0, p1

    invoke-virtual {v13, v0}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;

    move-result-object v10

    .line 109
    new-instance v9, Ljava/io/FileOutputStream;

    move-object/from16 v0, p2

    invoke-direct {v9, v0}, Ljava/io/FileOutputStream;-><init>(Ljava/lang/String;)V
    :try_end_80
    .catch Ljava/io/IOException; {:try_start_6b .. :try_end_80} :catch_1d4
    .catchall {:try_start_6b .. :try_end_80} :catchall_1d2

    .line 110
    .end local v8    # "fos":Ljava/io/FileOutputStream;
    .local v9, "fos":Ljava/io/FileOutputStream;
    const/16 v13, 0x4000

    :try_start_82
    new-array v1, v13, [B

    .line 111
    .local v1, "buffer":[B
    const/4 v2, 0x0

    .line 112
    .local v2, "count":I
    :goto_85
    invoke-virtual {v10, v1}, Ljava/io/InputStream;->read([B)I

    move-result v2

    if-lez v2, :cond_c2

    .line 113
    const/4 v13, 0x0

    invoke-virtual {v9, v1, v13, v2}, Ljava/io/FileOutputStream;->write([BII)V
    :try_end_8f
    .catch Ljava/io/IOException; {:try_start_82 .. :try_end_8f} :catch_90
    .catchall {:try_start_82 .. :try_end_8f} :catchall_16f

    goto :goto_85

    .line 134
    .end local v1    # "buffer":[B
    .end local v2    # "count":I
    :catch_90
    move-exception v5

    move-object v8, v9

    .line 135
    .end local v4    # "dir":Ljava/io/File;
    .end local v9    # "fos":Ljava/io/FileOutputStream;
    .restart local v5    # "e":Ljava/io/IOException;
    .restart local v8    # "fos":Ljava/io/FileOutputStream;
    :goto_92
    const/4 v6, 0x1

    .line 136
    :try_start_93
    sget-object v13, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v14, "copyFromAssetsToPayload IS IOException"

    invoke-static {v13, v14, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_9a
    .catchall {:try_start_93 .. :try_end_9a} :catchall_1d2

    .line 137
    const/4 v13, 0x0

    .line 140
    if-eqz v10, :cond_a0

    .line 141
    :try_start_9d
    invoke-virtual {v10}, Ljava/io/InputStream;->close()V
    :try_end_a0
    .catch Ljava/io/IOException; {:try_start_9d .. :try_end_a0} :catch_1ac

    .line 147
    :cond_a0
    :goto_a0
    if-eqz v8, :cond_a5

    .line 148
    :try_start_a2
    invoke-virtual {v8}, Ljava/io/FileOutputStream;->close()V
    :try_end_a5
    .catch Ljava/io/IOException; {:try_start_a2 .. :try_end_a5} :catch_1b6

    .line 153
    :cond_a5
    :goto_a5
    if-eqz v6, :cond_58

    .line 154
    new-instance v7, Ljava/io/File;

    move-object/from16 v0, p2

    invoke-direct {v7, v0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 155
    .restart local v7    # "file":Ljava/io/File;
    invoke-virtual {v7}, Ljava/io/File;->exists()Z

    move-result v14

    if-eqz v14, :cond_58

    .line 156
    invoke-virtual {v7}, Ljava/io/File;->delete()Z

    move-result v11

    .line 157
    .restart local v11    # "isDelete":Z
    if-nez v11, :cond_58

    .line 158
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload File Delete Failed"

    invoke-static {v14, v15}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_58

    .end local v5    # "e":Ljava/io/IOException;
    .end local v7    # "file":Ljava/io/File;
    .end local v8    # "fos":Ljava/io/FileOutputStream;
    .end local v11    # "isDelete":Z
    .restart local v1    # "buffer":[B
    .restart local v2    # "count":I
    .restart local v4    # "dir":Ljava/io/File;
    .restart local v9    # "fos":Ljava/io/FileOutputStream;
    :cond_c2
    move-object v8, v9

    .line 133
    .end local v9    # "fos":Ljava/io/FileOutputStream;
    .restart local v8    # "fos":Ljava/io/FileOutputStream;
    :goto_c3
    const/4 v13, 0x1

    .line 140
    if-eqz v10, :cond_c9

    .line 141
    :try_start_c6
    invoke-virtual {v10}, Ljava/io/InputStream;->close()V
    :try_end_c9
    .catch Ljava/io/IOException; {:try_start_c6 .. :try_end_c9} :catch_198

    .line 147
    :cond_c9
    :goto_c9
    if-eqz v8, :cond_ce

    .line 148
    :try_start_cb
    invoke-virtual {v8}, Ljava/io/FileOutputStream;->close()V
    :try_end_ce
    .catch Ljava/io/IOException; {:try_start_cb .. :try_end_ce} :catch_1a2

    .line 153
    :cond_ce
    :goto_ce
    if-eqz v6, :cond_58

    .line 154
    new-instance v7, Ljava/io/File;

    move-object/from16 v0, p2

    invoke-direct {v7, v0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 155
    .restart local v7    # "file":Ljava/io/File;
    invoke-virtual {v7}, Ljava/io/File;->exists()Z

    move-result v14

    if-eqz v14, :cond_58

    .line 156
    invoke-virtual {v7}, Ljava/io/File;->delete()Z

    move-result v11

    .line 157
    .restart local v11    # "isDelete":Z
    if-nez v11, :cond_58

    .line 158
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload File Delete Failed"

    invoke-static {v14, v15}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    goto/16 :goto_58

    .line 116
    .end local v1    # "buffer":[B
    .end local v2    # "count":I
    .end local v4    # "dir":Ljava/io/File;
    .end local v7    # "file":Ljava/io/File;
    .end local v11    # "isDelete":Z
    :cond_ec
    :try_start_ec
    invoke-virtual {v3}, Ljava/io/File;->delete()Z

    .line 117
    new-instance v4, Ljava/io/File;

    const/4 v13, 0x0

    const-string v14, "/"

    move-object/from16 v0, p2

    invoke-virtual {v0, v14}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v14

    move-object/from16 v0, p2

    invoke-virtual {v0, v13, v14}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v13

    invoke-direct {v4, v13}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 118
    .restart local v4    # "dir":Ljava/io/File;
    invoke-virtual {v4}, Ljava/io/File;->exists()Z

    move-result v13

    if-nez v13, :cond_10d

    .line 119
    invoke-virtual {v4}, Ljava/io/File;->mkdir()Z
    :try_end_10c
    .catch Ljava/io/IOException; {:try_start_ec .. :try_end_10c} :catch_1d4
    .catchall {:try_start_ec .. :try_end_10c} :catchall_1d2

    move-result v12

    .line 121
    :cond_10d
    if-nez v12, :cond_14a

    .line 122
    const/4 v13, 0x0

    .line 140
    if-eqz v10, :cond_115

    .line 141
    :try_start_112
    invoke-virtual {v10}, Ljava/io/InputStream;->close()V
    :try_end_115
    .catch Ljava/io/IOException; {:try_start_112 .. :try_end_115} :catch_138

    .line 147
    :cond_115
    :goto_115
    if-eqz v8, :cond_11a

    .line 148
    :try_start_117
    invoke-virtual {v8}, Ljava/io/FileOutputStream;->close()V
    :try_end_11a
    .catch Ljava/io/IOException; {:try_start_117 .. :try_end_11a} :catch_141

    .line 153
    :cond_11a
    :goto_11a
    if-eqz v6, :cond_58

    .line 154
    new-instance v7, Ljava/io/File;

    move-object/from16 v0, p2

    invoke-direct {v7, v0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 155
    .restart local v7    # "file":Ljava/io/File;
    invoke-virtual {v7}, Ljava/io/File;->exists()Z

    move-result v14

    if-eqz v14, :cond_58

    .line 156
    invoke-virtual {v7}, Ljava/io/File;->delete()Z

    move-result v11

    .line 157
    .restart local v11    # "isDelete":Z
    if-nez v11, :cond_58

    .line 158
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload File Delete Failed"

    invoke-static {v14, v15}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    goto/16 :goto_58

    .line 143
    .end local v7    # "file":Ljava/io/File;
    .end local v11    # "isDelete":Z
    :catch_138
    move-exception v5

    .line 144
    .restart local v5    # "e":Ljava/io/IOException;
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload FOS IOException"

    invoke-static {v14, v15, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_115

    .line 150
    .end local v5    # "e":Ljava/io/IOException;
    :catch_141
    move-exception v5

    .line 151
    .restart local v5    # "e":Ljava/io/IOException;
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload IOException"

    invoke-static {v14, v15, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_11a

    .line 125
    .end local v5    # "e":Ljava/io/IOException;
    :cond_14a
    :try_start_14a
    invoke-virtual/range {p0 .. p0}, Lcom/wknight/dexshell/ProxyApplication;->getResources()Landroid/content/res/Resources;

    move-result-object v13

    invoke-virtual {v13}, Landroid/content/res/Resources;->getAssets()Landroid/content/res/AssetManager;

    move-result-object v13

    move-object/from16 v0, p1

    invoke-virtual {v13, v0}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;

    move-result-object v10

    .line 126
    new-instance v9, Ljava/io/FileOutputStream;

    move-object/from16 v0, p2

    invoke-direct {v9, v0}, Ljava/io/FileOutputStream;-><init>(Ljava/lang/String;)V
    :try_end_15f
    .catch Ljava/io/IOException; {:try_start_14a .. :try_end_15f} :catch_1d4
    .catchall {:try_start_14a .. :try_end_15f} :catchall_1d2

    .line 127
    .end local v8    # "fos":Ljava/io/FileOutputStream;
    .restart local v9    # "fos":Ljava/io/FileOutputStream;
    const/16 v13, 0x4000

    :try_start_161
    new-array v1, v13, [B

    .line 128
    .restart local v1    # "buffer":[B
    const/4 v2, 0x0

    .line 129
    .restart local v2    # "count":I
    :goto_164
    invoke-virtual {v10, v1}, Ljava/io/InputStream;->read([B)I

    move-result v2

    if-lez v2, :cond_1d7

    .line 130
    const/4 v13, 0x0

    invoke-virtual {v9, v1, v13, v2}, Ljava/io/FileOutputStream;->write([BII)V
    :try_end_16e
    .catch Ljava/io/IOException; {:try_start_161 .. :try_end_16e} :catch_90
    .catchall {:try_start_161 .. :try_end_16e} :catchall_16f

    goto :goto_164

    .line 139
    .end local v1    # "buffer":[B
    .end local v2    # "count":I
    :catchall_16f
    move-exception v13

    move-object v8, v9

    .line 140
    .end local v4    # "dir":Ljava/io/File;
    .end local v9    # "fos":Ljava/io/FileOutputStream;
    .restart local v8    # "fos":Ljava/io/FileOutputStream;
    :goto_171
    if-eqz v10, :cond_176

    .line 141
    :try_start_173
    invoke-virtual {v10}, Ljava/io/InputStream;->close()V
    :try_end_176
    .catch Ljava/io/IOException; {:try_start_173 .. :try_end_176} :catch_1c0

    .line 147
    :cond_176
    :goto_176
    if-eqz v8, :cond_17b

    .line 148
    :try_start_178
    invoke-virtual {v8}, Ljava/io/FileOutputStream;->close()V
    :try_end_17b
    .catch Ljava/io/IOException; {:try_start_178 .. :try_end_17b} :catch_1c9

    .line 153
    :cond_17b
    :goto_17b
    if-eqz v6, :cond_197

    .line 154
    new-instance v7, Ljava/io/File;

    move-object/from16 v0, p2

    invoke-direct {v7, v0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 155
    .restart local v7    # "file":Ljava/io/File;
    invoke-virtual {v7}, Ljava/io/File;->exists()Z

    move-result v14

    if-eqz v14, :cond_197

    .line 156
    invoke-virtual {v7}, Ljava/io/File;->delete()Z

    move-result v11

    .line 157
    .restart local v11    # "isDelete":Z
    if-nez v11, :cond_197

    .line 158
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload File Delete Failed"

    invoke-static {v14, v15}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 161
    .end local v7    # "file":Ljava/io/File;
    .end local v11    # "isDelete":Z
    :cond_197
    throw v13

    .line 143
    .restart local v1    # "buffer":[B
    .restart local v2    # "count":I
    .restart local v4    # "dir":Ljava/io/File;
    :catch_198
    move-exception v5

    .line 144
    .restart local v5    # "e":Ljava/io/IOException;
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload FOS IOException"

    invoke-static {v14, v15, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto/16 :goto_c9

    .line 150
    .end local v5    # "e":Ljava/io/IOException;
    :catch_1a2
    move-exception v5

    .line 151
    .restart local v5    # "e":Ljava/io/IOException;
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload IOException"

    invoke-static {v14, v15, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto/16 :goto_ce

    .line 143
    .end local v1    # "buffer":[B
    .end local v2    # "count":I
    .end local v4    # "dir":Ljava/io/File;
    :catch_1ac
    move-exception v5

    .line 144
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload FOS IOException"

    invoke-static {v14, v15, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto/16 :goto_a0

    .line 150
    :catch_1b6
    move-exception v5

    .line 151
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload IOException"

    invoke-static {v14, v15, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto/16 :goto_a5

    .line 143
    .end local v5    # "e":Ljava/io/IOException;
    :catch_1c0
    move-exception v5

    .line 144
    .restart local v5    # "e":Ljava/io/IOException;
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload FOS IOException"

    invoke-static {v14, v15, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_176

    .line 150
    .end local v5    # "e":Ljava/io/IOException;
    :catch_1c9
    move-exception v5

    .line 151
    .restart local v5    # "e":Ljava/io/IOException;
    sget-object v14, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v15, "copyFromAssetsToPayload IOException"

    invoke-static {v14, v15, v5}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_17b

    .line 139
    .end local v5    # "e":Ljava/io/IOException;
    :catchall_1d2
    move-exception v13

    goto :goto_171

    .line 134
    :catch_1d4
    move-exception v5

    goto/16 :goto_92

    .end local v8    # "fos":Ljava/io/FileOutputStream;
    .restart local v1    # "buffer":[B
    .restart local v2    # "count":I
    .restart local v4    # "dir":Ljava/io/File;
    .restart local v9    # "fos":Ljava/io/FileOutputStream;
    :cond_1d7
    move-object v8, v9

    .end local v9    # "fos":Ljava/io/FileOutputStream;
    .restart local v8    # "fos":Ljava/io/FileOutputStream;
    goto/16 :goto_c3
.end method

.method private getPhoneAbi()[Ljava/lang/String;
    .registers 6

    .prologue
    const/4 v4, 0x1

    const/4 v3, 0x0

    .line 184
    const/4 v0, 0x0

    .line 185
    .local v0, "result":[Ljava/lang/String;
    sget v1, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v2, 0x15

    if-lt v1, v2, :cond_c

    .line 186
    sget-object v0, Landroid/os/Build;->SUPPORTED_32_BIT_ABIS:[Ljava/lang/String;

    .line 209
    :cond_b
    :goto_b
    return-object v0

    .line 189
    :cond_c
    sget-object v1, Landroid/os/Build;->CPU_ABI:Ljava/lang/String;

    const-string v2, "armeabi-v7a"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_2c

    sget-object v1, Landroid/os/Build;->CPU_ABI2:Ljava/lang/String;

    const-string v2, "armeabi"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_2c

    .line 191
    const/4 v1, 0x2

    new-array v0, v1, [Ljava/lang/String;

    .end local v0    # "result":[Ljava/lang/String;
    const-string v1, "armeabi-v7a"

    aput-object v1, v0, v3

    const-string v1, "armeabi"

    aput-object v1, v0, v4

    .restart local v0    # "result":[Ljava/lang/String;
    goto :goto_b

    .line 192
    :cond_2c
    sget-object v1, Landroid/os/Build;->CPU_ABI:Ljava/lang/String;

    const-string v2, "armeabi-v7a"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_47

    sget-object v1, Landroid/os/Build;->CPU_ABI2:Ljava/lang/String;

    const-string v2, "armeabi"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_47

    .line 194
    new-array v0, v4, [Ljava/lang/String;

    .end local v0    # "result":[Ljava/lang/String;
    const-string v1, "armeabi-v7a"

    aput-object v1, v0, v3

    .restart local v0    # "result":[Ljava/lang/String;
    goto :goto_b

    .line 195
    :cond_47
    sget-object v1, Landroid/os/Build;->CPU_ABI:Ljava/lang/String;

    const-string v2, "armeabi"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_58

    .line 197
    new-array v0, v4, [Ljava/lang/String;

    .end local v0    # "result":[Ljava/lang/String;
    const-string v1, "armeabi"

    aput-object v1, v0, v3

    .restart local v0    # "result":[Ljava/lang/String;
    goto :goto_b

    .line 198
    :cond_58
    sget-object v1, Landroid/os/Build;->CPU_ABI:Ljava/lang/String;

    const-string v2, "x86"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_69

    .line 200
    new-array v0, v4, [Ljava/lang/String;

    .end local v0    # "result":[Ljava/lang/String;
    const-string v1, "x86"

    aput-object v1, v0, v3

    .restart local v0    # "result":[Ljava/lang/String;
    goto :goto_b

    .line 201
    :cond_69
    sget-object v1, Landroid/os/Build;->CPU_ABI:Ljava/lang/String;

    const-string v2, "arm64"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_7a

    .line 203
    new-array v0, v4, [Ljava/lang/String;

    .end local v0    # "result":[Ljava/lang/String;
    const-string v1, "armeabi-v7a"

    aput-object v1, v0, v3

    .restart local v0    # "result":[Ljava/lang/String;
    goto :goto_b

    .line 204
    :cond_7a
    sget-object v1, Landroid/os/Build;->CPU_ABI:Ljava/lang/String;

    const-string v2, "x86_64"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_b

    .line 206
    new-array v0, v4, [Ljava/lang/String;

    .end local v0    # "result":[Ljava/lang/String;
    const-string v1, "x86"

    aput-object v1, v0, v3

    .restart local v0    # "result":[Ljava/lang/String;
    goto :goto_b
.end method

.method private getPhoneAbi2()Ljava/util/List;
    .registers 11
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()",
            "Ljava/util/List",
            "<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation

    .prologue
    .line 213
    invoke-static {}, Ljava/lang/Runtime;->getRuntime()Ljava/lang/Runtime;

    move-result-object v7

    .line 214
    .local v7, "run":Ljava/lang/Runtime;
    const-string v0, "getprop"

    .line 216
    .local v0, "cmd":Ljava/lang/String;
    :try_start_6
    invoke-virtual {v7, v0}, Ljava/lang/Runtime;->exec(Ljava/lang/String;)Ljava/lang/Process;

    move-result-object v6

    .line 219
    .local v6, "p":Ljava/lang/Process;
    invoke-virtual {v6}, Ljava/lang/Process;->waitFor()I

    move-result v8

    if-eqz v8, :cond_1e

    .line 221
    invoke-virtual {v6}, Ljava/lang/Process;->exitValue()I

    move-result v8

    const/4 v9, 0x1

    if-ne v8, v9, :cond_1e

    .line 222
    const-string v8, "WKnightShield"

    const-string v9, "\u547d\u4ee4\u6267\u884c\u5931\u8d25!"

    invoke-static {v8, v9}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 224
    :cond_1e
    new-instance v2, Ljava/io/BufferedInputStream;

    invoke-virtual {v6}, Ljava/lang/Process;->getInputStream()Ljava/io/InputStream;

    move-result-object v8

    invoke-direct {v2, v8}, Ljava/io/BufferedInputStream;-><init>(Ljava/io/InputStream;)V

    .line 225
    .local v2, "in":Ljava/io/BufferedInputStream;
    new-instance v3, Ljava/io/BufferedReader;

    new-instance v8, Ljava/io/InputStreamReader;

    invoke-direct {v8, v2}, Ljava/io/InputStreamReader;-><init>(Ljava/io/InputStream;)V

    invoke-direct {v3, v8}, Ljava/io/BufferedReader;-><init>(Ljava/io/Reader;)V

    .line 227
    .local v3, "inBr":Ljava/io/BufferedReader;
    new-instance v5, Ljava/util/ArrayList;

    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V

    .line 228
    .local v5, "lines":Ljava/util/List;, "Ljava/util/List<Ljava/lang/String;>;"
    :cond_36
    :goto_36
    invoke-virtual {v3}, Ljava/io/BufferedReader;->readLine()Ljava/lang/String;

    move-result-object v4

    .local v4, "line":Ljava/lang/String;
    if-eqz v4, :cond_51

    .line 229
    const-string v8, "ro.product.cpu"

    invoke-virtual {v4, v8}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v8

    if-eqz v8, :cond_36

    .line 231
    invoke-interface {v5, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    :try_end_47
    .catch Ljava/lang/Exception; {:try_start_6 .. :try_end_47} :catch_48

    goto :goto_36

    .line 236
    .end local v2    # "in":Ljava/io/BufferedInputStream;
    .end local v3    # "inBr":Ljava/io/BufferedReader;
    .end local v4    # "line":Ljava/lang/String;
    .end local v5    # "lines":Ljava/util/List;, "Ljava/util/List<Ljava/lang/String;>;"
    .end local v6    # "p":Ljava/lang/Process;
    :catch_48
    move-exception v1

    .line 237
    .local v1, "e":Ljava/lang/Exception;
    const-string v8, "WKnightShield"

    const-string v9, "can not find cpu abi"

    invoke-static {v8, v9}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 238
    const/4 v5, 0x0

    .end local v1    # "e":Ljava/lang/Exception;
    :cond_51
    return-object v5
.end method

.method private getUsingAbi()Ljava/lang/String;
    .registers 8

    .prologue
    .line 166
    invoke-direct {p0}, Lcom/wknight/dexshell/ProxyApplication;->getPhoneAbi()[Ljava/lang/String;

    move-result-object v1

    .line 167
    .local v1, "phoneAbis":[Ljava/lang/String;
    invoke-direct {p0}, Lcom/wknight/dexshell/ProxyApplication;->getPhoneAbi2()Ljava/util/List;

    move-result-object v2

    .line 168
    .local v2, "phoneAbis2":Ljava/util/List;, "Ljava/util/List<Ljava/lang/String;>;"
    const-string v3, "armeabi"

    .line 169
    .local v3, "result":Ljava/lang/String;
    array-length v5, v1

    const/4 v4, 0x0

    :goto_c
    if-ge v4, v5, :cond_1d

    aget-object v0, v1, v4

    .line 171
    .local v0, "phoneAbi":Ljava/lang/String;
    const-string v6, "x86"

    invoke-virtual {v0, v6}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v6

    if-eqz v6, :cond_1a

    const-string v3, "x86"

    .line 169
    :cond_1a
    add-int/lit8 v4, v4, 0x1

    goto :goto_c

    .line 174
    .end local v0    # "phoneAbi":Ljava/lang/String;
    :cond_1d
    if-eqz v2, :cond_3a

    .line 175
    invoke-interface {v2}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v4

    :cond_23
    :goto_23
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    move-result v5

    if-eqz v5, :cond_3a

    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/lang/String;

    .line 177
    .restart local v0    # "phoneAbi":Ljava/lang/String;
    const-string v5, "x86"

    invoke-virtual {v0, v5}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v5

    if-eqz v5, :cond_23

    const-string v3, "x86"

    goto :goto_23

    .line 180
    .end local v0    # "phoneAbi":Ljava/lang/String;
    :cond_3a
    return-object v3
.end method


# virtual methods
.method public addPathList(Ljava/lang/ClassLoader;Ldalvik/system/DexClassLoader;)V
    .registers 13
    .param p1, "mClassLoader"    # Ljava/lang/ClassLoader;
    .param p2, "mDexClassLoader"    # Ldalvik/system/DexClassLoader;

    .prologue
    .line 246
    const-string v7, "dalvik.system.BaseDexClassLoader"

    const-string v8, "pathList"

    invoke-static {v7, p1, v8}, Lcom/wknight/dexshell/RefInvoke;->getFieldOjbect(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v2

    .line 248
    .local v2, "mCLpathList":Ljava/lang/Object;
    const-string v7, "dalvik.system.BaseDexClassLoader"

    const-string v8, "pathList"

    invoke-static {v7, p2, v8}, Lcom/wknight/dexshell/RefInvoke;->getFieldOjbect(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v4

    .line 251
    .local v4, "mDCLpathList":Ljava/lang/Object;
    const-string v7, "dalvik.system.DexPathList"

    const-string v8, "dexElements"

    invoke-static {v7, v2, v8}, Lcom/wknight/dexshell/RefInvoke;->getFieldOjbect(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v7

    check-cast v7, [Ljava/lang/Object;

    move-object v3, v7

    check-cast v3, [Ljava/lang/Object;

    .line 253
    .local v3, "mCLpathListDE":[Ljava/lang/Object;
    const-string v7, "dalvik.system.DexPathList"

    const-string v8, "dexElements"

    invoke-static {v7, v4, v8}, Lcom/wknight/dexshell/RefInvoke;->getFieldOjbect(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v7

    check-cast v7, [Ljava/lang/Object;

    move-object v5, v7

    check-cast v5, [Ljava/lang/Object;

    .line 256
    .local v5, "mDCLpathListDE":[Ljava/lang/Object;
    const/4 v7, 0x0

    aget-object v7, v3, v7

    invoke-virtual {v7}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v7

    array-length v8, v5

    array-length v9, v3

    add-int/2addr v8, v9

    invoke-static {v7, v8}, Ljava/lang/reflect/Array;->newInstance(Ljava/lang/Class;I)Ljava/lang/Object;

    move-result-object v0

    .line 258
    .local v0, "dexElementsArray":Ljava/lang/Object;
    if-eqz v0, :cond_5e

    .line 259
    const/4 v1, 0x0

    .local v1, "m":I
    const/4 v6, 0x0

    .line 260
    .local v6, "n":I
    :goto_3c
    array-length v7, v5

    if-ge v1, v7, :cond_49

    .line 261
    aget-object v7, v5, v1

    invoke-static {v0, v6, v7}, Ljava/lang/reflect/Array;->set(Ljava/lang/Object;ILjava/lang/Object;)V

    .line 260
    add-int/lit8 v6, v6, 0x1

    add-int/lit8 v1, v1, 0x1

    goto :goto_3c

    .line 263
    :cond_49
    const/4 v1, 0x0

    :goto_4a
    array-length v7, v3

    if-ge v1, v7, :cond_57

    .line 264
    aget-object v7, v3, v1

    invoke-static {v0, v6, v7}, Ljava/lang/reflect/Array;->set(Ljava/lang/Object;ILjava/lang/Object;)V

    .line 263
    add-int/lit8 v6, v6, 0x1

    add-int/lit8 v1, v1, 0x1

    goto :goto_4a

    .line 266
    :cond_57
    const-string v7, "dalvik.system.DexPathList"

    const-string v8, "dexElements"

    invoke-static {v7, v8, v2, v0}, Lcom/wknight/dexshell/RefInvoke;->setFieldOjbect(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V

    .line 269
    .end local v1    # "m":I
    .end local v6    # "n":I
    :cond_5e
    return-void
.end method

.method protected attachBaseContext(Landroid/content/Context;)V
    .registers 12
    .param p1, "base"    # Landroid/content/Context;

    .prologue
    .line 53
    invoke-super {p0, p1}, Landroid/app/Application;->attachBaseContext(Landroid/content/Context;)V

    .line 55
    new-instance v8, Ljava/lang/StringBuilder;

    invoke-direct {v8}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {p0}, Lcom/wknight/dexshell/ProxyApplication;->getFilesDir()Ljava/io/File;

    move-result-object v9

    invoke-virtual {v9}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    const-string v9, "/"

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    sget-object v9, Lcom/wknight/dexshell/ProxyApplication;->shieldSoName:Ljava/lang/String;

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    const-string v9, ".so"

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    invoke-virtual {v8}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    .line 56
    .local v7, "soPath":Ljava/lang/String;
    invoke-direct {p0}, Lcom/wknight/dexshell/ProxyApplication;->getUsingAbi()Ljava/lang/String;

    move-result-object v0

    .line 58
    .local v0, "abiTye":Ljava/lang/String;
    const-string v8, "x86"

    invoke-virtual {v0, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-eqz v8, :cond_80

    .line 60
    new-instance v8, Ljava/lang/StringBuilder;

    invoke-direct {v8}, Ljava/lang/StringBuilder;-><init>()V

    sget-object v9, Lcom/wknight/dexshell/ProxyApplication;->shieldSoName:Ljava/lang/String;

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    const-string v9, "_x86.so"

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    invoke-virtual {v8}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v8

    invoke-direct {p0, v8, v7}, Lcom/wknight/dexshell/ProxyApplication;->copyFromAssetsToPayload(Ljava/lang/String;Ljava/lang/String;)Z

    .line 65
    :goto_4e
    invoke-static {v7}, Ljava/lang/System;->load(Ljava/lang/String;)V

    .line 68
    :try_start_51
    const-string v8, "wknight_odex"

    const/4 v9, 0x0

    invoke-virtual {p0, v8, v9}, Lcom/wknight/dexshell/ProxyApplication;->getDir(Ljava/lang/String;I)Ljava/io/File;

    move-result-object v5

    .line 69
    .local v5, "odex":Ljava/io/File;
    const-string v8, "wknight_dex"

    const/4 v9, 0x0

    invoke-virtual {p0, v8, v9}, Lcom/wknight/dexshell/ProxyApplication;->getDir(Ljava/lang/String;I)Ljava/io/File;

    move-result-object v1

    .line 70
    .local v1, "dex":Ljava/io/File;
    invoke-virtual {v5}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v6

    .line 71
    .local v6, "odexPath":Ljava/lang/String;
    invoke-virtual {v1}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v2

    .line 72
    .local v2, "dexPath":Ljava/lang/String;
    invoke-virtual {p0}, Lcom/wknight/dexshell/ProxyApplication;->getFilesDir()Ljava/io/File;

    move-result-object v8

    invoke-virtual {v8}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v4

    .line 75
    .local v4, "libPath":Ljava/lang/String;
    invoke-virtual {p0, p0}, Lcom/wknight/dexshell/ProxyApplication;->verify(Landroid/content/Context;)I

    move-result v8

    if-eqz v8, :cond_99

    .line 77
    const-string v8, "\u5e94\u7528\u8bc1\u4e66\u88ab\u4fee\u6539"

    const/4 v9, 0x1

    invoke-static {p0, v8, v9}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object v8

    invoke-virtual {v8}, Landroid/widget/Toast;->show()V
    :try_end_7f
    .catch Ljava/lang/IllegalArgumentException; {:try_start_51 .. :try_end_7f} :catch_a1
    .catch Ljava/lang/Exception; {:try_start_51 .. :try_end_7f} :catch_aa

    .line 88
    .end local v1    # "dex":Ljava/io/File;
    .end local v2    # "dexPath":Ljava/lang/String;
    .end local v4    # "libPath":Ljava/lang/String;
    .end local v5    # "odex":Ljava/io/File;
    .end local v6    # "odexPath":Ljava/lang/String;
    :goto_7f
    return-void

    .line 63
    :cond_80
    new-instance v8, Ljava/lang/StringBuilder;

    invoke-direct {v8}, Ljava/lang/StringBuilder;-><init>()V

    sget-object v9, Lcom/wknight/dexshell/ProxyApplication;->shieldSoName:Ljava/lang/String;

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    const-string v9, ".so"

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    invoke-virtual {v8}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v8

    invoke-direct {p0, v8, v7}, Lcom/wknight/dexshell/ProxyApplication;->copyFromAssetsToPayload(Ljava/lang/String;Ljava/lang/String;)Z

    goto :goto_4e

    .line 82
    .restart local v1    # "dex":Ljava/io/File;
    .restart local v2    # "dexPath":Ljava/lang/String;
    .restart local v4    # "libPath":Ljava/lang/String;
    .restart local v5    # "odex":Ljava/io/File;
    .restart local v6    # "odexPath":Ljava/lang/String;
    :cond_99
    :try_start_99
    invoke-virtual {p0}, Lcom/wknight/dexshell/ProxyApplication;->getPackageName()Ljava/lang/String;

    move-result-object v8

    invoke-virtual {p0, v8, v2, v6, v4}, Lcom/wknight/dexshell/ProxyApplication;->loadDexClass(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    :try_end_a0
    .catch Ljava/lang/IllegalArgumentException; {:try_start_99 .. :try_end_a0} :catch_a1
    .catch Ljava/lang/Exception; {:try_start_99 .. :try_end_a0} :catch_aa

    goto :goto_7f

    .line 83
    .end local v1    # "dex":Ljava/io/File;
    .end local v2    # "dexPath":Ljava/lang/String;
    .end local v4    # "libPath":Ljava/lang/String;
    .end local v5    # "odex":Ljava/io/File;
    .end local v6    # "odexPath":Ljava/lang/String;
    :catch_a1
    move-exception v3

    .line 84
    .local v3, "e":Ljava/lang/IllegalArgumentException;
    sget-object v8, Lcom/wknight/dexshell/ProxyApplication;->logTag:Ljava/lang/String;

    const-string v9, "attachBaseContext IllegalArgumentException"

    invoke-static {v8, v9, v3}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_7f

    .line 85
    .end local v3    # "e":Ljava/lang/IllegalArgumentException;
    :catch_aa
    move-exception v3

    .line 86
    .local v3, "e":Ljava/lang/Exception;
    invoke-virtual {v3}, Ljava/lang/Exception;->printStackTrace()V

    goto :goto_7f
.end method

.method public native loadDexClass(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
.end method

.method public native onCreate()V
.end method

.method public native rsaVerify([BI[BI)I
.end method

.method public verify(Landroid/content/Context;)I
    .registers 16
    .param p1, "context"    # Landroid/content/Context;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Exception;
        }
    .end annotation

    .prologue
    .line 272
    invoke-virtual {p0}, Lcom/wknight/dexshell/ProxyApplication;->getResources()Landroid/content/res/Resources;

    move-result-object v11

    invoke-virtual {v11}, Landroid/content/res/Resources;->getAssets()Landroid/content/res/AssetManager;

    move-result-object v11

    const-string v12, "wknight_c.dat"

    invoke-virtual {v11, v12}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;

    move-result-object v7

    .line 273
    .local v7, "is":Ljava/io/InputStream;
    new-instance v0, Ljava/io/ByteArrayOutputStream;

    invoke-direct {v0}, Ljava/io/ByteArrayOutputStream;-><init>()V

    .line 274
    .local v0, "baos":Ljava/io/ByteArrayOutputStream;
    const/16 v11, 0x4000

    new-array v1, v11, [B

    .line 275
    .local v1, "buffer":[B
    const/4 v6, 0x0

    .line 276
    .local v6, "count":I
    :goto_18
    invoke-virtual {v7, v1}, Ljava/io/InputStream;->read([B)I

    move-result v6

    if-lez v6, :cond_23

    .line 277
    const/4 v11, 0x0

    invoke-virtual {v0, v1, v11, v6}, Ljava/io/ByteArrayOutputStream;->write([BII)V

    goto :goto_18

    .line 279
    :cond_23
    invoke-virtual {v0}, Ljava/io/ByteArrayOutputStream;->toByteArray()[B

    move-result-object v2

    .line 281
    .local v2, "cer1":[B
    invoke-virtual {p1}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object v11

    invoke-virtual {p1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v12

    const/16 v13, 0x40

    invoke-virtual {v11, v12, v13}, Landroid/content/pm/PackageManager;->getPackageInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;

    move-result-object v8

    .line 282
    .local v8, "packageInfo":Landroid/content/pm/PackageInfo;
    iget-object v10, v8, Landroid/content/pm/PackageInfo;->signatures:[Landroid/content/pm/Signature;

    .line 283
    .local v10, "signatures":[Landroid/content/pm/Signature;
    const/4 v11, 0x0

    aget-object v9, v10, v11

    .line 284
    .local v9, "signature":Landroid/content/pm/Signature;
    const-string v11, "X.509"

    invoke-static {v11}, Ljava/security/cert/CertificateFactory;->getInstance(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;

    move-result-object v5

    .line 285
    .local v5, "certFactory":Ljava/security/cert/CertificateFactory;
    new-instance v11, Ljava/io/ByteArrayInputStream;

    invoke-virtual {v9}, Landroid/content/pm/Signature;->toByteArray()[B

    move-result-object v12

    invoke-direct {v11, v12}, Ljava/io/ByteArrayInputStream;-><init>([B)V

    invoke-virtual {v5, v11}, Ljava/security/cert/CertificateFactory;->generateCertificate(Ljava/io/InputStream;)Ljava/security/cert/Certificate;

    move-result-object v4

    check-cast v4, Ljava/security/cert/X509Certificate;

    .line 286
    .local v4, "cert":Ljava/security/cert/X509Certificate;
    invoke-virtual {v4}, Ljava/security/cert/X509Certificate;->getEncoded()[B

    move-result-object v3

    .line 288
    .local v3, "cer2":[B
    array-length v11, v2

    array-length v12, v3

    invoke-virtual {p0, v2, v11, v3, v12}, Lcom/wknight/dexshell/ProxyApplication;->rsaVerify([BI[BI)I

    move-result v11

    return v11
.end method
