package com.example.lib.Utils;

import afu.org.checkerframework.checker.igj.qual.I;

/**
 * @Desc: view
 * @Author: puyantao
 * @CreateDate: 2021/11/11 14:34
 */
public class FieldViewBinding {
    private String fileName;
    private String fieldType;
    private int id;
    private String packageName;
    private String allClassName;

    public FieldViewBinding() {
    }

    public FieldViewBinding(String fileName, String fieldType, int id, String packageName, String allClassName) {
        this.fileName = fileName;
        this.fieldType = fieldType;
        this.id = id;
        this.packageName = packageName;
        this.allClassName = allClassName;
    }

    public String getAllClassName() {
        return allClassName;
    }

    public void setAllClassName(String allClassName) {
        this.allClassName = allClassName;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
