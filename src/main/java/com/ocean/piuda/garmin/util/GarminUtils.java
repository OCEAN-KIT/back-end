package com.ocean.piuda.garmin.util;

import com.ocean.piuda.global.api.exception.BusinessException;
import com.ocean.piuda.global.api.exception.ExceptionType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public final class GarminUtils {

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    private GarminUtils() {
    }

    public static String toIsoString(LocalDateTime dt) {
        return dt == null ? null : dt.toString();
    }

    public static Long toEpoch(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.atZone(ZONE_SEOUL).toEpochSecond();
    }

    public static LocalDateTime epochToLocalDateTime(Long epochSeconds) {
        if (epochSeconds == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZONE_SEOUL);
    }

    public static Long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        if (o instanceof String s) return Long.parseLong(s);
        throw new BusinessException(ExceptionType.INVALID_PAYLOAD);
    }

    private static Double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        if (o instanceof String s) return Double.parseDouble(s);
        throw new BusinessException(ExceptionType.INVALID_PAYLOAD);
    }

    /**
     * work_logs: 첫 번째 컬럼(epoch) → ISO 문자열로 변환
     */
    public static List<List<Object>> mapWorkLogsEpochToIso(List<List<Object>> raw) {
        if (raw == null || raw.isEmpty()) return List.of();

        return raw.stream()
                .map(row -> {
                    if (row == null || row.isEmpty() || row.get(0) == null) return row;

                    Object first = row.get(0);
                    Long epoch = toLong(first);
                    LocalDateTime ldt = epochToLocalDateTime(epoch);

                    var copy = new ArrayList<>(row);
                    copy.set(0, ldt.toString());
                    return copy;
                })
                .toList();
    }

    /**
     * work_logs에서 특정 인덱스(수심/수온)의 평균값 계산
     * - row = [시간, 수심, 수온, ...] 구조를 가정
     * - 유효한 숫자 값이 하나도 없으면 null 반환
     */
    private static Double computeAverage(List<List<Object>> rows, int index) {
        if (rows == null || rows.isEmpty()) return null;

        double sum = 0.0;
        long count = 0L;

        for (List<Object> row : rows) {
            if (row == null || row.size() <= index) continue;
            Object val = row.get(index);
            if (val == null) continue;

            Double d = toDouble(val);
            sum += d;
            count++;
        }

        if (count == 0L) return null;
        return sum / count;
    }

    /**
     * 평균 수심 (index = 1)
     */
    public static Double computeAverageDepth(List<List<Object>> rows) {
        return computeAverage(rows, 1);
    }

    /**
     * 평균 수온 (index = 2)
     */
    public static Double computeAverageTemperature(List<List<Object>> rows) {
        return computeAverage(rows, 2);
    }
}
