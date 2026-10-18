// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'storm_track_payload_lightning_jump.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StormTrackPayloadLightningJumpCWProxy {
  StormTrackPayloadLightningJump at(DateTime at);

  StormTrackPayloadLightningJump sigma(num? sigma);

  StormTrackPayloadLightningJump rateFpm(num? rateFpm);

  StormTrackPayloadLightningJump method(
    StormTrackPayloadLightningJumpMethodEnum method,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadLightningJump(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadLightningJump(...).copyWith(id: 12, name: "My name")
  /// ```
  StormTrackPayloadLightningJump call({
    DateTime at,
    num? sigma,
    num? rateFpm,
    StormTrackPayloadLightningJumpMethodEnum method,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStormTrackPayloadLightningJump.copyWith(...)` or call `instanceOfStormTrackPayloadLightningJump.copyWith.fieldName(value)` for a single field.
class _$StormTrackPayloadLightningJumpCWProxyImpl
    implements _$StormTrackPayloadLightningJumpCWProxy {
  const _$StormTrackPayloadLightningJumpCWProxyImpl(this._value);

  final StormTrackPayloadLightningJump _value;

  @override
  StormTrackPayloadLightningJump at(DateTime at) => call(at: at);

  @override
  StormTrackPayloadLightningJump sigma(num? sigma) => call(sigma: sigma);

  @override
  StormTrackPayloadLightningJump rateFpm(num? rateFpm) =>
      call(rateFpm: rateFpm);

  @override
  StormTrackPayloadLightningJump method(
    StormTrackPayloadLightningJumpMethodEnum method,
  ) => call(method: method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadLightningJump(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadLightningJump(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StormTrackPayloadLightningJump call({
    Object? at = const $CopyWithPlaceholder(),
    Object? sigma = const $CopyWithPlaceholder(),
    Object? rateFpm = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
  }) {
    return StormTrackPayloadLightningJump(
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      sigma: sigma == const $CopyWithPlaceholder()
          ? _value.sigma
          // ignore: cast_nullable_to_non_nullable
          : sigma as num?,
      rateFpm: rateFpm == const $CopyWithPlaceholder()
          ? _value.rateFpm
          // ignore: cast_nullable_to_non_nullable
          : rateFpm as num?,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as StormTrackPayloadLightningJumpMethodEnum,
    );
  }
}

extension $StormTrackPayloadLightningJumpCopyWith
    on StormTrackPayloadLightningJump {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStormTrackPayloadLightningJump.copyWith(...)` or `instanceOfStormTrackPayloadLightningJump.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StormTrackPayloadLightningJumpCWProxy get copyWith =>
      _$StormTrackPayloadLightningJumpCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StormTrackPayloadLightningJump _$StormTrackPayloadLightningJumpFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StormTrackPayloadLightningJump', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['at', 'method']);
  final val = StormTrackPayloadLightningJump(
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    sigma: $checkedConvert('sigma', (v) => v as num?),
    rateFpm: $checkedConvert('rateFpm', (v) => v as num?),
    method: $checkedConvert(
      'method',
      (v) => $enumDecode(_$StormTrackPayloadLightningJumpMethodEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$StormTrackPayloadLightningJumpToJson(
  StormTrackPayloadLightningJump instance,
) => <String, dynamic>{
  'at': instance.at.toIso8601String(),
  'sigma': ?instance.sigma,
  'rateFpm': ?instance.rateFpm,
  'method': _$StormTrackPayloadLightningJumpMethodEnumEnumMap[instance.method]!,
};

const _$StormTrackPayloadLightningJumpMethodEnumEnumMap = {
  StormTrackPayloadLightningJumpMethodEnum.n2sigmaGlmV1: '2sigma-glm-v1',
};
