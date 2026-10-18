//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// Why a version was persisted (Core-Data-Model §5.2; stored codes 1–9)
enum PersistReason {
      @JsonValue(r'FIRST')
      FIRST(r'FIRST'),
      @JsonValue(r'REAPPEAR')
      REAPPEAR(r'REAPPEAR'),
      @JsonValue(r'SIGNIFICANT')
      SIGNIFICANT(r'SIGNIFICANT'),
      @JsonValue(r'TOMBSTONE')
      TOMBSTONE(r'TOMBSTONE'),
      @JsonValue(r'CHANGE')
      CHANGE(r'CHANGE'),
      @JsonValue(r'SAMPLE')
      SAMPLE(r'SAMPLE'),
      @JsonValue(r'KEYFRAME')
      KEYFRAME(r'KEYFRAME'),
      @JsonValue(r'HEARTBEAT')
      HEARTBEAT(r'HEARTBEAT'),
      @JsonValue(r'TRIGGER_EDGE')
      TRIGGER_EDGE(r'TRIGGER_EDGE');

  const PersistReason(this.value);

  final String value;

  @override
  String toString() => value;
}
