//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

enum HealthStatus {
      @JsonValue(r'healthy')
      healthy(r'healthy'),
      @JsonValue(r'degraded')
      degraded(r'degraded'),
      @JsonValue(r'failing')
      failing(r'failing'),
      @JsonValue(r'idle')
      idle(r'idle'),
      @JsonValue(r'disabled')
      disabled(r'disabled');

  const HealthStatus(this.value);

  final String value;

  @override
  String toString() => value;
}
