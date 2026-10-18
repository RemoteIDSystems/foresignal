//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// Analytic confidence in the evidence (separate from probability)
enum ConfidenceLevel {
      @JsonValue(r'low')
      low(r'low'),
      @JsonValue(r'moderate')
      moderate(r'moderate'),
      @JsonValue(r'high')
      high(r'high');

  const ConfidenceLevel(this.value);

  final String value;

  @override
  String toString() => value;
}
