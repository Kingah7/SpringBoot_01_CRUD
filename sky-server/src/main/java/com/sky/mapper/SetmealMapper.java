package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {
    List<Long> getSetmealIdByDishIds(List<Long> dishIds);

    Page<SetmealVO> queryPage(SetmealPageQueryDTO setmealPageQueryDTO);

    void update(Setmeal setmeal);


    @Update("update setmeal set status = #{status} where id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Long status);

    List<Long> getDishBySetmealId(Long id);

    List<Setmeal> list(Setmeal setmeal);

    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long id);
}
