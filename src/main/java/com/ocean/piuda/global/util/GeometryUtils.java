package com.ocean.piuda.global.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class GeometryUtils {
    // SRID 4326 (WGS84)
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 좌표를 받아 Point 객체 생성
     * GIS 표준에 따라 (Longitude, Latitude) 순서로 입력받습니다.
     * @param longitude 경도 (X)
     * @param latitude  위도 (Y)
     */
    public static Point createPoint(double longitude, double latitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}