package com.xj.xrouter.compiler;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.xj.xrouter.annotation.Module;
import com.xj.xrouter.annotation.Modules;
import com.xj.xrouter.annotation.Router;
import com.xj.xrouter.compiler.target.TargetInfo;
import com.xj.xrouter.compiler.util.UtilManager;
import com.xj.xrouter.compiler.util.Utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static com.xj.xrouter.compiler.Constants.OPTION_MODULE_NAME;

// @AutoService(Processor.class) // 生成META-INF等信息
// @SupportedSourceVersion(SourceVersion.RELEASE_7)
// @SupportedAnnotationTypes("com.ai.router.anno.Route")

public class RouterProcessor extends AbstractProcessor {
    private static final boolean DEBUG = true;
    private Messager messager;
    private Filer mFiler;
    private String mModuleName;
    private Logger mLogger;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        UtilManager.getMgr().init(processingEnv);
        messager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        Map<String, String> options = processingEnv.getOptions();

        mModuleName = options.get(OPTION_MODULE_NAME);
        mLogger = new Logger(processingEnv.getMessager());
        mLogger.info("options=" + options + ";mModuleName=" + mModuleName);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 注解为 null，直接返回
        if (annotations == null || annotations.size() == 0) {
            return true;
        }
        mModuleName = processingEnv.getOptions().get(OPTION_MODULE_NAME);
        UtilManager.getMgr().getMessager().printMessage(Diagnostic.Kind.NOTE, "process mModuleName=" + mModuleName);
        String simpleClassName = "RouterMap_" + mModuleName;

        // 扫描 Route 自己注解
        Set<? extends
                Element> elements = roundEnv.getElementsAnnotatedWith(Router.class);
        List<TargetInfo> targetInfos = new ArrayList<>();
        for (Element element : elements) {
            System.out.println("elements =" + elements);
            // 检查类型
            if (!Utils.checkTypeValid(element)) continue;
            TypeElement typeElement = (TypeElement) element;
            Router route = typeElement.getAnnotation(Router.class);
            targetInfos.add(new TargetInfo(typeElement, route.path()));
        }

        // 根据 module 名字生成相应的 java 文件
        if (!targetInfos.isEmpty()) {
            generateCode(targetInfos, simpleClassName);
        }
        return true;
    }

    /**
     * 生成对应的java文件
     *
     * @param targetInfos 代表router和activity
     * @param moduleName
     */
    private void generateCode(List<TargetInfo> targetInfos, String moduleName) {
        // Map<String, Class<? extends Activity>> routers

        TypeElement activityType = UtilManager
                .getMgr()
                .getElementUtils()
                .getTypeElement("android.app.Activity");

//        ParameterizedTypeName actParam = ParameterizedTypeName.get(ClassName.get(Class.class),
//                WildcardTypeName.subtypeOf(ClassName.get(activityType)));
        ParameterizedTypeName actParam = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class));

        ParameterizedTypeName parma = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), actParam);

        ParameterSpec parameterSpec = ParameterSpec.builder(parma, "routers").build();

        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(Constants.IROUTERMAP_HANDLEMAP)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec);

        for (TargetInfo info : targetInfos) {
            methodSpecBuilder.addStatement("routers.put($S, $T.class)", info.getRoute(), info.getTypeElement());
        }

        TypeElement interfaceType = UtilManager
                .getMgr()
                .getElementUtils()
                .getTypeElement(Constants.ROUTE_INTERFACE_NAME);

        TypeSpec typeSpec = TypeSpec.classBuilder(moduleName)
                .addSuperinterface(ClassName.get(interfaceType))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpecBuilder.build())
                .addJavadoc("Generated by Router. Do not edit it!\n")
                .build();
        try {
            JavaFile.builder(Constants.ROUTE_CLASS_PACKAGE, typeSpec)
                    .build()
                    .writeTo(UtilManager.getMgr().getFiler());
            System.out.println("generateCode: =" + Constants.ROUTE_CLASS_PACKAGE + "." + moduleName);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("generateCode:e  =" + e);
        }

    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> supportedOptions = new LinkedHashSet<>();
        supportedOptions.add(OPTION_MODULE_NAME);
        return supportedOptions;
    }

    /**
     * 定义你的注解处理器注册到哪些注解上
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Router.class.getCanonicalName());
        annotations.add(Module.class.getCanonicalName());
        annotations.add(Modules.class.getCanonicalName());
        return annotations;
    }

    /**
     * java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


}
