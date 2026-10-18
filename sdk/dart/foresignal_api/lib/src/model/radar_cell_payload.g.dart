// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'radar_cell_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RadarCellPayloadCWProxy {
  RadarCellPayload maxDbz(num maxDbz);

  RadarCellPayload areaKm2(num areaKm2);

  RadarCellPayload meshMax(num? meshMax);

  RadarCellPayload glmFlashes5m(int? glmFlashes5m);

  RadarCellPayload motion(RadarCellPayloadMotion? motion);

  RadarCellPayload automated(bool? automated);

  RadarCellPayload notAWarning(bool? notAWarning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RadarCellPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RadarCellPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  RadarCellPayload call({
    num maxDbz,
    num areaKm2,
    num? meshMax,
    int? glmFlashes5m,
    RadarCellPayloadMotion? motion,
    bool? automated,
    bool? notAWarning,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRadarCellPayload.copyWith(...)` or call `instanceOfRadarCellPayload.copyWith.fieldName(value)` for a single field.
class _$RadarCellPayloadCWProxyImpl implements _$RadarCellPayloadCWProxy {
  const _$RadarCellPayloadCWProxyImpl(this._value);

  final RadarCellPayload _value;

  @override
  RadarCellPayload maxDbz(num maxDbz) => call(maxDbz: maxDbz);

  @override
  RadarCellPayload areaKm2(num areaKm2) => call(areaKm2: areaKm2);

  @override
  RadarCellPayload meshMax(num? meshMax) => call(meshMax: meshMax);

  @override
  RadarCellPayload glmFlashes5m(int? glmFlashes5m) =>
      call(glmFlashes5m: glmFlashes5m);

  @override
  RadarCellPayload motion(RadarCellPayloadMotion? motion) =>
      call(motion: motion);

  @override
  RadarCellPayload automated(bool? automated) => call(automated: automated);

  @override
  RadarCellPayload notAWarning(bool? notAWarning) =>
      call(notAWarning: notAWarning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RadarCellPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RadarCellPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RadarCellPayload call({
    Object? maxDbz = const $CopyWithPlaceholder(),
    Object? areaKm2 = const $CopyWithPlaceholder(),
    Object? meshMax = const $CopyWithPlaceholder(),
    Object? glmFlashes5m = const $CopyWithPlaceholder(),
    Object? motion = const $CopyWithPlaceholder(),
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
  }) {
    return RadarCellPayload(
      maxDbz: maxDbz == const $CopyWithPlaceholder() || maxDbz == null
          ? _value.maxDbz
          // ignore: cast_nullable_to_non_nullable
          : maxDbz as num,
      areaKm2: areaKm2 == const $CopyWithPlaceholder() || areaKm2 == null
          ? _value.areaKm2
          // ignore: cast_nullable_to_non_nullable
          : areaKm2 as num,
      meshMax: meshMax == const $CopyWithPlaceholder()
          ? _value.meshMax
          // ignore: cast_nullable_to_non_nullable
          : meshMax as num?,
      glmFlashes5m: glmFlashes5m == const $CopyWithPlaceholder()
          ? _value.glmFlashes5m
          // ignore: cast_nullable_to_non_nullable
          : glmFlashes5m as int?,
      motion: motion == const $CopyWithPlaceholder()
          ? _value.motion
          // ignore: cast_nullable_to_non_nullable
          : motion as RadarCellPayloadMotion?,
      automated: automated == const $CopyWithPlaceholder()
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool?,
      notAWarning: notAWarning == const $CopyWithPlaceholder()
          ? _value.notAWarning
          // ignore: cast_nullable_to_non_nullable
          : notAWarning as bool?,
    );
  }
}

extension $RadarCellPayloadCopyWith on RadarCellPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRadarCellPayload.copyWith(...)` or `instanceOfRadarCellPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RadarCellPayloadCWProxy get copyWith => _$RadarCellPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RadarCellPayload _$RadarCellPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RadarCellPayload', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['maxDbz', 'areaKm2']);
      final val = RadarCellPayload(
        maxDbz: $checkedConvert('maxDbz', (v) => v as num),
        areaKm2: $checkedConvert('areaKm2', (v) => v as num),
        meshMax: $checkedConvert('meshMax', (v) => v as num?),
        glmFlashes5m: $checkedConvert(
          'glmFlashes5m',
          (v) => (v as num?)?.toInt(),
        ),
        motion: $checkedConvert(
          'motion',
          (v) => v == null
              ? null
              : RadarCellPayloadMotion.fromJson(v as Map<String, dynamic>),
        ),
        automated: $checkedConvert('automated', (v) => v as bool?),
        notAWarning: $checkedConvert('notAWarning', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$RadarCellPayloadToJson(RadarCellPayload instance) =>
    <String, dynamic>{
      'maxDbz': instance.maxDbz,
      'areaKm2': instance.areaKm2,
      'meshMax': ?instance.meshMax,
      'glmFlashes5m': ?instance.glmFlashes5m,
      'motion': ?instance.motion?.toJson(),
      'automated': ?instance.automated,
      'notAWarning': ?instance.notAWarning,
    };
