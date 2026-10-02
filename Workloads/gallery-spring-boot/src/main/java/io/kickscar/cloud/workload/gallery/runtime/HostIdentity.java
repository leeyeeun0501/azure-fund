package io.kickscar.cloud.workload.gallery.runtime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 이 요청을 처리한 인스턴스를 식별하는 **클라우드-독립** ID.
 * 목적은 LB가 요청을 여러 인스턴스에 분산하는지 화면 푸터에서 확인하는 것뿐이라,
 * 클라우드 메타데이터(EC2 IMDS 등)에 의존하지 않고 hostname을 쓴다
 * (Azure/AWS/GCP·로컬 어디서든 그 머신 이름 — 예: vm-lab13-web-1).
 * hostname이 불안정한 환경(localhost·blank)이면 주 NIC의 사설 IPv4로 폴백한다.
 */
@Component
public class HostIdentity {
    private volatile String cached;

    /** 필요 시 명시 지정용(테스트·오버라이드). 비어 있으면 자동 탐지. */
    @Value("${app.runtime.host-id:}")
    private String configuredId;

    public String getHostId() {
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (cached != null) {
                return cached;
            }
            cached = resolve();
            return cached;
        }
    }

    private String resolve() {
        if (configuredId != null && !configuredId.isBlank()) {
            return configuredId.trim();
        }
        // 1) hostname — 읽기 쉬움(vm-lab13-web-1 등), LB 분산이 한눈에
        try {
            String host = InetAddress.getLocalHost().getHostName();
            if (host != null && !host.isBlank() && !host.equalsIgnoreCase("localhost")) {
                return host.trim();
            }
        } catch (Exception ignored) {
            // 폴백으로 진행
        }
        // 2) 폴백 — 주 NIC의 사설 IPv4
        String ip = firstSiteLocalIpv4();
        return ip != null ? ip : "unknown";
    }

    private String firstSiteLocalIpv4() {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                if (!nic.isUp() || nic.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> addrs = nic.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
            // null 반환
        }
        return null;
    }
}
