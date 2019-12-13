package com.xj.xrounter.plugin

import com.android.build.api.transform.JarInput
import com.google.common.collect.ImmutableList
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Created by chenenyu on 2018/7/26.
 */
class Scanner {
    static final String TEMPLATE_ROUTE_TABLE = "com/xj/xrouter/api/IRouterMap"


    // 当前app module的records
    static List<Record> records
    // 所有app module的records（当项目存在多个app module时）
    static final HashMap<String, List<Record>> recordsMap = [:]

    static final String REGISTER_CLASS_NAME = "com/xj/xrouter/api/XRouter.class"

    private static final String APT_CLASS_PACKAGE_NAME = "com/xj/xrouter/apt"

    private static final Set<String> excludeJar = ["com.android.support", "android.arch.", "androidx."]

    static List<Record> getRecords(String name) {
        def records = recordsMap[name]
        if (records == null) {
            recordsMap[name] = ImmutableList.of(
                    new Record(TEMPLATE_ROUTE_TABLE))
        }
        return recordsMap[name]
    }

    static boolean shouldScanJar(JarInput jarInput) {
        excludeJar.each {
            if (jarInput.name.contains(it))
                return false
        }
        return true
    }

    static boolean shouldScanClass(File classFile) {
        return classFile.absolutePath.replaceAll("\\\\", "/").contains(APT_CLASS_PACKAGE_NAME)
    }

    /**
     * 扫描jar包
     */
    static void scanJar(File src, File dest) {
        if (src && src.exists()) {
            def jar = new JarFile(src)
            Enumeration enumeration = jar.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                println(" entryName=" + entryName)
                if (entryName.contains("XRouter")) {
                    println("XRouter entryName=" + entryName)
                }

                if (entryName == REGISTER_CLASS_NAME) {
                    println("find REGISTER_CLASS_NAME=" + REGISTER_CLASS_NAME)
                    // mark
                    XRouterTransform.registerTargetFile = dest
                } else if (entryName.startsWith(APT_CLASS_PACKAGE_NAME)) {
                    println("start with APT_CLASS_PACKAGE_NAME:" + APT_CLASS_PACKAGE_NAME)
                    InputStream inputStream = jar.getInputStream(jarEntry)
                    scanClass(inputStream)
                    inputStream.close()
                }
            }
            jar.close()
        }
    }

    static void scanClass(File classFile) {
        scanClass(new FileInputStream(classFile))
    }

    /**
     * 扫描class
     */
    static void scanClass(InputStream is) {
        is.withCloseable {
            ClassReader cr = new ClassReader(is)
            ScanClassVisitor cv = new ScanClassVisitor()
            cr.accept(cv, 0)
        }
    }

    static class ScanClassVisitor extends ClassVisitor {
        ScanClassVisitor() {
            super(Opcodes.ASM5)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            if (interfaces != null) {
                records.each { record ->
                    interfaces.each { interfaceName ->
                        println("interfaceName=" + interfaceName + " ;name=" + name + " ;record.templateName=" + record.templateName)
                        if (interfaceName == record.templateName) {
                            record.aptClasses.add(name)
                        }
                    }
                }
            }
        }
    }
}
