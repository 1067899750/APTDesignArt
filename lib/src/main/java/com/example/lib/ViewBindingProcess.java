package com.example.lib;

import com.example.annotation.BindView1;
import com.example.lib.Utils.ClassUtils;
import com.example.lib.Utils.ClassViewBinding;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
 * @Desc:
 * @Author: puyantao
 * @CreateDate: 2021/11/11 10:26
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.example.annotation.SkipPager"})
@SupportedOptions({"value"})
public class ViewBindingProcess extends AbstractProcessor {
    // 注解节点
    private Elements mElementTool;
    // 类信息
    private Types mTypeTool;
    // 专用日志
    private Messager mMessager;
    //文件类，生成JAVA文件的
    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementTool = processingEnvironment.getElementUtils();
        mTypeTool = processingEnvironment.getTypeUtils();
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
    }

    //可以有 SupportedSourceVersion 代替
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    //可以有 SupportedAnnotationTypes 代替
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }

    //可以有 SupportedOptions 代替
    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        List<ClassViewBinding> targetList = getTargetMap(set, roundEnvironment);
        createJavaFile(targetList);
        // true代表已经处理完毕了，不再处理了
        return false;
    }


    /**
     * 解析注解类
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    private List<ClassViewBinding> getTargetMap(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        List<ClassViewBinding> targetList = new ArrayList<>();

        // 1、获取代码中所有使用 @SkipPager 注解修饰的字段
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindView1.class);
        for (Element element : elementsAnnotatedWith) {
            //  获取简单的 类名 (MyActivity)
            String fileName = element.getSimpleName().toString();
            System.out.println("fileName == " + fileName);

            // 获取字段类型（全类名） (com.example.annotationdemo.MyActivity)
            String fieldType = element.asType().toString();
            System.out.println("fieldType == " + fieldType);

            // 获取注解元素的值
            String value = element.getAnnotation(BindView1.class).value();
            System.out.println("value == " + value);

            // 类的上一个节点是 包名  (com.example.annotationdemo)
            String packageName = mElementTool.getPackageOf(element).getQualifiedName().toString();
            System.out.println("packageName == " + packageName);

            //包名 （com.example.annotationdemo）
            Element enclosingElement = element.getEnclosingElement();
            System.out.println("enclosingElement == " + enclosingElement);
            targetList.add(new ClassViewBinding(fileName, fieldType,
                    value, packageName));
        }
        return targetList;
    }

    /**
     * 解析注解，生产文件
     *
     * @param fileList
     */
    private void createJavaFile(List<ClassViewBinding> fileList) {
        String packageName = "com.pu.dataBinding";
        List<MethodSpec> methodS = new ArrayList<>();
        for (ClassViewBinding data : fileList) {
           List<ParameterSpec> parameterSpecs = new ArrayList<>();
            // 开始真正的使用JavaPoet的方式来生成 Java代码文件
            MethodSpec methodSpec = MethodSpec.methodBuilder("start_" + data.getValue())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    //参数
                    .addParameter(ClassName.bestGuess(ClassUtils.activity), "v1")
                    .addParameter(ClassName.bestGuess(ClassUtils.bundle), "v2")
                    .addStatement("$T intent = new $T()",
                            //导入 Intent 类
                            ClassName.bestGuess(ClassUtils.intent),
                            ClassName.bestGuess(ClassUtils.intent))
                    //S : 表示字符串，T : class 类型
                    .addStatement("intent.setClassName($S, $S)", data.getPackageName(), data.getFieldType())
                    .addStatement("intent.putExtras(v2)")
                    .addStatement("v1.startActivity(intent)")
                    .build();
            methodS.add(methodSpec);
        }
        // 最终要生成的类名
        String className = "DataBindingUtil";
        //生产的类
        TypeSpec typeSpec = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                //方法
                .addMethods(methodS)
                .build();

        //生产文件
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                //文件注释
                .addFileComment("构建 Activity 跳转信息")
                .build();

        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}


















