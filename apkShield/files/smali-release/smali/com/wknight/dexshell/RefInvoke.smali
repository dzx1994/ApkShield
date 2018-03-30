.class public Lcom/wknight/dexshell/RefInvoke;
.super Ljava/lang/Object;
.source "RefInvoke.java"


# static fields
.field private static logTag:Ljava/lang/String;


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .prologue
    .line 11
    const-string v0, "RefInvoke"

    sput-object v0, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    return-void
.end method

.method constructor <init>()V
    .registers 1

    .prologue
    .line 13
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 14
    return-void
.end method

.method public static getFieldOjbect(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    .registers 8
    .param p0, "className"    # Ljava/lang/String;
    .param p1, "obj"    # Ljava/lang/Object;
    .param p2, "filedName"    # Ljava/lang/String;

    .prologue
    .line 64
    :try_start_0
    invoke-static {p0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v2

    .line 65
    .local v2, "objClass":Ljava/lang/Class;
    invoke-virtual {v2, p2}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v1

    .line 66
    .local v1, "field":Ljava/lang/reflect/Field;
    const/4 v3, 0x1

    invoke-virtual {v1, v3}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 67
    invoke-virtual {v1, p1}, Ljava/lang/reflect/Field;->get(Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_f
    .catch Ljava/lang/SecurityException; {:try_start_0 .. :try_end_f} :catch_11
    .catch Ljava/lang/NoSuchFieldException; {:try_start_0 .. :try_end_f} :catch_1b
    .catch Ljava/lang/IllegalArgumentException; {:try_start_0 .. :try_end_f} :catch_24
    .catch Ljava/lang/IllegalAccessException; {:try_start_0 .. :try_end_f} :catch_2d
    .catch Ljava/lang/ClassNotFoundException; {:try_start_0 .. :try_end_f} :catch_36

    move-result-object v3

    .line 79
    .end local v1    # "field":Ljava/lang/reflect/Field;
    .end local v2    # "objClass":Ljava/lang/Class;
    :goto_10
    return-object v3

    .line 68
    :catch_11
    move-exception v0

    .line 69
    .local v0, "e":Ljava/lang/SecurityException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "getFieldOjbect SecurityException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 79
    .end local v0    # "e":Ljava/lang/SecurityException;
    :goto_19
    const/4 v3, 0x0

    goto :goto_10

    .line 70
    :catch_1b
    move-exception v0

    .line 71
    .local v0, "e":Ljava/lang/NoSuchFieldException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "getFieldOjbect NoSuchFieldException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_19

    .line 72
    .end local v0    # "e":Ljava/lang/NoSuchFieldException;
    :catch_24
    move-exception v0

    .line 73
    .local v0, "e":Ljava/lang/IllegalArgumentException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "getFieldOjbect IllegalArgumentException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_19

    .line 74
    .end local v0    # "e":Ljava/lang/IllegalArgumentException;
    :catch_2d
    move-exception v0

    .line 75
    .local v0, "e":Ljava/lang/IllegalAccessException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "getFieldOjbect IllegalAccessException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_19

    .line 76
    .end local v0    # "e":Ljava/lang/IllegalAccessException;
    :catch_36
    move-exception v0

    .line 77
    .local v0, "e":Ljava/lang/ClassNotFoundException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "getFieldOjbect ClassNotFoundException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_19
.end method

.method public static getStaticFieldOjbect(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;
    .registers 8
    .param p0, "className"    # Ljava/lang/String;
    .param p1, "filedName"    # Ljava/lang/String;

    .prologue
    const/4 v3, 0x0

    .line 85
    :try_start_1
    invoke-static {p0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v2

    .line 86
    .local v2, "objClass":Ljava/lang/Class;
    invoke-virtual {v2, p1}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v1

    .line 87
    .local v1, "field":Ljava/lang/reflect/Field;
    const/4 v4, 0x1

    invoke-virtual {v1, v4}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 88
    const/4 v4, 0x0

    invoke-virtual {v1, v4}, Ljava/lang/reflect/Field;->get(Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_11
    .catch Ljava/lang/SecurityException; {:try_start_1 .. :try_end_11} :catch_13
    .catch Ljava/lang/NoSuchFieldException; {:try_start_1 .. :try_end_11} :catch_1c
    .catch Ljava/lang/IllegalArgumentException; {:try_start_1 .. :try_end_11} :catch_25
    .catch Ljava/lang/IllegalAccessException; {:try_start_1 .. :try_end_11} :catch_2e
    .catch Ljava/lang/ClassNotFoundException; {:try_start_1 .. :try_end_11} :catch_37

    move-result-object v3

    .line 100
    .end local v1    # "field":Ljava/lang/reflect/Field;
    .end local v2    # "objClass":Ljava/lang/Class;
    :goto_12
    return-object v3

    .line 89
    :catch_13
    move-exception v0

    .line 90
    .local v0, "e":Ljava/lang/SecurityException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "getStaticFieldOjbect SecurityException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_12

    .line 91
    .end local v0    # "e":Ljava/lang/SecurityException;
    :catch_1c
    move-exception v0

    .line 92
    .local v0, "e":Ljava/lang/NoSuchFieldException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "getStaticFieldOjbect NoSuchFieldException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_12

    .line 93
    .end local v0    # "e":Ljava/lang/NoSuchFieldException;
    :catch_25
    move-exception v0

    .line 94
    .local v0, "e":Ljava/lang/IllegalArgumentException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "getStaticFieldOjbect IllegalArgumentException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_12

    .line 95
    .end local v0    # "e":Ljava/lang/IllegalArgumentException;
    :catch_2e
    move-exception v0

    .line 96
    .local v0, "e":Ljava/lang/IllegalAccessException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "getStaticFieldOjbect IllegalAccessException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_12

    .line 97
    .end local v0    # "e":Ljava/lang/IllegalAccessException;
    :catch_37
    move-exception v0

    .line 98
    .local v0, "e":Ljava/lang/ClassNotFoundException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "getStaticFieldOjbect ClassNotFoundException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_12
.end method

.method public static invokeMethod(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10
    .param p0, "className"    # Ljava/lang/String;
    .param p1, "methodName"    # Ljava/lang/String;
    .param p2, "obj"    # Ljava/lang/Object;
    .param p3, "pareTyple"    # [Ljava/lang/Class;
    .param p4, "pareVaules"    # [Ljava/lang/Object;

    .prologue
    .line 43
    :try_start_0
    invoke-static {p0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v2

    .line 44
    .local v2, "objClass":Ljava/lang/Class;
    invoke-virtual {v2, p1, p3}, Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    move-result-object v1

    .line 45
    .local v1, "method":Ljava/lang/reflect/Method;
    invoke-virtual {v1, p2, p4}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_b
    .catch Ljava/lang/SecurityException; {:try_start_0 .. :try_end_b} :catch_d
    .catch Ljava/lang/IllegalArgumentException; {:try_start_0 .. :try_end_b} :catch_17
    .catch Ljava/lang/IllegalAccessException; {:try_start_0 .. :try_end_b} :catch_20
    .catch Ljava/lang/NoSuchMethodException; {:try_start_0 .. :try_end_b} :catch_29
    .catch Ljava/lang/reflect/InvocationTargetException; {:try_start_0 .. :try_end_b} :catch_32
    .catch Ljava/lang/ClassNotFoundException; {:try_start_0 .. :try_end_b} :catch_3b

    move-result-object v3

    .line 59
    .end local v1    # "method":Ljava/lang/reflect/Method;
    .end local v2    # "objClass":Ljava/lang/Class;
    :goto_c
    return-object v3

    .line 46
    :catch_d
    move-exception v0

    .line 47
    .local v0, "e":Ljava/lang/SecurityException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "invokeMethod SecurityException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 59
    .end local v0    # "e":Ljava/lang/SecurityException;
    :goto_15
    const/4 v3, 0x0

    goto :goto_c

    .line 48
    :catch_17
    move-exception v0

    .line 49
    .local v0, "e":Ljava/lang/IllegalArgumentException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "invokeMethod IllegalArgumentException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_15

    .line 50
    .end local v0    # "e":Ljava/lang/IllegalArgumentException;
    :catch_20
    move-exception v0

    .line 51
    .local v0, "e":Ljava/lang/IllegalAccessException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "invokeMethod IllegalAccessException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_15

    .line 52
    .end local v0    # "e":Ljava/lang/IllegalAccessException;
    :catch_29
    move-exception v0

    .line 53
    .local v0, "e":Ljava/lang/NoSuchMethodException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "invokeMethod NoSuchMethodException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_15

    .line 54
    .end local v0    # "e":Ljava/lang/NoSuchMethodException;
    :catch_32
    move-exception v0

    .line 55
    .local v0, "e":Ljava/lang/reflect/InvocationTargetException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "invokeMethod InvocationTargetException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_15

    .line 56
    .end local v0    # "e":Ljava/lang/reflect/InvocationTargetException;
    :catch_3b
    move-exception v0

    .line 57
    .local v0, "e":Ljava/lang/ClassNotFoundException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "invokeMethod ClassNotFoundException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_15
.end method

.method public static invokeStaticMethod(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Object;
    .registers 10
    .param p0, "className"    # Ljava/lang/String;
    .param p1, "methodName"    # Ljava/lang/String;
    .param p2, "pareTyple"    # [Ljava/lang/Class;
    .param p3, "pareVaules"    # [Ljava/lang/Object;

    .prologue
    const/4 v3, 0x0

    .line 20
    :try_start_1
    invoke-static {p0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v2

    .line 21
    .local v2, "objClass":Ljava/lang/Class;
    invoke-virtual {v2, p1, p2}, Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    move-result-object v1

    .line 22
    .local v1, "method":Ljava/lang/reflect/Method;
    const/4 v4, 0x0

    invoke-virtual {v1, v4, p3}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_d
    .catch Ljava/lang/SecurityException; {:try_start_1 .. :try_end_d} :catch_f
    .catch Ljava/lang/IllegalArgumentException; {:try_start_1 .. :try_end_d} :catch_18
    .catch Ljava/lang/IllegalAccessException; {:try_start_1 .. :try_end_d} :catch_21
    .catch Ljava/lang/NoSuchMethodException; {:try_start_1 .. :try_end_d} :catch_2a
    .catch Ljava/lang/reflect/InvocationTargetException; {:try_start_1 .. :try_end_d} :catch_33
    .catch Ljava/lang/ClassNotFoundException; {:try_start_1 .. :try_end_d} :catch_3c

    move-result-object v3

    .line 36
    .end local v1    # "method":Ljava/lang/reflect/Method;
    .end local v2    # "objClass":Ljava/lang/Class;
    :goto_e
    return-object v3

    .line 23
    :catch_f
    move-exception v0

    .line 24
    .local v0, "e":Ljava/lang/SecurityException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "invokeStaticMethod SecurityException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_e

    .line 25
    .end local v0    # "e":Ljava/lang/SecurityException;
    :catch_18
    move-exception v0

    .line 26
    .local v0, "e":Ljava/lang/IllegalArgumentException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "invokeStaticMethod IllegalArgumentException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_e

    .line 27
    .end local v0    # "e":Ljava/lang/IllegalArgumentException;
    :catch_21
    move-exception v0

    .line 28
    .local v0, "e":Ljava/lang/IllegalAccessException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "invokeStaticMethod IllegalAccessException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_e

    .line 29
    .end local v0    # "e":Ljava/lang/IllegalAccessException;
    :catch_2a
    move-exception v0

    .line 30
    .local v0, "e":Ljava/lang/NoSuchMethodException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "invokeStaticMethod NoSuchMethodException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_e

    .line 31
    .end local v0    # "e":Ljava/lang/NoSuchMethodException;
    :catch_33
    move-exception v0

    .line 32
    .local v0, "e":Ljava/lang/reflect/InvocationTargetException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "invokeStaticMethod InvocationTargetException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_e

    .line 33
    .end local v0    # "e":Ljava/lang/reflect/InvocationTargetException;
    :catch_3c
    move-exception v0

    .line 34
    .local v0, "e":Ljava/lang/ClassNotFoundException;
    sget-object v4, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v5, "invokeStaticMethod ClassNotFoundException"

    invoke-static {v4, v5, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_e
.end method

.method public static setFieldOjbect(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V
    .registers 9
    .param p0, "className"    # Ljava/lang/String;
    .param p1, "filedName"    # Ljava/lang/String;
    .param p2, "obj"    # Ljava/lang/Object;
    .param p3, "filedVaule"    # Ljava/lang/Object;

    .prologue
    .line 106
    :try_start_0
    invoke-static {p0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v2

    .line 107
    .local v2, "objClass":Ljava/lang/Class;
    invoke-virtual {v2, p1}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v1

    .line 108
    .local v1, "field":Ljava/lang/reflect/Field;
    const/4 v3, 0x1

    invoke-virtual {v1, v3}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 109
    invoke-virtual {v1, p2, p3}, Ljava/lang/reflect/Field;->set(Ljava/lang/Object;Ljava/lang/Object;)V
    :try_end_f
    .catch Ljava/lang/SecurityException; {:try_start_0 .. :try_end_f} :catch_10
    .catch Ljava/lang/NoSuchFieldException; {:try_start_0 .. :try_end_f} :catch_19
    .catch Ljava/lang/IllegalArgumentException; {:try_start_0 .. :try_end_f} :catch_22
    .catch Ljava/lang/IllegalAccessException; {:try_start_0 .. :try_end_f} :catch_2b
    .catch Ljava/lang/ClassNotFoundException; {:try_start_0 .. :try_end_f} :catch_34

    .line 121
    .end local v1    # "field":Ljava/lang/reflect/Field;
    .end local v2    # "objClass":Ljava/lang/Class;
    :goto_f
    return-void

    .line 110
    :catch_10
    move-exception v0

    .line 111
    .local v0, "e":Ljava/lang/SecurityException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "setFieldOjbect SecurityException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_f

    .line 112
    .end local v0    # "e":Ljava/lang/SecurityException;
    :catch_19
    move-exception v0

    .line 113
    .local v0, "e":Ljava/lang/NoSuchFieldException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "setFieldOjbect NoSuchFieldException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_f

    .line 114
    .end local v0    # "e":Ljava/lang/NoSuchFieldException;
    :catch_22
    move-exception v0

    .line 115
    .local v0, "e":Ljava/lang/IllegalArgumentException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "setFieldOjbect IllegalArgumentException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_f

    .line 116
    .end local v0    # "e":Ljava/lang/IllegalArgumentException;
    :catch_2b
    move-exception v0

    .line 117
    .local v0, "e":Ljava/lang/IllegalAccessException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "setFieldOjbect IllegalAccessException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_f

    .line 118
    .end local v0    # "e":Ljava/lang/IllegalAccessException;
    :catch_34
    move-exception v0

    .line 119
    .local v0, "e":Ljava/lang/ClassNotFoundException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "setFieldOjbect ClassNotFoundException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_f
.end method

.method public static setStaticOjbect(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V
    .registers 8
    .param p0, "className"    # Ljava/lang/String;
    .param p1, "filedName"    # Ljava/lang/String;
    .param p2, "filedVaule"    # Ljava/lang/Object;

    .prologue
    .line 126
    :try_start_0
    invoke-static {p0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v2

    .line 127
    .local v2, "objClass":Ljava/lang/Class;
    invoke-virtual {v2, p1}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v1

    .line 128
    .local v1, "field":Ljava/lang/reflect/Field;
    const/4 v3, 0x1

    invoke-virtual {v1, v3}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 129
    const/4 v3, 0x0

    invoke-virtual {v1, v3, p2}, Ljava/lang/reflect/Field;->set(Ljava/lang/Object;Ljava/lang/Object;)V
    :try_end_10
    .catch Ljava/lang/SecurityException; {:try_start_0 .. :try_end_10} :catch_11
    .catch Ljava/lang/NoSuchFieldException; {:try_start_0 .. :try_end_10} :catch_1a
    .catch Ljava/lang/IllegalArgumentException; {:try_start_0 .. :try_end_10} :catch_23
    .catch Ljava/lang/IllegalAccessException; {:try_start_0 .. :try_end_10} :catch_2c
    .catch Ljava/lang/ClassNotFoundException; {:try_start_0 .. :try_end_10} :catch_35

    .line 141
    .end local v1    # "field":Ljava/lang/reflect/Field;
    .end local v2    # "objClass":Ljava/lang/Class;
    :goto_10
    return-void

    .line 130
    :catch_11
    move-exception v0

    .line 131
    .local v0, "e":Ljava/lang/SecurityException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "setStaticOjbect SecurityException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_10

    .line 132
    .end local v0    # "e":Ljava/lang/SecurityException;
    :catch_1a
    move-exception v0

    .line 133
    .local v0, "e":Ljava/lang/NoSuchFieldException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "setStaticOjbect NoSuchFieldException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_10

    .line 134
    .end local v0    # "e":Ljava/lang/NoSuchFieldException;
    :catch_23
    move-exception v0

    .line 135
    .local v0, "e":Ljava/lang/IllegalArgumentException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "setStaticOjbect IllegalArgumentException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_10

    .line 136
    .end local v0    # "e":Ljava/lang/IllegalArgumentException;
    :catch_2c
    move-exception v0

    .line 137
    .local v0, "e":Ljava/lang/IllegalAccessException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "setStaticOjbect IllegalAccessException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_10

    .line 138
    .end local v0    # "e":Ljava/lang/IllegalAccessException;
    :catch_35
    move-exception v0

    .line 139
    .local v0, "e":Ljava/lang/ClassNotFoundException;
    sget-object v3, Lcom/wknight/dexshell/RefInvoke;->logTag:Ljava/lang/String;

    const-string v4, "setStaticOjbect ClassNotFoundException"

    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_10
.end method
