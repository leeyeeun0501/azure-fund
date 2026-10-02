package io.kickscar.cloud.workload.gallery.repository;

import io.kickscar.cloud.workload.gallery.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ItemRepository {
    private final SqlSession sqlSession;

    @Autowired
    public ItemRepository(SqlSession sqlSession) {
        this.sqlSession = sqlSession;

        try {
            DatabaseMetaData metaData = sqlSession.getConfiguration().getEnvironment().getDataSource().getConnection().getMetaData();
            log.info("ItemRepository initialized [datasource (driver name: {}, url: {}, username: {})]", metaData.getDriverName(),  metaData.getURL(), metaData.getUserName());
        } catch (SQLException e) {
            log.error("Failed to extract metadata from SqlSession", e);
            throw new IllegalStateException(e);
        }
    }

    public Item save(Item item) {
        sqlSession.insert("item.insert", item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(sqlSession.selectOne("item.findById", id));
    }

    public List<Item> findAll() {
        return sqlSession.selectList("item.findAll");
    }

    public boolean deleteById(Long id) {
        return 1 == sqlSession.delete("item.deleteById", id);
    }

    public long count() {
        return sqlSession.selectOne("item.count");
    }
}
