package com.example.arouter_compiler.utils;

/**
 * @Desc: 配置
 * @Author: puyantao
 * @CreateDate: 2022/2/9 14:26
 */
public interface ProcessorConfig {

    // @ARouter 注解 的 包名 + 类名
    String AROUTER_PACKAGE = "com.example.arouter_annotation.ARouter";

    // @Parameter 注解 的 包名 + 类名
    String PARAMETER_PACKAGE = "com.example.arouter_annotation.Parameter";

    // @BindView 注解 的 包名 + 类名
    String BIND_VIEW_PACKAGE = "com.example.arouter_annotation.BindView";

    // 接收参数的TAG标记
    String OPTIONS = "moduleName"; // 同学们：目的是接收 每个module名称
    String APT_PACKAGE = "packageNameForAPT"; // 同学们：目的是接收 包名（APT 存放的包名）

    String APT_PACKAGE_NAME = "com.example.arouter";

    // String全类名
    String STRING_PACKAGE = "java.lang.String";

    // Activity全类名
    String ACTIVITY_PACKAGE = "android.app.Activity";

    // ARouter api 包名
    String AROUTER_API_PACKAGE = "com.example.arouter_api";

    // ARouter api 的 ARouterGroup 高层标准
    String AROUTER_API_GROUP = AROUTER_API_PACKAGE + ".ARouterGroup";

    // ARouter api 的 ARouterPath 高层标准
    String AROUTER_API_PATH = AROUTER_API_PACKAGE + ".ARouterPath";

    // ARouter api 的 ParameterGet 高层标准
    String AROUTER_AIP_PARAMETER_GET = AROUTER_API_PACKAGE + ".ParameterGet";

    // ARouter api 的 ParameterGet 高层标准
    String AROUTER_AIP_BIND_VIEW_GET = AROUTER_API_PACKAGE + ".BindViewGet";

    // ARouter api 的 Call 高层标准
    String CALL = AROUTER_API_PACKAGE + ".Call";

    // 路由组，中的 Path 里面的 方法名
    String PATH_METHOD_NAME = "getPathMap";

    // 路由组，中的 Group 里面的 方法名
    String GROUP_METHOD_NAME = "getGroupMap";

    // 路由组，中的 Path 里面 的 变量名 1
    String PATH_VAR1 = "pathMap";

    // 路由组，中的 Group 里面 的 变量名 1
    String GROUP_VAR1 = "groupMap";


    // ARouter api 的 ParameterGet 方法参数的名字
    String PARAMETER_NAME = "targetParameter";

    // ARouter api 的 ParmeterGet 方法的名字
    String PARAMETER_METHOD_NAME = "getParameter";

    // 路由组，PATH 最终要生成的 文件名
    String PATH_FILE_NAME = "ARouter$$Path$$";

    // 路由组，GROUP 最终要生成的 文件名
    String GROUP_FILE_NAME = "ARouter$$Group$$";

    // ARouter aip 的 ParmeterGet 的 生成文件名称 $$Parameter
    String PARAMETER_FILE_NAME = "$$Parameter";



    // ARouter aip 的 BindViewGet 的 生成文件名称 $$Parameter
    String BIND_VIEW_FILE_NAME = "$$BindView";

    //BindView 方法名
    String BIND_VIEW_METHOD_NAME = "bindView";

    // ARouter api 的 bindViewGet 方法参数的名字
    String BIND_VIEW_PARAMETER_NAME = "activity";


    // RouterManager类名
    String ROUTER_MANAGER = "RouterManager";

}




