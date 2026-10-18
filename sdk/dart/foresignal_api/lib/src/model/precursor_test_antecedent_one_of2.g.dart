// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_antecedent_one_of2.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestAntecedentOneOf2CWProxy {
  PrecursorTestAntecedentOneOf2 intelEventType(
    PrecursorTestAntecedentOneOf2IntelEventTypeEnum intelEventType,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestAntecedentOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestAntecedentOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestAntecedentOneOf2 call({
    PrecursorTestAntecedentOneOf2IntelEventTypeEnum intelEventType,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestAntecedentOneOf2.copyWith(...)` or call `instanceOfPrecursorTestAntecedentOneOf2.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestAntecedentOneOf2CWProxyImpl
    implements _$PrecursorTestAntecedentOneOf2CWProxy {
  const _$PrecursorTestAntecedentOneOf2CWProxyImpl(this._value);

  final PrecursorTestAntecedentOneOf2 _value;

  @override
  PrecursorTestAntecedentOneOf2 intelEventType(
    PrecursorTestAntecedentOneOf2IntelEventTypeEnum intelEventType,
  ) => call(intelEventType: intelEventType);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestAntecedentOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestAntecedentOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestAntecedentOneOf2 call({
    Object? intelEventType = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestAntecedentOneOf2(
      intelEventType:
          intelEventType == const $CopyWithPlaceholder() ||
              intelEventType == null
          ? _value.intelEventType
          // ignore: cast_nullable_to_non_nullable
          : intelEventType as PrecursorTestAntecedentOneOf2IntelEventTypeEnum,
    );
  }
}

extension $PrecursorTestAntecedentOneOf2CopyWith
    on PrecursorTestAntecedentOneOf2 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestAntecedentOneOf2.copyWith(...)` or `instanceOfPrecursorTestAntecedentOneOf2.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestAntecedentOneOf2CWProxy get copyWith =>
      _$PrecursorTestAntecedentOneOf2CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestAntecedentOneOf2 _$PrecursorTestAntecedentOneOf2FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestAntecedentOneOf2', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['intelEventType']);
  final val = PrecursorTestAntecedentOneOf2(
    intelEventType: $checkedConvert(
      'intelEventType',
      (v) => $enumDecode(
        _$PrecursorTestAntecedentOneOf2IntelEventTypeEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestAntecedentOneOf2ToJson(
  PrecursorTestAntecedentOneOf2 instance,
) => <String, dynamic>{
  'intelEventType':
      _$PrecursorTestAntecedentOneOf2IntelEventTypeEnumEnumMap[instance
          .intelEventType]!,
};

const _$PrecursorTestAntecedentOneOf2IntelEventTypeEnumEnumMap = {
  PrecursorTestAntecedentOneOf2IntelEventTypeEnum.maritimeDisruption:
      'maritime-disruption',
  PrecursorTestAntecedentOneOf2IntelEventTypeEnum.aviationAnomaly:
      'aviation-anomaly',
  PrecursorTestAntecedentOneOf2IntelEventTypeEnum.infrastructureDisruption:
      'infrastructure-disruption',
  PrecursorTestAntecedentOneOf2IntelEventTypeEnum.weatherImpact:
      'weather-impact',
  PrecursorTestAntecedentOneOf2IntelEventTypeEnum.seismic: 'seismic',
  PrecursorTestAntecedentOneOf2IntelEventTypeEnum.custom: 'custom',
};
