package com.wj;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.backend.CodeHook;
import com.github.unidbg.arm.backend.UnHook;
import com.github.unidbg.arm.backend.unicorn.Unicorn;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.debugger.DebuggerType;
import com.github.unidbg.hook.hookzz.HookZz;
import com.github.unidbg.hook.hookzz.IHookZz;
import com.github.unidbg.hook.xhook.IxHook;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.XHookImpl;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.utils.Inspector;
import com.sun.jna.Pointer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JniDispatchSougou extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    public String apkPath = "/Users/jiewang/IdeaProjects/printf172/unidbg/unidbg-android/src/test/resources/sougou/sougou-7.9.6.6.apk";
    public String soPath = "unidbg-android/src/test/resources/sougou/libSCoreTools-7.9.6.6.so";

    private static LibraryResolver createLibraryResolver() {
        return new AndroidResolver(23);
    }

    private static AndroidEmulator createARMEmulator() {
        return AndroidEmulatorBuilder
                .for32Bit()
                .build();
    }

    public JniDispatchSougou() {
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



    public void init() {
        DvmClass Context = vm.resolveClass("android/content/Context");

        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv());
        list.add(0);
        list.add(vm.addLocalObject(Context.newObject(null)));
        Number number = module.callFunction(emulator, 0x9564 + 1, list.toArray());

        System.out.println("init： " + number.intValue());
    }

    public void hookUnicornWallKey() {
        emulator.getBackend().hook_add_new(new CodeHook() {
            @Override
            public void onAttach(UnHook unHook) {

            }

            @Override
            public void detach() {
            }

            @Override
            public void hook(Backend backend, long address, int size, Object user) {
                if (address == (module.base + 0xB332)) {
                    System.out.println("Hook By Unicorn hookUnicornWallKey");
                    RegisterContext ctx = emulator.getContext();
                    Pointer input1 = ctx.getPointerArg(0);
                    Inspector.inspect(input1.getByteArray(0, 0x100), " 参数1");
                }
                if (address == (module.base + 0xB336)) {
                    RegisterContext ctx = emulator.getContext();
                    Inspector.inspect(ctx.getPointerArg(0).getByteArray(0, 0x100), " Unicorn hook hookUnicornWallKey");
                }
            }
        }, module.base + 0xB332, module.base + 0xB336, null);
    }

    public void hookUnicornRsa() {
        emulator.getBackend().hook_add_new(new CodeHook() {
            @Override
            public void onAttach(UnHook unHook) {

            }

            @Override
            public void detach() {
            }

            @Override
            public void hook(Backend backend, long address, int size, Object user) {
                if (address == (module.base + 0xB34E)) {
                    RegisterContext ctx = emulator.getContext();
                    Pointer input1 = ctx.getPointerArg(0);
                    Inspector.inspect(input1.getByteArray(0, 0x100), " hookUnicornRsa 参数1");
                }
                if (address == (module.base + 0xB352)) {
                    RegisterContext ctx = emulator.getContext();
                    Inspector.inspect(ctx.getPointerArg(0).getByteArray(0, 0x100), " Unicorn hook hookUnicornRsa");
                }
            }
        }, module.base + 0xB34E, module.base + 0xB352, null);
    }

    public void hookUnicornBase64Encode() {
        emulator.getBackend().hook_add_new(new CodeHook() {
            @Override
            public void onAttach(UnHook unHook) {

            }

            @Override
            public void detach() {
            }

            @Override
            public void hook(Backend backend, long address, int size, Object user) {
                if (address == (module.base + 0xB372)) {
                    RegisterContext ctx = emulator.getContext();
                    Pointer input1 = ctx.getPointerArg(0);
                    Inspector.inspect(input1.getByteArray(0, 0x100), " hookUnicornBase64Encode 参数1");
                }
                if (address == (module.base + 0xB376)) {
                    RegisterContext ctx = emulator.getContext();
                    Inspector.inspect(ctx.getPointerArg(0).getByteArray(0, 0x100), " Unicorn hook hookUnicornBase64Encode");
                }
            }
        }, module.base + 0xB372, module.base + 0xB376, null);
    }

    public void decrypt() {
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv());
        list.add(0);
        list.add(vm.addLocalObject(new StringObject(vm, "k=VGbQ3uWhgn6lExyMw+eNtU28iwE8zSWjL3QhU5d4PmIUrX1Qxyik5F7CxY7y/IkGwqsgNP6R7tkLEnk03rwUfRBt2UCw0egZdywdRBUBylBP9eJg1U7zxkx94flqZixYzZ846/h2hfEm+4HJf8Y5DANsDCq0pBMJaM/bLst8E5g=&v=UjGgHmFGhBy67wE03NkwDQ==&u=RenKbdTgxtv3pU05o0jl+XSUjjsti31W/VEbzsAF0xloILW9biJGcRuBqQqFX7Xv&r=DYWEWJCorJ13GZbTfGyQvSaSpTuJ7OLawG7oK3BPhsE=&g=HW4OG3oU/cj2by6DazoEGsWzRMT5Ok+b7dxLHD87vFbCujUkiScAVWuupCBRMN9hBV4ldIiaQ6qJH2ekM8EWwg==&p=2hGBmWr52Im4BmrTYvJznw==")));
        Number number = module.callFunction(emulator, 0x9DA0 + 1, list.toArray());

        System.out.println("decrypt: " + number.intValue());
    }

    public void encrypt() {
//        emulator.attach().addBreakPoint(module, 0xB1B0);

        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv());
        list.add(0);

        list.add(vm.addLocalObject(new StringObject(vm, "http://app.weixin.sogou.com/api/searchapp1")));
        list.add(vm.addLocalObject(new StringObject(vm, "type=2&ie=utf8&page=2&query=python&select_count=0&usip=1")));
        list.add(vm.addLocalObject(new StringObject(vm, "")));
        Number number = module.callFunction(emulator, 0x9CA0 + 1, list.toArray());

        System.out.println("encrypt: " + vm.getObject(number.intValue()).getValue().toString());
    }

    public static void main(String[] args) throws IOException {
        JniDispatchSougou sougou = new JniDispatchSougou();
        sougou.hookUnicornWallKey();
        sougou.hookUnicornRsa();
        sougou.hookUnicornBase64Encode();

        sougou.init();
        sougou.encrypt();

        sougou.decrypt();

        sougou.destroy();
    }

    private void destroy() throws IOException {
        emulator.close();
    }
}