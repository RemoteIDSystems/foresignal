// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_cap_override.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisCapOverrideCWProxy {
  HypothesisCapOverride by(String by);

  HypothesisCapOverride at(DateTime at);

  HypothesisCapOverride rationale(String rationale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisCapOverride(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisCapOverride(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisCapOverride call({String by, DateTime at, String rationale});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisCapOverride.copyWith(...)` or call `instanceOfHypothesisCapOverride.copyWith.fieldName(value)` for a single field.
class _$HypothesisCapOverrideCWProxyImpl
    implements _$HypothesisCapOverrideCWProxy {
  const _$HypothesisCapOverrideCWProxyImpl(this._value);

  final HypothesisCapOverride _value;

  @override
  HypothesisCapOverride by(String by) => call(by: by);

  @override
  HypothesisCapOverride at(DateTime at) => call(at: at);

  @override
  HypothesisCapOverride rationale(String rationale) =>
      call(rationale: rationale);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisCapOverride(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisCapOverride(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisCapOverride call({
    Object? by = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
  }) {
    return HypothesisCapOverride(
      by: by == const $CopyWithPlaceholder() || by == null
          ? _value.by
          // ignore: cast_nullable_to_non_nullable
          : by as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      rationale: rationale == const $CopyWithPlaceholder() || rationale == null
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String,
    );
  }
}

extension $HypothesisCapOverrideCopyWith on HypothesisCapOverride {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisCapOverride.copyWith(...)` or `instanceOfHypothesisCapOverride.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisCapOverrideCWProxy get copyWith =>
      _$HypothesisCapOverrideCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisCapOverride _$HypothesisCapOverrideFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisCapOverride', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['by', 'at', 'rationale']);
  final val = HypothesisCapOverride(
    by: $checkedConvert('by', (v) => v as String),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    rationale: $checkedConvert('rationale', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$HypothesisCapOverrideToJson(
  HypothesisCapOverride instance,
) => <String, dynamic>{
  'by': instance.by,
  'at': instance.at.toIso8601String(),
  'rationale': instance.rationale,
};
