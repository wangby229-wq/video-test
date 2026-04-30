import api from './index'

export const simulatorApi = {
  heartbeat(deviceSerial) {
    return api.post('/simulator/heartbeat', { deviceSerial })
  },

  simulateRecording(data) {
    return api.post('/simulator/simulate-recording', data)
  }
}
