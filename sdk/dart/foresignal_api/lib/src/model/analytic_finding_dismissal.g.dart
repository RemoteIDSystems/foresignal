// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analytic_finding_dismissal.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalyticFindingDismissalCWProxy {
  AnalyticFindingDismissal reason(String reason);

  AnalyticFindingDismissal at(DateTime at);

  AnalyticFindingDismissal by(String by);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalyticFindingDismissal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalyticFindingDismissal(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalyticFindingDismissal call({String reason, DateTime at, String by});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalyticFindingDismissal.copyWith(...)` or call `instanceOfAnalyticFindingDismissal.copyWith.fieldName(value)` for a single field.
class _$AnalyticFindingDismissalCWProxyImpl
    implements _$AnalyticFindingDismissalCWProxy {
  const _$AnalyticFindingDismissalCWProxyImpl(this._value);

  final AnalyticFindingDismissal _value;

  @override
  AnalyticFindingDismissal reason(String reason) => call(reason: reason);

  @override
  AnalyticFindingDismissal at(DateTime at) => call(at: at);

  @override
  AnalyticFindingDismissal by(String by) => call(by: by);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalyticFindingDismissal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalyticFindingDismissal(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalyticFindingDismissal call({
    Object? reason = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? by = const $CopyWithPlaceholder(),
  }) {
    return AnalyticFindingDismissal(
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      by: by == const $CopyWithPlaceholder() || by == null
          ? _value.by
          // ignore: cast_nullable_to_non_nullable
          : by as String,
    );
  }
}

extension $AnalyticFindingDismissalCopyWith on AnalyticFindingDismissal {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalyticFindingDismissal.copyWith(...)` or `instanceOfAnalyticFindingDismissal.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalyticFindingDismissalCWProxy get copyWith =>
      _$AnalyticFindingDismissalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalyticFindingDismissal _$AnalyticFindingDismissalFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalyticFindingDismissal', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['reason', 'at', 'by']);
  final val = AnalyticFindingDismissal(
    reason: $checkedConvert('reason', (v) => v as String),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    by: $checkedConvert('by', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$AnalyticFindingDismissalToJson(
  AnalyticFindingDismissal instance,
) => <String, dynamic>{
  'reason': instance.reason,
  'at': instance.at.toIso8601String(),
  'by': instance.by,
};
