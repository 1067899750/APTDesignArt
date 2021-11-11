package com.example.lib.Utils;

/**
 * @Desc:
 * @Author: puyantao
 * @CreateDate: 2021/11/11 14:34
 */
public class FieldViewBinding {
    private String fileName;
    private String fieldType;
    private String value;
    private String packageName;

    public FieldViewBinding() {
    }

    public FieldViewBinding(String fileName, String fieldType, String value, String packageName) {
        this.fileName = fileName;
        this.fieldType = fieldType;
        this.value = value;
        this.packageName = packageName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
