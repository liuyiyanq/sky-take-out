package com.sky.controller.user;

import com.google.common.hash.BloomFilter;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.utils.BloomFilterFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.List;

/**
 * C端地址簿接口控制器，集成布隆过滤器以优化地址查询
 */
@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端地址簿接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 布隆过滤器实例，用于快速判断地址 ID 是否可能存在
     */
    private BloomFilter<String> bloomFilter;

    /**
     * 初始化布隆过滤器，在控制器实例化后加载所有现有的地址 ID
     */
    @PostConstruct
    public void initBloomFilter() {
        // 预期插入数量，可以根据实际情况调整
        int expectedInsertions = 10000; // 根据实际数据量调整
        // 误判率，可以根据实际情况调整
        double fpp = 0.01;
        // 创建布隆过滤器
        bloomFilter = BloomFilterFactory.createBloomFilter(expectedInsertions, fpp);

        // 创建一个空的 AddressBook 对象用于查询所有地址
        AddressBook query = new AddressBook();
        // 假设未设置任何字段会返回所有地址，如果需要，可以根据实际情况设置字段

        // 查询所有地址的 ID 并添加到布隆过滤器中
        List<AddressBook> allAddresses = addressBookService.list(query);
        for (AddressBook address : allAddresses) {
            if (address.getId() != null) {
                bloomFilter.put(String.valueOf(address.getId()));
            }
        }
    }

    /**
     * 查询当前登录用户的所有地址信息
     *
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result<List<AddressBook>> list() {
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBook);
        return Result.success(list);
    }

    /**
     * 新增地址，并将新的地址 ID 添加到布隆过滤器中
     *
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result save(@RequestBody AddressBook addressBook) {
        addressBookService.save(addressBook);
        if (addressBook.getId() != null) {
            bloomFilter.put(String.valueOf(addressBook.getId()));
        }
        return Result.success();
    }

    /**
     * 根据 ID 查询地址，使用布隆过滤器优化查询
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id) {
        // 使用布隆过滤器检查 ID 是否可能存在
        if (!bloomFilter.mightContain(String.valueOf(id))) {
            return Result.error("地址不存在");
        }

        // 如果布隆过滤器可能包含，继续查询数据库
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook == null) {
            return Result.error("地址不存在");
        }
        return Result.success(addressBook);
    }

    /**
     * 根据 ID 修改地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result update(@RequestBody AddressBook addressBook) {
        addressBookService.update(addressBook);
        // 由于地址 ID 已存在，不需要更新布隆过滤器
        return Result.success();
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook) {
        addressBookService.setDefault(addressBook);
        return Result.success();
    }

    /**
     * 根据 ID 删除地址
     *
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result deleteById(@RequestParam Long id) {
        addressBookService.deleteById(id);
        // 注意：布隆过滤器不支持删除操作，因此无法从过滤器中移除该 ID
        // 这可能导致误判，认为已删除的地址仍然存在
        return Result.success();
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefault() {
        // SQL: select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = new AddressBook();
        addressBook.setIsDefault(1);
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBook);

        if (list != null && list.size() == 1) {
            return Result.success(list.get(0));
        }

        return Result.error("没有查询到默认地址");
    }
}
