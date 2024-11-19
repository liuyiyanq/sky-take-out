package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品接口")
public class DishController {

    @Autowired
    DishService dishService;

    @Autowired
    RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result saveWithFlavor(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品:{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //清理缓存数据
        String key = "category_" + dishDTO.getCategoryId();
        cleanCache(key);

        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page (DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteBatch (@RequestParam List<Long> ids) {
        dishService.deleteBatch(ids);

        //清理缓存数据
        cleanCache("category_*");

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById (@PathVariable Long id) {
        DishVO dishVO = dishService.getByIdWithFlavors(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        dishService.update(dishDTO);

        //清理缓存数据
        cleanCache("category_*");

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result enableOrDisable (@PathVariable Integer status, long id) {
        dishService.enableOrDisable(status, id);

        //清理缓存数据
        cleanCache("category_*");

        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> listByCategoryId (Long categoryId) {
        List<Dish> dishes = dishService.listByCategoryId(categoryId);
        return Result.success(dishes);
    }

    // 清理缓存
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}

