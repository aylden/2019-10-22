package com.changgou.token;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

/*****
 * @Author:
 * @Date: 2019/7/7 13:48
 * @Description: com.changgou.token
 *  使用公钥解密令牌数据
 ****/
public class ParseJwtTest {

    /***
     * 校验令牌
     */
    @Test
    public void testParseToken(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfVklQLFJPTEVfVVNFUiIsIm5hbWUiOiJpdGhlaW1hIiwiaWQiOiIxIn0.CG6ZmV-4iaG88Q0xwXkCNSBQIny2RhBrWCxNGgTxqLMtuu_o72RmW4d1-uiVldBsn5_c9cVW1-wWE_rLY5reac8PJMpxoAi5IqvvAERBKi3uZTs1AXK8pGnAtl8F2CYzv6uhjZ8FNBLEtST4ipoKRVM4-FQtOngfvDcMPwsr1Xeox8yV6vC3lgl97wDIjMGMTdtJXLdgp7FFz8EvtxyxQ9WDf99MbupTUlMlBh88irqTRLBO7K8ZtZ_3Tio4Ezvjxa4yqEYRRQo7CXSIjJ669ya2OBEFfEHNmT45TLHFJ-YPrU8eaMdPV9GZBlpG5kVvfburSS0aaSqIiZoWyZ0B0Q";

        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjn1KhXFejTbLWkSGzaEg7e3PesZxi44UOftkCoDcFtnqaoSMW9miP1NYVOqEYcWkvyM3KlQDv8lj+PvG/apk37Z3g27xUPue6AJRkmbJCNAS/K5n6k9M//kXFM6IbVeOZssgmVqYfrXTEYVCHcKjC4G6EPJLzy8iKTXLDsUdfW2PR00eWIVpM905KmEINzIGSbPZDn0G9gD8vxYrnkl+MSqvXRjbjgKn4+9FMZ8sRum/C4vhFtu/+HCHn++zQpiViX9z0IayRkMafzyLHw08q7v4AN2PWnrr4q0HJnxGB1n+BHNnJxKBzXIUu4euoYWV4eqPXwyq1VxEPqb4ErALwwIDAQAB-----END PUBLIC KEY-----";

        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        //获取Jwt原始内容 载荷
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
