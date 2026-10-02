namespace Gallery.Domain;

// Spring Boot의 domain.Item에 대응. id / url / comment
public class Item
{
    public long Id { get; set; }
    public string Url { get; set; } = "";
    public string Comment { get; set; } = "";

    public Item() { }

    public Item(string url, string comment)
    {
        Url = url;
        Comment = comment;
    }

    public override string ToString() => $"Item(id={Id}, url={Url}, comment={Comment})";
}
