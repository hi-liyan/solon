package org.noear.solon.extend.sessionstate.redis;

import org.noear.redisx.RedisClient;
import org.noear.snack.ONode;
import org.noear.snack.core.Options;
import org.noear.snack.core.Feature;
import org.noear.solon.Utils;
import org.noear.solon.boot.ServerConstants;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.SessionState;

import java.util.Collection;

/**
 * 它会是个单例，不能有上下文数据
 * */
public class RedisSessionState implements SessionState {
    private static int _expiry =  60 * 60 * 2;
    private static String _domain=null;

    static {
        if (SessionProp.session_timeout > 0) {
            _expiry = SessionProp.session_timeout;
        }

        if (SessionProp.session_state_domain != null) {
            _domain = SessionProp.session_state_domain;
        }
    }


    private Context ctx;
    private RedisClient redisClient;
    protected RedisSessionState(Context ctx){
        this.ctx = ctx;
        this.redisClient = RedisSessionStateFactory.getInstance().redisClient();
    }

    //
    // cookies control
    //

    public  String cookieGet(String key){
        return ctx.cookie(key);
    }
    public  void   cookieSet(String key, String val) {
        if (SessionProp.session_state_domain_auto) {
            if (_domain != null) {
                if(ctx.uri().getHost().indexOf(_domain) < 0){ //非安全域
                    ctx.cookieSet(key, val, null, _expiry);
                    return;
                }
            }
        }

        ctx.cookieSet(key, val, _domain, _expiry);
    }

    //
    // session control
    //

    @Override
    public String sessionId() {
        String _sessionId = ctx.attr("sessionId",null);

        if(_sessionId == null){
            _sessionId = sessionIdGet(false);
            ctx.attrSet("sessionId",_sessionId);
        }

        return _sessionId;
    }

    @Override
    public String sessionChangeId() {
        sessionIdGet(true);
        ctx.attrSet("sessionId", null);
        return sessionId();
    }

    @Override
    public Collection<String> sessionKeys() {
        return redisClient.openAndGet((ru) -> ru.key(sessionId()).hashGetAllKeys());
    }

    private String sessionIdGet(boolean reset) {
        String skey = cookieGet(ServerConstants.SESSIONID_KEY);
        String smd5 = cookieGet(ServerConstants.SESSIONID_MD5());

        if(reset == false) {
            if (Utils.isEmpty(skey) == false && Utils.isEmpty(smd5) == false) {
                if (Utils.md5(skey + ServerConstants.SESSIONID_salt).equals(smd5)) {
                    return skey;
                }
            }
        }

        skey = Utils.guid();
        cookieSet(ServerConstants.SESSIONID_KEY, skey);
        cookieSet(ServerConstants.SESSIONID_MD5(), Utils.md5(skey + ServerConstants.SESSIONID_salt));
        return skey;
    }

    @Override
    public Object sessionGet(String key) {
        String json = redisClient.openAndGet((ru) -> ru.key(sessionId()).expire(_expiry).hashGet(key));

        if(json == null){
            return null;
        }

        ONode tmp = ONode.loadStr(json);
        String type = tmp.get("t").getString();
        ONode data = tmp.get("d");


        try {
            switch (type){
                case "Null":return null;
                case "Short":return data.val().getShort();
                case "Integer":return data.val().getInt();
                case "Long":return data.val().getLong();
                case "Float":return data.val().getFloat();
                case "Double":return data.val().getDouble();
                case "Date":return data.val().getDate();
                case "Boolean":return data.val().getBoolean();
                default:return data.toObject();
            }

        }catch (Exception ex){
            throw new RuntimeException("Session state deserialization error: "+ key + " = " + json);
        }
    }

    @Override
    public void sessionSet(String key, Object val) {
        if (val == null) {
            sessionRemove(key);
        } else {
            ONode tmp = new ONode();
            try {
                tmp.set("t", val.getClass().getSimpleName());
                tmp.set("d", ONode.loadObj(val, Options.serialize().remove(Feature.BrowserCompatible)));
            } catch (Exception ex) {
                throw new RuntimeException("Session state serialization error: " + key + " = " + val);
            }

            String json = tmp.toJson();

            redisClient.open((ru) -> ru.key(sessionId()).expire(_expiry).hashSet(key, json));
        }
    }

    @Override
    public void sessionRemove(String key) {
        redisClient.open((ru) -> ru.key(sessionId()).expire(_expiry).hashDel(key));
    }

    @Override
    public void sessionClear() {
        redisClient.open((ru)->ru.key(sessionId()).delete());
    }

    @Override
    public void sessionReset() {
        sessionClear();
        sessionChangeId();
    }

    @Override
    public void sessionRefresh() {
        String skey = cookieGet(ServerConstants.SESSIONID_KEY);

        if (Utils.isEmpty(skey) == false) {
            cookieSet(ServerConstants.SESSIONID_KEY, skey);
            cookieSet(ServerConstants.SESSIONID_MD5(), EncryptUtil.md5(skey + ServerConstants.SESSIONID_salt));

            redisClient.open((ru)->ru.key(sessionId()).expire(_expiry).delay());
        }
    }


    @Override
    public boolean replaceable() {
        return false;
    }
}
