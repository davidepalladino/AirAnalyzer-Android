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
 * @date 17th September, 2022
 *
 */

package it.davidepalladino.airanalyzer.controller.consts;

public interface BroadcastConst {
    String BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY = "ApplicantActivity";

    String BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGIN = "Login";
    String BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGOUT = "Logout";
    String BROADCAST_REQUEST_CODE_EXTENSION_USER_SIGNUP = "Signup";
    String BROADCAST_REQUEST_CODE_EXTENSION_USER_GET_ME = "GetMe";

    String BROADCAST_REQUEST_CODE_EXTENSION_MEASURES_GET_LATEST_DAY = "GetLatestDayMeasures";
    String BROADCAST_REQUEST_CODE_EXTENSION_MEASURES_GET_AVERAGE_DAY = "GetAverageDayMeasures";

    String BROADCAST_REQUEST_CODE_EXTENSION_ROOM_GET_ALL = "GetAllRooms";
    String BROADCAST_REQUEST_CODE_EXTENSION_ROOM_CHANGE_STATUS_ACTIVATION = "ChangeStatusActivationRoom";
    String BROADCAST_REQUEST_CODE_EXTENSION_ROOM_CHANGE_NAME = "ChangeNameRoom";

    String BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_GET_ALL = "GetAllNotifications";
    String BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_CHANGE_STATUS_VIEW = "ChangeStatusViewNotification";

    String BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_WRITE_CREDENTIALS = "SocketWriteCredentials";
}
