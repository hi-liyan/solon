package cn.dev33.satoken.solon;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.action.SaTokenAction;
import cn.dev33.satoken.annotation.*;
import cn.dev33.satoken.basic.SaBasicTemplate;
import cn.dev33.satoken.basic.SaBasicUtil;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.second.SaTokenSecondContextCreator;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.id.SaIdTemplate;
import cn.dev33.satoken.id.SaIdUtil;
import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.solon.integration.SaContextForSolon;
import cn.dev33.satoken.solon.integration.SaTokenMethodInterceptor;
import cn.dev33.satoken.sso.SaSsoTemplate;
import cn.dev33.satoken.sso.SaSsoUtil;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempInterface;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.core.Aop;
import org.noear.solon.core.Plugin;

/**
 * @author noear
 * @since 1.4
 */
@SuppressWarnings("deprecation")
public class XPluginImp implements Plugin {

    @Override
    public void start(SolonApp app) {
        Aop.context().beanAroundAdd(SaCheckPermission.class, SaTokenMethodInterceptor.INSTANCE);
        Aop.context().beanAroundAdd(SaCheckRole.class, SaTokenMethodInterceptor.INSTANCE);
        Aop.context().beanAroundAdd(SaCheckLogin.class, SaTokenMethodInterceptor.INSTANCE);
        Aop.context().beanAroundAdd(SaCheckSafe.class, SaTokenMethodInterceptor.INSTANCE);
        Aop.context().beanAroundAdd(SaCheckBasic.class, SaTokenMethodInterceptor.INSTANCE);

        //集成初始化

        //注入配置Bean
        SaTokenConfig saTokenConfig = Solon.cfg().getBean("sa-token", SaTokenConfig.class);
        SaManager.setConfig(saTokenConfig);

        // 注入上下文Bean
        SaManager.setSaTokenContext(new SaContextForSolon());

        // 注入Dao Bean
        Aop.getAsyn(SaTokenDao.class, bw -> {
            SaManager.setSaTokenDao(bw.raw());
        });

        // 注入二级上下文 Bean
        Aop.getAsyn(SaTokenSecondContextCreator.class, bw -> {
            SaTokenSecondContextCreator raw = bw.raw();
            SaManager.setSaTokenSecondContext(raw.create());
        });

        // 注入侦听器 Bean
        Aop.getAsyn(SaTokenListener.class, bw -> {
            SaManager.setSaTokenListener(bw.raw());
        });

        // 注入框架行为 Bean
        Aop.getAsyn(SaTokenAction.class, bw -> {
            SaManager.setSaTokenAction(bw.raw());
        });

        // 注入权限认证 Bean
        Aop.getAsyn(StpInterface.class, bw -> {
            SaManager.setStpInterface(bw.raw());
        });

        // 注入持久化 Bean
        Aop.getAsyn(SaTokenDao.class, bw -> {
            SaManager.setSaTokenDao(bw.raw());
        });

        // 临时令牌验证模块 Bean
        Aop.getAsyn(SaTempInterface.class, bw -> {
            SaManager.setSaTemp(bw.raw());
        });

        // Sa-Token-Id 身份凭证模块 Bean
        Aop.getAsyn(SaIdTemplate.class, bw -> {
            SaIdUtil.saIdTemplate = bw.raw();
        });

        // Sa-Token Http Basic 认证模块 Bean
        Aop.getAsyn(SaBasicTemplate.class, bw -> {
            SaBasicUtil.saBasicTemplate = bw.raw();
        });

        // Sa-Token-SSO 单点登录模块 Bean
        Aop.getAsyn(SaSsoTemplate.class, bw -> {
            SaSsoUtil.saSsoTemplate = bw.raw();
        });

        // 自定义 StpLogic 对象
        Aop.getAsyn(StpLogic.class, bw -> {
            StpUtil.setStpLogic(bw.raw());
        });
    }
}

