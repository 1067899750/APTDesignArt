package com.example.arouter_compiler;


import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.bean.RouterBean;
import com.example.arouter_compiler.utils.ProcessorConfig;
import com.example.arouter_compiler.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

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
    private static final String TAG = ARouterProcessor.class.getSimpleName();

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
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + " >>>>>>> options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + " >>>>>>> aptPackage:" + aptPackage);
        if (options != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "APT 环境搭建完成....");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "APT 环境有问题，请检查 options 与 aptPackage 为null...");
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
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "并没有发现 被@ARouter注解的地方呀");
            return false; // 没有机会处理
        }

        // 获取所有被 @ARouter 注解的 元素集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);

        // 通过Element工具类，获取Activity，Callback类型
        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);

        // 显示类信息（获取被注解的节点，类节点）这也叫自描述 Mirror
        //android.app.Activity
        TypeMirror activityMirror = activityType.asType();
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "mirror >>>>>>> " + activityMirror.toString());

        // 遍历所有的类节点，element == Activity
        for (Element element : elements) {
            // 获取类节点，获取包节点 （com.xiangxue.xxxxxx）
            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();

            aptPackage = aptPackage == null ? ProcessorConfig.APT_PACKAGE_NAME : aptPackage;

            // 获取简单类名，例如：MainActivity
            String className = element.getSimpleName().toString();
            // 打印出 就证明APT没有问题
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "被@ARetuer注解的类有：" + className);

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
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "mirror >>>>>> " + elementMirror.toString());

            //主要判断注解是否是在 Activity 里面添加
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
                messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "RouterBean Check Success:" + routerBean.toString());

                // 赋值 mAllPathMap 集合里面去
                List<RouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());

                // 如果从Map中找不到key为：bean.getGroup()的数据，就新建List集合再添加进Map
                // 仓库一 没有东西
                if (ProcessorUtils.isEmpty(routerBeans)) {
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    // 加入仓库一
                    mAllPathMap.put(routerBean.getGroup(), routerBeans);

                } else {
                    routerBeans.add(routerBean);

                }
            } else {
                // ERROR 编译期发生异常
                messager.printMessage(Diagnostic.Kind.ERROR, TAG + " ==> " + "@ARouter注解未按规范配置，如：/app/MainActivity");
            }
        }   // TODO end for  同学们注意：在循环外面了 （此循环结束后，仓库一 缓存一 就存好所有 Path值了）


        // mAllPathMap 里面有值了
        // 定义（生成类文件实现的接口） 有 Path Group
        // ARouterPath描述
        TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH);
        // ARouterGroup描述
        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);


        messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "pathType >>>>>> " + pathType.toString());
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "groupType >>>>>> " + groupType.toString());

        // TODO 第一大步：系列PATH
        try {
            createPathFile(pathType); // 生成 Path类
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "在生成PATH模板时，异常了 e:" + e.getMessage());
        }

        // TODO 第二大步：组头（带头大哥）
        try {
            createGroupFile(groupType, pathType);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "在生成GROUP模板时，异常了 e:" + e.getMessage());
        }

        return true;// 坑：必须写返回值，表示处理@ARouter注解完成
    }

    /**
     * 生成路由组Group文件，如：ARouter$$Group$$app
     *
     * @param groupType ARouterLoadGroup接口信息
     * @param pathType  ARouterLoadPath接口信息
     */
    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
        // 仓库二 缓存二 判断是否有需要生成的类文件
        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMap))
            return;

        // 返回值 这一段 Map<String, Class<? extends ARouterPath>>
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class), //Map
                ClassName.get(String.class), //String

                // Class<? extends ARouterPath>> 难度
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        // ? extends ARouterPath
                        WildcardTypeName.subtypeOf(ClassName.get(pathType)) // ? extends ARouterLoadPath

                        // WildcardTypeName.supertypeOf()  ：? superARouterLoadPath
                        // 最终的：Map<String, Class<? extends ARouterPath>>
                ));

        // 1.方法 public Map<String, Class<? extends ARouterPath>> getGroupMap()
        MethodSpec.Builder methodBuidler = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME) // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(methodReturns); // 方法返回值

        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuidler.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),

                // Class<? extends ARouterPath> 难度
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))), // ? extends ARouterPath
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class));

        //  groupMap.put("personal", ARouter$$Path$$personal.class);
        //	groupMap.put("order", ARouter$$Path$$order.class);
        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            methodBuidler.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR1, // groupMap.put
                    entry.getKey(), // order, personal ,app
                    ClassName.get(aptPackage, entry.getValue()));
        }

        // return groupMap;
        methodBuidler.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        // 最终生成的类文件名 ARouter$$Group$$ + personal
        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "APT生成路由组Group类文件：" +
                aptPackage + "." + finalClassName);

        // 生成类文件：ARouter$$Group$$app
        JavaFile.builder(aptPackage, // 包名
                TypeSpec.classBuilder(finalClassName) // 类名
                        .addSuperinterface(ClassName.get(groupType)) // 实现ARouterLoadGroup接口 implements ARouterGroup
                        .addModifiers(Modifier.PUBLIC) // public修饰符
                        .addMethod(methodBuidler.build()) // 方法的构建（方法参数 + 方法体）
                        .build()) // 类构建完成
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件
    }

    /**
     * 系列Path的类  生成工作
     *
     * @param pathType ARouterPath 高层的标准
     * @throws IOException
     */
    private void createPathFile(TypeElement pathType) throws IOException {
        // 判断 map仓库中，是否有需要生成的文件
        if (ProcessorUtils.isEmpty(mAllPathMap)) {
            return; // 连缓存一 仓库一 里面 值都没有 不用干活了
        }

        // 倒序生成代码

        // 任何的class类型，必须包装
        // Map<String, RouterBean>
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),         // Map
                ClassName.get(String.class),      // Map<String,
                ClassName.get(RouterBean.class)   // Map<String, RouterBean>
        );

        // 遍历仓库 app,order,personal
        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) { // personal
            // 1.方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class) // 给方法上添加注解  @Override
                    .addModifiers(Modifier.PUBLIC) // public修饰符
                    .returns(methodReturn) // 把Map<String, RouterBean> 加入方法返回
                    ;

            // Map<String, RouterBean> pathMap = new HashMap<>();
            // $N == 变量 为什么是这个，因为变量有引用 所以是$N
            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                    ClassName.get(Map.class),           // Map
                    ClassName.get(String.class),        // Map<String,
                    ClassName.get(RouterBean.class),    // Map<String, RouterBean>
                    ProcessorConfig.PATH_VAR1,          // Map<String, RouterBean> pathMap
                    ClassName.get(HashMap.class)        // Map<String, RouterBean> pathMap = new HashMap<>();
            );

            // 必须要循环，因为有多个
            // pathMap.put("/personal/Personal_Main2Activity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
            // Personal_Main2Activity.class);
            // pathMap.put("/personal/Personal_MainActivity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY));
            List<RouterBean> pathList = entry.getValue();
            /**
             $N == 变量 变量有引用 所以 N
             $L == TypeEnum.ACTIVITY
             */
            // personal 的细节
            for (RouterBean bean : pathList) {
                methodBuilder.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        ProcessorConfig.PATH_VAR1, // pathMap.put
                        bean.getPath(), // "/personal/Personal_Main2Activity"
                        ClassName.get(RouterBean.class), // RouterBean
                        ClassName.get(RouterBean.TypeEnum.class), // RouterBean.Type
                        bean.getTypeEnum(), // 枚举类型：ACTIVITY
                        ClassName.get((TypeElement) bean.getElement()), // MainActivity.class Main2Activity.class
                        bean.getPath(), // 路径名
                        bean.getGroup() // 组名
                );
            } // TODO end for

            // return pathMap;
            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            // TODO 注意：不能像以前一样，1.方法，2.类  3.包， 因为这里面有implements ，所以 方法和类要合为一体生成才行，这是特殊情况

            // 最终生成的类文件名  ARouter$$Path$$personal
            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();

            messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "APT生成路由Path类文件：" +
                    aptPackage + "." + finalClassName);

            // 生成类文件：ARouter$$Path$$personal
            JavaFile.builder(aptPackage, // 包名  APT 存放的路径
                    TypeSpec.classBuilder(finalClassName) // 类名
                            // 实现ARouterLoadPath接口  implements ARouterPath==pathType
                            .addSuperinterface(ClassName.get(pathType))
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .addMethod(methodBuilder.build()) // 方法的构建（方法参数 + 方法体）
                            .build()) // 类构建完成
                    .build() // JavaFile构建完成
                    .writeTo(filer); // 文件生成器开始生成类文件

            // 仓库二 缓存二  非常重要一步，注意：PATH 路径文件生成出来了，才能赋值路由组mAllGroupMap
            mAllGroupMap.put(entry.getKey(), finalClassName);
        }
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
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, TAG + " ==> " + "@ARouter注解中的path值，必须要以 / 开头");
            return false;
        }

        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (path.lastIndexOf("/") == 0) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, TAG + " ==> " + "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app,order,personal 作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + ">>>>>>> finalGroup : " + finalGroup);

        // app,order,personal == options
        // @ARouter注解中的group有赋值情况
        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, TAG + " ==> " + "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + ">>>>>>> options : " + options + " >>>>>>> group : " + group);
            bean.setGroup(finalGroup);
        }
        // 如果真的返回ture   RouterBean.group  xxxxx 赋值成功 没有问题
        return true;
    }


}

















