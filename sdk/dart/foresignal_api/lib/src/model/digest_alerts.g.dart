// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_alerts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestAlertsCWProxy {
  DigestAlerts sinceAlertId(int? sinceAlertId);

  DigestAlerts untilAlertId(int? untilAlertId);

  DigestAlerts bySeverity(DigestAlertsBySeverity? bySeverity);

  DigestAlerts byStatus(DigestAlertsByStatus? byStatus);

  DigestAlerts byRule(List<DigestAlertsByRuleInner>? byRule);

  DigestAlerts items(List<Alert>? items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestAlerts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestAlerts(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestAlerts call({
    int? sinceAlertId,
    int? untilAlertId,
    DigestAlertsBySeverity? bySeverity,
    DigestAlertsByStatus? byStatus,
    List<DigestAlertsByRuleInner>? byRule,
    List<Alert>? items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestAlerts.copyWith(...)` or call `instanceOfDigestAlerts.copyWith.fieldName(value)` for a single field.
class _$DigestAlertsCWProxyImpl implements _$DigestAlertsCWProxy {
  const _$DigestAlertsCWProxyImpl(this._value);

  final DigestAlerts _value;

  @override
  DigestAlerts sinceAlertId(int? sinceAlertId) =>
      call(sinceAlertId: sinceAlertId);

  @override
  DigestAlerts untilAlertId(int? untilAlertId) =>
      call(untilAlertId: untilAlertId);

  @override
  DigestAlerts bySeverity(DigestAlertsBySeverity? bySeverity) =>
      call(bySeverity: bySeverity);

  @override
  DigestAlerts byStatus(DigestAlertsByStatus? byStatus) =>
      call(byStatus: byStatus);

  @override
  DigestAlerts byRule(List<DigestAlertsByRuleInner>? byRule) =>
      call(byRule: byRule);

  @override
  DigestAlerts items(List<Alert>? items) => call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestAlerts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestAlerts(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestAlerts call({
    Object? sinceAlertId = const $CopyWithPlaceholder(),
    Object? untilAlertId = const $CopyWithPlaceholder(),
    Object? bySeverity = const $CopyWithPlaceholder(),
    Object? byStatus = const $CopyWithPlaceholder(),
    Object? byRule = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return DigestAlerts(
      sinceAlertId: sinceAlertId == const $CopyWithPlaceholder()
          ? _value.sinceAlertId
          // ignore: cast_nullable_to_non_nullable
          : sinceAlertId as int?,
      untilAlertId: untilAlertId == const $CopyWithPlaceholder()
          ? _value.untilAlertId
          // ignore: cast_nullable_to_non_nullable
          : untilAlertId as int?,
      bySeverity: bySeverity == const $CopyWithPlaceholder()
          ? _value.bySeverity
          // ignore: cast_nullable_to_non_nullable
          : bySeverity as DigestAlertsBySeverity?,
      byStatus: byStatus == const $CopyWithPlaceholder()
          ? _value.byStatus
          // ignore: cast_nullable_to_non_nullable
          : byStatus as DigestAlertsByStatus?,
      byRule: byRule == const $CopyWithPlaceholder()
          ? _value.byRule
          // ignore: cast_nullable_to_non_nullable
          : byRule as List<DigestAlertsByRuleInner>?,
      items: items == const $CopyWithPlaceholder()
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Alert>?,
    );
  }
}

extension $DigestAlertsCopyWith on DigestAlerts {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestAlerts.copyWith(...)` or `instanceOfDigestAlerts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestAlertsCWProxy get copyWith => _$DigestAlertsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestAlerts _$DigestAlertsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DigestAlerts', json, ($checkedConvert) {
  final val = DigestAlerts(
    sinceAlertId: $checkedConvert('sinceAlertId', (v) => (v as num?)?.toInt()),
    untilAlertId: $checkedConvert('untilAlertId', (v) => (v as num?)?.toInt()),
    bySeverity: $checkedConvert(
      'bySeverity',
      (v) => v == null
          ? null
          : DigestAlertsBySeverity.fromJson(v as Map<String, dynamic>),
    ),
    byStatus: $checkedConvert(
      'byStatus',
      (v) => v == null
          ? null
          : DigestAlertsByStatus.fromJson(v as Map<String, dynamic>),
    ),
    byRule: $checkedConvert(
      'byRule',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => DigestAlertsByRuleInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Alert.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DigestAlertsToJson(DigestAlerts instance) =>
    <String, dynamic>{
      'sinceAlertId': ?instance.sinceAlertId,
      'untilAlertId': ?instance.untilAlertId,
      'bySeverity': ?instance.bySeverity?.toJson(),
      'byStatus': ?instance.byStatus?.toJson(),
      'byRule': ?instance.byRule?.map((e) => e.toJson()).toList(),
      'items': ?instance.items?.map((e) => e.toJson()).toList(),
    };
