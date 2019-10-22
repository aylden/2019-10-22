package com.changgou.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局过滤器，获取令牌，解析判断
 * GlobalFilter
 * Ordered顺序
 * Authorization
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";
    private static final String loginUrl = "http://localhost:9001/oauth/login";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        if (UrlFilter.hasAuthority(request.getURI().toString())) {
            return chain.filter(exchange);
        }
        //从请求头中获取令牌
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        if (StringUtils.isEmpty(token)) {
            //从cookie中获取令牌
            HttpCookie tokenCookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (tokenCookie == null) {
                //从请求参数中获取令牌
                token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
                if (StringUtils.isEmpty(token)) {
                    //没有令牌,设置没有权限 401状态码，响应结束
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set("Location", loginUrl+"?from="+request.getURI().toString());
                    //response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
            } else {
                token = tokenCookie.getValue();
            }
            try {
                //Claims claims = JwtUtil.parseJWT(token);
            } catch (Exception e) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }
        //添加头信息
        request.mutate().header(AUTHORIZE_TOKEN, "bearer " + token);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
