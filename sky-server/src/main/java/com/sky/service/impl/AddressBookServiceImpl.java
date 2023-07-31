package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.AddressBookService;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sky.constant.MessageConstant.LOGIN_FAILED;


@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    @Resource
    private AddressBookMapper addressBookMapper;

    @Override
    public List<AddressBook> queryAddressList() {
        Long userId = BaseContext.getThreadLocal(Long.class);
        List<AddressBook> addressBookList = query().eq("user_id", userId).list();
        return addressBookList;
    }

    @Transactional
    @Override
    public void saveAddress(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getThreadLocal(Long.class));
        addressBookMapper.insert(addressBook);
    }

    @Override
    public void setDefaultAddress(Long id) {
        update().setSql("is_default = 1").eq("id", id).update();
    }
}
