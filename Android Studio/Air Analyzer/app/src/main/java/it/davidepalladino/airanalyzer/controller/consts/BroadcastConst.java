/*
 * This control class provides several constants for the broadcast requests.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 28th August, 2022
 *
 */

package it.davidepalladino.airanalyzer.controller.consts;

public interface BroadcastConst {
    String BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY = "ApplicantActivity";
    String BROADCAST_REQUEST_CODE_EXTENSION_LOGIN = "Login";
    String BROADCAST_REQUEST_CODE_EXTENSION_GET_ME = "GetMe";




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
