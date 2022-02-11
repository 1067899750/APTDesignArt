package com.example.arouter_compiler;

import com.example.arouter_annotation.Parameter;
import com.example.arouter_compiler.utils.ProcessorConfig;
import com.example.arouter_compiler.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

@AutoService(Processor.class) // 开启
@SupportedAnnotationTypes({ProcessorConfig.PARAMETER_PACKAGE})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ParameterProcessor extends AbstractProcessor {
    private static final String TAG = "ParameterProcessor";
    private Elements elementUtils; // 类信息
    private Types typeUtils;  // 具体类型
    private Messager messager; // 日志
    private Filer filer;  // 生成器

    // 临时map存储，用来存放被@Parameter注解的属性集合，生成类文件时遍历
    // key:类节点, value:被@Parameter注解的属性集合
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 扫描的时候，看那些地方使用到了@Parameter注解
        if (!ProcessorUtils.isEmpty(set)) {
            // 获取所有被 @Parameter 注解的 元素（属性）集合
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
            if (!ProcessorUtils.isEmpty(elements)) {
                // TODO　给仓库 存储相关信息
                for (Element element : elements) { // element == name， sex,  age

                    // 字段节点的上一个节点 类节点==Key
                    // 注解在属性的上面，属性节点父节点 是 类节点
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                    // enclosingElement == MainActivity == key

                    if (tempParameterMap.containsKey(enclosingElement)) {
                        tempParameterMap.get(enclosingElement).add(element);
                    } else { // 没有key MainActivity
                        List<Element> fields = new ArrayList<>();
                        fields.add(element);
                        tempParameterMap.put(enclosingElement, fields); // 加入缓存
                    }
                } // for end  缓存 tempParameterMap有值了

                // TODO 生成类文件
                // 判断是否有需要生成的类文件
                if (ProcessorUtils.isEmpty(tempParameterMap))
                    return true;
                TypeElement activityType = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
                TypeElement parameterType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_GET);
                // 生成方法
                // Object targetParameter
                ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, ProcessorConfig.PARAMETER_NAME).build();

                // 循环遍历 缓存tempParameterMap
                // 可能很多地方都使用了 @Parameter注解，那么就需要去遍历 仓库
                for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
                    // key：   Personal_MainActivity
                    // value： [name,sex,age]
                    TypeElement typeElement = entry.getKey();
                    // 非Activity直接报错
                    // 如果类名的类型和Activity类型不匹配
                    if (!typeUtils.isSubtype(typeElement.asType(), activityType.asType())) {
                        throw new RuntimeException("@Parameter注解目前仅限用于Activity类之上");
                    }
                    // 是Activity
                    // 获取类名 == MainActivity
                    ClassName className = ClassName.get(typeElement);
                    messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> className  " + className);
                    // 方法生成成功
                    ParameterFactory factory = new ParameterFactory.Builder(parameterSpec)
                            .setMessager(messager)
                            .setClassName(className)
                            .build();

                    // MainActivity t = (MainActivity) targetParameter;
                    factory.addFirstStatement();

                    // 多行语句
                    for (Element element : entry.getValue()) {
                        factory.buildStatement(element);
                    }

                    // 最终生成的类文件名（类名$$Parameter） 例如：MainActivity$$Parameter
                    String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
                    messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> " + "APT生成获取参数类文件：" +
                            className.packageName() + "." + finalClassName);

                    // 开始生成文件，例如：MainActivity$$Parameter
                    TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName) // 类名
                            .addSuperinterface(ClassName.get(parameterType)) //  implements ParameterGet 实现ParameterLoad接口
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .addMethod(factory.build()) // 方法的构建（方法参数 + 方法体）
                            .build(); // 类构建完成

                    try {
                        JavaFile.builder(className.packageName(),  typeSpec)
                                .build() // JavaFile构建完成
                                .writeTo(filer); // 文件生成器开始生成类文件
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // 执行两次 为了防止第二有问题 加了if (set.isEmpty()) {}  内部机制回来检测一遍 所以有了第二次
        //false 执行一次
        return false; //执行一次
    }
}
