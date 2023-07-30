package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController("adminCategoryController")
@RequestMapping("admin/category")
@Slf4j
@Api(tags = "分类相关接口")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> getCtgByType(Integer type) {
        log.info("根据类型查询分类:{}", type);
        List<Category> list = categoryService.getCtgByType(type);
        return Result.success(list);
    }

    /**
     * 分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分页查询:{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 修改分类
     *
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改分类:{}", categoryDTO);
        categoryService.updateCategory(categoryDTO);

        return Result.success();
    }


    @PostMapping
    @ApiOperation("新增分类")
    public Result save(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类:{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除分类")
    public Result deleteById(Integer id) {
        Result result = categoryService.deleteById(id);
        return Result.success(result);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用")
    public Result changeStatus(@PathVariable Integer status,Long id) {

        categoryService.changeStatus(status,id);
        return Result.success();
    }
}
