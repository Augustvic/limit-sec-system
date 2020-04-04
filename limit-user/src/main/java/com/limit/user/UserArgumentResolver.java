package com.limit.user;

import com.limit.user.access.UserContext;
import com.limit.user.entity.SeckillUser;
import com.limit.user.service.SeckillUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

	@Autowired
    SeckillUserService userService;

	/**
	 * 判断参数类型是否是 SeckillUser
	 * @param parameter 传入参数
	 * @return 参数类型是 SeckillUser 返回 true
	 */
	public boolean supportsParameter(MethodParameter parameter) {
		// 获取参数类型
		Class<?> clazz = parameter.getParameterType();
		return clazz== SeckillUser.class;
	}

	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		return UserContext.getUser();
	}
}
