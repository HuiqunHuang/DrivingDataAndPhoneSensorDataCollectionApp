/**
 * Copyright 2019 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package obdii.starter.automotive.iot.ibm.com.iot4a_obdii.device;

import obdii.starter.automotive.iot.ibm.com.iot4a_obdii.obd.EventDataGenerator;

public interface IVehicleDevice {
    public void setAccessInfo(AccessInfo accessInfo);
    public AccessInfo getAccessInfo();
    public boolean hasValidAccessInfo();

    public void startPublishing(final EventDataGenerator eventGenerator, final int uploadIntervalMS, final NotificationHandler notificationHandler);
    public void stopPublishing();
    public void clean();
}
