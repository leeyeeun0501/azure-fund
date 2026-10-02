namespace Gallery.Runtime;

// EC2 메타데이터 대신 Azure IMDS(Instance Metadata Service)로 VM ID를 조회한다.
// Azure VM이 아니면(로컬 실행) null이 되어 footer가 표시되지 않는다.
public class AzureInstanceIdentity : IInstanceIdentity
{
    public string? InstanceId { get; }

    public AzureInstanceIdentity(ILogger<AzureInstanceIdentity> log)
    {
        try
        {
            using var http = new HttpClient { Timeout = TimeSpan.FromMilliseconds(700) };
            http.DefaultRequestHeaders.Add("Metadata", "true");

            // Azure Instance Metadata Service. Azure VM에서만 응답한다.
            var vmId = http.GetStringAsync(
                "http://169.254.169.254/metadata/instance/compute/vmId?api-version=2021-02-01&format=text")
                .GetAwaiter().GetResult();

            InstanceId = string.IsNullOrWhiteSpace(vmId) ? null : vmId.Trim();
            if (InstanceId is not null)
                log.LogInformation("Azure instance detected: {VmId}", InstanceId);
        }
        catch
        {
            // 로컬/비-Azure 환경: IMDS 미응답
            InstanceId = null;
        }
    }
}
