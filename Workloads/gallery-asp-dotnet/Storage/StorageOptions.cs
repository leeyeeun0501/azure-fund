namespace Gallery.Storage;

// appsettings의 "Storage" 섹션 바인딩. Spring Boot의 app.storage.* 에 대응.
public class StorageOptions
{
    public string Type { get; set; } = "local"; // local | blob

    public LocalOptions Local { get; set; } = new();
    public BlobOptions Blob { get; set; } = new();

    public class LocalOptions
    {
        // 프로세스 작업 디렉터리 기준 상대 경로(기본 "uploads"). Spring Boot와 동일 기본값
        public string Path { get; set; } = "uploads";
        // 브라우저에서 접근하는 업로드 URL prefix
        public string UrlPrefix { get; set; } = "/assets/images/uploads";
    }

    public class BlobOptions
    {
        public string ConnectionString { get; set; } = "";
        public string Container { get; set; } = "gallery";
    }
}
