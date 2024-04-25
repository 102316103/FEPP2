package com.syscom.fep.gateway.netty;

import com.syscom.fep.frmcommon.ssl.X509MultiKeyManager;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.Principal;
import java.util.List;

public class NettyTransmissionSslAliasX509MultiKeyManager extends X509MultiKeyManager {
    private final String alias;

    public NettyTransmissionSslAliasX509MultiKeyManager(List<X509KeyManager> x509KeyManagerList, String alias) {
        super(x509KeyManagerList);
        this.alias = alias;
    }

    /**
     * @param keyType the key algorithm type name
     * @param issuers the list of acceptable CA issuer subject names
     *                or null if it does not matter which issuers are used.
     * @return
     */
    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        if (StringUtils.isBlank(alias)) {
            return super.getServerAliases(keyType, issuers);
        }
        return new String[]{this.chooseEngineServerAlias(keyType, issuers, null)};
    }

    /**
     * @param keyType the key algorithm type name.
     * @param issuers the list of acceptable CA issuer subject names
     *                or null if it does not matter which issuers are used.
     * @param socket  the socket to be used for this connection.  This
     *                parameter can be null, which indicates that
     *                implementations are free to select an alias applicable
     *                to any socket.
     * @return
     */
    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        if (StringUtils.isBlank(alias)) {
            return super.chooseServerAlias(keyType, issuers, socket);
        }
        return this.chooseEngineServerAlias(keyType, issuers, null);
    }

    /**
     * @param keyType the key algorithm type name.
     * @param issuers the list of acceptable CA issuer subject names
     *                or null if it does not matter which issuers are used.
     * @param engine  the <code>SSLEngine</code> to be used for this
     *                connection.  This parameter can be null, which indicates
     *                that implementations of this interface are free to
     *                select an alias applicable to any engine.
     * @return
     */
    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        if (StringUtils.isBlank(alias)) {
            return super.chooseEngineServerAlias(keyType, issuers, engine);
        }
        return this.alias;
    }
}
