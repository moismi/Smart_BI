package com.nanchengyu.nanchengyubi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanchengyu.nanchengyubi.annotation.AuthCheck;
import com.nanchengyu.nanchengyubi.common.BaseResponse;
import com.nanchengyu.nanchengyubi.common.DeleteRequest;
import com.nanchengyu.nanchengyubi.common.ErrorCode;
import com.nanchengyu.nanchengyubi.common.ResultUtils;
import com.nanchengyu.nanchengyubi.constant.UserConstant;
import com.nanchengyu.nanchengyubi.exception.BusinessException;
import com.nanchengyu.nanchengyubi.exception.ThrowUtils;
import com.nanchengyu.nanchengyubi.model.dto.user.*;
import com.nanchengyu.nanchengyubi.model.entity.User;
import com.nanchengyu.nanchengyubi.model.entity.UserCode;
import com.nanchengyu.nanchengyubi.model.vo.LoginUserVO;
import com.nanchengyu.nanchengyubi.model.vo.UserCodeVO;
import com.nanchengyu.nanchengyubi.model.vo.UserVO;
import com.nanchengyu.nanchengyubi.service.CreditService;
import com.nanchengyu.nanchengyubi.service.UserCodeService;
import com.nanchengyu.nanchengyubi.service.UserService;
import com.nanchengyu.nanchengyubi.model.dto.user.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户接口
 *
 * @author nanchengyu
 */
@RestController
@Api(tags = "UserController")
@RequestMapping("/user")
@Slf4j
//@CrossOrigin(origins = "http://bi.kongshier.top", allowCredentials = "true")
@CrossOrigin(origins = "http://localhost:8000", allowCredentials = "true")
public class UserController {
    @Resource
    private UserService userService;


    @Resource
    private UserCodeService userCodeService;
    @Resource
    private CreditService creditService;


    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数为空");
        }
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户注销")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    @ApiOperation(value = "获取当前登录用户")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 管理员添加用户
     *
     * @param userAddRequest
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "管理员添加用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {

        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long userId = userService.addUser(userAddRequest);
        ThrowUtils.throwIf(userId < 0, ErrorCode.PARAMS_ERROR, "添加用户失败");
        return ResultUtils.success(userId);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "管理员删除用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "管理员更新用户信息")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户更新信息
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update/user")
    @ApiOperation(value = "更新用户信息")
    public BaseResponse<Boolean> updateByProfileUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                                     HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @ApiOperation(value = "管理员根据id获取用户")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取包装类")
    public BaseResponse<UserCodeVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        QueryWrapper<UserCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", id);
        UserCode userCode = userCodeService.getOne(queryWrapper);
        User user = response.getData();
        // 脱敏，将密码设置为空
        user.setUserPassword("");
        UserCodeVO userCodeVO = new UserCodeVO();
        // 将user的属性复制给userCodeVO
        BeanUtils.copyProperties(user, userCodeVO);
        userCodeVO.setId(userCode.getId());
        return ResultUtils.success(userCodeVO);
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取用户列表（仅管理员）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取用户封装列表")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }
    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    @ApiOperation(value = "用户更新个人信息")
    public BaseResponse<Boolean> updateMyInfo(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 调用 updateMyUser 方法来更新个人信息
        boolean updateResult = updateMyUser(userUpdateMyRequest, request);

        return ResultUtils.success(updateResult);
    }

    // 移除原来的 updateMyUser 方法，将其逻辑合并到 updateMyInfo 方法中
    private boolean updateMyUser(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 根据 当前登录用户 获取积分总数
     * @param request
     * @return
     */
    @GetMapping("/get/credit")
    public BaseResponse<Long> getCreditByUserId(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long creditTotal = creditService.getCreditTotal(userId);
        if (creditTotal == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(creditTotal);
    }


}
