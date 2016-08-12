package com.favorites.web;

import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.favorites.comm.Const;
import com.favorites.domain.Collect;
import com.favorites.domain.CollectRepository;
import com.favorites.domain.Favorites;
import com.favorites.domain.FavoritesRepository;
import com.favorites.domain.User;
import com.favorites.domain.UserRepository;
import com.favorites.domain.result.ExceptionMsg;
import com.favorites.domain.result.Response;
import com.favorites.utils.DateUtils;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController{
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CollectRepository collectRepository;
	@Autowired
	private FavoritesRepository favoritesRepository;
 
	@RequestMapping(value="/login",method=RequestMethod.POST)
	public Response login(User user) {
		logger.info("login begin, param is "+user);
		try {
			User loginUser=userRepository.findByUserNameOrEmail(user.getUserName(), user.getEmail());
			if(loginUser==null || !loginUser.getPassWord().equals(getPwd(user.getPassWord()))){
				return result(ExceptionMsg.LoginNameOrPassWordError);
			}
			getSession().setAttribute(Const.LOGIN_SESSION_KEY, loginUser);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("login failed, ",e);
			return result(ExceptionMsg.FAILED);
		}
		return result();
	}
	
	@RequestMapping(value="/regist",method=RequestMethod.POST)
	public Response create(User user) {
		logger.info("create user begin, param is "+user);
		try {
			User registUser = userRepository.findByEmail(user.getEmail());
			if(null != registUser){
				return result(ExceptionMsg.EmailUsed);
			}
			User userNameUser = userRepository.findByUserName(user.getUserName());
			if(null != userNameUser){
				return result(ExceptionMsg.UserNameUsed);
			}
			user.setPassWord(getPwd(user.getPassWord()));
			user.setCreateTime(DateUtils.getCurrentTime());
			user.setLastModifyTime(DateUtils.getCurrentTime());
			userRepository.save(user);
			Favorites favorites = new Favorites();
			favorites.setName("未读列表");
			favorites.setUserId(user.getId());
			favorites.setCreateTime(DateUtils.getCurrentTime());
			favorites.setLastModifyTime(DateUtils.getCurrentTime());
			favoritesRepository.save(favorites);
			getSession().setAttribute(Const.LOGIN_SESSION_KEY, user);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("create user failed, ",e);
			return result(ExceptionMsg.FAILED);
		}
		return result();
	}
	
	
	@RequestMapping(value="/collect",method=RequestMethod.POST)
	public Response login(Collect collect) {
		logger.info("collect begin, param is "+collect);
		try {
			collect.setUserId(getUserId());
			collect.setCreateTime(DateUtils.getCurrentTime());
			collect.setLastModifyTime(DateUtils.getCurrentTime());
			collect.setIsDelete("no");
			if(StringUtils.isNotBlank(collect.getNewFavorites())){
				Favorites favorites = favoritesRepository.findByUserIdAndName(getUserId(), collect.getNewFavorites());
				if(null == favorites){
					favorites = new Favorites();
					favorites.setName(collect.getNewFavorites());
					favorites.setUserId(getUserId());
					favorites.setCreateTime(DateUtils.getCurrentTime());
					favorites.setLastModifyTime(DateUtils.getCurrentTime());
					favoritesRepository.save(favorites);
				}
				collect.setFavoritesId(favorites.getId());
			}
			if(StringUtils.isBlank(collect.getType())){
				collect.setType("public");
			}
			collectRepository.save(collect);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("collect failed, ",e);
			return result(ExceptionMsg.FAILED);
		}
		return result();
	}
	
	@RequestMapping("/uid")
    String uid(HttpSession session) {
        UUID uid = (UUID) session.getAttribute("uid");
        if (uid == null) {
            uid = UUID.randomUUID();
        }
        session.setAttribute("uid", uid);
        return session.getId();
    }
}