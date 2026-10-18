// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_report.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorReportCWProxy {
  DoctorReport id(String id);

  DoctorReport sourceId(String sourceId);

  DoctorReport createdAt(DateTime createdAt);

  DoctorReport trigger(DoctorReportTriggerEnum trigger);

  DoctorReport status(DoctorReportStatusEnum status);

  DoctorReport findings(List<DoctorFinding> findings);

  DoctorReport diff(StructureDiff? diff);

  DoctorReport probe(DoctorReportProbe? probe);

  DoctorReport fixture(DoctorReportFixture? fixture);

  DoctorReport llm(DoctorReportLlm? llm);

  DoctorReport adapterVersion(String? adapterVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReport(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorReport call({
    String id,
    String sourceId,
    DateTime createdAt,
    DoctorReportTriggerEnum trigger,
    DoctorReportStatusEnum status,
    List<DoctorFinding> findings,
    StructureDiff? diff,
    DoctorReportProbe? probe,
    DoctorReportFixture? fixture,
    DoctorReportLlm? llm,
    String? adapterVersion,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorReport.copyWith(...)` or call `instanceOfDoctorReport.copyWith.fieldName(value)` for a single field.
class _$DoctorReportCWProxyImpl implements _$DoctorReportCWProxy {
  const _$DoctorReportCWProxyImpl(this._value);

  final DoctorReport _value;

  @override
  DoctorReport id(String id) => call(id: id);

  @override
  DoctorReport sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  DoctorReport createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  DoctorReport trigger(DoctorReportTriggerEnum trigger) =>
      call(trigger: trigger);

  @override
  DoctorReport status(DoctorReportStatusEnum status) => call(status: status);

  @override
  DoctorReport findings(List<DoctorFinding> findings) =>
      call(findings: findings);

  @override
  DoctorReport diff(StructureDiff? diff) => call(diff: diff);

  @override
  DoctorReport probe(DoctorReportProbe? probe) => call(probe: probe);

  @override
  DoctorReport fixture(DoctorReportFixture? fixture) => call(fixture: fixture);

  @override
  DoctorReport llm(DoctorReportLlm? llm) => call(llm: llm);

  @override
  DoctorReport adapterVersion(String? adapterVersion) =>
      call(adapterVersion: adapterVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReport(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorReport call({
    Object? id = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? trigger = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? findings = const $CopyWithPlaceholder(),
    Object? diff = const $CopyWithPlaceholder(),
    Object? probe = const $CopyWithPlaceholder(),
    Object? fixture = const $CopyWithPlaceholder(),
    Object? llm = const $CopyWithPlaceholder(),
    Object? adapterVersion = const $CopyWithPlaceholder(),
  }) {
    return DoctorReport(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      trigger: trigger == const $CopyWithPlaceholder() || trigger == null
          ? _value.trigger
          // ignore: cast_nullable_to_non_nullable
          : trigger as DoctorReportTriggerEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DoctorReportStatusEnum,
      findings: findings == const $CopyWithPlaceholder() || findings == null
          ? _value.findings
          // ignore: cast_nullable_to_non_nullable
          : findings as List<DoctorFinding>,
      diff: diff == const $CopyWithPlaceholder()
          ? _value.diff
          // ignore: cast_nullable_to_non_nullable
          : diff as StructureDiff?,
      probe: probe == const $CopyWithPlaceholder()
          ? _value.probe
          // ignore: cast_nullable_to_non_nullable
          : probe as DoctorReportProbe?,
      fixture: fixture == const $CopyWithPlaceholder()
          ? _value.fixture
          // ignore: cast_nullable_to_non_nullable
          : fixture as DoctorReportFixture?,
      llm: llm == const $CopyWithPlaceholder()
          ? _value.llm
          // ignore: cast_nullable_to_non_nullable
          : llm as DoctorReportLlm?,
      adapterVersion: adapterVersion == const $CopyWithPlaceholder()
          ? _value.adapterVersion
          // ignore: cast_nullable_to_non_nullable
          : adapterVersion as String?,
    );
  }
}

extension $DoctorReportCopyWith on DoctorReport {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorReport.copyWith(...)` or `instanceOfDoctorReport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorReportCWProxy get copyWith => _$DoctorReportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorReport _$DoctorReportFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DoctorReport', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'sourceId',
      'createdAt',
      'trigger',
      'status',
      'findings',
    ],
  );
  final val = DoctorReport(
    id: $checkedConvert('id', (v) => v as String),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    trigger: $checkedConvert(
      'trigger',
      (v) => $enumDecode(_$DoctorReportTriggerEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DoctorReportStatusEnumEnumMap, v),
    ),
    findings: $checkedConvert(
      'findings',
      (v) => (v as List<dynamic>)
          .map((e) => DoctorFinding.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    diff: $checkedConvert(
      'diff',
      (v) =>
          v == null ? null : StructureDiff.fromJson(v as Map<String, dynamic>),
    ),
    probe: $checkedConvert(
      'probe',
      (v) => v == null
          ? null
          : DoctorReportProbe.fromJson(v as Map<String, dynamic>),
    ),
    fixture: $checkedConvert(
      'fixture',
      (v) => v == null
          ? null
          : DoctorReportFixture.fromJson(v as Map<String, dynamic>),
    ),
    llm: $checkedConvert(
      'llm',
      (v) => v == null
          ? null
          : DoctorReportLlm.fromJson(v as Map<String, dynamic>),
    ),
    adapterVersion: $checkedConvert('adapterVersion', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$DoctorReportToJson(DoctorReport instance) =>
    <String, dynamic>{
      'id': instance.id,
      'sourceId': instance.sourceId,
      'createdAt': instance.createdAt.toIso8601String(),
      'trigger': _$DoctorReportTriggerEnumEnumMap[instance.trigger]!,
      'status': _$DoctorReportStatusEnumEnumMap[instance.status]!,
      'findings': instance.findings.map((e) => e.toJson()).toList(),
      'diff': ?instance.diff?.toJson(),
      'probe': ?instance.probe?.toJson(),
      'fixture': ?instance.fixture?.toJson(),
      'llm': ?instance.llm?.toJson(),
      'adapterVersion': ?instance.adapterVersion,
    };

const _$DoctorReportTriggerEnumEnumMap = {
  DoctorReportTriggerEnum.MANUAL: 'MANUAL',
  DoctorReportTriggerEnum.AUTO: 'AUTO',
  DoctorReportTriggerEnum.MCP: 'MCP',
};

const _$DoctorReportStatusEnumEnumMap = {
  DoctorReportStatusEnum.OK: 'OK',
  DoctorReportStatusEnum.DEGRADED_INFO: 'DEGRADED_INFO',
  DoctorReportStatusEnum.BROKEN: 'BROKEN',
};
