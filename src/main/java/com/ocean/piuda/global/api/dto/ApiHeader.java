package com.ocean.piuda.global.api.dto;

public record ApiHeader(
    String name,
    String value
) {

  public static ApiHeader of(String name, String value) {
    return new ApiHeader(name, value);
  }
}
