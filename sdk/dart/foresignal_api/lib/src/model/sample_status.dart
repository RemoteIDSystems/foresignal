//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// ok; no-coverage: outside the grid/footprint or a no-coverage code (\"outside product coverage; this is not evidence of clear weather\"); no-data: inside coverage but nodata; stale: frame older than maxGap relative to the requested time; unavailable: product disabled, source failing, no frame or frame expired
enum SampleStatus {
      @JsonValue(r'ok')
      ok(r'ok'),
      @JsonValue(r'no-coverage')
      noCoverage(r'no-coverage'),
      @JsonValue(r'no-data')
      noData(r'no-data'),
      @JsonValue(r'stale')
      stale(r'stale'),
      @JsonValue(r'unavailable')
      unavailable(r'unavailable'),
      @JsonValue(r'stale-position')
      stalePosition(r'stale-position');

  const SampleStatus(this.value);

  final String value;

  @override
  String toString() => value;
}
