package com.wechat.service;

import com.wechat.entity.Device;
import com.wechat.entity.DeviceBinding;
import com.wechat.repository.DeviceBindingRepository;
import com.wechat.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DeviceService {

  @Autowired
  private DeviceRepository deviceRepository;

  @Autowired
  private DeviceBindingRepository deviceBindingRepository;

  @Transactional
  public Device registerDevice(String deviceSerial, String deviceName) {
    if (deviceRepository.existsByDeviceSerial(deviceSerial)) {
      throw new RuntimeException("设备序列号已存在");
    }

    Device device = new Device();
    device.setDeviceId("DEV_" + System.currentTimeMillis());
    device.setDeviceSerial(deviceSerial);
    device.setDeviceName(deviceName);
    device.setStatus("offline");

    return deviceRepository.save(device);
  }

  @Transactional
  public DeviceBinding bindDevice(String deviceSerial, String userIds, String userNames, String bindingType) {
    Device device = deviceRepository.findByDeviceSerial(deviceSerial)
        .orElseThrow(() -> new RuntimeException("设备不存在"));

    if (deviceBindingRepository.existsByDeviceSerial(deviceSerial)) {
      throw new RuntimeException("设备已绑定，如需更新绑定人员请先解绑");
    }

    DeviceBinding binding = new DeviceBinding();
    binding.setDeviceId(device.getDeviceId());
    binding.setDeviceSerial(deviceSerial);
    binding.setUserIds(userIds);
    binding.setUserNames(userNames);
    binding.setBindingType(bindingType);

    return deviceBindingRepository.save(binding);
  }

  @Transactional
  public void unbindDevice(String deviceSerial) {
    DeviceBinding binding = deviceBindingRepository.findByDeviceSerial(deviceSerial)
        .orElseThrow(() -> new RuntimeException("绑定关系不存在"));

    deviceBindingRepository.delete(binding);
  }

  @Transactional
  public DeviceBinding updateBinding(String deviceSerial, String userIds, String userNames) {
    DeviceBinding binding = deviceBindingRepository.findByDeviceSerial(deviceSerial)
        .orElseThrow(() -> new RuntimeException("绑定关系不存在"));

    binding.setUserIds(userIds);
    binding.setUserNames(userNames);
    return deviceBindingRepository.save(binding);
  }

  public Optional<Device> getDeviceBySerial(String deviceSerial) {
    return deviceRepository.findByDeviceSerial(deviceSerial);
  }

  public Optional<DeviceBinding> getBindingByDeviceSerial(String deviceSerial) {
    return deviceBindingRepository.findByDeviceSerial(deviceSerial);
  }

  public Optional<DeviceBinding> getBindingByUserId(String userId) {
    List<DeviceBinding> bindings = deviceBindingRepository.findAll();
    for (DeviceBinding binding : bindings) {
      if (binding.getUserIds() != null && binding.getUserIds().contains(userId)) {
        return Optional.of(binding);
      }
    }
    return Optional.empty();
  }

  public List<Device> getAllDevices() {
    return deviceRepository.findAll();
  }

  public List<DeviceBinding> getAllBindings() {
    return deviceBindingRepository.findAll();
  }

  @Transactional
  public void updateDeviceStatus(String deviceSerial, String status) {
    Device device = deviceRepository.findByDeviceSerial(deviceSerial)
        .orElseThrow(() -> new RuntimeException("设备不存在"));

    device.setStatus(status);
    if ("online".equals(status)) {
      device.setLastOnlineTime(new java.util.Date());
    }

    deviceRepository.save(device);
  }
}
