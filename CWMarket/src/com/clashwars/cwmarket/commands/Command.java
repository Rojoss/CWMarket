package com.clashwars.cwmarket.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Command {

	public String[] permissions();

	public String[] aliases();

	public String[] secondaryAliases() default {};

}
