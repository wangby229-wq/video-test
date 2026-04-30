package com.wechat.repository;

import com.wechat.entity.DeviceBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceBindingRepository extends JpaRepository<DeviceBinding, Long> {
  Optional<DeviceBinding> findByDeviceSerial(String deviceSerial);

  boolean existsByDeviceSerial(String deviceSerial);
}
