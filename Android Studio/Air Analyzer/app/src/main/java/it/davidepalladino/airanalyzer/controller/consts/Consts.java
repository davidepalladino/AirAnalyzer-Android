/*
 * This control class provides several constants for the purpose of the application.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 29th November, 2021
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

public interface Consts {
    static final byte MAX_ATTEMPTS_LOGIN = 3;
    static final int TIME_LOGIN_TIMEOUT = 3000;
    static final int TIME_ADD_DEVICE = 30000;
    static final int TIME_RESET_COLOR_BAR_GRAPH = 2500;
    static final int TIME_INTERVALS_NOTIFICATION[] = {15, 30, 60};
}
