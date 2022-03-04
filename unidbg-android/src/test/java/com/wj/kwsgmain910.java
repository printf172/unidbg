package com.wj;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.BlockHook;
import com.github.unidbg.debugger.BreakPointCallback;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.api.AssetManager;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.wrapper.DvmBoolean;
import com.github.unidbg.linux.android.dvm.wrapper.DvmInteger;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.memory.MemoryBlock;
import com.github.unidbg.spi.SyscallHandler;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import com.github.unidbg.virtualmodule.android.JniGraphics;
import unicorn.ArmConst;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class kwsgmain910 extends AbstractJni implements IOResolver {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    kwsgmain910() {

        // 创建模拟器实例
        emulator = AndroidEmulatorBuilder.for32Bit().build();

        // 模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));
        // 创建Android虚拟机
        vm = emulator.createDalvikVM(new File("/Users/jiewang/IdeaProjects/printf172/unidbg/unidbg-android/src/test/resources/ks/ks9.10.10.apk"));
        new JniGraphics(emulator, vm).register(memory);
        new AndroidModule(emulator, vm).register(memory);
        vm.setJni(this);

        SyscallHandler<AndroidFileIO> handler = emulator.getSyscallHandler();
        handler.addIOResolver(this);
        DalvikModule dm = vm.loadLibrary("kwsgmain", true);
        // 加载好的 libhookinunidbg.so对应为一个模块
        module = dm.getModule();

        // 执行JNIOnLoad（如果有的话）
        dm.callJNI_OnLoad(emulator);
    }

    public static void main(String[] args) {
        kwsgmain910 demo = new kwsgmain910();
        demo.hookonegetstring();
        demo.callInit();
        demo.get_NS_sig3();

    }

    @Override
    public FileResult resolve(Emulator emulator, String pathname, int oflags) {
        System.out.println("fuck:"+pathname);
        return null;
    }

    public void callInit(){
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        DvmObject<?> thiz = vm.resolveClass("com/kuaishou/android/security/internal/dispatch/JNICLibrary").newObject(null);
        list.add(vm.addLocalObject(thiz)); // 第二个参数，实例方法是jobject，静态方法是jclass，直接填0，一般用不到。
        DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null); // context
        vm.addLocalObject(context);
        list.add(10412); //参数1
        StringObject appkey = new StringObject(vm,"d7b7d042-d4f2-4012-be60-d97ff2429c17");
        vm.addLocalObject(appkey);
        list.add(vm.addLocalObject(new ArrayObject(null, appkey,null,null,context,null,null)));
        // 直接通过地址调用
        Number numbers = module.callFunction(emulator, 0x53129, list.toArray());
        DvmObject<?> object = vm.getObject(numbers.intValue());
        String result = (String) object.getValue();
        System.out.println("result:"+result);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature){
            case "com/yxcorp/gifshow/App->getPackageCodePath()Ljava/lang/String;":{
                return new StringObject(vm, "/data/app/com.smile.gifmaker-oyRnT1esU1Pf5iDY6JKtjA==/base.apk");
            }
            case "com/yxcorp/gifshow/App->getAssets()Landroid/content/res/AssetManager;":{
                return new AssetManager(vm, signature);
            }
            case "com/yxcorp/gifshow/App->getPackageName()Ljava/lang/String;":{
                return new StringObject(vm, "com.smile.gifmaker");
            }
            case "com/yxcorp/gifshow/App->getPackageManager()Landroid/content/pm/PackageManager;":{
                return vm.resolveClass("android/content/pm/PackageManager").newObject(null);
            }
        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public int getStaticIntField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature){
            case "android/content/pm/PackageManager->GET_SIGNATURES:I":{
                return 64;
            }
        }
        return super.getStaticIntField(vm, dvmClass, signature);
    }

    public void hookonegetstring(){
        emulator.attach().addBreakPoint(module.base + 0x690de, new BreakPointCallback() {
            @Override
            public boolean onHit(Emulator<?> emulator, long address) {
                String input = "d7b7d042-d4f2-4012-be60-d97ff2429c17";
                MemoryBlock replaceBlock = emulator.getMemory().malloc(input.length(), true);
                replaceBlock.getPointer().write(input.getBytes(StandardCharsets.UTF_8));
                // 修改r0为指向新字符串的新指针
                emulator.getBackend().reg_write(ArmConst.UC_ARM_REG_R0, replaceBlock.getPointer().peer);
                emulator.getBackend().reg_write(ArmConst.UC_ARM_REG_PC, address + 3);
                return true;
            }
        });
    }

    public String get_NS_sig3() {
        System.out.println("sig3 start");
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        DvmObject<?> thiz = vm.resolveClass("com/kuaishou/android/security/mainplugin/JNICLibrary").newObject(null);
        list.add(vm.addLocalObject(thiz)); // 第二个参数，实例方法是jobject，静态方法是jclass，直接填0，一般用不到。
        DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null); // context
        vm.addLocalObject(context);
        list.add(10418); //参数1
        StringObject urlObj = new StringObject(vm, "/rest/n/feed/selectionbb9caf23ee1fda57a6c167198aba919f");
        vm.addLocalObject(urlObj);
        ArrayObject arrayObject = new ArrayObject(urlObj);
        StringObject appkey = new StringObject(vm,"d7b7d042-d4f2-4012-be60-d97ff2429c17");
        vm.addLocalObject(appkey);
        DvmInteger intergetobj = DvmInteger.valueOf(vm, -1);
        vm.addLocalObject(intergetobj);
        DvmBoolean boolobj = DvmBoolean.valueOf(vm, false);
        vm.addLocalObject(boolobj);
        list.add(vm.addLocalObject(new ArrayObject(arrayObject, appkey,intergetobj,boolobj,context,null,boolobj)));
        // 直接通过地址调用
        Number numbers = module.callFunction(emulator, 0x53129, list.toArray());
        DvmObject<?> object = vm.getObject(numbers.intValue());
        String result = (String) object.getValue();
        System.out.println("result:"+result);
        return result;
    }

    @Override
    public void callStaticVoidMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature){
            case "com/kuaishou/android/security/internal/common/ExceptionProxy->nativeReport(ILjava/lang/String;)V":{
                return;
            }
        }
        super.callStaticVoidMethodV(vm, dvmClass, signature, vaList);
    }

    @Override
    public boolean callBooleanMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature){
            case "java/lang/Boolean->booleanValue()Z":{
//                return (boolean) dvmObject.getValue();
                return false;
            }
        }
        return super.callBooleanMethodV(vm, dvmObject, signature, vaList);
    }
}

