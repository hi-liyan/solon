package demo;

import demo.dso.service.UserService;
import org.apache.ibatis.session.Configuration;
import org.noear.solon.SolonBuilder;
import org.noear.solon.core.Aop;

/**
 * @author noear 2021/7/12 created
 */
public class DemoApp {
    public static void main(String[] args) {
        new SolonBuilder()
                .onEvent(Configuration.class, e -> {
                    //添加 mybatis-plug 分页支持
                    //
//                    MybatisPlusInterceptor plusInterceptor = new MybatisPlusInterceptor();
//                    plusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
//                    e.addInterceptor(plusInterceptor);
                })
                .onPluginLoadEnd(e -> {
                    //重新定义 SqlSessionFactoryBuilder
                    //Aop.wrapAndPut(MybatisSqlSessionFactoryBuilder.class, new MybatisSqlSessionFactoryBuilderImpl());
                })
                .start(DemoApp.class, args);

        //test
        UserService userService = Aop.get(UserService.class);
        assert userService.getUserList() != null;
    }
}
