//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// Situation lifecycle status; `closed` is final
enum SituationStatus {
      @JsonValue(r'emerging')
      emerging(r'emerging'),
      @JsonValue(r'active')
      active(r'active'),
      @JsonValue(r'dormant')
      dormant(r'dormant'),
      @JsonValue(r'closed')
      closed(r'closed');

  const SituationStatus(this.value);

  final String value;

  @override
  String toString() => value;
}
