// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intelligence_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelligenceEventCWProxy {
  IntelligenceEvent id(String id);

  IntelligenceEvent type(IntelligenceEventTypeEnum type);

  IntelligenceEvent title(String title);

  IntelligenceEvent status(IntelligenceEventStatusEnum status);

  IntelligenceEvent severity(Severity severity);

  IntelligenceEvent severityOverridden(bool? severityOverridden);

  IntelligenceEvent private(bool? private);

  IntelligenceEvent origin(IntelligenceEventOriginEnum origin);

  IntelligenceEvent recipeId(String? recipeId);

  IntelligenceEvent recipeVersion(int? recipeVersion);

  IntelligenceEvent detector(DetectorRef detector);

  IntelligenceEvent mergedInto(String? mergedInto);

  IntelligenceEvent notes(String? notes);

  IntelligenceEvent version(int version);

  IntelligenceEvent createdAt(DateTime createdAt);

  IntelligenceEvent updatedAt(DateTime updatedAt);

  IntelligenceEvent evidence(IntelligenceEventEvidence evidence);

  IntelligenceEvent inference(IntelligenceEventInference inference);

  IntelligenceEvent speculation(IntelligenceEventSpeculation speculation);

  IntelligenceEvent context(IntelligenceEventContext? context);

  IntelligenceEvent exposure(Exposure? exposure);

  IntelligenceEvent log(List<IntelEventLogEntry>? log);

  IntelligenceEvent manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelligenceEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelligenceEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelligenceEvent call({
    String id,
    IntelligenceEventTypeEnum type,
    String title,
    IntelligenceEventStatusEnum status,
    Severity severity,
    bool? severityOverridden,
    bool? private,
    IntelligenceEventOriginEnum origin,
    String? recipeId,
    int? recipeVersion,
    DetectorRef detector,
    String? mergedInto,
    String? notes,
    int version,
    DateTime createdAt,
    DateTime updatedAt,
    IntelligenceEventEvidence evidence,
    IntelligenceEventInference inference,
    IntelligenceEventSpeculation speculation,
    IntelligenceEventContext? context,
    Exposure? exposure,
    List<IntelEventLogEntry>? log,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelligenceEvent.copyWith(...)` or call `instanceOfIntelligenceEvent.copyWith.fieldName(value)` for a single field.
class _$IntelligenceEventCWProxyImpl implements _$IntelligenceEventCWProxy {
  const _$IntelligenceEventCWProxyImpl(this._value);

  final IntelligenceEvent _value;

  @override
  IntelligenceEvent id(String id) => call(id: id);

  @override
  IntelligenceEvent type(IntelligenceEventTypeEnum type) => call(type: type);

  @override
  IntelligenceEvent title(String title) => call(title: title);

  @override
  IntelligenceEvent status(IntelligenceEventStatusEnum status) =>
      call(status: status);

  @override
  IntelligenceEvent severity(Severity severity) => call(severity: severity);

  @override
  IntelligenceEvent severityOverridden(bool? severityOverridden) =>
      call(severityOverridden: severityOverridden);

  @override
  IntelligenceEvent private(bool? private) => call(private: private);

  @override
  IntelligenceEvent origin(IntelligenceEventOriginEnum origin) =>
      call(origin: origin);

  @override
  IntelligenceEvent recipeId(String? recipeId) => call(recipeId: recipeId);

  @override
  IntelligenceEvent recipeVersion(int? recipeVersion) =>
      call(recipeVersion: recipeVersion);

  @override
  IntelligenceEvent detector(DetectorRef detector) => call(detector: detector);

  @override
  IntelligenceEvent mergedInto(String? mergedInto) =>
      call(mergedInto: mergedInto);

  @override
  IntelligenceEvent notes(String? notes) => call(notes: notes);

  @override
  IntelligenceEvent version(int version) => call(version: version);

  @override
  IntelligenceEvent createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  IntelligenceEvent updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  @override
  IntelligenceEvent evidence(IntelligenceEventEvidence evidence) =>
      call(evidence: evidence);

  @override
  IntelligenceEvent inference(IntelligenceEventInference inference) =>
      call(inference: inference);

  @override
  IntelligenceEvent speculation(IntelligenceEventSpeculation speculation) =>
      call(speculation: speculation);

  @override
  IntelligenceEvent context(IntelligenceEventContext? context) =>
      call(context: context);

  @override
  IntelligenceEvent exposure(Exposure? exposure) => call(exposure: exposure);

  @override
  IntelligenceEvent log(List<IntelEventLogEntry>? log) => call(log: log);

  @override
  IntelligenceEvent manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelligenceEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelligenceEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelligenceEvent call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? severityOverridden = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? recipeId = const $CopyWithPlaceholder(),
    Object? recipeVersion = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? inference = const $CopyWithPlaceholder(),
    Object? speculation = const $CopyWithPlaceholder(),
    Object? context = const $CopyWithPlaceholder(),
    Object? exposure = const $CopyWithPlaceholder(),
    Object? log = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return IntelligenceEvent(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as IntelligenceEventTypeEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as IntelligenceEventStatusEnum,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity,
      severityOverridden: severityOverridden == const $CopyWithPlaceholder()
          ? _value.severityOverridden
          // ignore: cast_nullable_to_non_nullable
          : severityOverridden as bool?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as IntelligenceEventOriginEnum,
      recipeId: recipeId == const $CopyWithPlaceholder()
          ? _value.recipeId
          // ignore: cast_nullable_to_non_nullable
          : recipeId as String?,
      recipeVersion: recipeVersion == const $CopyWithPlaceholder()
          ? _value.recipeVersion
          // ignore: cast_nullable_to_non_nullable
          : recipeVersion as int?,
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
      notes: notes == const $CopyWithPlaceholder()
          ? _value.notes
          // ignore: cast_nullable_to_non_nullable
          : notes as String?,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
      evidence: evidence == const $CopyWithPlaceholder() || evidence == null
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as IntelligenceEventEvidence,
      inference: inference == const $CopyWithPlaceholder() || inference == null
          ? _value.inference
          // ignore: cast_nullable_to_non_nullable
          : inference as IntelligenceEventInference,
      speculation:
          speculation == const $CopyWithPlaceholder() || speculation == null
          ? _value.speculation
          // ignore: cast_nullable_to_non_nullable
          : speculation as IntelligenceEventSpeculation,
      context: context == const $CopyWithPlaceholder()
          ? _value.context
          // ignore: cast_nullable_to_non_nullable
          : context as IntelligenceEventContext?,
      exposure: exposure == const $CopyWithPlaceholder()
          ? _value.exposure
          // ignore: cast_nullable_to_non_nullable
          : exposure as Exposure?,
      log: log == const $CopyWithPlaceholder()
          ? _value.log
          // ignore: cast_nullable_to_non_nullable
          : log as List<IntelEventLogEntry>?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $IntelligenceEventCopyWith on IntelligenceEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelligenceEvent.copyWith(...)` or `instanceOfIntelligenceEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelligenceEventCWProxy get copyWith =>
      _$IntelligenceEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelligenceEvent _$IntelligenceEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelligenceEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'type',
      'title',
      'status',
      'severity',
      'origin',
      'detector',
      'version',
      'createdAt',
      'updatedAt',
      'evidence',
      'inference',
      'speculation',
    ],
  );
  final val = IntelligenceEvent(
    id: $checkedConvert('id', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$IntelligenceEventTypeEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$IntelligenceEventStatusEnumEnumMap, v),
    ),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecode(_$SeverityEnumMap, v),
    ),
    severityOverridden: $checkedConvert(
      'severityOverridden',
      (v) => v as bool?,
    ),
    private: $checkedConvert('private', (v) => v as bool?),
    origin: $checkedConvert(
      'origin',
      (v) => $enumDecode(_$IntelligenceEventOriginEnumEnumMap, v),
    ),
    recipeId: $checkedConvert('recipeId', (v) => v as String?),
    recipeVersion: $checkedConvert(
      'recipeVersion',
      (v) => (v as num?)?.toInt(),
    ),
    detector: $checkedConvert(
      'detector',
      (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
    notes: $checkedConvert('notes', (v) => v as String?),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
    evidence: $checkedConvert(
      'evidence',
      (v) => IntelligenceEventEvidence.fromJson(v as Map<String, dynamic>),
    ),
    inference: $checkedConvert(
      'inference',
      (v) => IntelligenceEventInference.fromJson(v as Map<String, dynamic>),
    ),
    speculation: $checkedConvert(
      'speculation',
      (v) => IntelligenceEventSpeculation.fromJson(v as Map<String, dynamic>),
    ),
    context: $checkedConvert(
      'context',
      (v) => v == null
          ? null
          : IntelligenceEventContext.fromJson(v as Map<String, dynamic>),
    ),
    exposure: $checkedConvert(
      'exposure',
      (v) => v == null ? null : Exposure.fromJson(v as Map<String, dynamic>),
    ),
    log: $checkedConvert(
      'log',
      (v) => (v as List<dynamic>?)
          ?.map((e) => IntelEventLogEntry.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$IntelligenceEventToJson(IntelligenceEvent instance) =>
    <String, dynamic>{
      'id': instance.id,
      'type': _$IntelligenceEventTypeEnumEnumMap[instance.type]!,
      'title': instance.title,
      'status': _$IntelligenceEventStatusEnumEnumMap[instance.status]!,
      'severity': _$SeverityEnumMap[instance.severity]!,
      'severityOverridden': ?instance.severityOverridden,
      'private': ?instance.private,
      'origin': _$IntelligenceEventOriginEnumEnumMap[instance.origin]!,
      'recipeId': ?instance.recipeId,
      'recipeVersion': ?instance.recipeVersion,
      'detector': instance.detector.toJson(),
      'mergedInto': ?instance.mergedInto,
      'notes': ?instance.notes,
      'version': instance.version,
      'createdAt': instance.createdAt.toIso8601String(),
      'updatedAt': instance.updatedAt.toIso8601String(),
      'evidence': instance.evidence.toJson(),
      'inference': instance.inference.toJson(),
      'speculation': instance.speculation.toJson(),
      'context': ?instance.context?.toJson(),
      'exposure': ?instance.exposure?.toJson(),
      'log': ?instance.log?.map((e) => e.toJson()).toList(),
      'manifestId': ?instance.manifestId,
    };

const _$IntelligenceEventTypeEnumEnumMap = {
  IntelligenceEventTypeEnum.maritimeDisruption: 'maritime-disruption',
  IntelligenceEventTypeEnum.aviationAnomaly: 'aviation-anomaly',
  IntelligenceEventTypeEnum.infrastructureDisruption:
      'infrastructure-disruption',
  IntelligenceEventTypeEnum.weatherImpact: 'weather-impact',
  IntelligenceEventTypeEnum.seismic: 'seismic',
  IntelligenceEventTypeEnum.custom: 'custom',
};

const _$IntelligenceEventStatusEnumEnumMap = {
  IntelligenceEventStatusEnum.emerging: 'emerging',
  IntelligenceEventStatusEnum.active: 'active',
  IntelligenceEventStatusEnum.resolved: 'resolved',
  IntelligenceEventStatusEnum.dismissed: 'dismissed',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};

const _$IntelligenceEventOriginEnumEnumMap = {
  IntelligenceEventOriginEnum.recipe: 'recipe',
  IntelligenceEventOriginEnum.analyst: 'analyst',
  IntelligenceEventOriginEnum.agent: 'agent',
  IntelligenceEventOriginEnum.operator_: 'operator',
};
