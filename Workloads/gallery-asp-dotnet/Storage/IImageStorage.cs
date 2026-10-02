namespace Gallery.Storage;

// Spring Boot의 storage.ImageStorage 인터페이스에 대응.
public interface IImageStorage
{
    // 업로드 성공 시 접근 URL 반환, 실패(빈 파일 등) 시 null
    string? Upload(IFormFile file);
    void Delete(string url);
}
