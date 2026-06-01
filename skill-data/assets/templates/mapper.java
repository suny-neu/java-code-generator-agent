package {basePackage}.repository;

import {basePackage}.entity.{EntityName};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * {EntityName} Mapper接口
 *
 * @author {author}
 * @since {date}
 */
@org.apache.ibatis.annotations.Mapper
public interface {EntityName}Mapper extends BaseMapper<{EntityName}> {

    // 继承BaseMapper后自动获得基础CRUD方法:
    // - int insert(T entity)
    // - int deleteById(Serializable id)
    // - int updateById(T entity)
    // - T selectById(Serializable id)
    // - List<T> selectList(Wrapper<T> wrapper)
    // - Long selectCount(Wrapper<T> wrapper)
    // 等...

    // 如需自定义查询方法，在此添加
    // default {EntityName} selectByXxx(String xxx) {
    //     LambdaQueryWrapper<{EntityName}> wrapper = new LambdaQueryWrapper<>();
    //     wrapper.eq({EntityName}::getXxx, xxx);
    //     return selectOne(wrapper);
    // }
}
