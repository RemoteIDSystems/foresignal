//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// History tier: STANDARD (per-source windows, chunked deletes) or TRACK (one `trackWindow`, partition drops)
enum HistoryTier {
      @JsonValue(r'STANDARD')
      STANDARD(r'STANDARD'),
      @JsonValue(r'TRACK')
      TRACK(r'TRACK');

  const HistoryTier(this.value);

  final String value;

  @override
  String toString() => value;
}
