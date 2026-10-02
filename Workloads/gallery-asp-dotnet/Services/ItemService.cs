using Gallery.Domain;
using Gallery.Repositories;
using Gallery.Storage;

namespace Gallery.Services;

// Spring Boot의 service.ItemService에 대응.
public class ItemService
{
    private readonly IImageStorage _storage;
    private readonly ItemRepository _repository;
    private readonly ILogger<ItemService> _log;

    public ItemService(IImageStorage storage, ItemRepository repository, ILogger<ItemService> log)
    {
        _storage = storage;
        _repository = repository;
        _log = log;
    }

    public List<Item> GetItems() => _repository.FindAll();

    public void RegisterItem(IFormFile file, string comment)
    {
        var url = _storage.Upload(file);
        if (url is null) return;

        var item = _repository.Save(new Item(url, comment));
        _log.LogInformation("Uploaded: {Item}", item);
    }

    public void UnregisterItem(long id)
    {
        var item = _repository.FindById(id);
        if (item is null) return;

        if (_repository.DeleteById(item.Id))
        {
            _storage.Delete(item.Url);
        }
    }
}
