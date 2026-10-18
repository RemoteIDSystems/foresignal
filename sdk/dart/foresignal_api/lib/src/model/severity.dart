//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

enum Severity {
      @JsonValue(r'info')
      info(r'info'),
      @JsonValue(r'warning')
      warning(r'warning'),
      @JsonValue(r'critical')
      critical(r'critical');

  const Severity(this.value);

  final String value;

  @override
  String toString() => value;
}
