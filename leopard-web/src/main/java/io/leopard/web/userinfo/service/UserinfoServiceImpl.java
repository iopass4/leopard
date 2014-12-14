package io.leopard.web.userinfo.service;

import io.leopard.commons.utility.ListUtil;
import io.leopard.web.userinfo.UriListChecker;
import io.leopard.web4j.passport.LoginBox;
import io.leopard.web4j.passport.PassportUser;
import io.leopard.web4j.passport.PassportValidateDao;
import io.leopard.web4j.passport.SessionUtil;
import io.leopard.web4j.servlet.RequestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserinfoServiceImpl implements UserinfoService {

	protected Log logger = LogFactory.getLog(UserinfoServiceImpl.class);

	private static List<String> EXCLUDE_URI_LIST = new ArrayList<String>();
	static {//
		// "/oauth/", "/webservice/", "/monitor/", "/udb/", "/leopard/",
		// "/test/parameter.do"
		EXCLUDE_URI_LIST.add("/oauth/");
		EXCLUDE_URI_LIST.add("/webservice/");
		EXCLUDE_URI_LIST.add("/monitor/");
		EXCLUDE_URI_LIST.add("/passport/");
		EXCLUDE_URI_LIST.add("/leopard/");
		EXCLUDE_URI_LIST.add("/test/parameter.do");

		EXCLUDE_URI_LIST.add("/passport/login.do");
		EXCLUDE_URI_LIST.add("/loging.do");
		EXCLUDE_URI_LIST.add("/security/getCsrfToken.do");
	};

	@Autowired(required = false)
	private PassportValidateDao passportValidateDao;

	@Resource
	private ConfigHandler loginHandler;
	@Resource
	private LoginBox loginBox;

	protected UriListChecker excludeLoginUriListChecker;// 忽略登录的URL列表

	@PostConstruct
	public void init() {
		List<String> excludeUris = loginHandler.getExcludeUris();
		excludeUris = ListUtil.defaultList(excludeUris);

		excludeUris.addAll(EXCLUDE_URI_LIST);
		excludeLoginUriListChecker = new UriListChecker(excludeUris);
	}

	@Override
	public boolean isExcludeUri(HttpServletRequest request) {
		String uri = RequestUtil.getRequestContextUri(request);
		// 不需要登录的URL判断
		boolean isExcludeUri = excludeLoginUriListChecker.exists(uri);
		return isExcludeUri;
	}

	/**
	 * 转到登录页面.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void showLoginBox(HttpServletRequest request, HttpServletResponse response) {
		String ip = RequestUtil.getProxyIp(request);
		String message = "您[" + ip + "]未登录,uri:" + request.getRequestURI();
		logger.info(message);

		loginBox.showLoginBox(request, response);
	}

	@Override
	public Long login(HttpServletRequest request, HttpServletResponse response) {
		PassportUser user = SessionUtil.getUserinfo(request.getSession());
		if (user == null) {
			if (passportValidateDao != null) {
				// user = this.validateAndCache(request, response);
				user = passportValidateDao.validate(request, response);
				if (user != null) {
					SessionUtil.setUserinfo(request, user);
				}
			}
			if (user == null) {
				return null;
			}
		}
		return user.getUid();
	}

	// protected PassportUser validateAndCache(HttpServletRequest request, HttpServletResponse response) {
	// PassportUser user = passportValidateDao.validate(request, response);
	// if (user == null) {
	// return null;
	// }
	// SessionUtil.setUserinfo(request, user);
	// return user;
	// }
}
