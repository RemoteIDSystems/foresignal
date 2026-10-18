// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'smoke_polygon_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SmokePolygonPayloadCWProxy {
  SmokePolygonPayload density(SmokePolygonPayloadDensityEnum density);

  SmokePolygonPayload densityRank(int densityRank);

  SmokePolygonPayload start(DateTime? start);

  SmokePolygonPayload end(DateTime? end);

  SmokePolygonPayload satellite(String? satellite);

  SmokePolygonPayload label(String label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SmokePolygonPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SmokePolygonPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  SmokePolygonPayload call({
    SmokePolygonPayloadDensityEnum density,
    int densityRank,
    DateTime? start,
    DateTime? end,
    String? satellite,
    String label,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSmokePolygonPayload.copyWith(...)` or call `instanceOfSmokePolygonPayload.copyWith.fieldName(value)` for a single field.
class _$SmokePolygonPayloadCWProxyImpl implements _$SmokePolygonPayloadCWProxy {
  const _$SmokePolygonPayloadCWProxyImpl(this._value);

  final SmokePolygonPayload _value;

  @override
  SmokePolygonPayload density(SmokePolygonPayloadDensityEnum density) =>
      call(density: density);

  @override
  SmokePolygonPayload densityRank(int densityRank) =>
      call(densityRank: densityRank);

  @override
  SmokePolygonPayload start(DateTime? start) => call(start: start);

  @override
  SmokePolygonPayload end(DateTime? end) => call(end: end);

  @override
  SmokePolygonPayload satellite(String? satellite) =>
      call(satellite: satellite);

  @override
  SmokePolygonPayload label(String label) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SmokePolygonPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SmokePolygonPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SmokePolygonPayload call({
    Object? density = const $CopyWithPlaceholder(),
    Object? densityRank = const $CopyWithPlaceholder(),
    Object? start = const $CopyWithPlaceholder(),
    Object? end = const $CopyWithPlaceholder(),
    Object? satellite = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return SmokePolygonPayload(
      density: density == const $CopyWithPlaceholder() || density == null
          ? _value.density
          // ignore: cast_nullable_to_non_nullable
          : density as SmokePolygonPayloadDensityEnum,
      densityRank:
          densityRank == const $CopyWithPlaceholder() || densityRank == null
          ? _value.densityRank
          // ignore: cast_nullable_to_non_nullable
          : densityRank as int,
      start: start == const $CopyWithPlaceholder()
          ? _value.start
          // ignore: cast_nullable_to_non_nullable
          : start as DateTime?,
      end: end == const $CopyWithPlaceholder()
          ? _value.end
          // ignore: cast_nullable_to_non_nullable
          : end as DateTime?,
      satellite: satellite == const $CopyWithPlaceholder()
          ? _value.satellite
          // ignore: cast_nullable_to_non_nullable
          : satellite as String?,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
    );
  }
}

extension $SmokePolygonPayloadCopyWith on SmokePolygonPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSmokePolygonPayload.copyWith(...)` or `instanceOfSmokePolygonPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SmokePolygonPayloadCWProxy get copyWith =>
      _$SmokePolygonPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SmokePolygonPayload _$SmokePolygonPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SmokePolygonPayload', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['density', 'densityRank', 'label']);
      final val = SmokePolygonPayload(
        density: $checkedConvert(
          'density',
          (v) => $enumDecode(_$SmokePolygonPayloadDensityEnumEnumMap, v),
        ),
        densityRank: $checkedConvert('densityRank', (v) => (v as num).toInt()),
        start: $checkedConvert(
          'start',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        end: $checkedConvert(
          'end',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        satellite: $checkedConvert('satellite', (v) => v as String?),
        label: $checkedConvert('label', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$SmokePolygonPayloadToJson(
  SmokePolygonPayload instance,
) => <String, dynamic>{
  'density': _$SmokePolygonPayloadDensityEnumEnumMap[instance.density]!,
  'densityRank': instance.densityRank,
  'start': ?instance.start?.toIso8601String(),
  'end': ?instance.end?.toIso8601String(),
  'satellite': ?instance.satellite,
  'label': instance.label,
};

const _$SmokePolygonPayloadDensityEnumEnumMap = {
  SmokePolygonPayloadDensityEnum.light: 'light',
  SmokePolygonPayloadDensityEnum.medium: 'medium',
  SmokePolygonPayloadDensityEnum.heavy: 'heavy',
};
