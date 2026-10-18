// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stream_alerts200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StreamAlerts200ResponseCWProxy {
  StreamAlerts200Response type(StreamAlerts200ResponseTypeEnum type);

  StreamAlerts200Response alert(Alert alert);

  StreamAlerts200Response suppressed(bool? suppressed);

  StreamAlerts200Response exercise(bool? exercise);

  StreamAlerts200Response reason(StreamAlerts200ResponseReasonEnum reason);

  StreamAlerts200Response resumeAfter(int? resumeAfter);

  StreamAlerts200Response watermarkSeq(int? watermarkSeq);

  StreamAlerts200Response id(int id);

  StreamAlerts200Response ruleId(String ruleId);

  StreamAlerts200Response ruleName(String? ruleName);

  StreamAlerts200Response seq(int seq);

  StreamAlerts200Response source_(String source_);

  StreamAlerts200Response externalId(String externalId);

  StreamAlerts200Response kind(String? kind);

  StreamAlerts200Response firedAt(DateTime firedAt);

  StreamAlerts200Response message(String message);

  StreamAlerts200Response acknowledgedAt(DateTime? acknowledgedAt);

  StreamAlerts200Response severity(
    StreamAlerts200ResponseSeverityEnum severity,
  );

  StreamAlerts200Response summary(String? summary);

  StreamAlerts200Response enrichedAt(DateTime? enrichedAt);

  StreamAlerts200Response record(Record? record);

  StreamAlerts200Response rev(int rev);

  StreamAlerts200Response state(StreamAlerts200ResponseStateEnum state);

  StreamAlerts200Response observedAt(DateTime? observedAt);

  StreamAlerts200Response acknowledgedBy(String? acknowledgedBy);

  StreamAlerts200Response resolvedAt(DateTime? resolvedAt);

  StreamAlerts200Response resolvedBy(String? resolvedBy);

  StreamAlerts200Response resolvedSeq(int? resolvedSeq);

  StreamAlerts200Response feedback(
    StreamAlerts200ResponseFeedbackEnum? feedback,
  );

  StreamAlerts200Response feedbackAt(DateTime? feedbackAt);

  StreamAlerts200Response note(String? note);

  StreamAlerts200Response snoozedUntil(DateTime? snoozedUntil);

  StreamAlerts200Response suppressedReason(
    StreamAlerts200ResponseSuppressedReasonEnum? suppressedReason,
  );

  StreamAlerts200Response tags(List<String>? tags);

  StreamAlerts200Response recordSnapshot(AlertRecordSnapshot? recordSnapshot);

  StreamAlerts200Response summaryCitations(List<AskCitation>? summaryCitations);

  StreamAlerts200Response sensitive(bool? sensitive);

  StreamAlerts200Response region(RegionTags? region);

  StreamAlerts200Response context(AlertContext? context);

  StreamAlerts200Response exerciseRunId(String? exerciseRunId);

  StreamAlerts200Response signals(List<AlertSignal>? signals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StreamAlerts200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StreamAlerts200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  StreamAlerts200Response call({
    StreamAlerts200ResponseTypeEnum type,
    Alert alert,
    bool? suppressed,
    bool? exercise,
    StreamAlerts200ResponseReasonEnum reason,
    int? resumeAfter,
    int? watermarkSeq,
    int id,
    String ruleId,
    String? ruleName,
    int seq,
    String source_,
    String externalId,
    String? kind,
    DateTime firedAt,
    String message,
    DateTime? acknowledgedAt,
    StreamAlerts200ResponseSeverityEnum severity,
    String? summary,
    DateTime? enrichedAt,
    Record? record,
    int rev,
    StreamAlerts200ResponseStateEnum state,
    DateTime? observedAt,
    String? acknowledgedBy,
    DateTime? resolvedAt,
    String? resolvedBy,
    int? resolvedSeq,
    StreamAlerts200ResponseFeedbackEnum? feedback,
    DateTime? feedbackAt,
    String? note,
    DateTime? snoozedUntil,
    StreamAlerts200ResponseSuppressedReasonEnum? suppressedReason,
    List<String>? tags,
    AlertRecordSnapshot? recordSnapshot,
    List<AskCitation>? summaryCitations,
    bool? sensitive,
    RegionTags? region,
    AlertContext? context,
    String? exerciseRunId,
    List<AlertSignal>? signals,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStreamAlerts200Response.copyWith(...)` or call `instanceOfStreamAlerts200Response.copyWith.fieldName(value)` for a single field.
class _$StreamAlerts200ResponseCWProxyImpl
    implements _$StreamAlerts200ResponseCWProxy {
  const _$StreamAlerts200ResponseCWProxyImpl(this._value);

  final StreamAlerts200Response _value;

  @override
  StreamAlerts200Response type(StreamAlerts200ResponseTypeEnum type) =>
      call(type: type);

  @override
  StreamAlerts200Response alert(Alert alert) => call(alert: alert);

  @override
  StreamAlerts200Response suppressed(bool? suppressed) =>
      call(suppressed: suppressed);

  @override
  StreamAlerts200Response exercise(bool? exercise) => call(exercise: exercise);

  @override
  StreamAlerts200Response reason(StreamAlerts200ResponseReasonEnum reason) =>
      call(reason: reason);

  @override
  StreamAlerts200Response resumeAfter(int? resumeAfter) =>
      call(resumeAfter: resumeAfter);

  @override
  StreamAlerts200Response watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  StreamAlerts200Response id(int id) => call(id: id);

  @override
  StreamAlerts200Response ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  StreamAlerts200Response ruleName(String? ruleName) =>
      call(ruleName: ruleName);

  @override
  StreamAlerts200Response seq(int seq) => call(seq: seq);

  @override
  StreamAlerts200Response source_(String source_) => call(source_: source_);

  @override
  StreamAlerts200Response externalId(String externalId) =>
      call(externalId: externalId);

  @override
  StreamAlerts200Response kind(String? kind) => call(kind: kind);

  @override
  StreamAlerts200Response firedAt(DateTime firedAt) => call(firedAt: firedAt);

  @override
  StreamAlerts200Response message(String message) => call(message: message);

  @override
  StreamAlerts200Response acknowledgedAt(DateTime? acknowledgedAt) =>
      call(acknowledgedAt: acknowledgedAt);

  @override
  StreamAlerts200Response severity(
    StreamAlerts200ResponseSeverityEnum severity,
  ) => call(severity: severity);

  @override
  StreamAlerts200Response summary(String? summary) => call(summary: summary);

  @override
  StreamAlerts200Response enrichedAt(DateTime? enrichedAt) =>
      call(enrichedAt: enrichedAt);

  @override
  StreamAlerts200Response record(Record? record) => call(record: record);

  @override
  StreamAlerts200Response rev(int rev) => call(rev: rev);

  @override
  StreamAlerts200Response state(StreamAlerts200ResponseStateEnum state) =>
      call(state: state);

  @override
  StreamAlerts200Response observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  @override
  StreamAlerts200Response acknowledgedBy(String? acknowledgedBy) =>
      call(acknowledgedBy: acknowledgedBy);

  @override
  StreamAlerts200Response resolvedAt(DateTime? resolvedAt) =>
      call(resolvedAt: resolvedAt);

  @override
  StreamAlerts200Response resolvedBy(String? resolvedBy) =>
      call(resolvedBy: resolvedBy);

  @override
  StreamAlerts200Response resolvedSeq(int? resolvedSeq) =>
      call(resolvedSeq: resolvedSeq);

  @override
  StreamAlerts200Response feedback(
    StreamAlerts200ResponseFeedbackEnum? feedback,
  ) => call(feedback: feedback);

  @override
  StreamAlerts200Response feedbackAt(DateTime? feedbackAt) =>
      call(feedbackAt: feedbackAt);

  @override
  StreamAlerts200Response note(String? note) => call(note: note);

  @override
  StreamAlerts200Response snoozedUntil(DateTime? snoozedUntil) =>
      call(snoozedUntil: snoozedUntil);

  @override
  StreamAlerts200Response suppressedReason(
    StreamAlerts200ResponseSuppressedReasonEnum? suppressedReason,
  ) => call(suppressedReason: suppressedReason);

  @override
  StreamAlerts200Response tags(List<String>? tags) => call(tags: tags);

  @override
  StreamAlerts200Response recordSnapshot(AlertRecordSnapshot? recordSnapshot) =>
      call(recordSnapshot: recordSnapshot);

  @override
  StreamAlerts200Response summaryCitations(
    List<AskCitation>? summaryCitations,
  ) => call(summaryCitations: summaryCitations);

  @override
  StreamAlerts200Response sensitive(bool? sensitive) =>
      call(sensitive: sensitive);

  @override
  StreamAlerts200Response region(RegionTags? region) => call(region: region);

  @override
  StreamAlerts200Response context(AlertContext? context) =>
      call(context: context);

  @override
  StreamAlerts200Response exerciseRunId(String? exerciseRunId) =>
      call(exerciseRunId: exerciseRunId);

  @override
  StreamAlerts200Response signals(List<AlertSignal>? signals) =>
      call(signals: signals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StreamAlerts200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StreamAlerts200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StreamAlerts200Response call({
    Object? type = const $CopyWithPlaceholder(),
    Object? alert = const $CopyWithPlaceholder(),
    Object? suppressed = const $CopyWithPlaceholder(),
    Object? exercise = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? resumeAfter = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? ruleName = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? firedAt = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? acknowledgedAt = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? enrichedAt = const $CopyWithPlaceholder(),
    Object? record = const $CopyWithPlaceholder(),
    Object? rev = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? acknowledgedBy = const $CopyWithPlaceholder(),
    Object? resolvedAt = const $CopyWithPlaceholder(),
    Object? resolvedBy = const $CopyWithPlaceholder(),
    Object? resolvedSeq = const $CopyWithPlaceholder(),
    Object? feedback = const $CopyWithPlaceholder(),
    Object? feedbackAt = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
    Object? snoozedUntil = const $CopyWithPlaceholder(),
    Object? suppressedReason = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? recordSnapshot = const $CopyWithPlaceholder(),
    Object? summaryCitations = const $CopyWithPlaceholder(),
    Object? sensitive = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
    Object? context = const $CopyWithPlaceholder(),
    Object? exerciseRunId = const $CopyWithPlaceholder(),
    Object? signals = const $CopyWithPlaceholder(),
  }) {
    return StreamAlerts200Response(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as StreamAlerts200ResponseTypeEnum,
      alert: alert == const $CopyWithPlaceholder() || alert == null
          ? _value.alert
          // ignore: cast_nullable_to_non_nullable
          : alert as Alert,
      suppressed: suppressed == const $CopyWithPlaceholder()
          ? _value.suppressed
          // ignore: cast_nullable_to_non_nullable
          : suppressed as bool?,
      exercise: exercise == const $CopyWithPlaceholder()
          ? _value.exercise
          // ignore: cast_nullable_to_non_nullable
          : exercise as bool?,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as StreamAlerts200ResponseReasonEnum,
      resumeAfter: resumeAfter == const $CopyWithPlaceholder()
          ? _value.resumeAfter
          // ignore: cast_nullable_to_non_nullable
          : resumeAfter as int?,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      ruleName: ruleName == const $CopyWithPlaceholder()
          ? _value.ruleName
          // ignore: cast_nullable_to_non_nullable
          : ruleName as String?,
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      firedAt: firedAt == const $CopyWithPlaceholder() || firedAt == null
          ? _value.firedAt
          // ignore: cast_nullable_to_non_nullable
          : firedAt as DateTime,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
      acknowledgedAt: acknowledgedAt == const $CopyWithPlaceholder()
          ? _value.acknowledgedAt
          // ignore: cast_nullable_to_non_nullable
          : acknowledgedAt as DateTime?,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as StreamAlerts200ResponseSeverityEnum,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      enrichedAt: enrichedAt == const $CopyWithPlaceholder()
          ? _value.enrichedAt
          // ignore: cast_nullable_to_non_nullable
          : enrichedAt as DateTime?,
      record: record == const $CopyWithPlaceholder()
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as Record?,
      rev: rev == const $CopyWithPlaceholder() || rev == null
          ? _value.rev
          // ignore: cast_nullable_to_non_nullable
          : rev as int,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as StreamAlerts200ResponseStateEnum,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      acknowledgedBy: acknowledgedBy == const $CopyWithPlaceholder()
          ? _value.acknowledgedBy
          // ignore: cast_nullable_to_non_nullable
          : acknowledgedBy as String?,
      resolvedAt: resolvedAt == const $CopyWithPlaceholder()
          ? _value.resolvedAt
          // ignore: cast_nullable_to_non_nullable
          : resolvedAt as DateTime?,
      resolvedBy: resolvedBy == const $CopyWithPlaceholder()
          ? _value.resolvedBy
          // ignore: cast_nullable_to_non_nullable
          : resolvedBy as String?,
      resolvedSeq: resolvedSeq == const $CopyWithPlaceholder()
          ? _value.resolvedSeq
          // ignore: cast_nullable_to_non_nullable
          : resolvedSeq as int?,
      feedback: feedback == const $CopyWithPlaceholder()
          ? _value.feedback
          // ignore: cast_nullable_to_non_nullable
          : feedback as StreamAlerts200ResponseFeedbackEnum?,
      feedbackAt: feedbackAt == const $CopyWithPlaceholder()
          ? _value.feedbackAt
          // ignore: cast_nullable_to_non_nullable
          : feedbackAt as DateTime?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      snoozedUntil: snoozedUntil == const $CopyWithPlaceholder()
          ? _value.snoozedUntil
          // ignore: cast_nullable_to_non_nullable
          : snoozedUntil as DateTime?,
      suppressedReason: suppressedReason == const $CopyWithPlaceholder()
          ? _value.suppressedReason
          // ignore: cast_nullable_to_non_nullable
          : suppressedReason as StreamAlerts200ResponseSuppressedReasonEnum?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      recordSnapshot: recordSnapshot == const $CopyWithPlaceholder()
          ? _value.recordSnapshot
          // ignore: cast_nullable_to_non_nullable
          : recordSnapshot as AlertRecordSnapshot?,
      summaryCitations: summaryCitations == const $CopyWithPlaceholder()
          ? _value.summaryCitations
          // ignore: cast_nullable_to_non_nullable
          : summaryCitations as List<AskCitation>?,
      sensitive: sensitive == const $CopyWithPlaceholder()
          ? _value.sensitive
          // ignore: cast_nullable_to_non_nullable
          : sensitive as bool?,
      region: region == const $CopyWithPlaceholder()
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as RegionTags?,
      context: context == const $CopyWithPlaceholder()
          ? _value.context
          // ignore: cast_nullable_to_non_nullable
          : context as AlertContext?,
      exerciseRunId: exerciseRunId == const $CopyWithPlaceholder()
          ? _value.exerciseRunId
          // ignore: cast_nullable_to_non_nullable
          : exerciseRunId as String?,
      signals: signals == const $CopyWithPlaceholder()
          ? _value.signals
          // ignore: cast_nullable_to_non_nullable
          : signals as List<AlertSignal>?,
    );
  }
}

extension $StreamAlerts200ResponseCopyWith on StreamAlerts200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStreamAlerts200Response.copyWith(...)` or `instanceOfStreamAlerts200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StreamAlerts200ResponseCWProxy get copyWith =>
      _$StreamAlerts200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StreamAlerts200Response _$StreamAlerts200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StreamAlerts200Response', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'type',
      'alert',
      'reason',
      'id',
      'ruleId',
      'seq',
      'source',
      'externalId',
      'firedAt',
      'message',
      'severity',
      'rev',
      'state',
    ],
  );
  final val = StreamAlerts200Response(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$StreamAlerts200ResponseTypeEnumEnumMap, v),
    ),
    alert: $checkedConvert(
      'alert',
      (v) => Alert.fromJson(v as Map<String, dynamic>),
    ),
    suppressed: $checkedConvert('suppressed', (v) => v as bool? ?? false),
    exercise: $checkedConvert('exercise', (v) => v as bool? ?? false),
    reason: $checkedConvert(
      'reason',
      (v) => $enumDecode(_$StreamAlerts200ResponseReasonEnumEnumMap, v),
    ),
    resumeAfter: $checkedConvert('resumeAfter', (v) => (v as num?)?.toInt()),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num?)?.toInt()),
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    ruleId: $checkedConvert('ruleId', (v) => v as String),
    ruleName: $checkedConvert('ruleName', (v) => v as String?),
    seq: $checkedConvert('seq', (v) => (v as num).toInt()),
    source_: $checkedConvert('source', (v) => v as String),
    externalId: $checkedConvert('externalId', (v) => v as String),
    kind: $checkedConvert('kind', (v) => v as String?),
    firedAt: $checkedConvert('firedAt', (v) => DateTime.parse(v as String)),
    message: $checkedConvert('message', (v) => v as String),
    acknowledgedAt: $checkedConvert(
      'acknowledgedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecode(_$StreamAlerts200ResponseSeverityEnumEnumMap, v),
    ),
    summary: $checkedConvert('summary', (v) => v as String?),
    enrichedAt: $checkedConvert(
      'enrichedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    record: $checkedConvert(
      'record',
      (v) => v == null ? null : Record.fromJson(v as Map<String, dynamic>),
    ),
    rev: $checkedConvert('rev', (v) => (v as num).toInt()),
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$StreamAlerts200ResponseStateEnumEnumMap, v),
    ),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    acknowledgedBy: $checkedConvert('acknowledgedBy', (v) => v as String?),
    resolvedAt: $checkedConvert(
      'resolvedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    resolvedBy: $checkedConvert('resolvedBy', (v) => v as String?),
    resolvedSeq: $checkedConvert('resolvedSeq', (v) => (v as num?)?.toInt()),
    feedback: $checkedConvert(
      'feedback',
      (v) =>
          $enumDecodeNullable(_$StreamAlerts200ResponseFeedbackEnumEnumMap, v),
    ),
    feedbackAt: $checkedConvert(
      'feedbackAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    note: $checkedConvert('note', (v) => v as String?),
    snoozedUntil: $checkedConvert(
      'snoozedUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    suppressedReason: $checkedConvert(
      'suppressedReason',
      (v) => $enumDecodeNullable(
        _$StreamAlerts200ResponseSuppressedReasonEnumEnumMap,
        v,
      ),
    ),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    recordSnapshot: $checkedConvert(
      'recordSnapshot',
      (v) => v == null
          ? null
          : AlertRecordSnapshot.fromJson(v as Map<String, dynamic>),
    ),
    summaryCitations: $checkedConvert(
      'summaryCitations',
      (v) => (v as List<dynamic>?)
          ?.map((e) => AskCitation.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    sensitive: $checkedConvert('sensitive', (v) => v as bool?),
    region: $checkedConvert(
      'region',
      (v) => v == null ? null : RegionTags.fromJson(v as Map<String, dynamic>),
    ),
    context: $checkedConvert(
      'context',
      (v) =>
          v == null ? null : AlertContext.fromJson(v as Map<String, dynamic>),
    ),
    exerciseRunId: $checkedConvert('exerciseRunId', (v) => v as String?),
    signals: $checkedConvert(
      'signals',
      (v) => (v as List<dynamic>?)
          ?.map((e) => AlertSignal.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$StreamAlerts200ResponseToJson(
  StreamAlerts200Response instance,
) => <String, dynamic>{
  'type': _$StreamAlerts200ResponseTypeEnumEnumMap[instance.type]!,
  'alert': instance.alert.toJson(),
  'suppressed': ?instance.suppressed,
  'exercise': ?instance.exercise,
  'reason': _$StreamAlerts200ResponseReasonEnumEnumMap[instance.reason]!,
  'resumeAfter': ?instance.resumeAfter,
  'watermarkSeq': ?instance.watermarkSeq,
  'id': instance.id,
  'ruleId': instance.ruleId,
  'ruleName': ?instance.ruleName,
  'seq': instance.seq,
  'source': instance.source_,
  'externalId': instance.externalId,
  'kind': ?instance.kind,
  'firedAt': instance.firedAt.toIso8601String(),
  'message': instance.message,
  'acknowledgedAt': ?instance.acknowledgedAt?.toIso8601String(),
  'severity': _$StreamAlerts200ResponseSeverityEnumEnumMap[instance.severity]!,
  'summary': ?instance.summary,
  'enrichedAt': ?instance.enrichedAt?.toIso8601String(),
  'record': ?instance.record?.toJson(),
  'rev': instance.rev,
  'state': _$StreamAlerts200ResponseStateEnumEnumMap[instance.state]!,
  'observedAt': ?instance.observedAt?.toIso8601String(),
  'acknowledgedBy': ?instance.acknowledgedBy,
  'resolvedAt': ?instance.resolvedAt?.toIso8601String(),
  'resolvedBy': ?instance.resolvedBy,
  'resolvedSeq': ?instance.resolvedSeq,
  'feedback': ?_$StreamAlerts200ResponseFeedbackEnumEnumMap[instance.feedback],
  'feedbackAt': ?instance.feedbackAt?.toIso8601String(),
  'note': ?instance.note,
  'snoozedUntil': ?instance.snoozedUntil?.toIso8601String(),
  'suppressedReason':
      ?_$StreamAlerts200ResponseSuppressedReasonEnumEnumMap[instance
          .suppressedReason],
  'tags': ?instance.tags,
  'recordSnapshot': ?instance.recordSnapshot?.toJson(),
  'summaryCitations': ?instance.summaryCitations
      ?.map((e) => e.toJson())
      .toList(),
  'sensitive': ?instance.sensitive,
  'region': ?instance.region?.toJson(),
  'context': ?instance.context?.toJson(),
  'exerciseRunId': ?instance.exerciseRunId,
  'signals': ?instance.signals?.map((e) => e.toJson()).toList(),
};

const _$StreamAlerts200ResponseTypeEnumEnumMap = {
  StreamAlerts200ResponseTypeEnum.fired: 'fired',
  StreamAlerts200ResponseTypeEnum.updated: 'updated',
  StreamAlerts200ResponseTypeEnum.acknowledged: 'acknowledged',
  StreamAlerts200ResponseTypeEnum.snoozed: 'snoozed',
  StreamAlerts200ResponseTypeEnum.resolved: 'resolved',
};

const _$StreamAlerts200ResponseReasonEnumEnumMap = {
  StreamAlerts200ResponseReasonEnum.bufferOverflow: 'buffer_overflow',
  StreamAlerts200ResponseReasonEnum.replayCap: 'replay_cap',
  StreamAlerts200ResponseReasonEnum.resyncRequired: 'resync_required',
};

const _$StreamAlerts200ResponseSeverityEnumEnumMap = {
  StreamAlerts200ResponseSeverityEnum.info: 'info',
  StreamAlerts200ResponseSeverityEnum.warning: 'warning',
  StreamAlerts200ResponseSeverityEnum.critical: 'critical',
};

const _$StreamAlerts200ResponseStateEnumEnumMap = {
  StreamAlerts200ResponseStateEnum.firing: 'firing',
  StreamAlerts200ResponseStateEnum.acknowledged: 'acknowledged',
  StreamAlerts200ResponseStateEnum.resolved: 'resolved',
};

const _$StreamAlerts200ResponseFeedbackEnumEnumMap = {
  StreamAlerts200ResponseFeedbackEnum.useful: 'useful',
  StreamAlerts200ResponseFeedbackEnum.noise: 'noise',
  StreamAlerts200ResponseFeedbackEnum.late_: 'late',
  StreamAlerts200ResponseFeedbackEnum.falsePositive: 'false_positive',
};

const _$StreamAlerts200ResponseSuppressedReasonEnumEnumMap = {
  StreamAlerts200ResponseSuppressedReasonEnum.alertSnoozed: 'alert_snoozed',
  StreamAlerts200ResponseSuppressedReasonEnum.ruleSnoozed: 'rule_snoozed',
};
