package com.wj;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.backend.CodeHook;
import com.github.unidbg.arm.backend.UnHook;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.utils.Inspector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JniDispatchSina extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    public String apkPath = "/Users/jiewang/IdeaProjects/hacker/package/xinlangxinwen754.apk";
    public String soPath = "/Users/jiewang/IdeaProjects/hacker/package/xinlangxinwen754/lib/armeabi-v7a/libsina_news_util.so";

    private static LibraryResolver createLibraryResolver() {
        return new AndroidResolver(23);
    }

    private static AndroidEmulator createARMEmulator() {
        return AndroidEmulatorBuilder
                .for32Bit()
                .build();
    }

    public JniDispatchSina() {
        emulator = createARMEmulator();
        final Memory memory = emulator.getMemory();
        memory.setLibraryResolver(createLibraryResolver());

        vm = emulator.createDalvikVM(new File(apkPath));
        vm.setVerbose(true);
        DalvikModule dm = vm.loadLibrary(new File(soPath), false);
        vm.setJni(this);

        dm.callJNI_OnLoad(emulator);
        module = dm.getModule();
    }

    public void encrypt() {
        String data = "/?resource=hbpage&newsId=HB-1-snhs/index_v2-search&page=1&newspage=0&keyword=%25E5%258C%2597%25E4%25BA%25AC&tab=news&sort=0&is_ec=0&lDid=be886037-e4f5-429f-8351-e022255e92f5&oldChwm=12070_0001&appVersion=7.54.0&city=&loginType=0&ua=Netease-MuMu__sinanews__7.54.0__android__6.0.1__740&deviceId=c2c31feed08b0eb2&resolution=1170x1872&connectionType=2&weiboUid=&ssoVer=3&osVersion=6.0.1&ipv4=36.112.14.18&chwm=12070_0001&from=6000095012&aId=01AxFiSTUtBHx1PlBQuPFvFzDAjcC2b8Dtffsyx4_QIk4GdFM.&a=&osSdk=23&i=&close_ad=&accessToken=&m=&o=&sand=nkCfM0mBfDnMlndcB9cUf%2FUH5SF6s1RcOdTo%2FI5Wz%2BY%3D&s=&seId=214d11d4aa&deviceModel=Netease__Android__MuMu&location=0.0%2C0.0";

        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv());
        list.add(0);

        list.add(vm.addLocalObject(new StringObject(vm, data)));

        Number number = module.callFunction(emulator, 0x5188 + 1, list.toArray());

        System.out.println("encrypt: " + vm.getObject(number.intValue()).getValue().toString());
    }

    public static void main(String[] args) throws IOException {
        JniDispatchSina sina = new JniDispatchSina();
        sina.encrypt();
        sina.destroy();
    }

    private void destroy() throws IOException {
        emulator.close();
    }

    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature) {
            case "com/sina/news/BuildConfig->APPLICATION_ID:Ljava/lang/String;":
                return new StringObject(vm, "com.sina.news");
        }
        return super.getStaticObjectField(vm,dvmClass,signature);
    }
    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature) {
            case "com/sina/news/SinaNewsApplication->getAppContext()Landroid/content/Context;":
                return vm.resolveClass("android/content/Context").newObject(null);
        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }
    @Override
    public int getStaticIntField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature) {
            case "android/os/Build$VERSION->SDK_INT:I":
                return 23;
        }
        return super.getStaticIntField(vm, dvmClass, signature);
    }
    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature) {
            case "android/content/Context->getPackageName()Ljava/lang/String;":
                return new StringObject(vm, "com.sina.news");
        }
        return super.callObjectMethod(vm,dvmObject,signature,varArg);
    }
}