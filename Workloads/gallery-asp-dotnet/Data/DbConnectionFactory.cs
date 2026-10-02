using System.Data;
using Microsoft.Data.Sqlite;
using MySqlConnector;

namespace Gallery.Data;

// Spring Boot의 datasource + MyBatis SqlSessionFactory에 대응.
// DB 선택은 소스에 하드코딩하지 않고 실행 파라미터(설정)로 정한다.
//   기본 Provider=sqlite (H2 대응), --Database:Provider=mariadb 로 MariaDB(MySqlConnector) 전환.
//   (Java가 spring.datasource.url 스킴으로 드라이버를 고르는 것에 대응하는 최소 계층.
//    .NET에는 URL 스킴 자동 선택이 없어 provider 값으로 연결 타입만 매핑한다.)
public class DbConnectionFactory
{
    private readonly string _provider;
    private readonly string _connString;

    public DbConnectionFactory(IConfiguration config)
    {
        _provider = config["Database:Provider"] ?? "sqlite";
        _connString = config.GetConnectionString("Default") ?? "Data Source=gallery.db";
    }

    public bool IsMariaDb => _provider.Equals("mariadb", StringComparison.OrdinalIgnoreCase);

    // provider별 연결 생성. Dapper는 이 IDbConnection 위에서 raw SQL을 실행한다.
    public IDbConnection Create() =>
        IsMariaDb ? new MySqlConnection(_connString) : new SqliteConnection(_connString);

    // dialect 1: 스키마 DDL (AUTO_INCREMENT 문법 차이).
    // MariaDB DDL은 Spring Boot의 schema.sql과 동일하게 맞춰 같은 item 테이블을 공유한다.
    public string CreateTableSql => IsMariaDb
        ? """
          CREATE TABLE IF NOT EXISTS `item` (
              `id` INT NOT NULL AUTO_INCREMENT,
              `url` VARCHAR(200) NOT NULL,
              `comment` VARCHAR(200) NOT NULL,
              PRIMARY KEY (`id`))
          ENGINE = InnoDB
          """
        : """
          CREATE TABLE IF NOT EXISTS item (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              url VARCHAR(200) NOT NULL,
              comment VARCHAR(200) NOT NULL)
          """;

    // dialect 2: 마지막 삽입 id (MyBatis의 select last_insert_id()에 대응).
    public string LastInsertIdSql => IsMariaDb ? "SELECT LAST_INSERT_ID()" : "SELECT last_insert_rowid()";
}
