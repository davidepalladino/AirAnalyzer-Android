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
 * @date 16th September, 2022
 *
 */

package it.davidepalladino.airanalyzer.controller.consts;

public interface BroadcastConst {
    String BROADCAST_REQUEST_CODE_APPLICANT_ACTIVITY = "ApplicantActivity";

    String BROADCAST_REQUEST_CODE_EXTENSION_USER_LOGIN = "login";
    String BROADCAST_REQUEST_CODE_EXTENSION_USER_SIGNUP = "signup";
    String BROADCAST_REQUEST_CODE_EXTENSION_USER_GET_ME = "getMe";

    String BROADCAST_REQUEST_CODE_EXTENSION_MEASURES_GET_LATEST_DAY = "getLatestDayMeasures";
    String BROADCAST_REQUEST_CODE_EXTENSION_MEASURES_GET_AVERAGE_DAY = "getAverageDayMeasures";

    String BROADCAST_REQUEST_CODE_EXTENSION_ROOM_GET_ALL = "getAllRooms";
    String BROADCAST_REQUEST_CODE_EXTENSION_ROOM_CHANGE_STATUS_ACTIVATION = "changeStatusActivation";
    String BROADCAST_REQUEST_CODE_EXTENSION_ROOM_CHANGE_NAME = "changeNameRoom";

    String BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_GET_ALL = "getAllNotifications";
    String BROADCAST_REQUEST_CODE_EXTENSION_NOTIFICATION_CHANGE_STATUS_VIEW = "SetStatusNotifications";

    String BROADCAST_REQUEST_CODE_EXTENSION_SOCKET_WRITE_CREDENTIALS = "SocketWriteCredentials";
}
