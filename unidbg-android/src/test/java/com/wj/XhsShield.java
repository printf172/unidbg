package com.wj;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSink;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class XhsShield extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    private Headers headers;
    private Request request;
    private String url;

    XhsShield(){
        emulator = AndroidEmulatorBuilder.for32Bit().setProcessName("com.xhs").build(); // 创建模拟器实例，要模拟32位或者64位，在这里区分
        final Memory memory = emulator.getMemory(); // 模拟器的内存操作接口
        memory.setLibraryResolver(new AndroidResolver(23)); // 设置系统类库解析
        vm = emulator.createDalvikVM(new File("/Users/jiewang/IdeaProjects/printf172/unidbg/unidbg-android/src/test/resources/xiaohongshu/xiaohongshu-6.97.0.apk"));
        vm.setVerbose(true);
        DalvikModule libcdm = vm.loadLibrary(new File("/Users/jiewang/IdeaProjects/printf172/unidbg/unidbg-android/src/test/resources/xiaohongshu/libshield.so"), true);
//        emulator.attach().addBreakPoint(libcdm.getModule().findSymbolByName("pthread_create").getAddress());
        DalvikModule dm = vm.loadLibrary("shield", true);

        vm.setJni(this);
        module = dm.getModule();
        System.out.println("call JNIOnLoad");
        dm.callJNI_OnLoad(emulator);


        String url = "https://edith.xiaohongshu.com/api/sns/v3/user/info?user_id=5aae2cffb1da1410848cc29f";
        request = new Request.Builder()
                .url(url)
                .addHeader("x-b3-traceid", "9f9fb0630e150e0f")
                .addHeader("xy-common-params", "fid=162988201010e62257db56717f42068a3d56cc4cd8f1&device_fingerprint=20210621164233f1abe938f11f4371dad0e9cb3764ef0101e128bc882febd5&device_fingerprint1=20210621164233f1abe938f11f4371dad0e9cb3764ef0101e128bc882febd5&launch_id=1629940903&tz=Asia%2FShanghai&channel=PMgdt9803121&versionName=6.89.0.1&deviceId=2b586458-dd63-3630-b79f-29b88a036ca7&platform=android&sid=session.1629882581793303421285&identifier_flag=4&t=1629947848&project_id=ECFAAF&build=6890179&x_trace_page_current=note_detail_r10&lang=zh-Hans&app_id=ECFAAF01&uis=light")
                .addHeader("user-agent", "Dalvik/2.1.0 (Linux; U; Android 10; Redmi Note 8 MIUI/V12.0.2.0.QCOCNXM) Resolution/1080*2340 Version/6.89.0.1 Build/6890179 Device/(Xiaomi;Redmi Note 8) discover/6.89.0.1 NetType/WiFi")
                .build();
    }

    // 第一个初始化函数
    public void callinitializeNative(){
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
        module.callFunction(emulator, 0x6c11d, list.toArray());
    };

    // 第二个初始化函数
    public long callinitialize(){
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
        list.add(vm.addLocalObject(new StringObject(vm, "main")));
        Number number = module.callFunction(emulator, 0x6b801, list.toArray());
        return number.longValue();
    }

    // 目标函数
    public void callintercept(long ptr){
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
        DvmObject<?> chain = vm.resolveClass("okhttp3/Interceptor$Chain").newObject(null);
        list.add(vm.addLocalObject(chain));
        list.add(ptr);
        Number number = module.callFunction(emulator, 0x6b9e9, list.toArray());
        System.out.println("result:"+number.intValue());
    };
    public static void main(String[] args) {
        XhsShield test = new XhsShield();
        test.callinitializeNative();
        long ptr = test.callinitialize();
        System.out.println("call intercept");
        test.callintercept(ptr);


    }

    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature){
            case "com/xingin/shield/http/ContextHolder->sLogger:Lcom/xingin/shield/http/ShieldLogger;":{
                return vm.resolveClass("com/xingin/shield/http/ShieldLogger").newObject(signature);
            }
            case "com/xingin/shield/http/ContextHolder->sDeviceId:Ljava/lang/String;":{
                return new StringObject(vm, "81e30ab8-2b81-33dd-8435-f9404554b4b5");
            }
        }
        return super.getStaticObjectField(vm, dvmClass, signature);
    }

    @Override
    public void callVoidMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature){
            case "com/xingin/shield/http/ShieldLogger->nativeInitializeStart()V":{
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->nativeInitializeEnd()V": {
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->initializeStart()V": {
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->initializedEnd()V": {
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->buildSourceStart()V": {
                return;
            }
            case "okhttp3/RequestBody->writeTo(Lokio/BufferedSink;)V": {
                BufferedSink bufferedSink = (BufferedSink) vaList.getObjectArg(0).getValue();
                RequestBody requestBody = (RequestBody) dvmObject.getValue();
                if(requestBody != null){
                    try {
                        requestBody.writeTo(bufferedSink);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->buildSourceEnd()V": {
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->calculateStart()V": {
                System.out.println("calculateStart —— 开始计算");
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->calculateEnd()V": {
                System.out.println("calculateEnd —— 结束计算");
                return;
            }
        }
        super.callVoidMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature){
            case "java/nio/charset/Charset->defaultCharset()Ljava/nio/charset/Charset;":{
                return vm.resolveClass("java/nio/charset/Charset").newObject(Charset.defaultCharset());
            }
            case "com/xingin/shield/http/Base64Helper->decode(Ljava/lang/String;)[B":{
                String input = (String) vaList.getObjectArg(0).getValue();
                byte[] result = Base64.decodeBase64(input);
                return new ByteArray(vm, result);
            }
        }
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
    }

    @Override
    public int getIntField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        switch (signature){
            case "android/content/pm/PackageInfo->versionCode:I":{
                return 6970181;
            }
        }
        return super.getIntField(vm, dvmObject, signature);
    }

    @Override
    public int getStaticIntField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature){
            case "com/xingin/shield/http/ContextHolder->sAppId:I":{
                return -319115519;
            }
        }
        return super.getStaticIntField(vm, dvmClass, signature);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "android/content/Context->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;":
                return vm.resolveClass("android/content/SharedPreferences").newObject(vaList.getObjectArg(0));
            case "android/content/SharedPreferences->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;": {
                if(((StringObject) dvmObject.getValue()).getValue().equals("s")){
                    System.out.println("getString :"+vaList.getObjectArg(0).getValue());
                    if (vaList.getObjectArg(0).getValue().equals("main")) {
                        return new StringObject(vm, "");
                    }
                    if(vaList.getObjectArg(0).getValue().equals("main_hmac")){
                        return  new StringObject(vm, "a9+xPqTwWr7ua8QlDuTyLjvNTAszAxbIhBWeugeCNpcorLQJTUiH6JbLFDrW1cypknldr7izHSeoGQ1HzB6VAVu7iMG6FU1+bEt7/e+9cx6LmeDCOKSapcI9elpXr9ba");
                    }
                }
            }
            case "okhttp3/Interceptor$Chain->request()Lokhttp3/Request;": {
                DvmClass clazz = vm.resolveClass("okhttp3/Request");
                return clazz.newObject(request);
            }
            case "okhttp3/Request->url()Lokhttp3/HttpUrl;": {
                DvmClass clazz = vm.resolveClass("okhttp3/HttpUrl");
                Request request = (Request) dvmObject.getValue();
                return clazz.newObject(request.url());
            }
            case "okhttp3/HttpUrl->encodedPath()Ljava/lang/String;": {
                HttpUrl httpUrl = (HttpUrl) dvmObject.getValue();
                return new StringObject(vm, httpUrl.encodedPath());
            }
            case "okhttp3/HttpUrl->encodedQuery()Ljava/lang/String;": {
                HttpUrl httpUrl = (HttpUrl) dvmObject.getValue();
                return new StringObject(vm, httpUrl.encodedQuery());
            }
            case "okhttp3/Request->body()Lokhttp3/RequestBody;": {
                Request request = (Request) dvmObject.getValue();
                return vm.resolveClass("okhttp3/RequestBody").newObject(request.body());
            }
            case "okhttp3/Request->headers()Lokhttp3/Headers;": {
                Request request = (Request) dvmObject.getValue();
                return vm.resolveClass("okhttp3/Headers").newObject(request.headers());
            }
            case "okio/Buffer->writeString(Ljava/lang/String;Ljava/nio/charset/Charset;)Lokio/Buffer;": {
                System.out.println("write to my buffer:"+vaList.getObjectArg(0).getValue());
                Buffer buffer = (Buffer) dvmObject.getValue();
                buffer.writeString(vaList.getObjectArg(0).getValue().toString(), (Charset) vaList.getObjectArg(1).getValue());
                return dvmObject;
            }
            case "okhttp3/Headers->name(I)Ljava/lang/String;": {
                Headers headers = (Headers) dvmObject.getValue();
                return new StringObject(vm, headers.name(vaList.getIntArg(0)));
            }
            case "okhttp3/Headers->value(I)Ljava/lang/String;": {
                Headers headers = (Headers) dvmObject.getValue();
                return new StringObject(vm, headers.value(vaList.getIntArg(0)));
            }
            case "okio/Buffer->clone()Lokio/Buffer;": {
                Buffer buffer = (Buffer) dvmObject.getValue();
                return vm.resolveClass("okio/Buffer").newObject(buffer.clone());
            }
            case "okhttp3/Request->newBuilder()Lokhttp3/Request$Builder;": {
                Request request = (Request) dvmObject.getValue();
                return vm.resolveClass("okhttp3/Request$Builder").newObject(request.newBuilder());
            }
            case "okhttp3/Request$Builder->header(Ljava/lang/String;Ljava/lang/String;)Lokhttp3/Request$Builder;": {
                Request.Builder builder = (Request.Builder) dvmObject.getValue();
                builder.header(vaList.getObjectArg(0).getValue().toString(), vaList.getObjectArg(1).getValue().toString());
                return dvmObject;
            }
            case "okhttp3/Request$Builder->build()Lokhttp3/Request;": {
                Request.Builder builder = (Request.Builder) dvmObject.getValue();
                return vm.resolveClass("okhttp3/Request").newObject(builder.build());
            }
            case "okhttp3/Interceptor$Chain->proceed(Lokhttp3/Request;)Lokhttp3/Response;": {
                return vm.resolveClass("okhttp3/Response").newObject(null);
            }
        }

        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public DvmObject<?> newObjectV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature){
            case "okio/Buffer-><init>()V":
                return dvmClass.newObject(new Buffer());
        }
        return super.newObjectV(vm, dvmClass, signature, vaList);
    }

    @Override
    public int callIntMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature){
            case "okhttp3/Headers->size()I":
                Headers headers = (Headers) dvmObject.getValue();
                return headers.size();
            case "okhttp3/Response->code()I":
                return 200;
            case "okio/Buffer->read([B)I":
                Buffer buffer = (Buffer) dvmObject.getValue();
                return buffer.read((byte[]) vaList.getObjectArg(0).getValue());
        }
        return super.callIntMethodV(vm, dvmObject, signature, vaList);
    }

}


