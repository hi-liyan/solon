package org.noear.solon.extend.schedule;

import org.noear.solon.SolonApp;
import org.noear.solon.core.Aop;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.event.AppLoadEndEvent;

/**
 * solon.extend.schedule 相对于 cron4j-solon-plugin 的区别：
 *
 * getInterval 和 getThreads 可动态控制；例，夜间或流量小时弹性变小数值.
 *
 * */
public class XPluginImp implements Plugin {
    @Override
    public void start(SolonApp app) {
        Aop.context().beanOnloaded(() -> {
            Aop.context().beanForeach((v) -> {
                if (v.raw() instanceof IJob) {
                    JobManager.register(new JobEntity(v.name(), v.raw()));
                }
            });
        });

        app.onEvent(AppLoadEndEvent.class, e -> {
            JobManager.run(JobRunner.global);
        });
    }
}