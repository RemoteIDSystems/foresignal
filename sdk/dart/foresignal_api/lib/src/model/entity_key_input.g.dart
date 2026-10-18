// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'entity_key_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EntityKeyInputCWProxy {
  EntityKeyInput keyType(EntityKeyInputKeyTypeEnum keyType);

  EntityKeyInput keyValue(String keyValue);

  EntityKeyInput confidence(num? confidence);

  EntityKeyInput validFrom(DateTime? validFrom);

  EntityKeyInput validTo(DateTime? validTo);

  EntityKeyInput force(bool? force);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityKeyInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityKeyInput(...).copyWith(id: 12, name: "My name")
  /// ```
  EntityKeyInput call({
    EntityKeyInputKeyTypeEnum keyType,
    String keyValue,
    num? confidence,
    DateTime? validFrom,
    DateTime? validTo,
    bool? force,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEntityKeyInput.copyWith(...)` or call `instanceOfEntityKeyInput.copyWith.fieldName(value)` for a single field.
class _$EntityKeyInputCWProxyImpl implements _$EntityKeyInputCWProxy {
  const _$EntityKeyInputCWProxyImpl(this._value);

  final EntityKeyInput _value;

  @override
  EntityKeyInput keyType(EntityKeyInputKeyTypeEnum keyType) =>
      call(keyType: keyType);

  @override
  EntityKeyInput keyValue(String keyValue) => call(keyValue: keyValue);

  @override
  EntityKeyInput confidence(num? confidence) => call(confidence: confidence);

  @override
  EntityKeyInput validFrom(DateTime? validFrom) => call(validFrom: validFrom);

  @override
  EntityKeyInput validTo(DateTime? validTo) => call(validTo: validTo);

  @override
  EntityKeyInput force(bool? force) => call(force: force);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityKeyInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityKeyInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EntityKeyInput call({
    Object? keyType = const $CopyWithPlaceholder(),
    Object? keyValue = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validTo = const $CopyWithPlaceholder(),
    Object? force = const $CopyWithPlaceholder(),
  }) {
    return EntityKeyInput(
      keyType: keyType == const $CopyWithPlaceholder() || keyType == null
          ? _value.keyType
          // ignore: cast_nullable_to_non_nullable
          : keyType as EntityKeyInputKeyTypeEnum,
      keyValue: keyValue == const $CopyWithPlaceholder() || keyValue == null
          ? _value.keyValue
          // ignore: cast_nullable_to_non_nullable
          : keyValue as String,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num?,
      validFrom: validFrom == const $CopyWithPlaceholder()
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime?,
      validTo: validTo == const $CopyWithPlaceholder()
          ? _value.validTo
          // ignore: cast_nullable_to_non_nullable
          : validTo as DateTime?,
      force: force == const $CopyWithPlaceholder()
          ? _value.force
          // ignore: cast_nullable_to_non_nullable
          : force as bool?,
    );
  }
}

extension $EntityKeyInputCopyWith on EntityKeyInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEntityKeyInput.copyWith(...)` or `instanceOfEntityKeyInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EntityKeyInputCWProxy get copyWith => _$EntityKeyInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EntityKeyInput _$EntityKeyInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EntityKeyInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['keyType', 'keyValue']);
      final val = EntityKeyInput(
        keyType: $checkedConvert(
          'keyType',
          (v) => $enumDecode(_$EntityKeyInputKeyTypeEnumEnumMap, v),
        ),
        keyValue: $checkedConvert('keyValue', (v) => v as String),
        confidence: $checkedConvert('confidence', (v) => v as num? ?? 1.0),
        validFrom: $checkedConvert(
          'validFrom',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        validTo: $checkedConvert(
          'validTo',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        force: $checkedConvert('force', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$EntityKeyInputToJson(EntityKeyInput instance) =>
    <String, dynamic>{
      'keyType': _$EntityKeyInputKeyTypeEnumEnumMap[instance.keyType]!,
      'keyValue': instance.keyValue,
      'confidence': ?instance.confidence,
      'validFrom': ?instance.validFrom?.toIso8601String(),
      'validTo': ?instance.validTo?.toIso8601String(),
      'force': ?instance.force,
    };

const _$EntityKeyInputKeyTypeEnumEnumMap = {
  EntityKeyInputKeyTypeEnum.airframe: 'airframe',
  EntityKeyInputKeyTypeEnum.icao24: 'icao24',
  EntityKeyInputKeyTypeEnum.registration: 'registration',
  EntityKeyInputKeyTypeEnum.imo: 'imo',
  EntityKeyInputKeyTypeEnum.mmsi: 'mmsi',
  EntityKeyInputKeyTypeEnum.norad: 'norad',
  EntityKeyInputKeyTypeEnum.cospar: 'cospar',
  EntityKeyInputKeyTypeEnum.callsign: 'callsign',
};
