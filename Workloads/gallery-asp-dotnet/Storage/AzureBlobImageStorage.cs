using Azure.Storage.Blobs;
using Azure.Storage.Blobs.Models;
using Microsoft.Extensions.Options;

namespace Gallery.Storage;

// Spring Boot의 storage.S3ImageStorage에 대응하는 클라우드 스토리지.
// 이 트랙(.NET/Windows)은 Azure이므로 S3 대신 Azure Blob Storage를 쓴다.
public class AzureBlobImageStorage : IImageStorage
{
    private readonly BlobContainerClient _container;
    private readonly ILogger<AzureBlobImageStorage> _log;

    public AzureBlobImageStorage(IOptions<StorageOptions> options, ILogger<AzureBlobImageStorage> log)
    {
        var opt = options.Value.Blob;
        _log = log;

        var service = new BlobServiceClient(opt.ConnectionString);
        _container = service.GetBlobContainerClient(opt.Container);
        _container.CreateIfNotExists(PublicAccessType.Blob);

        _log.LogInformation("AzureBlobImageStorage initialized [container={Container}]", opt.Container);
    }

    public string? Upload(IFormFile file)
    {
        if (file.Length == 0) return null;

        var ext = Path.GetExtension(file.FileName);
        var saveName = Guid.NewGuid().ToString("N") + ext;
        var blob = _container.GetBlobClient(saveName);

        using (var s = file.OpenReadStream())
        {
            blob.Upload(s, overwrite: true);
        }

        return blob.Uri.ToString();
    }

    public void Delete(string url)
    {
        if (string.IsNullOrEmpty(url)) return;

        var name = url[(url.LastIndexOf('/') + 1)..];
        _container.DeleteBlobIfExists(name);
        _log.LogInformation("Blob deleted: {Name}", name);
    }
}
