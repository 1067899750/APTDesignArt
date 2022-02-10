package com.example.arouter_compiler;


import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.bean.RouterBean;
import com.example.arouter_compiler.utils.ProcessorConfig;
import com.example.arouter_compiler.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
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
import javax.lang.model.element.Modifier;
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
@SupportedSourceVersion(SourceVersion.RELEASE_8)
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
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> aptPackage:" + aptPackage);
        if (options != null && aptPackage != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建完成....");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境有问题，请检查 options 与 aptPackage 为null...");
        }
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
        if (set.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "并没有发现 被@ARouter注解的地方呀");
            return false; // 没有机会处理
        }

        // 获取所有被 @ARouter 注解的 元素集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);

        // 通过Element工具类，获取Activity，Callback类型
        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);

        // 显示类信息（获取被注解的节点，类节点）这也叫自描述 Mirror
        //android.app.Activity
        TypeMirror activityMirror = activityType.asType();
        messager.printMessage(Diagnostic.Kind.NOTE, "mirror >>>>>>>>>>>>>>>>>>>>>> " + activityMirror.toString());

        // 遍历所有的类节点，element == Activity
        for (Element element : elements) {
            // 获取类节点，获取包节点 （com.xiangxue.xxxxxx）
//            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();

            // 获取简单类名，例如：MainActivity
            String className = element.getSimpleName().toString();
            // 打印出 就证明APT没有问题
            messager.printMessage(Diagnostic.Kind.NOTE, "被@ARetuer注解的类有：" + className);

            // 拿到注解
            ARouter aRouter = element.getAnnotation(ARouter.class);

            // TODO  一系列的检查工作
            // 在循环里面，对 “路由对象” 进行封装
            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(aRouter.group())
                    .addPath(aRouter.path())
                    .addElement(element)
                    .build();

            // ARouter注解的类 必须继承 Activity
            // Main2Activity的具体详情 例如：继承了 Activity
            //如：com.example.annotationdemo.MainActivity
            TypeMirror elementMirror = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE, "mirror >>>>>>>>>>>>>>>>>>>>>> " + elementMirror.toString());

            if (typeTool.isSubtype(elementMirror, activityMirror)) {
                // activityMirror  android.app.Activity描述信息
                // 最终证明是 Activity
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
            } else {
                // a.java 的干法 就会抛出异常
                // 不匹配抛出异常，这里谨慎使用！考虑维护问题
                throw new RuntimeException("@ARouter注解目前仅限用于Activity类之上");
            }

            if (checkRouterPath(routerBean)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean Check Success:" + routerBean.toString());

                // 赋值 mAllPathMap 集合里面去
                List<RouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());

                // 如果从Map中找不到key为：bean.getGroup()的数据，就新建List集合再添加进Map
                // 仓库一 没有东西
                if (ProcessorUtils.isEmpty(routerBeans)){
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    // 加入仓库一
                    mAllPathMap.put(routerBean.getGroup(), routerBeans);

                } else {
                    routerBeans.add(routerBean);

                }
            } else {
                // ERROR 编译期发生异常
                messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            }
        }   // TODO end for  同学们注意：在循环外面了 （此循环结束后，仓库一 缓存一 就存好所有 Path值了）


        // mAllPathMap 里面有值了
        // 定义（生成类文件实现的接口） 有 Path Group
        // ARouterPath描述
//        TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH);
//        // ARouterGroup描述
//        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);
//        messager.printMessage(Diagnostic.Kind.ERROR, "pathType >>>>>> " + pathType.toString());
//        messager.printMessage(Diagnostic.Kind.ERROR, "groupType >>>>>> " + groupType.toString());

        return true;// 坑：必须写返回值，表示处理@ARouter注解完成
    }









    /**
     * 校验@ARouter注解的值，如果group未填写就从必填项path中截取数据
     *
     * @param bean 路由详细信息，最终实体封装类
     */
    private boolean checkRouterPath(RouterBean bean) {
        //"app"   "order"   "personal"
        String group = bean.getGroup();
        //"/app/MainActivity"   "/order/Order_MainActivity"   "/personal/Personal_MainActivity"
        String path = bean.getPath();

        // 校验
        // @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")){
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
            return false;
        }

        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (path.lastIndexOf("/") == 0){
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app,order,personal 作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>> finalGroup : " + finalGroup);

        // app,order,personal == options
        // @ARouter注解中的group有赋值情况
        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)){
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>> options : " + options + " >>>>>>> group : " + group);
            bean.setGroup(finalGroup);
        }
        // 如果真的返回ture   RouterBean.group  xxxxx 赋值成功 没有问题
        return true;
    }


}

















