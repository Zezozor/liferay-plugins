/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.sync.model.impl;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.sync.SyncClientMinBuildException;
import com.liferay.sync.SyncDeviceActiveException;
import com.liferay.sync.SyncDeviceWipeException;
import com.liferay.sync.SyncServicesUnavailableException;
import com.liferay.sync.service.SyncDeviceLocalServiceUtil;
import com.liferay.sync.shared.util.SyncDeviceConstants;
import com.liferay.sync.util.PortletPropsKeys;
import com.liferay.sync.util.PortletPropsValues;

/**
 * @author Shinn Lok
 */
@ProviderType
public class SyncDeviceImpl extends SyncDeviceBaseImpl {

	@Override
	public void checkStatus() throws PortalException, SystemException {
		if (getStatus() == SyncDeviceConstants.STATUS_INACTIVE) {
			throw new SyncDeviceActiveException();
		}
		else if (getStatus() == SyncDeviceConstants.STATUS_PENDING_WIPE) {
			SyncDeviceLocalServiceUtil.updateSyncDevice(
				getSyncDeviceId(), getHostName(), getType(), getBuildNumber(),
				getFeatureSet(), SyncDeviceConstants.STATUS_WIPED);
			throw new SyncDeviceWipeException();
		}

		if (!PrefsPropsUtil.getBoolean(
				getCompanyId(), PortletPropsKeys.SYNC_SERVICES_ENABLED,
				PortletPropsValues.SYNC_SERVICES_ENABLED)) {

			throw new SyncServicesUnavailableException();
		}

		if (!isSupported()) {
			throw new SyncClientMinBuildException();
		}
	}

	public boolean isSupported() throws SystemException {
		int minBuildNumber = 0;

		String type = getType();

		if (type.startsWith("desktop")) {
			minBuildNumber = PrefsPropsUtil.getInteger(
				getCompanyId(), PortletPropsKeys.SYNC_CLIENT_MIN_BUILD_DESKTOP,
				PortletPropsValues.SYNC_CLIENT_MIN_BUILD_DESKTOP);
		}
		else if (type.equals("mobile-android")) {
			minBuildNumber = PrefsPropsUtil.getInteger(
				getCompanyId(), PortletPropsKeys.SYNC_CLIENT_MIN_BUILD_ANDROID,
				PortletPropsValues.SYNC_CLIENT_MIN_BUILD_ANDROID);
		}
		else if (type.equals("mobile-ios")) {
			minBuildNumber = PrefsPropsUtil.getInteger(
				getCompanyId(), PortletPropsKeys.SYNC_CLIENT_MIN_BUILD_IOS,
				PortletPropsValues.SYNC_CLIENT_MIN_BUILD_IOS);
		}

		if (getBuildNumber() >= minBuildNumber) {
			return true;
		}

		return false;
	}

	@Override
	public boolean supports(int featureSet) {
		if (getFeatureSet() >= featureSet) {
			return true;
		}
		else {
			return false;
		}
	}

}