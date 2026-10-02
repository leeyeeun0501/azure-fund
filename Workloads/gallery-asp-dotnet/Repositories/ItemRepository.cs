using System.Data;
using Dapper;
using Gallery.Data;
using Gallery.Domain;

namespace Gallery.Repositories;

// Spring Boot의 repository.ItemRepository(MyBatis mapper 래핑)에 대응.
// MyBatis item.xml의 raw SQL을 Dapper로 그대로 옮겼다. findAll(id desc) / findById / save / deleteById / count
public class ItemRepository
{
    private readonly DbConnectionFactory _factory;

    public ItemRepository(DbConnectionFactory factory) => _factory = factory;

    public List<Item> FindAll()
    {
        using var conn = _factory.Create();
        return conn.Query<Item>(
            "SELECT id, url, comment FROM item ORDER BY id DESC").ToList();
    }

    public Item? FindById(long id)
    {
        using var conn = _factory.Create();
        return conn.QuerySingleOrDefault<Item>(
            "SELECT id, url, comment FROM item WHERE id = @id", new { id });
    }

    public Item Save(Item item)
    {
        using var conn = _factory.Create();
        conn.Open(); // INSERT와 LAST_INSERT_ID를 같은 연결(세션)에서 실행하기 위해 명시적으로 연다
        conn.Execute(
            "INSERT INTO item (url, comment) VALUES (@Url, @Comment)", item);
        item.Id = conn.ExecuteScalar<long>(_factory.LastInsertIdSql);
        return item;
    }

    public bool DeleteById(long id)
    {
        using var conn = _factory.Create();
        return conn.Execute("DELETE FROM item WHERE id = @id", new { id }) == 1;
    }

    public long Count()
    {
        using var conn = _factory.Create();
        return conn.ExecuteScalar<long>("SELECT COUNT(*) FROM item");
    }
}
