// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_antecedent_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestAntecedentOneOf1CWProxy {
  PrecursorTestAntecedentOneOf1 alertRuleId(String alertRuleId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestAntecedentOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestAntecedentOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestAntecedentOneOf1 call({String alertRuleId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestAntecedentOneOf1.copyWith(...)` or call `instanceOfPrecursorTestAntecedentOneOf1.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestAntecedentOneOf1CWProxyImpl
    implements _$PrecursorTestAntecedentOneOf1CWProxy {
  const _$PrecursorTestAntecedentOneOf1CWProxyImpl(this._value);

  final PrecursorTestAntecedentOneOf1 _value;

  @override
  PrecursorTestAntecedentOneOf1 alertRuleId(String alertRuleId) =>
      call(alertRuleId: alertRuleId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestAntecedentOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestAntecedentOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestAntecedentOneOf1 call({
    Object? alertRuleId = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestAntecedentOneOf1(
      alertRuleId:
          alertRuleId == const $CopyWithPlaceholder() || alertRuleId == null
          ? _value.alertRuleId
          // ignore: cast_nullable_to_non_nullable
          : alertRuleId as String,
    );
  }
}

extension $PrecursorTestAntecedentOneOf1CopyWith
    on PrecursorTestAntecedentOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestAntecedentOneOf1.copyWith(...)` or `instanceOfPrecursorTestAntecedentOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestAntecedentOneOf1CWProxy get copyWith =>
      _$PrecursorTestAntecedentOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestAntecedentOneOf1 _$PrecursorTestAntecedentOneOf1FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestAntecedentOneOf1', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['alertRuleId']);
  final val = PrecursorTestAntecedentOneOf1(
    alertRuleId: $checkedConvert('alertRuleId', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestAntecedentOneOf1ToJson(
  PrecursorTestAntecedentOneOf1 instance,
) => <String, dynamic>{'alertRuleId': instance.alertRuleId};
