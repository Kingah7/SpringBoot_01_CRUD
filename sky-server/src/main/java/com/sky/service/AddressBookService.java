package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.entity.AddressBook;

import java.util.List;


public interface AddressBookService extends IService<AddressBook> {
    List<AddressBook> queryAddressList();

    void saveAddress(AddressBook addressBook);

    void setDefaultAddress(Long id);
}
