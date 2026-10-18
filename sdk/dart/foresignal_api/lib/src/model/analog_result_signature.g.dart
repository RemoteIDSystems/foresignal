// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analog_result_signature.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalogResultSignatureCWProxy {
  AnalogResultSignature dayN(int dayN);

  AnalogResultSignature computedAt(DateTime? computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogResultSignature(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogResultSignature(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalogResultSignature call({int dayN, DateTime? computedAt});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalogResultSignature.copyWith(...)` or call `instanceOfAnalogResultSignature.copyWith.fieldName(value)` for a single field.
class _$AnalogResultSignatureCWProxyImpl
    implements _$AnalogResultSignatureCWProxy {
  const _$AnalogResultSignatureCWProxyImpl(this._value);

  final AnalogResultSignature _value;

  @override
  AnalogResultSignature dayN(int dayN) => call(dayN: dayN);

  @override
  AnalogResultSignature computedAt(DateTime? computedAt) =>
      call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogResultSignature(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogResultSignature(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalogResultSignature call({
    Object? dayN = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return AnalogResultSignature(
      dayN: dayN == const $CopyWithPlaceholder() || dayN == null
          ? _value.dayN
          // ignore: cast_nullable_to_non_nullable
          : dayN as int,
      computedAt: computedAt == const $CopyWithPlaceholder()
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime?,
    );
  }
}

extension $AnalogResultSignatureCopyWith on AnalogResultSignature {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalogResultSignature.copyWith(...)` or `instanceOfAnalogResultSignature.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalogResultSignatureCWProxy get copyWith =>
      _$AnalogResultSignatureCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalogResultSignature _$AnalogResultSignatureFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalogResultSignature', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['dayN']);
  final val = AnalogResultSignature(
    dayN: $checkedConvert('dayN', (v) => (v as num).toInt()),
    computedAt: $checkedConvert(
      'computedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$AnalogResultSignatureToJson(
  AnalogResultSignature instance,
) => <String, dynamic>{
  'dayN': instance.dayN,
  'computedAt': ?instance.computedAt?.toIso8601String(),
};
