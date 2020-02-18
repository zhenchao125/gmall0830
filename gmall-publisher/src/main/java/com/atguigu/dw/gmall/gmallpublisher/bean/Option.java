package com.atguigu.dw.gmall.gmallpublisher.bean;

/**
 * @Author lzc
 * @Date 2020/1/3 15:20
 */
public class Option {
    private String name;
    private double value;

    public Option() {
    }

    public Option(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}

/*
{
    "name": "20岁以下",
    "value": 0.0
}
 */