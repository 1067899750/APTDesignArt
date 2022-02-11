package com.example.arouter_compiler.factory;

import com.example.arouter_annotation.BindView;
import com.example.arouter_compiler.utils.ProcessorConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

/**
 * @Override public void activity(MainActivity activity) {
 * MainActivity t = (MainActivity) activity;
 * t.skipBtn=(Button)t.findViewById(2131231084);
 * }
 */

public class BindViewFactory {
    public static final String TAG = BindViewFactory.class.getSimpleName();
    // 方法的构建
    private MethodSpec.Builder method;

    // 类名，如：MainActivity  /  Personal_MainActivity
    private ClassName className;

    // Messager 用来报告错误，警告和其他提示信息
    private Messager messager;

    private BindViewFactory(Builder builder) {
        this.className = builder.className;
        this.messager = builder.messager;

        this.method = MethodSpec.methodBuilder(ProcessorConfig.BIND_VIEW_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);
    }

    public void addTransformActivity(){
        //MainActivity t = (MainActivity) activity
        method.addStatement("$T t = ($T) " + ProcessorConfig.BIND_VIEW_PARAMETER_NAME,
                className, className);
    }


    public void buildStatement(Element element) {
        BindView annotation = element.getAnnotation(BindView.class);
        //  获取简单的 类名：skipBtn
        String fileName = element.getSimpleName().toString();
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + " = fileName => " + fileName);

        // 获取字段类型（全类名）: android.widget.Button
        String fieldType = element.asType().toString();
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + " = fieldType => " + fieldType);

        int value = annotation.value();
        if (value == -1) {
            throw new RuntimeException("请输入正确的参数");
        }
        method.addStatement("t.$L=($T)t.findViewById($L)",
                fileName, ClassName.bestGuess(fieldType), value);
    }


    public MethodSpec build() {
        return method.build();
    }


    /**
     * Builder构建者设计模式
     */
    public static class Builder {
        // Messager用来报告错误，警告和其他提示信息
        private Messager messager;

        // 类名，如：MainActivity
        private ClassName className;

        // 方法参数体
        private ParameterSpec parameterSpec;

        public Builder(ParameterSpec parameterSpec) {
            this.parameterSpec = parameterSpec;
        }

        public BindViewFactory.Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public BindViewFactory.Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }


        public BindViewFactory build() {
            if (parameterSpec == null) {
                throw new IllegalArgumentException("bindView方法参数体为空");
            }

            if (className == null) {
                throw new IllegalArgumentException("方法内容中的className为空");
            }

            if (messager == null) {
                throw new IllegalArgumentException("messager为空，Messager用来报告错误、警告和其他提示信息");
            }

            return new BindViewFactory(this);
        }
    }


}
