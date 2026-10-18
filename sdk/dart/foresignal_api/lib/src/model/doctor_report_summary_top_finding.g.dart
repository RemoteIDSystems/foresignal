// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_report_summary_top_finding.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorReportSummaryTopFindingCWProxy {
  DoctorReportSummaryTopFinding code(
    DoctorReportSummaryTopFindingCodeEnum code,
  );

  DoctorReportSummaryTopFinding suggestedAction(String? suggestedAction);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportSummaryTopFinding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportSummaryTopFinding(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorReportSummaryTopFinding call({
    DoctorReportSummaryTopFindingCodeEnum code,
    String? suggestedAction,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorReportSummaryTopFinding.copyWith(...)` or call `instanceOfDoctorReportSummaryTopFinding.copyWith.fieldName(value)` for a single field.
class _$DoctorReportSummaryTopFindingCWProxyImpl
    implements _$DoctorReportSummaryTopFindingCWProxy {
  const _$DoctorReportSummaryTopFindingCWProxyImpl(this._value);

  final DoctorReportSummaryTopFinding _value;

  @override
  DoctorReportSummaryTopFinding code(
    DoctorReportSummaryTopFindingCodeEnum code,
  ) => call(code: code);

  @override
  DoctorReportSummaryTopFinding suggestedAction(String? suggestedAction) =>
      call(suggestedAction: suggestedAction);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportSummaryTopFinding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportSummaryTopFinding(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorReportSummaryTopFinding call({
    Object? code = const $CopyWithPlaceholder(),
    Object? suggestedAction = const $CopyWithPlaceholder(),
  }) {
    return DoctorReportSummaryTopFinding(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as DoctorReportSummaryTopFindingCodeEnum,
      suggestedAction: suggestedAction == const $CopyWithPlaceholder()
          ? _value.suggestedAction
          // ignore: cast_nullable_to_non_nullable
          : suggestedAction as String?,
    );
  }
}

extension $DoctorReportSummaryTopFindingCopyWith
    on DoctorReportSummaryTopFinding {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorReportSummaryTopFinding.copyWith(...)` or `instanceOfDoctorReportSummaryTopFinding.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorReportSummaryTopFindingCWProxy get copyWith =>
      _$DoctorReportSummaryTopFindingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorReportSummaryTopFinding _$DoctorReportSummaryTopFindingFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DoctorReportSummaryTopFinding', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['code']);
  final val = DoctorReportSummaryTopFinding(
    code: $checkedConvert(
      'code',
      (v) => $enumDecode(_$DoctorReportSummaryTopFindingCodeEnumEnumMap, v),
    ),
    suggestedAction: $checkedConvert('suggestedAction', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$DoctorReportSummaryTopFindingToJson(
  DoctorReportSummaryTopFinding instance,
) => <String, dynamic>{
  'code': _$DoctorReportSummaryTopFindingCodeEnumEnumMap[instance.code]!,
  'suggestedAction': ?instance.suggestedAction,
};

const _$DoctorReportSummaryTopFindingCodeEnumEnumMap = {
  DoctorReportSummaryTopFindingCodeEnum.AUTH_FAILED: 'AUTH_FAILED',
  DoctorReportSummaryTopFindingCodeEnum.UA_BLOCKED: 'UA_BLOCKED',
  DoctorReportSummaryTopFindingCodeEnum.ENDPOINT_MOVED: 'ENDPOINT_MOVED',
  DoctorReportSummaryTopFindingCodeEnum.RATE_LIMITED: 'RATE_LIMITED',
  DoctorReportSummaryTopFindingCodeEnum.UPSTREAM_ERROR: 'UPSTREAM_ERROR',
  DoctorReportSummaryTopFindingCodeEnum.HTML_INSTEAD_OF_DATA:
      'HTML_INSTEAD_OF_DATA',
  DoctorReportSummaryTopFindingCodeEnum.EMPTY_RESULT: 'EMPTY_RESULT',
  DoctorReportSummaryTopFindingCodeEnum.FIELD_RENAMED: 'FIELD_RENAMED',
  DoctorReportSummaryTopFindingCodeEnum.FIELD_REMOVED: 'FIELD_REMOVED',
  DoctorReportSummaryTopFindingCodeEnum.TYPE_CHANGED: 'TYPE_CHANGED',
  DoctorReportSummaryTopFindingCodeEnum.FORMAT_CHANGED: 'FORMAT_CHANGED',
  DoctorReportSummaryTopFindingCodeEnum.PARSER_REJECTS: 'PARSER_REJECTS',
  DoctorReportSummaryTopFindingCodeEnum.COVERAGE_DROPPED: 'COVERAGE_DROPPED',
  DoctorReportSummaryTopFindingCodeEnum.NO_CHANGE_DETECTED:
      'NO_CHANGE_DETECTED',
  DoctorReportSummaryTopFindingCodeEnum.PROBE_SKIPPED_BUDGET:
      'PROBE_SKIPPED_BUDGET',
};
