/*
 * This control class provides several constants for the broadcast requests.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 4th December, 2021
 *
 * This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version
 *
 * This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 */

package it.davidepalladino.airanalyzer.controller.consts;

public interface BroadcastConst {
    String BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY = "ApplicantActivity";
    String BROADCAST_REQUEST_CODE_EXTENSION_LOGIN = "Login";
    String BROADCAST_REQUEST_CODE_EXTENSION_SIGNUP = "Signup";
    String BROADCAST_REQUEST_CODE_EXTENSION_CHECK_USERNAME = "CheckUsername";
    String BROADCAST_REQUEST_CODE_EXTENSION_CHECK_EMAIL = "CheckEmail";
    String BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_TODAY_LATEST = "GetMeasuresTodayLatest";
    String BROADCAST_REQUEST_CODE_EXTENSION_ADD_ROOM = "AddRoom";
    String BROADCAST_REQUEST_CODE_EXTENSION_REMOVE_ROOM = "RemoveRoom";
    String BROADCAST_REQUEST_CODE_EXTENSION_RENAME_ROOM = "RenameRoom";
    String BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOMS = "GetActiveRooms";
    String BROADCAST_REQUEST_CODE_EXTENSION_GET_INACTIVE_ROOMS = "GetInactiveRooms";
    String BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_DATE_LATEST = "GetMeasuresDateLatest";
    String BROADCAST_REQUEST_CODE_EXTENSION_GET_MEASURES_DATE_AVERAGE = "GetMeasuresDateAverage";
    String BROADCAST_REQUEST_CODE_EXTENSION_GET_NOTIFICATIONS_LATEST = "GetNotificationsLatest";
    String BROADCAST_REQUEST_CODE_EXTENSIONS_SET_STATUS_NOTIFICATIONS = "SetStatusNotifications";
    String BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_WRITE_CREDENTIALS = "SocketWriteCredentials";
}
