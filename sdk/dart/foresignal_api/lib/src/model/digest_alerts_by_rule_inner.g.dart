// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_alerts_by_rule_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestAlertsByRuleInnerCWProxy {
  DigestAlertsByRuleInner ruleId(String ruleId);

  DigestAlertsByRuleInner ruleName(String? ruleName);

  DigestAlertsByRuleInner count(int count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestAlertsByRuleInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestAlertsByRuleInner(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestAlertsByRuleInner call({String ruleId, String? ruleName, int count});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestAlertsByRuleInner.copyWith(...)` or call `instanceOfDigestAlertsByRuleInner.copyWith.fieldName(value)` for a single field.
class _$DigestAlertsByRuleInnerCWProxyImpl
    implements _$DigestAlertsByRuleInnerCWProxy {
  const _$DigestAlertsByRuleInnerCWProxyImpl(this._value);

  final DigestAlertsByRuleInner _value;

  @override
  DigestAlertsByRuleInner ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  DigestAlertsByRuleInner ruleName(String? ruleName) =>
      call(ruleName: ruleName);

  @override
  DigestAlertsByRuleInner count(int count) => call(count: count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestAlertsByRuleInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestAlertsByRuleInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestAlertsByRuleInner call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? ruleName = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
  }) {
    return DigestAlertsByRuleInner(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      ruleName: ruleName == const $CopyWithPlaceholder()
          ? _value.ruleName
          // ignore: cast_nullable_to_non_nullable
          : ruleName as String?,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
    );
  }
}

extension $DigestAlertsByRuleInnerCopyWith on DigestAlertsByRuleInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestAlertsByRuleInner.copyWith(...)` or `instanceOfDigestAlertsByRuleInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestAlertsByRuleInnerCWProxy get copyWith =>
      _$DigestAlertsByRuleInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestAlertsByRuleInner _$DigestAlertsByRuleInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DigestAlertsByRuleInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ruleId', 'count']);
  final val = DigestAlertsByRuleInner(
    ruleId: $checkedConvert('ruleId', (v) => v as String),
    ruleName: $checkedConvert('ruleName', (v) => v as String?),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$DigestAlertsByRuleInnerToJson(
  DigestAlertsByRuleInner instance,
) => <String, dynamic>{
  'ruleId': instance.ruleId,
  'ruleName': ?instance.ruleName,
  'count': instance.count,
};
