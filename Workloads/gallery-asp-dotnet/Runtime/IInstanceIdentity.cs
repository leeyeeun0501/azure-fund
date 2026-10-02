namespace Gallery.Runtime;

// Spring Boot의 runtime.EC2InstanceIdentity에 대응(footer에 클라우드 인스턴스 ID 표시).
public interface IInstanceIdentity
{
    string? InstanceId { get; }
}
