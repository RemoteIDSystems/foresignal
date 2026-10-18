// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'lightning_cluster_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LightningClusterPayloadCWProxy {
  LightningClusterPayload flashes5m(
    LightningClusterPayloadFlashes5mEnum flashes5m,
  );

  LightningClusterPayload radiusKm(num? radiusKm);

  LightningClusterPayload maxEnergyFj(num? maxEnergyFj);

  LightningClusterPayload label(String label);

  LightningClusterPayload automated(bool automated);

  LightningClusterPayload notAWarning(bool notAWarning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LightningClusterPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LightningClusterPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  LightningClusterPayload call({
    LightningClusterPayloadFlashes5mEnum flashes5m,
    num? radiusKm,
    num? maxEnergyFj,
    String label,
    bool automated,
    bool notAWarning,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLightningClusterPayload.copyWith(...)` or call `instanceOfLightningClusterPayload.copyWith.fieldName(value)` for a single field.
class _$LightningClusterPayloadCWProxyImpl
    implements _$LightningClusterPayloadCWProxy {
  const _$LightningClusterPayloadCWProxyImpl(this._value);

  final LightningClusterPayload _value;

  @override
  LightningClusterPayload flashes5m(
    LightningClusterPayloadFlashes5mEnum flashes5m,
  ) => call(flashes5m: flashes5m);

  @override
  LightningClusterPayload radiusKm(num? radiusKm) => call(radiusKm: radiusKm);

  @override
  LightningClusterPayload maxEnergyFj(num? maxEnergyFj) =>
      call(maxEnergyFj: maxEnergyFj);

  @override
  LightningClusterPayload label(String label) => call(label: label);

  @override
  LightningClusterPayload automated(bool automated) =>
      call(automated: automated);

  @override
  LightningClusterPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LightningClusterPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LightningClusterPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LightningClusterPayload call({
    Object? flashes5m = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
    Object? maxEnergyFj = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
  }) {
    return LightningClusterPayload(
      flashes5m: flashes5m == const $CopyWithPlaceholder() || flashes5m == null
          ? _value.flashes5m
          // ignore: cast_nullable_to_non_nullable
          : flashes5m as LightningClusterPayloadFlashes5mEnum,
      radiusKm: radiusKm == const $CopyWithPlaceholder()
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num?,
      maxEnergyFj: maxEnergyFj == const $CopyWithPlaceholder()
          ? _value.maxEnergyFj
          // ignore: cast_nullable_to_non_nullable
          : maxEnergyFj as num?,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
      automated: automated == const $CopyWithPlaceholder() || automated == null
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool,
      notAWarning:
          notAWarning == const $CopyWithPlaceholder() || notAWarning == null
          ? _value.notAWarning
          // ignore: cast_nullable_to_non_nullable
          : notAWarning as bool,
    );
  }
}

extension $LightningClusterPayloadCopyWith on LightningClusterPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLightningClusterPayload.copyWith(...)` or `instanceOfLightningClusterPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LightningClusterPayloadCWProxy get copyWith =>
      _$LightningClusterPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LightningClusterPayload _$LightningClusterPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LightningClusterPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['flashes5m', 'label', 'automated', 'notAWarning'],
  );
  final val = LightningClusterPayload(
    flashes5m: $checkedConvert(
      'flashes5m',
      (v) => $enumDecode(_$LightningClusterPayloadFlashes5mEnumEnumMap, v),
    ),
    radiusKm: $checkedConvert('radiusKm', (v) => v as num?),
    maxEnergyFj: $checkedConvert('maxEnergyFj', (v) => v as num?),
    label: $checkedConvert('label', (v) => v as String),
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$LightningClusterPayloadToJson(
  LightningClusterPayload instance,
) => <String, dynamic>{
  'flashes5m':
      _$LightningClusterPayloadFlashes5mEnumEnumMap[instance.flashes5m]!,
  'radiusKm': ?instance.radiusKm,
  'maxEnergyFj': ?instance.maxEnergyFj,
  'label': instance.label,
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
};

const _$LightningClusterPayloadFlashes5mEnumEnumMap = {
  LightningClusterPayloadFlashes5mEnum.number3: 3,
  LightningClusterPayloadFlashes5mEnum.number5: 5,
  LightningClusterPayloadFlashes5mEnum.number10: 10,
  LightningClusterPayloadFlashes5mEnum.number20: 20,
  LightningClusterPayloadFlashes5mEnum.number50: 50,
  LightningClusterPayloadFlashes5mEnum.number100: 100,
  LightningClusterPayloadFlashes5mEnum.number200: 200,
};
