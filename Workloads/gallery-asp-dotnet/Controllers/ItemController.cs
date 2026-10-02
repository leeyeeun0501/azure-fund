using Gallery.Domain;
using Gallery.Runtime;
using Gallery.Services;
using Microsoft.AspNetCore.Mvc;

namespace Gallery.Controllers;

// Spring Boot의 controller.ItemController에 대응. 엔드포인트 3개 동일.
//   GET  /            → 목록
//   POST /new         → multipart 업로드(file, comment)
//   GET  /{id}/delete → 삭제
public class ItemController : Controller
{
    private readonly ItemService _service;
    private readonly IInstanceIdentity _identity;
    private readonly ILogger<ItemController> _log;

    public ItemController(ItemService service, IInstanceIdentity identity, ILogger<ItemController> log)
    {
        _service = service;
        _identity = identity;
        _log = log;
    }

    [HttpGet("/")]
    public IActionResult Index()
    {
        List<Item> items = _service.GetItems();
        ViewData["InstanceId"] = _identity.InstanceId;
        return View(items);
    }

    [HttpPost("/new")]
    public IActionResult Create(IFormFile file, [FromForm] string comment = "")
    {
        _log.LogInformation("Request[POST /new] [{File}, {Comment}]", file?.FileName, comment);
        if (file is not null)
        {
            _service.RegisterItem(file, comment);
        }
        return Redirect("/");
    }

    [HttpGet("/{id:long}/delete")]
    public IActionResult Delete(long id)
    {
        _log.LogInformation("Request[DELETE {Id}]", id);
        _service.UnregisterItem(id);
        return Redirect("/");
    }
}
