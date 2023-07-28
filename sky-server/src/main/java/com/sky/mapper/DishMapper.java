package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    Page<DishVO> queryPage(DishPageQueryDTO dishPageQueryDTO);

    void update(Dish dish);

    @Update("update dish set status = #{status} where id = #{id}")
    void changeStatus(@Param("status") Long status, @Param("id") Long id);
}
