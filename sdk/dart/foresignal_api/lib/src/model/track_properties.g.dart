// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'track_properties.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TrackPropertiesCWProxy {
  TrackProperties sourceId(String sourceId);

  TrackProperties externalId(String externalId);

  TrackProperties kind(String? kind);

  TrackProperties from(DateTime from);

  TrackProperties to(DateTime to);

  TrackProperties times(List<List<int>> times);

  TrackProperties values(Map<String, List<List<num?>>>? values);

  TrackProperties live(bool? live);

  TrackProperties pointsScanned(int? pointsScanned);

  TrackProperties pointsReturned(int pointsReturned);

  TrackProperties toleranceMeters(num? toleranceMeters);

  TrackProperties segments(int segments);

  TrackProperties approximate(bool? approximate);

  TrackProperties downsampled(bool? downsampled);

  TrackProperties truncated(bool? truncated);

  TrackProperties watermarkSeq(int watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TrackProperties(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TrackProperties(...).copyWith(id: 12, name: "My name")
  /// ```
  TrackProperties call({
    String sourceId,
    String externalId,
    String? kind,
    DateTime from,
    DateTime to,
    List<List<int>> times,
    Map<String, List<List<num?>>>? values,
    bool? live,
    int? pointsScanned,
    int pointsReturned,
    num? toleranceMeters,
    int segments,
    bool? approximate,
    bool? downsampled,
    bool? truncated,
    int watermarkSeq,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTrackProperties.copyWith(...)` or call `instanceOfTrackProperties.copyWith.fieldName(value)` for a single field.
class _$TrackPropertiesCWProxyImpl implements _$TrackPropertiesCWProxy {
  const _$TrackPropertiesCWProxyImpl(this._value);

  final TrackProperties _value;

  @override
  TrackProperties sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  TrackProperties externalId(String externalId) => call(externalId: externalId);

  @override
  TrackProperties kind(String? kind) => call(kind: kind);

  @override
  TrackProperties from(DateTime from) => call(from: from);

  @override
  TrackProperties to(DateTime to) => call(to: to);

  @override
  TrackProperties times(List<List<int>> times) => call(times: times);

  @override
  TrackProperties values(Map<String, List<List<num?>>>? values) =>
      call(values: values);

  @override
  TrackProperties live(bool? live) => call(live: live);

  @override
  TrackProperties pointsScanned(int? pointsScanned) =>
      call(pointsScanned: pointsScanned);

  @override
  TrackProperties pointsReturned(int pointsReturned) =>
      call(pointsReturned: pointsReturned);

  @override
  TrackProperties toleranceMeters(num? toleranceMeters) =>
      call(toleranceMeters: toleranceMeters);

  @override
  TrackProperties segments(int segments) => call(segments: segments);

  @override
  TrackProperties approximate(bool? approximate) =>
      call(approximate: approximate);

  @override
  TrackProperties downsampled(bool? downsampled) =>
      call(downsampled: downsampled);

  @override
  TrackProperties truncated(bool? truncated) => call(truncated: truncated);

  @override
  TrackProperties watermarkSeq(int watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TrackProperties(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TrackProperties(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TrackProperties call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? times = const $CopyWithPlaceholder(),
    Object? values = const $CopyWithPlaceholder(),
    Object? live = const $CopyWithPlaceholder(),
    Object? pointsScanned = const $CopyWithPlaceholder(),
    Object? pointsReturned = const $CopyWithPlaceholder(),
    Object? toleranceMeters = const $CopyWithPlaceholder(),
    Object? segments = const $CopyWithPlaceholder(),
    Object? approximate = const $CopyWithPlaceholder(),
    Object? downsampled = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
  }) {
    return TrackProperties(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      times: times == const $CopyWithPlaceholder() || times == null
          ? _value.times
          // ignore: cast_nullable_to_non_nullable
          : times as List<List<int>>,
      values: values == const $CopyWithPlaceholder()
          ? _value.values
          // ignore: cast_nullable_to_non_nullable
          : values as Map<String, List<List<num?>>>?,
      live: live == const $CopyWithPlaceholder()
          ? _value.live
          // ignore: cast_nullable_to_non_nullable
          : live as bool?,
      pointsScanned: pointsScanned == const $CopyWithPlaceholder()
          ? _value.pointsScanned
          // ignore: cast_nullable_to_non_nullable
          : pointsScanned as int?,
      pointsReturned:
          pointsReturned == const $CopyWithPlaceholder() ||
              pointsReturned == null
          ? _value.pointsReturned
          // ignore: cast_nullable_to_non_nullable
          : pointsReturned as int,
      toleranceMeters: toleranceMeters == const $CopyWithPlaceholder()
          ? _value.toleranceMeters
          // ignore: cast_nullable_to_non_nullable
          : toleranceMeters as num?,
      segments: segments == const $CopyWithPlaceholder() || segments == null
          ? _value.segments
          // ignore: cast_nullable_to_non_nullable
          : segments as int,
      approximate: approximate == const $CopyWithPlaceholder()
          ? _value.approximate
          // ignore: cast_nullable_to_non_nullable
          : approximate as bool?,
      downsampled: downsampled == const $CopyWithPlaceholder()
          ? _value.downsampled
          // ignore: cast_nullable_to_non_nullable
          : downsampled as bool?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      watermarkSeq:
          watermarkSeq == const $CopyWithPlaceholder() || watermarkSeq == null
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int,
    );
  }
}

extension $TrackPropertiesCopyWith on TrackProperties {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTrackProperties.copyWith(...)` or `instanceOfTrackProperties.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TrackPropertiesCWProxy get copyWith => _$TrackPropertiesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TrackProperties _$TrackPropertiesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TrackProperties', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'sourceId',
      'externalId',
      'from',
      'to',
      'times',
      'pointsReturned',
      'segments',
      'watermarkSeq',
    ],
  );
  final val = TrackProperties(
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    externalId: $checkedConvert('externalId', (v) => v as String),
    kind: $checkedConvert('kind', (v) => v as String?),
    from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
    to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
    times: $checkedConvert(
      'times',
      (v) => (v as List<dynamic>)
          .map(
            (e) => (e as List<dynamic>).map((e) => (e as num).toInt()).toList(),
          )
          .toList(),
    ),
    values: $checkedConvert(
      'values',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(
          k,
          (e as List<dynamic>)
              .map((e) => (e as List<dynamic>).map((e) => e as num?).toList())
              .toList(),
        ),
      ),
    ),
    live: $checkedConvert('live', (v) => v as bool?),
    pointsScanned: $checkedConvert(
      'pointsScanned',
      (v) => (v as num?)?.toInt(),
    ),
    pointsReturned: $checkedConvert(
      'pointsReturned',
      (v) => (v as num).toInt(),
    ),
    toleranceMeters: $checkedConvert('toleranceMeters', (v) => v as num?),
    segments: $checkedConvert('segments', (v) => (v as num).toInt()),
    approximate: $checkedConvert('approximate', (v) => v as bool?),
    downsampled: $checkedConvert('downsampled', (v) => v as bool?),
    truncated: $checkedConvert('truncated', (v) => v as bool?),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$TrackPropertiesToJson(TrackProperties instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'externalId': instance.externalId,
      'kind': ?instance.kind,
      'from': instance.from.toIso8601String(),
      'to': instance.to.toIso8601String(),
      'times': instance.times,
      'values': ?instance.values,
      'live': ?instance.live,
      'pointsScanned': ?instance.pointsScanned,
      'pointsReturned': instance.pointsReturned,
      'toleranceMeters': ?instance.toleranceMeters,
      'segments': instance.segments,
      'approximate': ?instance.approximate,
      'downsampled': ?instance.downsampled,
      'truncated': ?instance.truncated,
      'watermarkSeq': instance.watermarkSeq,
    };
