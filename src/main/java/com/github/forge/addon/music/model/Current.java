package com.github.forge.addon.music.model;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Created by pestano on 15/10/15.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Qualifier
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Current {
}
