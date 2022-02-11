package com.example.arouter_compiler;

import com.example.arouter_annotation.BindView;
import com.example.arouter_compiler.factory.BindViewFactory;
import com.example.arouter_compiler.utils.ProcessorConfig;
import com.example.arouter_compiler.utils.ProcessorUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class) // 开启
@SupportedAnnotationTypes({ProcessorConfig.BIND_VIEW_PACKAGE})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ViewProcessor extends AbstractProcessor {
    private static final String TAG = ViewProcessor.class.getSimpleName();
    private Elements elementUtils; // 类信息
    private Types typeUtils;  // 具体类型
    private Messager messager; // 日志
    private Filer filer;  // 生成器

    // 临时map存储，用来存放被@BindView 注解的属性集合，生成类文件时遍历
    // key:类节点, value:被@BindView 注解的属性集合
    private Map<TypeElement, List<Element>> bindViewMap = new HashMap<>();

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
        if (!ProcessorUtils.isEmpty(set)) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);

            if (!ProcessorUtils.isEmpty(elements)) {
                for (Element element : elements) {
                    //获取父节点
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                    if (bindViewMap.containsKey(enclosingElement)) {
                        bindViewMap.get(enclosingElement).add(element);

                    } else {
                        List<Element> views = new ArrayList<>();
                        views.add(element);
                        bindViewMap.put(enclosingElement, views);

                    }
                } // for end  缓存 tempParameterMap有值了

                //获取 com.example.arouter_api.BindViewGet 接口
                TypeElement parameterType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_AIP_BIND_VIEW_GET);

                // TODO 生成类文件
                // 判断是否有需要生成的类文件
                if (ProcessorUtils.isEmpty(bindViewMap))
                    return true;

                //循环生成方法
                for (Map.Entry<TypeElement, List<Element>> entry : bindViewMap.entrySet()) {
                    TypeElement typeElement = entry.getKey();


                    // 是Activity
                    // 获取全类名 == com.example.annotationdemo.MainActivity
                    ClassName className = ClassName.get(typeElement);
                    messager.printMessage(Diagnostic.Kind.NOTE, TAG + " ==> className  " + className);

                    // Object targetParameter
                    ParameterSpec parameterSpec = ParameterSpec.builder(
                            ClassName.bestGuess(ProcessorConfig.ACTIVITY_PACKAGE),
                            ProcessorConfig.BIND_VIEW_PARAMETER_NAME)
                            .build();

                    //todo 生成方法
                    BindViewFactory factory = new BindViewFactory.Builder(parameterSpec)
                            .setClassName(className)
                            .setMessager(messager)
                            .build();

                    factory.addTransformActivity();

                    // 多行语句
                    for (Element element : entry.getValue()) {
                        factory.buildStatement(element);
                    }


                    // 最终生成的类文件名（类名$$Parameter） 例如：MainActivity$$Parameter
                    String finalClassName = typeElement.getSimpleName() + ProcessorConfig.BIND_VIEW_FILE_NAME;
                    //开始生成文件
                    TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName)
                            .addMethod(factory.build())
                            .addSuperinterface(ClassName.get(parameterType))
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .build();

                    try {
                        JavaFile.builder(className.packageName(), typeSpec)
                                .addFileComment("获取 activity 控件")
                                .build()
                                .writeTo(filer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

}


