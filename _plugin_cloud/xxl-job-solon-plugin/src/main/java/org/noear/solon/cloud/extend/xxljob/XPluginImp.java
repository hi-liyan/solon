package org.noear.solon.cloud.extend.xxljob;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.cloud.CloudManager;
import org.noear.solon.cloud.extend.xxljob.service.CloudJobServiceImpl;
import org.noear.solon.core.Aop;
import org.noear.solon.core.Plugin;

/**
 * @author noear
 * @since 1.4
 */
public class XPluginImp implements Plugin {
    @Override
    public void start(SolonApp app) {
        if (Utils.isEmpty(XxlJobProps.instance.getServer())) {
            return;
        }

        if (XxlJobProps.instance.getJobEnable()) {
            //注册Job服务
            CloudManager.register(CloudJobServiceImpl.instance);

            //注册构建器和提取器
            Aop.context().beanExtractorAdd(XxlJob.class, new XxlJobExtractor());

            //构建自动配置
            Aop.context().beanMake(XxlJobAutoConfig.class);

            Aop.beanOnloaded(() -> {
                try {
                    XxlJobExecutor executor = Aop.getOrNew(XxlJobExecutor.class);
                    executor.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
