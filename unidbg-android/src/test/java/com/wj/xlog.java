package com.wj;

import com.github.unidbg.AbstractEmulator;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.debugger.DebuggerType;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.hook.hookzz.*;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.linux.android.dvm.wrapper.DvmInteger;
import com.github.unidbg.linux.file.SimpleFileIO;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.utils.Inspector;
import com.sun.jna.Pointer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class xlog extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;


    xlog() throws FileNotFoundException {
        emulator = AndroidEmulatorBuilder.for32Bit().setProcessName("com.meituan").build();
        final Memory memory = emulator.getMemory(); // 模拟器的内存操作接口
        memory.setLibraryResolver(new AndroidResolver(23)); // 设置系统类库解析

        vm = emulator.createDalvikVM(new File("/Users/jiewang/Downloads/dy10.5.0.apk")); // 创建Android虚拟机

        vm.setVerbose(true); // 设置是否打印Jni调用细节
        DalvikModule dm = vm.loadLibrary(new File("/Users/jiewang/IdeaProjects/printf172/unidbg/unidbg-android/src/test/resources/dy/libcms.so"), true);

        module = dm.getModule(); //

//        DvmClass dvmClass = vm.resolveClass("com/ss/sys/ces/a");
//        dvmClass.callStaticJniMethod(emulator,"Francies", "()V");
//        dvmClass.callStaticJniMethod(emulator,"njss", "(ILjava/lang/Object;)Ljava/lang/Object;");
//        dvmClass.callStaticJniMethod(emulator,"Zeoy", "()V");
//        dvmClass.callStaticJniMethod(emulator,"Louis", "()V");
//        dvmClass.callStaticJniMethod(emulator,"Bill", "()V");
// 保存的path
//        String traceFile = "/Users/jiewang/IdeaProjects/printf172/unidbg/unidbg-android/src/test/resources/dy/trace.txt";
//        PrintStream traceStream = new PrintStream(new FileOutputStream(traceFile), true);
//        emulator.traceCode(module.base, module.base+module.size).setRedirect(traceStream);
        emulator.traceWrite(0x0000A29CL, 0x0000A2A2L + 4);
        vm.setJni(this);
        dm.callJNI_OnLoad(emulator);

        Debugger debugger = emulator.attach();
        debugger.addBreakPoint(module, 0xa2a0);
        emulator.traceWrite(0x0000A1BEL, 0x0000A2A0L);
    }

    //xlog
    public void test(){
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
        list.add(222);
        Object custom = null;
        DvmObject<?> context = vm.resolveClass("com.ss.android.ugc.aweme.app.host.HostApplication").newObject(custom);// context
        list.add(vm.addLocalObject(context));
        list.add(vm.addLocalObject(vm.resolveClass("java.lang.Object").newObject("SS-202")));
        module.callFunction(emulator, 0x643c9, list.toArray());
    }

    public void hooka2a0(){
        // 加载HookZz
        IHookZz hookZz = HookZz.getInstance(emulator);

        hookZz.wrap(module.base + 0xa2a0 + 1 , new WrapCallback<HookZzArm32RegisterContext>() { // inline wrap导出函数
            @Override
            // 类似于 frida onEnter
            public void preCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
                // 类似于Frida args[0]
                Inspector.inspect(ctx.getR0Pointer().getByteArray(0, 0x10), "Arg1");
                System.out.println(ctx.getR1Long());
                Inspector.inspect(ctx.getR2Pointer().getByteArray(0, 0x10), "Arg3");
            };

            @Override
            // 类似于 frida onLeave
            public void postCall(Emulator<?> emulator, HookZzArm32RegisterContext ctx, HookEntryInfo info) {
            }
        });


    }

    public static void main(String[] args) throws FileNotFoundException {
//        Logger.getLogger("com.github.unidbg.linux.ARM32SyscallHandler").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.unix.UnixSyscallHandler").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.AbstractEmulator").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.linux.android.dvm.DalvikVM").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.linux.android.dvm.BaseVM").setLevel(Level.DEBUG);
//        Logger.getLogger("com.github.unidbg.linux.android.dvm").setLevel(Level.DEBUG);

        xlog xlog = new xlog();

        xlog.hooka2a0();
        xlog.test();


    }

    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "java/lang/Object->getBytes(Ljava/lang/String;)[B":
                String str = (String) dvmObject.getValue();
                return new ByteArray(vm, str.getBytes());
        }
        return null;
    }

    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "njss(ILjava/lang/Object;)Ljava/lang/Object;":
                int i = vaList.getIntArg(0);
                DvmObject<?> dvmObject = vaList.getObjectArg(1);
                return dvmObject;
        }
        return null;
    }
}

