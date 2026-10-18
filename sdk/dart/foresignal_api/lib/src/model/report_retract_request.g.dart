// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_retract_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportRetractRequestCWProxy {
  ReportRetractRequest revision(int? revision);

  ReportRetractRequest reason(String reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportRetractRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportRetractRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportRetractRequest call({int? revision, String reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportRetractRequest.copyWith(...)` or call `instanceOfReportRetractRequest.copyWith.fieldName(value)` for a single field.
class _$ReportRetractRequestCWProxyImpl
    implements _$ReportRetractRequestCWProxy {
  const _$ReportRetractRequestCWProxyImpl(this._value);

  final ReportRetractRequest _value;

  @override
  ReportRetractRequest revision(int? revision) => call(revision: revision);

  @override
  ReportRetractRequest reason(String reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportRetractRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportRetractRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportRetractRequest call({
    Object? revision = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return ReportRetractRequest(
      revision: revision == const $CopyWithPlaceholder()
          ? _value.revision
          // ignore: cast_nullable_to_non_nullable
          : revision as int?,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
    );
  }
}

extension $ReportRetractRequestCopyWith on ReportRetractRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportRetractRequest.copyWith(...)` or `instanceOfReportRetractRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportRetractRequestCWProxy get copyWith =>
      _$ReportRetractRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportRetractRequest _$ReportRetractRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReportRetractRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['reason']);
  final val = ReportRetractRequest(
    revision: $checkedConvert('revision', (v) => (v as num?)?.toInt()),
    reason: $checkedConvert('reason', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ReportRetractRequestToJson(
  ReportRetractRequest instance,
) => <String, dynamic>{
  'revision': ?instance.revision,
  'reason': instance.reason,
};
