// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_finding.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorFindingCWProxy {
  DoctorFinding code(DoctorFindingCodeEnum code);

  DoctorFinding severity(Severity severity);

  DoctorFinding evidence(List<String> evidence);

  DoctorFinding path(String? path);

  DoctorFinding suggestedAction(String suggestedAction);

  DoctorFinding heuristic(bool? heuristic);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorFinding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorFinding(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorFinding call({
    DoctorFindingCodeEnum code,
    Severity severity,
    List<String> evidence,
    String? path,
    String suggestedAction,
    bool? heuristic,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorFinding.copyWith(...)` or call `instanceOfDoctorFinding.copyWith.fieldName(value)` for a single field.
class _$DoctorFindingCWProxyImpl implements _$DoctorFindingCWProxy {
  const _$DoctorFindingCWProxyImpl(this._value);

  final DoctorFinding _value;

  @override
  DoctorFinding code(DoctorFindingCodeEnum code) => call(code: code);

  @override
  DoctorFinding severity(Severity severity) => call(severity: severity);

  @override
  DoctorFinding evidence(List<String> evidence) => call(evidence: evidence);

  @override
  DoctorFinding path(String? path) => call(path: path);

  @override
  DoctorFinding suggestedAction(String suggestedAction) =>
      call(suggestedAction: suggestedAction);

  @override
  DoctorFinding heuristic(bool? heuristic) => call(heuristic: heuristic);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorFinding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorFinding(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorFinding call({
    Object? code = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? path = const $CopyWithPlaceholder(),
    Object? suggestedAction = const $CopyWithPlaceholder(),
    Object? heuristic = const $CopyWithPlaceholder(),
  }) {
    return DoctorFinding(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as DoctorFindingCodeEnum,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity,
      evidence: evidence == const $CopyWithPlaceholder() || evidence == null
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<String>,
      path: path == const $CopyWithPlaceholder()
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String?,
      suggestedAction:
          suggestedAction == const $CopyWithPlaceholder() ||
              suggestedAction == null
          ? _value.suggestedAction
          // ignore: cast_nullable_to_non_nullable
          : suggestedAction as String,
      heuristic: heuristic == const $CopyWithPlaceholder()
          ? _value.heuristic
          // ignore: cast_nullable_to_non_nullable
          : heuristic as bool?,
    );
  }
}

extension $DoctorFindingCopyWith on DoctorFinding {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorFinding.copyWith(...)` or `instanceOfDoctorFinding.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorFindingCWProxy get copyWith => _$DoctorFindingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorFinding _$DoctorFindingFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DoctorFinding', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['code', 'severity', 'evidence', 'suggestedAction'],
      );
      final val = DoctorFinding(
        code: $checkedConvert(
          'code',
          (v) => $enumDecode(_$DoctorFindingCodeEnumEnumMap, v),
        ),
        severity: $checkedConvert(
          'severity',
          (v) => $enumDecode(_$SeverityEnumMap, v),
        ),
        evidence: $checkedConvert(
          'evidence',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        path: $checkedConvert('path', (v) => v as String?),
        suggestedAction: $checkedConvert('suggestedAction', (v) => v as String),
        heuristic: $checkedConvert('heuristic', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$DoctorFindingToJson(DoctorFinding instance) =>
    <String, dynamic>{
      'code': _$DoctorFindingCodeEnumEnumMap[instance.code]!,
      'severity': _$SeverityEnumMap[instance.severity]!,
      'evidence': instance.evidence,
      'path': ?instance.path,
      'suggestedAction': instance.suggestedAction,
      'heuristic': ?instance.heuristic,
    };

const _$DoctorFindingCodeEnumEnumMap = {
  DoctorFindingCodeEnum.AUTH_FAILED: 'AUTH_FAILED',
  DoctorFindingCodeEnum.UA_BLOCKED: 'UA_BLOCKED',
  DoctorFindingCodeEnum.ENDPOINT_MOVED: 'ENDPOINT_MOVED',
  DoctorFindingCodeEnum.RATE_LIMITED: 'RATE_LIMITED',
  DoctorFindingCodeEnum.UPSTREAM_ERROR: 'UPSTREAM_ERROR',
  DoctorFindingCodeEnum.HTML_INSTEAD_OF_DATA: 'HTML_INSTEAD_OF_DATA',
  DoctorFindingCodeEnum.EMPTY_RESULT: 'EMPTY_RESULT',
  DoctorFindingCodeEnum.FIELD_RENAMED: 'FIELD_RENAMED',
  DoctorFindingCodeEnum.FIELD_REMOVED: 'FIELD_REMOVED',
  DoctorFindingCodeEnum.TYPE_CHANGED: 'TYPE_CHANGED',
  DoctorFindingCodeEnum.FORMAT_CHANGED: 'FORMAT_CHANGED',
  DoctorFindingCodeEnum.PARSER_REJECTS: 'PARSER_REJECTS',
  DoctorFindingCodeEnum.COVERAGE_DROPPED: 'COVERAGE_DROPPED',
  DoctorFindingCodeEnum.NO_CHANGE_DETECTED: 'NO_CHANGE_DETECTED',
  DoctorFindingCodeEnum.PROBE_SKIPPED_BUDGET: 'PROBE_SKIPPED_BUDGET',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
