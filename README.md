# shibboleth-mfa-totp
A module for implementing TOTP on Shibboleth IdP.

## Description
* Incorporating this one-time password module into the existing Shibboleth IdP, multi-factor authentication can be used with Shibboleth federation authentication.
* You can set whether to use one-time password authentication for each user. Therefore, it is necessary to store the attribute information that allows one-time password authentication in the LDAP used by Shibboleth IdP.
* Google Authenticator and MS Multi-Factor Authentication can be used as tokens to generate one-time passwords, so personal smartphones can be used as software tokens.
* The private key for the one-time password is encrypted and stored in LDAP using the AES encryption method. We are planning to provide PAM (Pluggable Authentication Modules) and Apache authentication modules that can perform one-time password authentication using this private key.


## Requirement
* The Shibboleth IdP has already been built and login authentication using the user ID and password is possible.
* The version of Shibboleth IdP is 3.4.6.
* Single sign-on to the service provider is available.
* "Extraterritorial Control" of the Java runtime environment of the application server to which Shibboleth is deployed must have been lifted.
* The HTTP request parameter name posted from the Shibboleth IdP login matches the "c: _0" attribute value of the following bean id defined in "${IDP_HOME}/conf/authn/totp-authn-config.xml". If they are different, you need to change posted value or value of "IDP_HOME /conf/authn/totp-authn-config.xml" to match both.

|  input  |  attribute  |
| ---- | ---- |
|  user namne  |  com.sios.idp.shibboleth.authn.Totp.UsernameFieldName  |
|  TOTP  |  com.sios.idp.shibboleth.authn.Totp.TotpFieldName  |

## Usage
###  Private key generation script
This module requires private key. mksecret is a script that generates private key. Ruby is required for the script to work. Set private key before encrypted to Authenticator and after encrypted to LDAP.

```
$ ./mksecret -m -p hoge
パスフレーズ: hoge
事前共有鍵 (暗号化前データ): { 0xA5, 0x96, 0xC0, 0xCF, 0xC0, 0x0B, 0xC8, 0x61, 0xAF, 0x5D }
事前共有鍵 (暗号化前文字列): UWLMBT6ABPEGDL25
事前共有鍵 (暗号化後文字列): NmCO52yi2rj+ysfkxEEcML/O/aEHdymin3cWdnwUzFOMlxLZAWUwrYGNz2iOlP34K10UocSxc0EKxC7if4qXkw==
```
### Build
You need maven and java 11 to build this module. 
This command will generate shibboleth-mfa-totp-X.X.X.jar in the target directory.
```
$ mvn clean package
```

## Installation
### Download
Download "shibboleth-mfa-totp-X.X.X-package.zip".
### File placement
```
$ unzip shibboleth-mfa-totp-X.X.X-package.zip
```

| Component |
| ---- |
|shibboleth-mfa-totp-X.X.X.zip|
| > IDP_HOME/flows/authn/Totp/totp-authn-beans.xml |
| > IDP_HOME/flows/authn/Totp/totp-authn-flow.xml |
| > IDP_HOME/conf/authn/jaas-authn-config-totp.xml |
| > IDP_HOME/conf/authn/totp-authn-config.xml |
| > IDP_HOME/views/totplogin.vm |
| > shibboleth-mfa-totp-X.X.X.jar |


```
$ mkdir ${IDP_HOME}/flows/authn/Totp
$ cp IDP_HOME/flows/authn/Totp/totp-authn-beans.xml ${IDP_HOME}/flows/authn/Totp/
$ cp IDP_HOME/flows/authn/Totp/totp-authn-flow.xml ${IDP_HOME}/flows/authn/Totp/
$ cp IDP_HOME/conf/authn/jaas-authn-config-totp.xml ${IDP_HOME}/conf/authn/
$ cp IDP_HOME/conf/authn/totp-authn-config.xml ${IDP_HOME}/conf/authn/
$ cp IDP_HOME/views/totplogin.vm ${IDP_HOME}/views/

```
### Common settings
```
$ vim ${IDP_HOME}/conf/authn/general-authn.xml
// ...

<bean id="authn/Totp" parent="shibboleth.AuthenticationFlow" p:passiveAuthenticationSupported="true" p:forcedAuthenticationSupported="true" >
+    <property name="supportedPrincipals">
+        <list>
+            <bean parent="shibboleth.SAML2AuthnContextClassRef" c:classRef="urn:oasis:names:tc:SAML:2.0:ac:classes:Totp" />
+        </list>
+    </property>
</bean>

<bean id="authn/MFA" parent="shibboleth.AuthenticationFlow"     p:passiveAuthenticationSupported="true" p:forcedAuthenticationSupported="true">

// ...

        <bean parent="shibboleth.SAML1AuthenticationMethod" c:method="urn:oasis:names:tc:SAML:1.0:am:password" />

+       <bean parent="shibboleth.SAML2AuthnContextClassRef" c:classRef="urn:oasis:names:tc:SAML:2.0:ac:classes:Totp" />
    </list>

// ...
```

```
vim ${IDP_HOME}/conf/authn/authn-events-flow.xml

// ...

+ <!--
<end-state id="MyCustomEvent" />

<global-transitions>
     <transition on="MyCustomEvent" to="MyCustomEvent" />
</global-transitions>
+ -->


+    <action-state id="UnknownUsername">
+        <evaluate expression="'proceed'" />
+        <transition on="proceed" to="DisplayUsernameTotpPage" />
+   </action-state>

// ...

```

If this file does not exist, copy ${IDP_HOME}/webapp/WEB-INF/web.xml to ${IDP_HOME}/edit-webapp/WEB-INF before editing.
```
vim ${IDP_HOME}/edit-webapp/WEB-INF/web.xml

// ...

<listener>
     
+   <listener-class>com.sios.idp.shibboleth.common.TotpAuthnServletContextListener</listener-class>

</listener>

// ...

```

```
vim ${IDP_HOME}/conf/idp.properties

// ...

- idp.authn.flows=Password
+ idp.authn.flows=MFA

// ...

- #idp.authn.favorSSO = false
+ idp.authn.favorSSO = false

// ...
```

```
vim ${IDP_HOME}/conf/authn/totp-authn-config.xml

// ...

+    <util:list id="shibboleth.authn.Password.PrincipalOverride">
+         <bean parent="shibboleth.SAML2AuthnContextClassRef" c:classRef="urn:    oasis:names:tc:SAML:2.0:ac:classes:Totp" />
+    </util:list>

</beans>
```

```
vim ${IDP_HOME}/system/conf/general-authn-system.xml

// ...

        <entry key="urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified" value="124" />
+       <entry key="urn:oasis:names:tc:SAML:2.0:ac:classes:Totp" value="125" />

// ...

```
```
vim ${IDP_HOME}/conf/relying-party.xml

// ...

+<!-- LDAP認証の認証結果を表すBeanを定義する-->
+<bean id="PasswordPrincipal" parent="shibboleth.SAML2AuthnContextClassRef" c:classRef="urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport" />

+<!-- TOTP認証の認証結果を表すBeanを定義する-->
+<bean id="TotpPrincipal" parent="shibboleth.SAML2AuthnContextClassRef" c:classRef="urn:oasis:names:tc:SAML:2.0:ac:classes:Totp" />

<util:list id="shibboleth.RelyingPartyOverrides">

+       <bean parent="RelyingPartyByName" c:relyingPartyIds="urn:federation:MicrosoftOnline">
            <property name="profileConfigurations">
+               <list>
+                     <bean parent="SAML2.SSO" p:encryptAssertions="false" p:signAssertions="true" p:signResponses="false" p:encryptNameIDs="false" p:disallowedFeatures-ref="SAML2.SSO.FEATURE_AUTHNCONTEXT">
+                         <property name="defaultAuthenticationMethods">
+                             <list>
+                                 <ref bean="TotpPrincipal" />
+                                 <ref bean="PasswordPrincipal" />
+                             </list>
+                         </property>
+                     </bean>
+                     <bean parent="SAML2.ECP" p:encryptAssertions="false" p:signAssertions="true" p:signResponses="false" p:nameIDFormatPrecedence="ur
n:oasis:names:tc:SAML:2.0:nameid-format:persistent" />
+               </list>
+           </property>
+       </bean>
    </util:list>

// ...
```
```
vim ${IDP_HOME}/conf/authn/mfa-authn-config.xml

// ...

<util:map id="shibboleth.authn.MFA.TransitionMap">
    <!-- Run authn/Flow1 first. -->
    <entry key="">
-        <bean parent="shibboleth.authn.MFA.Transition" p:nextFlow="authn/IPAddress" />
+        <bean parent="shibboleth.authn.MFA.Transition" p:nextFlow="authn/Password" />
    </entry>

    <!-- If that returns "proceed", run authn/Flow2 next. -->
-    <entry key="authn/IPAddress">
+    <entry key="authn/Password">
-        <bean parent="shibboleth.authn.MFA.Transition" p:nextFlowStrategy-ref="checkSecondFactor" />
+        <bean parent="shibboleth.authn.MFA.Transition" p:nextFlowStrategy-ref="checkTotpFlow" />
    </entry>

    <!-- An implicit final rule will return whatever the second flow returns. -->
</util:map>

- <bean id="checkSecondFactor" parent="shibboleth.ContextFunctions.Scripted" factory-method="inlineScript" >
+ <bean id="checkTotpFlow" parent="shibboleth.ContextFunctions.Scripted" factory-method="inlineScript" p:customObject-ref="secondFactorHelpersMap">
    <constructor-arg>
        <value>
-          <![CDATA[
-               nextFlow = "authn/Password";
-                
-               // Check if second factor is necessary for request to be sat    isfied.
-               authCtx = input.getSubcontext("net.shibboleth.idp.authn.cont    ext.AuthenticationContext");
-               mfaCtx = authCtx.getSubcontext("net.shibboleth.idp.authn.con    text.MultiFactorAuthenticationContext");
-               if (mfaCtx.isAcceptable()) {
-                    nextFlow = null;
-               }
-
-               nextFlow;   // pass control to second factor or end with the     first
-            ]]>    
+        <![CDATA[
+           logger = Java.type("org.slf4j.LoggerFactory").getLogger("net.shibboleth.idp.authn.impl.TransitionMultiFactorAuthentication");
+               nextFlow = "authn/Totp";
+               allowIp = "133.101."
+               httpRequestServletContext = custom.get("httpServletRequestContext");

+               // ログインユーザー名を取得する
+               usernameLookupStrategyClass = Java.type("net.shibboleth.idp.session.context.navigate.CanonicalUsernameLookupStrategy");
+               usernameLookupStrategy = new usernameLookupStrategyClass();
+               username = usernameLookupStrategy.apply(input);

+               // 30 日信頼処理を定義している Bean を取得し、ユーザー名より認証省力有効期間内かどうかをチェックする
+               checker = custom.get("totpAuthnSessionChecker");
+               result = checker.isValidTotpAuthnSession(username);

+               // Chack IPAddress
                if (httpRequestServletContext.requestURI.equals("/idp/profile/SAML2/SOAP/ECP")) {
+                   nextFlow = null;
+               } else if (httpRequestServletContext.remoteAddr.startsWith(allowIp)) {
+                   nextFlow = null;
+               } else {
+                   // TOTP 利用フラグ(mfaTotpAuthnUseFlag)を LDAP から取得する。
+                   resCtx = input.getSubcontext("net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext",true);
+                   resCtx.setPrincipal(usernameLookupStrategy.apply(input));
+                   resCtx.getRequestedIdPAttributeNames().add("mfaTotpAuthnUseFlag");
+                   resCtx.resolveAttributes(custom.get("attributeResolver"));
+                   attribute = resCtx.getResolvedIdPAttributes().get("mfaTotpAuthnUseFlag");
+                   valueType = Java.type("net.shibboleth.idp.attribute.StringAttributeValue");

+                   if (attribute == null) {
+                      nextFlow = null;
+                      } else {
+                      if (attribute.getValues().contains(new valueType("1"))) {
+                         if (result) {
+                         // 30 日信頼が有効期間内の場合、TOTP 認証を省略する
+                         nextFlow = null;
+                      } else {
+                         // 30 日信頼が有効期間外の場合、TOTP 認証を行う
+                         nextFlow = "authn/Totp";
+                      }
+                    }else{
+　　　　　　　          nextFlow = null;//追加
+                    }
+                  }
+                 input.removeSubcontext(resCtx);
+                }
+               nextFlow;
+           ]]>
        </value>
    </constructor-arg>
</bean>

+<bean id="TotpAuthnSessionChecker" class="com.sios.idp.shibboleth.common.TotpAuthnSessionChecker" scope="prototype" p:httpServletRequest-ref="shibboleth.HttpServletRequest" />
+    <util:map id="secondFactorHelpersMap">
+       <entry key="attributeResolver" value-ref="shibboleth.AttributeResolverService" />
+       <entry key="httpServletRequestContext" value-ref="shibboleth.HttpServletRequest" />
+        <entry key="totpAuthnSessionChecker" value-ref="TotpAuthnSessionChecker" />
+    </util:map>

```
### Application settings

| Configuration files |
| ---- |
| ${IDP_HOME}/edit-webapp/WEB-INF/web.xml |
| ${IDP_HOME}/conf/authn/jaas.config |
| ${IDP_HOME}/conf/logback.xml |

#### Main configuratoin
```
vim ${IDP_HOME}/edit-webapp/WEB-INF/web.xml

<context-param>
        <param-name>ldapUrl</param-name>
        <param-value>ldap://ccldap06.kyoto-su.ac.jp ldap://ccldap07.kyoto-su.ac.jp</param-value>
    </context-param>
    <context-param>
        <param-name>ldapBindDN</param-name>
        <param-value>cn=ldapReadUser,dc=kyoto-su,dc=ac,dc=jp</param-value>
    </context-param>
    <context-param>
        <param-name>ldapBindPassword</param-name>
        <param-value>erudappuYomikomiOnri-</param-value>
    </context-param>
    <context-param>
        <param-name>ldapBaseDN</param-name>
        <param-value>ou=cc,dc=kyoto-su,dc=ac,dc=jp</param-value>
    </context-param>
    <context-param>
        <param-name>ldapFilter</param-name>
         <param-value>uid={0}</param-value>
    </context-param>
    <context-param>
        <param-name>cipherArgorithm</param-name>
        <param-value>AES</param-value>
    </context-param>
    <context-param>
        <param-name>saltLength</param-name>
        <param-value>32</param-value>
    </context-param>
    <context-param>
        <param-name>iterationCount</param-name>
        <param-value>1500</param-value>
    </context-param>
    <context-param>
        <param-name>cipherKeyLength</param-name>
        <param-value>256</param-value>
    </context-param>
    <context-param>
        <param-name>cipherTransformationName</param-name>
        <param-value>AES/CBC/PKCS5Padding</param-value>
    </context-param>
    <context-param>
        <param-name>immutableIdAttributeName</param-name>
        <param-value>forwardMail</param-value>
    </context-param>
    <!-- 認証セッション ID の Cookie 名 -->
    <context-param>
       <param-name>totpAuthnSessionIdCookieName</param-name>
       <param-value>totp-session-id</param-value>
    </context-param>
    <!--処理日時の Cookie 名 -->
    <context-param>
       <param-name>totpAuthnSessionIssueDateCookieName</param-name>
       <param-value>totp-session-time</param-value>
    </context-param>
    <!--認証セッションの信頼の有効期限 -->
    <context-param>
       <param-name>totpAuthnSessionExpirationSec</param-name>
       <param-value>2592000</param-value>
    </context-param>
    <!--認証セッション ID 生成時に付与するソルト文字列 -->
    <context-param>
       <param-name>totpAuthnSessionIdSalt</param-name>
       <param-value>sios-totp-test</param-value>
    </context-param>
    <context-param>
        <param-name>totpFlagAttributeName</param-name>
        <param-value>mfaTotpAuthnUseFlag</param-value>
    </context-param>
    <context-param>
        <param-name>secretKeyAttributeName</param-name>
        <param-value>mfaTotpSharedSecret</param-value>
    </context-param>
    <context-param>
        <param-name>secretKeyPassPhrase</param-name>
        <param-value>i6&amp;ntbvnfsziw)okjlwkhH9uOdb/cbb4wzdmwtcjhy^o(skvcz9wkbHBxn^d~nkjqlias%koptuvixwtrogr-eopw~qe76Yxnrfyz</p
aram-value>
    </context-param>
    <context-param>
        <param-name>daoImplClassName</param-name>
        <param-value>com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl</param-value>
    </context-param>
    <context-param>
        <param-name>timeStepSec</param-name>
        <param-value>30</param-value>
    </context-param>
    <context-param>
        <param-name>totpLength</param-name>
        <param-value>6</param-value>
    </context-param>
    <context-param>
        <param-name>allowedTimeCountOffset</param-name>
        <param-value>1</param-value>
    </context-param>
     <context-param>
        <param-name>totpCacheExpirationBufferSec</param-name>
        <param-value>30</param-value>
    </context-param>
```

The settings of the one-time authentication module are based on the assumption that Google Authenticator will be used as the one-time password issuing device.
| parameter | Details | example |
| ---- | ---- | ---- |
| ldapUrl [String] | Host name or IP address and port number of the destination directory server | ldap://ccldap06.kyoto-su.ac.jp <br>ldap://ccldap07.kyoto-su.ac.jp |
| ldapBindDN [String] | BindDN used when connecting to a directory server | cn=ldapReadUser,dc=kyoto-su,dc=ac,dc=jp |
| ldapBindPassword [String] | Bind Password used when connecting to the directory server | (*****)
| ldapBaseDN [String] | BaseDN used when searching a directory server | ou=cc,dc=kyoto-su,dc=ac,dc=jp |
| ldapFilter [String] | LDAP filter used when searching directory servers | uid={0} |
| cipherArgorithm [String] | Algorithm used when encrypting secret key information | AES |
| saltLength [Integer] | The length of Salt used internally when encrypting the secret key | 32 |
| iterationCount [Integer] | Number of repetitions of hash function used internally when encrypting secret key information | 1500 |
| cipherKeyLength [Integer] | The length of the key used when encrypting the secret key information | 256 |
| cipherTransformationName [String] | Processing method used when secret key was encrypted | AES/CBC/PKCS5Padding |
| immutableIdAttributeName [String] | LDAP attribute that uniquely identifies a one-time password authentication user | forwardMail |
| totpAuthnSessionIdCookieName [String] | Authentication session ID cookie name | totp-session-id |
| totpAuthnSessionIssueDateCookieName [String] | Processing date and time cookie name | totp-session-time |
| totpAuthnSessionExpirationSec [Integer] | Authentication session trust expiration date | 2592000 |
| totpAuthnSessionIdSalt [String] | Salt string given when generating the authentication session ID | sios-totp-test |
| totpFlagAttributeName [String] | LDAP attribute of availability information for one-time password authentication<br>1 : available<br>other : unavailable | mfaTotpAuthnUseFlag |
| secretKeyAttributeName [String] | LDAP attribute of secret key used for one-time password authentication | mfaTotpSharedSecret |
| secretKeyPassPhrase [String] | Passphrase used to encrypt secret key | (*****) |
| daoImplClassName [String] | DAO implementation class used when connecting to the backend database server | com.sios.idp.shibboleth.datasource.dao.LdapDaoImpl |
| timeStepSec [Integer] | The available time of the one-time password. This value should match the settings of the device (software) that issues the one-time password. | 30 |
| totpLength [Integer] | One-time password length. This value should match the settings of the device (software) that issues the one-time password.  | 6 |
| allowedTimeCountOffset [Integer] | Number of TimeSteps allowed for one-time password authentication | 1 |
| totpCacheExpirationBufferSec [Integer] | Specifies the number of buffer seconds to cache the one-time password. The actual cache time is calculated by the following formula.<br> timeStepSec x (allowedTimeCountOffset + 1) + totpCacheExpirationBufferSec | 30 |
#### JAAS configuration
```
vim ${IDP_HOME}/conf/authn/jaas.config

ShibUserPassAuth {
    org.ldaptive.jaas.LdapLoginModule required
      ldapUrl="ldap://10.1.2.164:389"
      baseDn="ou=IT,ou=Users,dc=ldap,dc=local"
      bindDn="cn=admin,dc=ldap,dc=local"
      bindCredential="password"
      useStartTLS="false"
      userFilter="cn={user}"
      subtreeSearch="true";
};

TOTP {
     com.sios.idp.shibboleth.authn.jaas.TotpLoginModule required
         TotpProviderClass="com.sios.idp.shibboleth.authn.totp.TotpProviderImpl";
 };
```
* JAAS login configuration "ShibUserPassAuth" is a setting used to authenticate users who do not use one-time password authentication.
* JAAS login configuration The setting item specifications of "ShibUserPassAuth" conform to the specifications of the Shibboleth standard LDAP password login module. See the org.ldaptive.jaas.LdapLoginModule documentation. http://www.ldaptive.org/docs/guide/jaas.html
* JAAS login configuration "TOTP" is a setting used to authenticate users who use one-time password authentication.
* For the setting item specifications of the Shibboleth standard LDAP password login module "org.ldaptive.jaas.LdapLoginModule", refer to the "org.ldaptive.jaas.LdapLoginModule" document. http://www.ldaptive.org/docs/guide/jaas.html

| parameter | Details | example |
| ---- | ---- | ---- |
| TotpProviderClass [String] | Specifies the fully qualified name of the TOTP-provided class. The classpath to the specified class must be in place. The implementation of the TOTP provided class can be replaced, but in principle it is not necessary to change it. | "com.sios.idp.shibboleth.authn.totp.TotpProviderImpl" |


#### Log level configuration
```
vim ${IDP_HOME}/conf/logback.xml

// ...

+   <logger name="com.sios.idp.shibboleth" level="INFO" />
</configuration>
```
| level |
| ---- |
| DEBUG |
|INFO|
|WARN|
|ERROR|
DEBUG outputs the authentication information to the log. We recommend a log level below INFO.

### Deploy
Customize the Shibboleth IdP under /opt/shibboleth-idp/edit-webapp, and rebuild and deploy the idp.war with the Shibboleth standard build script.
It is not necessary to reflect it in /usr/share/tomcat/webapps/idp.
```
cp shibboleth-mfa-totp-X.X.X.jar ${IDP_HOME}/edit-webapp/WEB-INF/lib
```

```
${IDP_HOME}/bin/build.sh
Buildfile: ${IDP_HOME}/bin/build.xml

build-war:
Installation Directory: [${IDP_HOME}] ?

INFO [net.shibboleth.idp.installer.BuildWar:72] - Rebuilding ${IDP_HOME}/war/idp.war, Version X.X.X
INFO [net.shibboleth.idp.installer.BuildWar:81] - Initial populate from ${IDP_HOME}/dist/webapp to ${IDP_HOME}/webpapp.tmp
INFO [net.shibboleth.idp.installer.BuildWar:90] - Overlay from ${IDP_HOME}/edit-webapp to ${IDP_HOME}/webpapp.tmp
INFO [net.shibboleth.idp.installer.BuildWar:99] - Creating war file ${IDP_HOME}/war/idp.war

BUILD SUCCESSFUL
Total time: 6 seconds
```

```
cp ${IDP_HOME}/war/idp.war /usr/local/tomcat/webapps/idp.war
```

```
systemctl restart tomcat.service
```


### Edit vm template
```
vim  ${IDP_HOME}/views/totplogin.vm

// ...

+ #set ($class_up = '')
+ #set ($up = $class_up.class.forName("net.shibboleth.idp.authn.principal.UsernamePrincipal"))
+ #set ($authenticatedResult = $authenticationContext.class.forName('net.shibboleth.idp.authn.AuthenticationResult').cast($authenticationContext.getActiveResults().get('authn/Password')))
+ #set ($mfaAuthenticatedResult = $authenticationContext.getSubcontext("net.shibboleth.idp.authn.context.MultiFactorAuthenticationContext").getActiveResults().get('authn/Password'))
+ #if ($authenticatedResult)
  #set ($userId = $authenticatedResult.getSubject().getPrincipals($up).iterator().next().getName())
+ #elseif($mfaAuthenticatedResult)
  #set ($userId = $mfaAuthenticatedResult.getSubject().getPrincipals($up).iterator().next().getName())
#end

// ...

 #if ($passwordEnabled)
 +      <input type="hidden" name="j_username" value="#if($userId)$encoder.encodeForHTML($userId)#end" />

// ...

-               <div class="form-element-wrapper">
-                   <label for="username">#springMessageText("idp.login.use    rname", "Username")</label>
-                   <input class="form-element form-field" id="username" name="    j_username" type="text" value="#if($username)$encoder.encodeForHTML($username)#    end" />
- <div class="form-element-wrapper">
-               </div>
-               <div class="form-element-wrapper">
-                   <label for="password">#springMessageText("idp.login.passwor    d", "Password")</label>                                    
-                   <input class="form-element form-field" id="password" name="    j_password" type="password" value="" />
-               </div>
+               <div class="form-element-wrapper">
+                   <label for="token">#springMessageText("idp.login.totp", "Totp")</label>
+                   <input class="form-element form-field" id="token" name="j_token" type="te    xt" value="">
+               </div>

// ...
            #end

+           <div class="form-element-wrapper">
+               <input type="checkbox" name="truststotpauthnsession" value="1" id="truststotpauthnsession">
+               <label for="truststotpauthnsession">#springMessageText("idp.login.truststotpauthnsession", "Trust this device for 30 days") </label>
+           </div>

```
### Shibboleth IdP settings
```
vim  ${IDP_HOME}/conf/attribute-resolver.xml

// ...

    <AttributeDefinition id="mfaTotpAuthnUseFlag" xsi:type="Simple">
	<InputDataConnector ref="myLDAP" attributeNames="mfaTotpAuthnUseFlag" />
        <AttributeEncoder xsi:type="SAML1String" name="urn:mace:dir:attribute-def:mfaTotpAuthnUseFlag" encodeType="false" />
        <AttributeEncoder xsi:type="SAML2String" name="urn:oid:1.3.6.1.4.1.32216.1.1.3.1.1.1" friendlyName="mfaTotpAuthnUseFlag" encodeType="false" />
    </AttributeDefinition>

    <AttributeDefinition id="mfaTotpImmutableId" xsi:type="Simple">
	<InputDataConnector ref="myLDAP" attributeNames="mfaTotpImmutableId" />
        <AttributeEncoder xsi:type="SAML1String" name="urn:mace:dir:attribute-def:mfaTotpImmutableId" encodeType="false" />
        <AttributeEncoder xsi:type="SAML2String" name="urn:oid:1.3.6.1.4.1.32216.1.1.3.1.1.2" friendlyName="mfaTotpImmutableId" encodeType="false" />
    </AttributeDefinition>

    <AttributeDefinition id="mfaTotpSharedSecret" xsi:type="Simple">
	<InputDataConnector ref="myLDAP" attributeNames="mfaTotpSharedSecret" />
        <AttributeEncoder xsi:type="SAML1String" name="urn:mace:dir:attribute-def:mfaTotpSharedSecret" encodeType="false" />
        <AttributeEncoder xsi:type="SAML2String" name="urn:oid:1.3.6.1.4.1.32216.1.1.3.1.1.3" friendlyName="mfaTotpSharedSecret" encodeType="false" />
    </AttributeDefinition>

    <AttributeDefinition id="mfaTotpSubEmail" xsi:type="Simple">
	<InputDataConnector ref="myLDAP" attributeNames="mfaTotpSubEmail" />
        <AttributeEncoder xsi:type="SAML1String" name="urn:mace:dir:attribute-def:mfaTotpSubEmail" encodeType="false" />
        <AttributeEncoder xsi:type="SAML2String" name="urn:oid:1.3.6.1.4.1.32216.1.1.3.1.1.4" friendlyName="mfaTotpSubEmail" encodeType="false" />
    </AttributeDefinition>


// ...

```

## License
This project is licensed under the Apache2.0 License.

## Author

SIOS Technology, Inc.

web site    :   https://sios.jp/

GitHub      :   https://github.com/SIOS-Technology-Inc
