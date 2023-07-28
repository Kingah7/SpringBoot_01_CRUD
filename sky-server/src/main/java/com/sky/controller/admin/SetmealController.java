package com.sky.controller.admin;

import com.baomidou.mybatisplus.extension.api.R;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Resource
    private SetmealService setmealService;

    /**
     * 分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> queryPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult pageResult = setmealService.queryPage(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    @ApiOperation("新增套餐")
    public Result saveWithDish(@RequestBody SetmealDTO setmealDTO) {

        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐:{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根基id查询套餐:{}", id);
        SetmealVO setmealVO = setmealService.getBySetmealId(id);
        return Result.success(setmealVO);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("起售停售")
    public Result changeStatus(@PathVariable Long status, Long id) {
        log.info("根据id更改套餐状态:{},{}", status, id);
        setmealService.changeStatus(id, status);
        return Result.success();
    }

    /**
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除")
    public Result deleteByIds(@RequestParam List<Long> ids) {
        setmealService.deleteByIds(ids);
        return Result.success();
    }
}
