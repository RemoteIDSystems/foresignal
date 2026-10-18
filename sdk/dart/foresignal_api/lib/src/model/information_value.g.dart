// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'information_value.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InformationValueCWProxy {
  InformationValue setId(String setId);

  InformationValue computedAt(DateTime computedAt);

  InformationValue entropy(num entropy);

  InformationValue version(String? version);

  InformationValue indicators(List<InformationValueIndicatorsInner> indicators);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InformationValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InformationValue(...).copyWith(id: 12, name: "My name")
  /// ```
  InformationValue call({
    String setId,
    DateTime computedAt,
    num entropy,
    String? version,
    List<InformationValueIndicatorsInner> indicators,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInformationValue.copyWith(...)` or call `instanceOfInformationValue.copyWith.fieldName(value)` for a single field.
class _$InformationValueCWProxyImpl implements _$InformationValueCWProxy {
  const _$InformationValueCWProxyImpl(this._value);

  final InformationValue _value;

  @override
  InformationValue setId(String setId) => call(setId: setId);

  @override
  InformationValue computedAt(DateTime computedAt) =>
      call(computedAt: computedAt);

  @override
  InformationValue entropy(num entropy) => call(entropy: entropy);

  @override
  InformationValue version(String? version) => call(version: version);

  @override
  InformationValue indicators(
    List<InformationValueIndicatorsInner> indicators,
  ) => call(indicators: indicators);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InformationValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InformationValue(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InformationValue call({
    Object? setId = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
    Object? entropy = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? indicators = const $CopyWithPlaceholder(),
  }) {
    return InformationValue(
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
      entropy: entropy == const $CopyWithPlaceholder() || entropy == null
          ? _value.entropy
          // ignore: cast_nullable_to_non_nullable
          : entropy as num,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String?,
      indicators:
          indicators == const $CopyWithPlaceholder() || indicators == null
          ? _value.indicators
          // ignore: cast_nullable_to_non_nullable
          : indicators as List<InformationValueIndicatorsInner>,
    );
  }
}

extension $InformationValueCopyWith on InformationValue {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInformationValue.copyWith(...)` or `instanceOfInformationValue.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InformationValueCWProxy get copyWith => _$InformationValueCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InformationValue _$InformationValueFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InformationValue', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['setId', 'computedAt', 'entropy', 'indicators'],
      );
      final val = InformationValue(
        setId: $checkedConvert('setId', (v) => v as String),
        computedAt: $checkedConvert(
          'computedAt',
          (v) => DateTime.parse(v as String),
        ),
        entropy: $checkedConvert('entropy', (v) => v as num),
        version: $checkedConvert('version', (v) => v as String?),
        indicators: $checkedConvert(
          'indicators',
          (v) => (v as List<dynamic>)
              .map(
                (e) => InformationValueIndicatorsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$InformationValueToJson(InformationValue instance) =>
    <String, dynamic>{
      'setId': instance.setId,
      'computedAt': instance.computedAt.toIso8601String(),
      'entropy': instance.entropy,
      'version': ?instance.version,
      'indicators': instance.indicators.map((e) => e.toJson()).toList(),
    };
