using Microsoft.Extensions.Options;

namespace Gallery.Storage;

// Spring Boot의 storage.LocalImageStorage에 대응.
// UUID 파일명으로 저장하고 UrlPrefix + "/" + 파일명을 반환한다.
public class LocalImageStorage : IImageStorage
{
    private readonly StorageOptions.LocalOptions _opt;
    private readonly string _rootPath;
    private readonly ILogger<LocalImageStorage> _log;

    public LocalImageStorage(IOptions<StorageOptions> options, IWebHostEnvironment env, ILogger<LocalImageStorage> log)
    {
        _opt = options.Value.Local;
        _log = log;

        // 상대 경로면 ContentRoot 기준으로 해석(Spring Boot의 WorkingDirectory 기준 상대 경로와 동일 개념)
        _rootPath = Path.IsPathRooted(_opt.Path)
            ? _opt.Path
            : Path.Combine(env.ContentRootPath, _opt.Path);

        Directory.CreateDirectory(_rootPath);
        _log.LogInformation("LocalImageStorage initialized [path={Path}, urlPrefix={Url}]", _rootPath, _opt.UrlPrefix);
    }

    public string? Upload(IFormFile file)
    {
        if (file.Length == 0) return null;

        var ext = Path.GetExtension(file.FileName);
        var saveName = Guid.NewGuid().ToString("N") + ext;
        var fullPath = Path.Combine(_rootPath, saveName);

        using (var fs = new FileStream(fullPath, FileMode.Create))
        {
            file.CopyTo(fs);
        }

        return $"{_opt.UrlPrefix}/{saveName}";
    }

    public void Delete(string url)
    {
        if (string.IsNullOrEmpty(url)) return;

        var fileName = url[(url.LastIndexOf('/') + 1)..];
        var fullPath = Path.Combine(_rootPath, fileName);

        if (!File.Exists(fullPath))
        {
            _log.LogWarning("File not found for deletion: {File}", fileName);
            return;
        }

        File.Delete(fullPath);
        _log.LogInformation("File deleted: {File}", fileName);
    }
}
