// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_antecedent_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestAntecedentOneOfCWProxy {
  PrecursorTestAntecedentOneOf rule(Map<String, Object> rule);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestAntecedentOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestAntecedentOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestAntecedentOneOf call({Map<String, Object> rule});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestAntecedentOneOf.copyWith(...)` or call `instanceOfPrecursorTestAntecedentOneOf.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestAntecedentOneOfCWProxyImpl
    implements _$PrecursorTestAntecedentOneOfCWProxy {
  const _$PrecursorTestAntecedentOneOfCWProxyImpl(this._value);

  final PrecursorTestAntecedentOneOf _value;

  @override
  PrecursorTestAntecedentOneOf rule(Map<String, Object> rule) =>
      call(rule: rule);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestAntecedentOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestAntecedentOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestAntecedentOneOf call({
    Object? rule = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestAntecedentOneOf(
      rule: rule == const $CopyWithPlaceholder() || rule == null
          ? _value.rule
          // ignore: cast_nullable_to_non_nullable
          : rule as Map<String, Object>,
    );
  }
}

extension $PrecursorTestAntecedentOneOfCopyWith
    on PrecursorTestAntecedentOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestAntecedentOneOf.copyWith(...)` or `instanceOfPrecursorTestAntecedentOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestAntecedentOneOfCWProxy get copyWith =>
      _$PrecursorTestAntecedentOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestAntecedentOneOf _$PrecursorTestAntecedentOneOfFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestAntecedentOneOf', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['rule']);
  final val = PrecursorTestAntecedentOneOf(
    rule: $checkedConvert(
      'rule',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestAntecedentOneOfToJson(
  PrecursorTestAntecedentOneOf instance,
) => <String, dynamic>{'rule': instance.rule};
