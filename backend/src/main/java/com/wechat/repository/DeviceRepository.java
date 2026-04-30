package com.wechat.repository;

import com.wechat.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceId(String deviceId);
    Optional<Device> findByDeviceSerial(String deviceSerial);
    boolean existsByDeviceSerial(String deviceSerial);
}
