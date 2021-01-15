package org.noear.solon.extend.cloud.annotation;

import org.noear.solon.annotation.Note;

import java.lang.annotation.*;

/**
 * 消息订阅
 *
 * @author noear
 * @since 1.2
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CloudEvent {
    @Note("topic")
    String value();
}
