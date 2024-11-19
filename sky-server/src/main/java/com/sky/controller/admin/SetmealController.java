package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(("/admin/setmeal"))
@Api(tags = "套餐接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("添加套餐")
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO) {
        setmealService.addSetmeal(setmealDTO);
        return Result.success();
    }

    @PostMapping("status/{status}")
    @ApiOperation("启用/禁用套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result enableOrDisable(@PathVariable Integer status, Long id) {
        setmealService.enableOrDisable(status, id);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> pageSetmeal(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult pageResult =  setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO) {
        setmealService.updateSetmeal(setmealDTO);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getSetmeal(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getSetmeal(id);
        return Result.success(setmealVO);
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result<SetmealVO> deleteBatch(@RequestParam List<Long> ids) {
        setmealService.deleteBatch(ids);
        return Result.success();
    }

}
