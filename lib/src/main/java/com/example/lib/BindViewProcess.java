package com.example.lib;

import com.example.annotation.BindView1;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @Desc: 只要成为 AbstractProcessor的子类，就已经是注解处理器了
 * @Author: puyantao
 * @CreateDate: 2021/11/3 15:22
 */
// Google的这个AutoService可以去生成配置文件哦
@AutoService(Processor.class)
// 配置版本（Java编译时的版本）
@SupportedSourceVersion(SourceVersion.RELEASE_8)
// 允许注解处理器  可以去处理的注解，不是所有的注解处理器都可以去处理
@SupportedAnnotationTypes({"com.example.annotation.BindView1"})
// 注解处理器能够接收的参数（例如：如果想把AndroidApp信息传递到这个注解处理器(Java工程)，是没法实现的，所以需要通过这个才能接收到）
@SupportedOptions({"value"})
public class BindViewProcess extends AbstractProcessor {
    // 注解节点
    Elements elementsTool;
    // 类信息
    Types typesTool;
    // 专用日志
    Messager messager;
    //文件类，生成JAVA文件的
    Filer filer;

    // 用于做一些初始化的工作，就像Activity的onCreate方法一样


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementsTool = processingEnvironment.getElementUtils();
        typesTool = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        String value = processingEnvironment.getOptions().get("value");
        messager.printMessage(Diagnostic.Kind.NOTE, "打印信息" + value);
    }

    // 把下面这种写法换成 注解的方式，所以注释掉
    // 获得支持的注解类型，如果这个没有，那么最下面的process方法就没有注解去处理
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }

    // 通过什么JDK版本进行编译，这个是必填的
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    // 用来接收外面传递进来的参数
    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    /**
     * 如果我们在编译的时候，会执行此方法
     *
     * @param set              能够拿到所有使用了BindView的集合（例如：在属性上使用了一次，在类上使用了一次，那么这个集合的size就是2）
     * @param roundEnvironment 环境
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String test = "我在编译的时候就执行了哦" + set.size();
        messager.printMessage(Diagnostic.Kind.NOTE, test);

//        return setMothed1(set, roundEnvironment);
//        return setMothed2(set, roundEnvironment);
        return setMothed3(set, roundEnvironment);
    }

    private boolean setMothed1(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 最终处理那些地方使用到了 BindView的注解，然后可以去生成Java文件哦
        if (set.isEmpty()) {
            return false;
        }

        for (TypeElement element : set) {
            // 类节点之上，就是包节点
            String qualifiedName = elementsTool.getPackageOf(element).getQualifiedName().toString();
            // 获取类的 简单类名
            String simpleName = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE,
                    "使用到注解的信息有 --- 包节点:" + qualifiedName + " 简单类名:" + simpleName);
            String finalCreateClassName = simpleName + "_" + "BindView";


            try {
                JavaFileObject sourceFile = filer.createSourceFile(qualifiedName + "." + finalCreateClassName);
                Writer writer = sourceFile.openWriter();
                writer.write("public class " + finalCreateClassName + "{\r\n\r\n");
                writer.write("public static void main(String[] argc) {\r\n");
                writer.write(" System.out.println(\"1 text code study ....\");\r\n");
                writer.write("}\r\n");
                writer.write("}");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // true代表已经处理完毕了，不再处理了
        return true;
    }


    private boolean setMothed2(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 全局扫描 获取 被BindView注解的
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindView1.class);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>> size:" + elementsAnnotatedWith.size());
        for (Element element : elementsAnnotatedWith) {
            // "public class " + className
            // Dagger2
            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "args")
                    .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                    .build();

            messager.printMessage(Diagnostic.Kind.NOTE, "1111111111...");

            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(main)
                    .build();

            messager.printMessage(Diagnostic.Kind.NOTE, "2222222222...");

            JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                    .build();

            messager.printMessage(Diagnostic.Kind.NOTE, "333333333...");

            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "444444444...exception:" + e.getMessage());
            }
            messager.printMessage(Diagnostic.Kind.NOTE, "执行完毕了...");
        }
        // true代表已经处理完毕了，不再处理了
        return true;
    }

    private boolean setMothed3(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 全局扫描 获取 被BindView注解的
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindView1.class);
        for (Element element : elementsAnnotatedWith) {
            // 类的上一个节点是 包
            String packageName = elementsTool.getPackageOf(element).getQualifiedName().toString();

            // 获取简单的 类名
            String className = element.getSimpleName().toString();

            // 打印一下信息
            messager.printMessage(Diagnostic.Kind.NOTE, "被BindView注解过的信息有 packageName:" + packageName + " className:" + className);

            // 最终要生成的类名
            String finalResultClassNmae = className + "$$$BinderView";

            // 开始真正的使用JavaPoet的方式来生成 Java代码文件
            MethodSpec methodSpec = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "argc")
                    .addStatement("$T.out.print($S)", System.class, "Hello World")
                    .build();

            TypeSpec typeSpec = TypeSpec.classBuilder(finalResultClassNmae)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodSpec)
                    .build();

            JavaFile file = JavaFile.builder(packageName, typeSpec).build();

            try {
                file.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }


}










