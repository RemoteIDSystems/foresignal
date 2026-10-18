// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_rules.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraRulesCWProxy {
  InfraRules version(String? version);

  InfraRules rules(List<InfraRulesRulesInner> rules);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraRules(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraRules(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraRules call({String? version, List<InfraRulesRulesInner> rules});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraRules.copyWith(...)` or call `instanceOfInfraRules.copyWith.fieldName(value)` for a single field.
class _$InfraRulesCWProxyImpl implements _$InfraRulesCWProxy {
  const _$InfraRulesCWProxyImpl(this._value);

  final InfraRules _value;

  @override
  InfraRules version(String? version) => call(version: version);

  @override
  InfraRules rules(List<InfraRulesRulesInner> rules) => call(rules: rules);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraRules(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraRules(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraRules call({
    Object? version = const $CopyWithPlaceholder(),
    Object? rules = const $CopyWithPlaceholder(),
  }) {
    return InfraRules(
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String?,
      rules: rules == const $CopyWithPlaceholder() || rules == null
          ? _value.rules
          // ignore: cast_nullable_to_non_nullable
          : rules as List<InfraRulesRulesInner>,
    );
  }
}

extension $InfraRulesCopyWith on InfraRules {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraRules.copyWith(...)` or `instanceOfInfraRules.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraRulesCWProxy get copyWith => _$InfraRulesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraRules _$InfraRulesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InfraRules', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['rules']);
      final val = InfraRules(
        version: $checkedConvert('version', (v) => v as String?),
        rules: $checkedConvert(
          'rules',
          (v) => (v as List<dynamic>)
              .map(
                (e) => InfraRulesRulesInner.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$InfraRulesToJson(InfraRules instance) =>
    <String, dynamic>{
      'version': ?instance.version,
      'rules': instance.rules.map((e) => e.toJson()).toList(),
    };
