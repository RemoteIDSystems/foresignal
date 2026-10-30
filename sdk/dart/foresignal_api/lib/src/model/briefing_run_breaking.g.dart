// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'briefing_run_breaking.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BriefingRunBreakingCWProxy {
  BriefingRunBreaking alertId(int alertId);

  BriefingRunBreaking ruleId(String? ruleId);

  BriefingRunBreaking ruleName(String? ruleName);

  BriefingRunBreaking severity(BriefingRunBreakingSeverityEnum severity);

  BriefingRunBreaking firedAt(DateTime? firedAt);

  BriefingRunBreaking message(String? message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingRunBreaking(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingRunBreaking(...).copyWith(id: 12, name: "My name")
  /// ```
  BriefingRunBreaking call({
    int alertId,
    String? ruleId,
    String? ruleName,
    BriefingRunBreakingSeverityEnum severity,
    DateTime? firedAt,
    String? message,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBriefingRunBreaking.copyWith(...)` or call `instanceOfBriefingRunBreaking.copyWith.fieldName(value)` for a single field.
class _$BriefingRunBreakingCWProxyImpl implements _$BriefingRunBreakingCWProxy {
  const _$BriefingRunBreakingCWProxyImpl(this._value);

  final BriefingRunBreaking _value;

  @override
  BriefingRunBreaking alertId(int alertId) => call(alertId: alertId);

  @override
  BriefingRunBreaking ruleId(String? ruleId) => call(ruleId: ruleId);

  @override
  BriefingRunBreaking ruleName(String? ruleName) => call(ruleName: ruleName);

  @override
  BriefingRunBreaking severity(BriefingRunBreakingSeverityEnum severity) =>
      call(severity: severity);

  @override
  BriefingRunBreaking firedAt(DateTime? firedAt) => call(firedAt: firedAt);

  @override
  BriefingRunBreaking message(String? message) => call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingRunBreaking(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingRunBreaking(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BriefingRunBreaking call({
    Object? alertId = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? ruleName = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? firedAt = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
  }) {
    return BriefingRunBreaking(
      alertId: alertId == const $CopyWithPlaceholder() || alertId == null
          ? _value.alertId
          // ignore: cast_nullable_to_non_nullable
          : alertId as int,
      ruleId: ruleId == const $CopyWithPlaceholder()
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String?,
      ruleName: ruleName == const $CopyWithPlaceholder()
          ? _value.ruleName
          // ignore: cast_nullable_to_non_nullable
          : ruleName as String?,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as BriefingRunBreakingSeverityEnum,
      firedAt: firedAt == const $CopyWithPlaceholder()
          ? _value.firedAt
          // ignore: cast_nullable_to_non_nullable
          : firedAt as DateTime?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
    );
  }
}

extension $BriefingRunBreakingCopyWith on BriefingRunBreaking {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBriefingRunBreaking.copyWith(...)` or `instanceOfBriefingRunBreaking.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BriefingRunBreakingCWProxy get copyWith =>
      _$BriefingRunBreakingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BriefingRunBreaking _$BriefingRunBreakingFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BriefingRunBreaking', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['alertId', 'severity']);
      final val = BriefingRunBreaking(
        alertId: $checkedConvert('alertId', (v) => (v as num).toInt()),
        ruleId: $checkedConvert('ruleId', (v) => v as String?),
        ruleName: $checkedConvert('ruleName', (v) => v as String?),
        severity: $checkedConvert(
          'severity',
          (v) => $enumDecode(_$BriefingRunBreakingSeverityEnumEnumMap, v),
        ),
        firedAt: $checkedConvert(
          'firedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        message: $checkedConvert('message', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BriefingRunBreakingToJson(
  BriefingRunBreaking instance,
) => <String, dynamic>{
  'alertId': instance.alertId,
  'ruleId': ?instance.ruleId,
  'ruleName': ?instance.ruleName,
  'severity': _$BriefingRunBreakingSeverityEnumEnumMap[instance.severity]!,
  'firedAt': ?instance.firedAt?.toIso8601String(),
  'message': ?instance.message,
};

const _$BriefingRunBreakingSeverityEnumEnumMap = {
  BriefingRunBreakingSeverityEnum.info: 'info',
  BriefingRunBreakingSeverityEnum.warning: 'warning',
  BriefingRunBreakingSeverityEnum.critical: 'critical',
};
