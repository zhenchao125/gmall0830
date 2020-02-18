package com.atguigu.re;

import java.util.Arrays;

/**
 * @Author lzc
 * @Date 2020/2/18 18:47
 */
public class RegExpDemo {
    public static void main(String[] args) {
//        String s = "41";
//        String s = "1603071634";
//        boolean result = s.matches("1[345678]\\d{9}");  //

//        String s = "lzc@atguigu.com.cn";
//        boolean result = s.matches("\\w{3,15}@\\S+\\.(com|cn|edu|org|com\\.cn)");  //
//        System.out.println(result);
//        String s = "sdalfjsl34u923ldfjlru033957934579ur";
//        System.out.println(s.replaceAll("\\d+", "@"));
//        String s = "今今天天我我我我要要请你们洗洗脚";
//        System.out.println(s.replaceAll("(.)\\1+", "$1"));
//        String s = "18603071634";
//        System.out.println(s.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1****$3"));
        String s = ".192.168.2..100........1.";
        String[] split = s.split("\\.");
        System.out.println(Arrays.toString(split));

    }
}
/*
()捕获组
\1 取第一组


正则中的两种情况:
    贪婪式(默认是贪婪) 一般使用这种
    懒惰式 在数量词的后面添加?表示启用懒惰式


数量词:
a{m} 正好m个a
a{m,} 至少m个a
a{m,n} 至少m个a, 至多n个  大于等于m小于等于n

a+ 表示至少一个 等价于 a{1,}
a* 表示至少0个 a{0,}
a? 要么0个要么1个 a{0,1}


正则的基本语法:
    [ab] a或者b
    [a-z] 所有的小写字母
    [a-zA-Z0-9_]  数字字母下划线
    [^a] 非字符a
    [^ab] 非字符a和非字符b
        注意: ^只有在[]内部才表示非, 如果不是在内部表示字符开头
    \d 表示数字 等价于 [0-9]  (digital)
    \D 表示非数字 等级于[^0-9]
    \w 表示单词字符串 数字字母下划线 等价于 [a-zA-Z0-9_]  (word)
    \W 表示非单词字符 等价于 [^a-zA-Z0-9_]
    \s 表示空白字符  space  空格 \n \r \t
    \S 非空白字符
    .  表示任意字符 去除: \n \r
    \. 只匹配真正的 .

    ^ 表示字符串的开头
    $ 字符串的结尾





正则表达式: RegularExpression
是一个工具, 用来处理文本的最大强大的工具, 没有之一
怎么理解正则表达式:
    法律          正则

    官员          字符串

java中的正则:
    Pattern
        模式
    Matcher
        匹配器

在java的字符串的方法中, 有4个方法是直接支持正则表达式
    match
    replaceAll
    replaceFirst
    split

*/
