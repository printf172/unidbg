package com.github.unidbg.ios.gpb;

import com.github.unidbg.Emulator;
import com.github.unidbg.ios.objc.NSArray;
import com.github.unidbg.ios.objc.ObjC;
import com.github.unidbg.ios.struct.objc.ObjcClass;
import com.github.unidbg.ios.struct.objc.ObjcObject;

import java.util.ArrayList;
import java.util.List;

/**
 * https://github.com/martinloesethjensen/FilesInFirebase/blob/master/pods/Protobuf/objectivec/GPBDescriptor.h
 */
public class GPBDescriptor {

    private final Emulator<?> emulator;
    private final ObjcObject descriptor;

    public static String toProtobufDef(Emulator<?> emulator, ObjC objc, String msgClass) {
        ObjcClass objcClass = objc.getClass(msgClass);
        boolean hasDescriptor = objc.respondsToSelector(objcClass, "descriptor");
        if (hasDescriptor) {
            ObjcObject descriptor = objcClass.callObjc("descriptor");
            return new GPBDescriptor(emulator, descriptor).buildMsgDef();
        } else {
            throw new UnsupportedOperationException(objcClass.getName() + " is NOT protobuf class");
        }
    }

    private GPBDescriptor(Emulator<?> emulator, ObjcObject descriptor) {
        this.emulator = emulator;
        this.descriptor = descriptor;
    }

    private String buildMsgDef() {
        StringBuilder builder = new StringBuilder();

        String name = descriptor.callObjc("name").toNSString().getString();
        builder.append("message ").append(name).append(" {\n");

        NSArray fields = descriptor.callObjc("fields").toNSArray();
        List<GPBEnumDescriptor> enumDescriptors = new ArrayList<>();
        for (ObjcObject field : fields) {
            String fieldName = field.callObjc("name").toNSString().getString();
            int number = field.callObjcInt("number");
            int dataTypeValue = field.callObjcInt("dataType");
            int required = field.callObjcInt("isRequired");
            int optional = field.callObjcInt("isOptional");
            int fieldTypeValue = field.callObjcInt("fieldType");
            int hasDefaultValue = field.callObjcInt("hasDefaultValue");
            if (hasDefaultValue != 0) {
                throw new UnsupportedOperationException("hasDefaultValue=" + hasDefaultValue);
            }

            builder.append("  ");
            GPBFieldType fieldType = GPBFieldType.of(fieldTypeValue);
            switch (fieldType) {
                case GPBFieldTypeSingle: {
                    if (required == optional) {
                        throw new IllegalStateException("fieldName=" + fieldName + ", fieldType=" + fieldTypeValue + ", required=" + required);
                    }
                    if (optional == 0) {
                        builder.append("required ");
                    } else {
                        builder.append("optional ");
                    }
                    break;
                }
                case GPBFieldTypeRepeated:
                    builder.append("repeated ");
                    break;
                case GPBFieldTypeMap: {
                    int mapKeyDataType = field.callObjcInt("mapKeyDataType");
                    GPBDataType dataType = GPBDataType.of(mapKeyDataType);
                    builder.append("map<").append(dataType.buildMsgDef(field, name, GPBFieldType.GPBFieldTypeSingle, enumDescriptors)).append(", ");
                    break;
                }
                default:
                    throw new UnsupportedOperationException("fieldType=" + fieldType);
            }
            GPBDataType dataType = GPBDataType.of(dataTypeValue);
            builder.append(dataType.buildMsgDef(field, name, fieldType, enumDescriptors));
            builder.append(" ");
            builder.append(fieldName);
            builder.append(" = ").append(number).append(";");
            builder.append("\n");
        }

        builder.append("}");

        for (GPBEnumDescriptor descriptor : enumDescriptors) {
            builder.append("\n").append(descriptor.buildMsgDef(emulator, name));
        }

        return builder.toString();
    }
}
