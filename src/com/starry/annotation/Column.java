package com.starry.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {
	String type();    // 列类型
	String length() default "10";   // 长度
	boolean nullAble() default true;  // 可否为空，默认为true
	String comment() default "";   // 备注信息
}
