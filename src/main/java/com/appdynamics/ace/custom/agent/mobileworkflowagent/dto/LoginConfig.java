package com.appdynamics.ace.custom.agent.mobileworkflowagent.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by stefan.marx on 13.02.17.
 */
public class LoginConfig {
    private String _user;
    private String _passwd;
    private String _account;
    private String _token;

    private String _proxyHost;

    public String getProxyHost() {
        return _proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        _proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return _proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        _proxyPort = proxyPort;
    }

    private int _proxyPort;

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    private String _url;

    public String getUser() {
        return _user;
    }

    public void setUser(String user) {
        _user = user;
    }

    public String getPasswd() {
        return _passwd;
    }

    public void setPasswd(String passwd) {
        _passwd = passwd;
    }

    public String getAccount() {
        return _account;
    }

    public void setAccount(String account) {
        _account = account;
    }

    public String getToken() {
        return _token;
    }

    public void setToken(String token) {
        _token = token;
    }

    public String toString() {
        if (isBasicLogin()) {
            return "Basic Auth: "+getUser();
        }else {
            return "Token Auth: "+getAccount();
        }
    }

    public boolean hasUrl() {
        return _url != null && _url.length()>0;
    }

    @JsonIgnore
    public boolean isBasicLogin() {
        return (_user!=null && _user.length()>0);
    }

    @JsonIgnore
    public boolean isProxyEnabled() {
        return _proxyHost!= null && _proxyHost.length()>0;
    }
}
