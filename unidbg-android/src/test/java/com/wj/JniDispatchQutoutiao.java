package com.wj;

/**
 * @auther wj
 * @date 3/19/21 5:27 PM
 */
import com.github.unidbg.*;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;



import java.io.File;
import java.io.IOException;

public class JniDispatchQutoutiao extends AbstractJni {

    private static LibraryResolver createLibraryResolver() {
        return new AndroidResolver(23);
    }

    private static AndroidEmulator createARMEmulator() {
        return AndroidEmulatorBuilder.for32Bit().setProcessName("com.jifen.qukan").build();
    }

    private final AndroidEmulator emulator;
    private final Module module;
    private final VM vm;

    private final DvmClass Native;

    private JniDispatchQutoutiao() {
        emulator = createARMEmulator();
        final Memory memory = emulator.getMemory();
        memory.setLibraryResolver(createLibraryResolver());

        vm = emulator.createDalvikVM((File) null);
        vm.setJni(this);
        vm.setVerbose(true);

        // 自行修改文件路径
        DalvikModule dm = vm.loadLibrary(new File("/Users/jiewang/IdeaProjects/printf172/unidbg/unidbg-android/src/test/resources/qutoutiao/libNativeExample.so"), false);
        dm.callJNI_OnLoad(emulator);
        module = dm.getModule();

        Native = vm.resolveClass("com/jifen/qukan/utils/NativeUtils");
    }

    private void destroy() throws IOException {
        emulator.close();
        System.out.println("destroy");
    }


    private void test() {
        String methodSign = "innoSign(Ljava/lang/String;)Ljava/lang/String";


        String data = "OSVersion=10&deviceCode=&device_code=&distinct_id=043e625eb95223d2&dtu=014&guid=58711eba326665ea7ab4d0b1240.45219669&h5_zip_version=1004&is_pure=0&keyword=遭强拆&keywordSource=search&lat=40.024513&limit=20.0&lon=116.438364&network=wifi&oaid=6df1bbbb-f7fc-ddb0-a6af-e3df9fff5280&page=1&searchSource=0&tabCode=0&time=1616382630067&tk=ACFNv7icWB4TnxA2YE7xSvo1TqJACh4rX0k0NzUxNDk1MDg5NTIyNQ&token=&traceId=33e711cbccb5fc4fb037ce610eabd530&tuid=Tb-4nFgeE58QNmBO8Ur6NQ&uuid=38629f21663a4ff1a39e0b4e98e07f1a&version=30995000&versionName=3.9.95.000.0909.1541";

        Object ret = Native.callStaticJniMethodObject(emulator, methodSign, data);

        System.out.println("callObject执行结果:"+((DvmObject) ret).getValue());

//        byte[] tt = (byte[]) ((DvmObject) ret).getValue();
//        System.out.println(new String(tt));
    }

    public static void main(String[] args) throws Exception {

        JniDispatchQutoutiao test = new JniDispatchQutoutiao();

        test.test();

        test.destroy();
    }
}
