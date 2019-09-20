package com.incentives.piggyback.location.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import com.incentives.piggyback.location.adapter.ObjectAdapter;
import com.incentives.piggyback.location.dto.LocationEntity;
import com.incentives.piggyback.location.entity.Location;
import com.incentives.piggyback.location.exception.ExceptionResponseCode;
import com.incentives.piggyback.location.exception.PiggyException;
import com.incentives.piggyback.location.repository.LocationRepository;
import com.incentives.piggyback.location.service.LocationService;
import com.incentives.piggyback.location.utils.CommonUtility;

@Service
public class LocationServiceImpl implements LocationService {

	@Autowired
	private LocationRepository locationRepository;

	@Override
	public String saveLocationCoordinates(Location location) {
		validateLocationParameters(location);
		//save in database
		List<LocationEntity> existingDetails = locationRepository.findByUserId(location.getUserId());
		if (CommonUtility.isValidList(existingDetails)) {

		} else {
			locationRepository.save(ObjectAdapter.getLocationEntity(location));
		}
		return "Location updated successfully!";
	}

	@Override
	public List<String> getNearbyUsers(String userId, double latitude, double longitude,
			Integer page, double optimizedRadius) {
		Point point = new Point(latitude, longitude);
		Circle circle = new Circle(point, new Distance(optimizedRadius, Metrics.KILOMETERS));
		Pageable pageable = PageRequest.of((page == null)? 0 : page, 50);
		List<LocationEntity> nearbyLocations = locationRepository.findByUserIdNotAndLocationWithin
				(userId, circle, pageable);
		List<String> userIds = new ArrayList<String>();
		nearbyLocations.forEach(location-> {
			userIds.add(location.getUserId());
		});
		return userIds;
	}

	private void validateLocationParameters(Location location) {
		if (!(CommonUtility.isValidDouble(location.getLatitude()) && CommonUtility.isValidDouble(location.getLongitude())
				&& CommonUtility.isValidString(location.getUserId())))
			throw new PiggyException(ExceptionResponseCode.USER_DATA_NOT_FOUND_IN_REQUEST);
	}

}