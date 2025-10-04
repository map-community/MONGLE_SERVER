package com.algangi.mongle.global.annotation;

import org.hibernate.annotations.IdGeneratorType;

import com.algangi.mongle.global.util.UlidGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IdGeneratorType(UlidGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ULID {

}
