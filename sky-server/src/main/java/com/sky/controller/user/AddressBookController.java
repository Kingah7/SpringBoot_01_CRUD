package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Api(tags = "C端-地址铺接口")
public class AddressBookController {

    @Resource
    private AddressBookService addressBookService;

    @GetMapping("/list")
    @ApiOperation("查询当前用户所有地址信息")
    public Result<List<AddressBook>> queryAddressList() {
        log.info("查询当前用户所有地址信息...");
        List<AddressBook> addressBookList = addressBookService.queryAddressList();
        return Result.success(addressBookList);
    }

    @PostMapping
    @ApiOperation("新增地址")
    public Result saveAddress(@RequestBody AddressBook addressBook) {
        log.info("新增地址:{}", addressBook);
        addressBook.setIsDefault(0);
        addressBookService.saveAddress(addressBook);
        return Result.success();
    }

    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefaultAddress(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址:{}", addressBook);
        addressBookService.update().setSql("is_default = 0")
                .eq("is_default", 1)
                .eq("user_id", BaseContext.getThreadLocal(Long.class)).update();
        addressBookService.setDefaultAddress(addressBook.getId());
        return Result.success();
    }

    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result queryDefaultAddress() {
        log.info("查询默认地址...");
        AddressBook addressBook = addressBookService.query()
                .eq("user_id", BaseContext.getThreadLocal(Long.class))
                .eq("is_default", 1)
                .one();

        return Result.success(addressBook);
    }

    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result updateAddressById(@RequestBody AddressBook addressBook) {
        log.info("根据id修改地址:{}", addressBook);
        addressBookService.updateById(addressBook);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getAddressById(@PathVariable Long id) {
        log.info("根据id查询地址:{}", id);
        AddressBook addressBook = addressBookService.query().eq("id", id).one();
        return Result.success(addressBook);
    }

    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result deleteAddressById(Long id) {
        log.info("根据id删除地址:{}", id);
        addressBookService.removeById(id);

        return Result.success();
    }
}
