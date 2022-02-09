package com.example.arouter_compiler;


import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.bean.RouterBean;
import com.example.arouter_compiler.utils.ProcessorConfig;
import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)
// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({ProcessorConfig.AROUTER_PACKAGE})
// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
// 注解处理器能够接收的参数（例如：如果想把Android App信息传递到这个注解处理器(Java工程)，
// 是没法实现的，所以需要通过这个才能接收到
@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE})

public class ARouterProcessor extends AbstractProcessor {
    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;

    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // Message用来打印 日志相关信息
    private Messager messager;

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;


    private String options; // 各个模块传递过来的模块名 例如：app order personal
    private String aptPackage; // 各个模块传递过来的目录 用于统一存放 apt生成的文件

    // 仓库一 Path  缓存一
    // Map<"personal", List<RouterBean>>
    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>(); // 目前是一个

    // 仓库二 Group 缓存二
    // Map<"personal", "ARouter$$Path$$personal.class">
    private Map<String, String> mAllGroupMap = new HashMap<>();

    // 做初始化工作，就相当于 Activity中的 onCreate函数一样的作用
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementTool = processingEnvironment.getElementUtils();
        typeTool = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        // 只有接受到 App壳 传递过来的书籍，才能证明我们的 APT环境搭建完成
        options = processingEnvironment.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnvironment.getOptions().get(ProcessorConfig.APT_PACKAGE);
        messager.printMessage(Diagnostic.Kind.ERROR, ">>>>>>>>>>>>>>>>>>>>>> options:" + options);
        messager.printMessage(Diagnostic.Kind.WARNING, ">>>>>>>>>>>>>>>>>>>>>> aptPackage:" + aptPackage);
        if (options != null && aptPackage != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建完成....");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境有问题，请检查 options 与 aptPackage 为null...");
        }
        System.out.println("====》options ：" + options + "/n" + " ====》aptPackage ：" + aptPackage);
    }

    /**
     * 相当于main函数，开始处理注解
     * 注解处理器的核心方法，处理具体的注解，生成Java文件
     *
     * @param set              使用了支持处理注解的节点集合
     * @param roundEnvironment 当前或是之前的运行环境,可以通过该对象查找的注解。
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("====》options ：" + options + "/n" + " ====》aptPackage ：" + aptPackage);
//        if (set.isEmpty()) {
//            messager.printMessage(Diagnostic.Kind.NOTE, "并没有发现 被@ARouter注解的地方呀");
//            return false; // 没有机会处理
//        }

        // 获取所有被 @ARouter 注解的 元素集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);

        // 通过Element工具类，获取Activity，Callback类型
        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);

        // 显示类信息（获取被注解的节点，类节点）这也叫自描述 Mirror
        TypeMirror activityMirror = activityType.asType();

        // 遍历所有的类节点
        for (Element element : elements) {
            // 获取类节点，获取包节点 （com.xiangxue.xxxxxx）
            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();

            System.out.println("pa ：" + packageName);
        }
        return true;
    }
}

















