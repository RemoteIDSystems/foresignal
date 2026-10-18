// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'storm_swath_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StormSwathPayloadCWProxy {
  StormSwathPayload trackId(String trackId);

  StormSwathPayload meshMaxMm(num? meshMaxMm);

  StormSwathPayload durationMin(num durationMin);

  StormSwathPayload status(StormSwathPayloadStatusEnum? status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormSwathPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormSwathPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  StormSwathPayload call({
    String trackId,
    num? meshMaxMm,
    num durationMin,
    StormSwathPayloadStatusEnum? status,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStormSwathPayload.copyWith(...)` or call `instanceOfStormSwathPayload.copyWith.fieldName(value)` for a single field.
class _$StormSwathPayloadCWProxyImpl implements _$StormSwathPayloadCWProxy {
  const _$StormSwathPayloadCWProxyImpl(this._value);

  final StormSwathPayload _value;

  @override
  StormSwathPayload trackId(String trackId) => call(trackId: trackId);

  @override
  StormSwathPayload meshMaxMm(num? meshMaxMm) => call(meshMaxMm: meshMaxMm);

  @override
  StormSwathPayload durationMin(num durationMin) =>
      call(durationMin: durationMin);

  @override
  StormSwathPayload status(StormSwathPayloadStatusEnum? status) =>
      call(status: status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormSwathPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormSwathPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StormSwathPayload call({
    Object? trackId = const $CopyWithPlaceholder(),
    Object? meshMaxMm = const $CopyWithPlaceholder(),
    Object? durationMin = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
  }) {
    return StormSwathPayload(
      trackId: trackId == const $CopyWithPlaceholder() || trackId == null
          ? _value.trackId
          // ignore: cast_nullable_to_non_nullable
          : trackId as String,
      meshMaxMm: meshMaxMm == const $CopyWithPlaceholder()
          ? _value.meshMaxMm
          // ignore: cast_nullable_to_non_nullable
          : meshMaxMm as num?,
      durationMin:
          durationMin == const $CopyWithPlaceholder() || durationMin == null
          ? _value.durationMin
          // ignore: cast_nullable_to_non_nullable
          : durationMin as num,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as StormSwathPayloadStatusEnum?,
    );
  }
}

extension $StormSwathPayloadCopyWith on StormSwathPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStormSwathPayload.copyWith(...)` or `instanceOfStormSwathPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StormSwathPayloadCWProxy get copyWith =>
      _$StormSwathPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StormSwathPayload _$StormSwathPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StormSwathPayload', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['trackId', 'durationMin']);
      final val = StormSwathPayload(
        trackId: $checkedConvert('trackId', (v) => v as String),
        meshMaxMm: $checkedConvert('meshMaxMm', (v) => v as num?),
        durationMin: $checkedConvert('durationMin', (v) => v as num),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$StormSwathPayloadStatusEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StormSwathPayloadToJson(StormSwathPayload instance) =>
    <String, dynamic>{
      'trackId': instance.trackId,
      'meshMaxMm': ?instance.meshMaxMm,
      'durationMin': instance.durationMin,
      'status': ?_$StormSwathPayloadStatusEnumEnumMap[instance.status],
    };

const _$StormSwathPayloadStatusEnumEnumMap = {
  StormSwathPayloadStatusEnum.active: 'active',
  StormSwathPayloadStatusEnum.ended: 'ended',
};
