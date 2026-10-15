package io.reactiverse.vertx.maven.plugin.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ModuleNameReader {

    /**
     * Reads the module name from a {@code module-info.class} file in the given classes directory.
     *
     * @return the module name, or {@code null} if no {@code module-info.class} exists
     * @throws IOException if the file exists but cannot be read or parsed
     */
    public static String readModuleName(File classesDirectory) throws IOException {
        File moduleInfo = new File(classesDirectory, "module-info.class");
        if (!moduleInfo.isFile()) {
            return null;
        }
        try (InputStream is = Files.newInputStream(moduleInfo.toPath())) {
            ClassReader classReader = new ClassReader(is);
            String[] result = new String[1];
            classReader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public ModuleVisitor visitModule(String name, int access, String version) {
                    result[0] = name;
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return result[0];
        }
    }
}
