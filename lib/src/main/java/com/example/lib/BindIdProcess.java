package com.example.lib;

import com.example.annotation.BindId;
import com.example.lib.Utils.FieldViewBinding;
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

/**
 * @Desc: findViewById
 * @Author: puyantao
 * @CreateDate: 2021/11/12 11:26
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.example.annotation.BindId"})
@SupportedOptions({"id"})
public class BindIdProcess extends AbstractProcessor {
    // 注解节点
    private Elements elementUtils;
    // 类信息
    private Types typeUtils;
    // 专用日志
    private Messager messager;
    //文件类，生成JAVA文件的
    private Filer filer;

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
        Map<String, List<FieldViewBinding>> targetMap = getTargetMap(set, roundEnvironment);
        createJavaFile(targetMap);
        return false;
    }

    private Map<String, List<FieldViewBinding>> getTargetMap(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindId.class);
        Map<String, List<FieldViewBinding>> targetMap = new HashMap<>();

        for (Element element : elementsAnnotatedWith) {
            //  获取简单的 类名
            String fileName = element.getSimpleName().toString();
            System.out.println("fileName == " + fileName);
            // 获取字段类型（全类名）
            String fieldType = element.asType().toString();
            System.out.println("fieldType == " + fieldType);
            // 获取注解元素的值
            int id = element.getAnnotation(BindId.class).id();
            System.out.println("id == " + id);
            //类的上一个节点是 包名
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            System.out.println("packageName == " + packageName);

            //全类名
            String allClassName = element.getEnclosingElement().toString();
            System.out.println("allClassName == " + allClassName);
            List<FieldViewBinding> list = targetMap.get(allClassName);
            if (list == null) {
                list = new ArrayList<>();
                targetMap.put(allClassName, list);
            }
            list.add(new FieldViewBinding(fileName, fieldType,
                    id, packageName, allClassName));
        }
        System.out.println("targetMap == " + targetMap.size());
        return targetMap;
    }


    private void createJavaFile(Map<String, List<FieldViewBinding>> targetMap) {
        //baom
        String packageName = "com.pu.dataBinding";
        // 最终要生成的类名
        String className = "FindViewUtil";
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className);
        for (String key : targetMap.keySet()) {
            System.out.println("key" + key);
            String[] split = key.split("\\.");
            // 开始真正的使用JavaPoet的方式来生成 Java代码文件
            MethodSpec.Builder builder = MethodSpec.methodBuilder("init_" + split[split.length - 1])
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    //参数
                    .addParameter(ClassName.bestGuess(key), "v1");
            for (FieldViewBinding data : targetMap.get(key)) {
                builder.addStatement("v1.$L=($T)v1.findViewById($L)",
                        data.getFileName(),
                        ClassName.bestGuess(data.getFieldType()),
                        data.getId());
            }

            //生产的类
            typeSpecBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    //方法
                    .addMethod(builder.build());
        }


        //生产文件
        JavaFile javaFile = JavaFile.builder(packageName, typeSpecBuilder.build())
                //文件注释
                .addFileComment("构建 Activity 跳转信息")
                .build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        };
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }
}














