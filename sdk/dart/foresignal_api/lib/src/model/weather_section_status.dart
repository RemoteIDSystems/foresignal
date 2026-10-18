//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// Status of a derived weather section; anything but ok means unknown, never clear
enum WeatherSectionStatus {
      @JsonValue(r'ok')
      ok(r'ok'),
      @JsonValue(r'stale')
      stale(r'stale'),
      @JsonValue(r'no-coverage')
      noCoverage(r'no-coverage'),
      @JsonValue(r'unavailable')
      unavailable(r'unavailable');

  const WeatherSectionStatus(this.value);

  final String value;

  @override
  String toString() => value;
}
