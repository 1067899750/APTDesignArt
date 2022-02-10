package com.example.arouter_compiler;

import com.example.arouter_annotation.BindActivity;
import com.example.arouter_compiler.utils.ProcessorConfig;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
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

// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)
// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({"com.example.arouter_annotation.BindActivity"})
// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
// 注解处理器能够接收的参数（例如：如果想把Android App信息传递到这个注解处理器(Java工程)，
// 是没法实现的，所以需要通过这个才能接收到
@SupportedOptions({"value"})

public class TextProcessor extends AbstractProcessor {
    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;

    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // Message用来打印 日志相关信息
    private Messager messager;

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementTool = processingEnvironment.getElementUtils();
        typeTool = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 全局扫描 获取 被BindView注解的
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindActivity.class);
        for (Element element : elementsAnnotatedWith) {
            // 类的上一个节点是 包
            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();

            // 获取简单的 类名
            String className = element.getSimpleName().toString();

            // 打印一下信息
            messager.printMessage(Diagnostic.Kind.NOTE, "被BindView注解过的信息有 packageName:" + packageName + " className:" + className);

            // 最终要生成的类名
            String finalResultClassName = className + "$$$BinderView";

            /**
             * package com.example.helloworld;
             *
             * public final class HelloWorld {
             *   public static void main(String[] args) {
             *     System.out.println("Hello, JavaPoet!");
             *   }
             * }
             */
            // 1.方法
            MethodSpec mainMethod = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "args")
                    .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                    .build();

            // 2.类
            TypeSpec helloWorld = TypeSpec.classBuilder(finalResultClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(mainMethod)
                    .build();

            // 3.包
            JavaFile packageFile = JavaFile.builder(packageName, helloWorld)
                    .build();

            try {
                // 去生成
                packageFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        return false;
    }
}











