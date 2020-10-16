package webapp.demoa_interceptor;

import org.noear.solon.core.XInterceptorChain;
import org.noear.solon.core.XInterceptor;
import org.noear.solon.core.MethodHolder;
import org.noear.solon.core.XContext;

public class AroundHandler implements XInterceptor {

    @Override
    public Object doIntercept(Object obj, MethodHolder mH, Object[] args, XInterceptorChain invokeChain) throws Throwable {
        XContext.current().output("@XAround:我也加一点；");
        return invokeChain.doInvoke(obj, args);
    }
}
