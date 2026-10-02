using Dapper;
using Gallery.Data;
using Gallery.Repositories;
using Gallery.Runtime;
using Gallery.Services;
using Gallery.Storage;
using Microsoft.Extensions.FileProviders;

var builder = WebApplication.CreateBuilder(args);

// 기본 포트 8080 (Spring Boot 버전과 동일). IIS 배포 시엔 사이트 바인딩이 우선하고,
// ASPNETCORE_URLS 환경 변수로도 덮어쓸 수 있다.
builder.WebHost.UseUrls("http://0.0.0.0:8080");

builder.Services.AddControllersWithViews();
builder.Services.AddHealthChecks();

// 스토리지 옵션 바인딩
builder.Services.Configure<StorageOptions>(builder.Configuration.GetSection("Storage"));

// DB 연결 팩토리: DB 선택은 소스가 아니라 실행 파라미터(설정)로 정한다.
//   기본 SQLite(H2 대응), --Database:Provider=mariadb --ConnectionStrings:Default=... 로 MariaDB 전환.
builder.Services.AddSingleton<DbConnectionFactory>();

// 스토리지 구현: 기본 Local, Type=blob이면 Azure Blob
var storageType = builder.Configuration["Storage:Type"] ?? "local";
if (storageType.Equals("blob", StringComparison.OrdinalIgnoreCase))
    builder.Services.AddSingleton<IImageStorage, AzureBlobImageStorage>();
else
    builder.Services.AddScoped<IImageStorage, LocalImageStorage>();

builder.Services.AddScoped<ItemRepository>();
builder.Services.AddScoped<ItemService>();
builder.Services.AddSingleton<IInstanceIdentity, AzureInstanceIdentity>();

var app = builder.Build();

// 테이블 보장 (Spring Boot의 spring.sql.init / schema.sql에 대응)
using (var scope = app.Services.CreateScope())
{
    var factory = scope.ServiceProvider.GetRequiredService<DbConnectionFactory>();
    using var conn = factory.Create();
    conn.Execute(factory.CreateTableSql);
}

// wwwroot/assets 정적 파일
app.UseStaticFiles();

// 로컬 스토리지일 때 업로드 폴더를 /assets/images/uploads URL로 서빙
if (storageType.Equals("local", StringComparison.OrdinalIgnoreCase))
{
    var local = app.Services
        .GetRequiredService<Microsoft.Extensions.Options.IOptions<StorageOptions>>().Value.Local;

    var uploadsPath = Path.IsPathRooted(local.Path)
        ? local.Path
        : Path.Combine(app.Environment.ContentRootPath, local.Path);
    Directory.CreateDirectory(uploadsPath);

    app.UseStaticFiles(new StaticFileOptions
    {
        FileProvider = new PhysicalFileProvider(uploadsPath),
        RequestPath = local.UrlPrefix
    });
}

app.MapHealthChecks("/health");
app.MapControllers();

app.Run();
