//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

enum FindingSeverity {
      @JsonValue(r'info')
      info(r'info'),
      @JsonValue(r'notable')
      notable(r'notable'),
      @JsonValue(r'significant')
      significant(r'significant');

  const FindingSeverity(this.value);

  final String value;

  @override
  String toString() => value;
}
