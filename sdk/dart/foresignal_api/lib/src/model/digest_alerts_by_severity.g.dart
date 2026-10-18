// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_alerts_by_severity.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestAlertsBySeverityCWProxy {
  DigestAlertsBySeverity critical(int? critical);

  DigestAlertsBySeverity warning(int? warning);

  DigestAlertsBySeverity info(int? info);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestAlertsBySeverity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestAlertsBySeverity(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestAlertsBySeverity call({int? critical, int? warning, int? info});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestAlertsBySeverity.copyWith(...)` or call `instanceOfDigestAlertsBySeverity.copyWith.fieldName(value)` for a single field.
class _$DigestAlertsBySeverityCWProxyImpl
    implements _$DigestAlertsBySeverityCWProxy {
  const _$DigestAlertsBySeverityCWProxyImpl(this._value);

  final DigestAlertsBySeverity _value;

  @override
  DigestAlertsBySeverity critical(int? critical) => call(critical: critical);

  @override
  DigestAlertsBySeverity warning(int? warning) => call(warning: warning);

  @override
  DigestAlertsBySeverity info(int? info) => call(info: info);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestAlertsBySeverity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestAlertsBySeverity(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestAlertsBySeverity call({
    Object? critical = const $CopyWithPlaceholder(),
    Object? warning = const $CopyWithPlaceholder(),
    Object? info = const $CopyWithPlaceholder(),
  }) {
    return DigestAlertsBySeverity(
      critical: critical == const $CopyWithPlaceholder()
          ? _value.critical
          // ignore: cast_nullable_to_non_nullable
          : critical as int?,
      warning: warning == const $CopyWithPlaceholder()
          ? _value.warning
          // ignore: cast_nullable_to_non_nullable
          : warning as int?,
      info: info == const $CopyWithPlaceholder()
          ? _value.info
          // ignore: cast_nullable_to_non_nullable
          : info as int?,
    );
  }
}

extension $DigestAlertsBySeverityCopyWith on DigestAlertsBySeverity {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestAlertsBySeverity.copyWith(...)` or `instanceOfDigestAlertsBySeverity.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestAlertsBySeverityCWProxy get copyWith =>
      _$DigestAlertsBySeverityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestAlertsBySeverity _$DigestAlertsBySeverityFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DigestAlertsBySeverity', json, ($checkedConvert) {
  final val = DigestAlertsBySeverity(
    critical: $checkedConvert('critical', (v) => (v as num?)?.toInt()),
    warning: $checkedConvert('warning', (v) => (v as num?)?.toInt()),
    info: $checkedConvert('info', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$DigestAlertsBySeverityToJson(
  DigestAlertsBySeverity instance,
) => <String, dynamic>{
  'critical': ?instance.critical,
  'warning': ?instance.warning,
  'info': ?instance.info,
};
