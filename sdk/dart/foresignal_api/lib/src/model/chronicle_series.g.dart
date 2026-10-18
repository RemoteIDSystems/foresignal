// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_series.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSeriesCWProxy {
  ChronicleSeries chronicleId(String chronicleId);

  ChronicleSeries ref(String ref);

  ChronicleSeries from(DateTime? from);

  ChronicleSeries to(DateTime? to);

  ChronicleSeries points(List<SeriesPoint> points);

  ChronicleSeries backfilled(bool? backfilled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSeries call({
    String chronicleId,
    String ref,
    DateTime? from,
    DateTime? to,
    List<SeriesPoint> points,
    bool? backfilled,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSeries.copyWith(...)` or call `instanceOfChronicleSeries.copyWith.fieldName(value)` for a single field.
class _$ChronicleSeriesCWProxyImpl implements _$ChronicleSeriesCWProxy {
  const _$ChronicleSeriesCWProxyImpl(this._value);

  final ChronicleSeries _value;

  @override
  ChronicleSeries chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleSeries ref(String ref) => call(ref: ref);

  @override
  ChronicleSeries from(DateTime? from) => call(from: from);

  @override
  ChronicleSeries to(DateTime? to) => call(to: to);

  @override
  ChronicleSeries points(List<SeriesPoint> points) => call(points: points);

  @override
  ChronicleSeries backfilled(bool? backfilled) => call(backfilled: backfilled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSeries call({
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? points = const $CopyWithPlaceholder(),
    Object? backfilled = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSeries(
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      points: points == const $CopyWithPlaceholder() || points == null
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<SeriesPoint>,
      backfilled: backfilled == const $CopyWithPlaceholder()
          ? _value.backfilled
          // ignore: cast_nullable_to_non_nullable
          : backfilled as bool?,
    );
  }
}

extension $ChronicleSeriesCopyWith on ChronicleSeries {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSeries.copyWith(...)` or `instanceOfChronicleSeries.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSeriesCWProxy get copyWith => _$ChronicleSeriesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSeries _$ChronicleSeriesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleSeries', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['chronicleId', 'ref', 'points']);
      final val = ChronicleSeries(
        chronicleId: $checkedConvert('chronicleId', (v) => v as String),
        ref: $checkedConvert('ref', (v) => v as String),
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        points: $checkedConvert(
          'points',
          (v) => (v as List<dynamic>)
              .map((e) => SeriesPoint.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        backfilled: $checkedConvert('backfilled', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleSeriesToJson(ChronicleSeries instance) =>
    <String, dynamic>{
      'chronicleId': instance.chronicleId,
      'ref': instance.ref,
      'from': ?instance.from?.toIso8601String(),
      'to': ?instance.to?.toIso8601String(),
      'points': instance.points.map((e) => e.toJson()).toList(),
      'backfilled': ?instance.backfilled,
    };
