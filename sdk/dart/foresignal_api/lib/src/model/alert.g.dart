// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertCWProxy {
  Alert id(int id);

  Alert ruleId(String ruleId);

  Alert ruleName(String? ruleName);

  Alert seq(int seq);

  Alert source_(String source_);

  Alert externalId(String externalId);

  Alert kind(String? kind);

  Alert firedAt(DateTime firedAt);

  Alert message(String message);

  Alert acknowledgedAt(DateTime? acknowledgedAt);

  Alert severity(AlertSeverityEnum severity);

  Alert summary(String? summary);

  Alert enrichedAt(DateTime? enrichedAt);

  Alert record(Record? record);

  Alert rev(int rev);

  Alert state(AlertStateEnum state);

  Alert observedAt(DateTime? observedAt);

  Alert acknowledgedBy(String? acknowledgedBy);

  Alert resolvedAt(DateTime? resolvedAt);

  Alert resolvedBy(String? resolvedBy);

  Alert resolvedSeq(int? resolvedSeq);

  Alert feedback(AlertFeedbackEnum? feedback);

  Alert feedbackAt(DateTime? feedbackAt);

  Alert note(String? note);

  Alert snoozedUntil(DateTime? snoozedUntil);

  Alert suppressed(bool? suppressed);

  Alert suppressedReason(AlertSuppressedReasonEnum? suppressedReason);

  Alert tags(List<String>? tags);

  Alert recordSnapshot(AlertRecordSnapshot? recordSnapshot);

  Alert summaryCitations(List<AskCitation>? summaryCitations);

  Alert sensitive(bool? sensitive);

  Alert region(RegionTags? region);

  Alert context(AlertContext? context);

  Alert exercise(bool? exercise);

  Alert exerciseRunId(String? exerciseRunId);

  Alert signals(List<AlertSignal>? signals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Alert(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Alert(...).copyWith(id: 12, name: "My name")
  /// ```
  Alert call({
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
    AlertSeverityEnum severity,
    String? summary,
    DateTime? enrichedAt,
    Record? record,
    int rev,
    AlertStateEnum state,
    DateTime? observedAt,
    String? acknowledgedBy,
    DateTime? resolvedAt,
    String? resolvedBy,
    int? resolvedSeq,
    AlertFeedbackEnum? feedback,
    DateTime? feedbackAt,
    String? note,
    DateTime? snoozedUntil,
    bool? suppressed,
    AlertSuppressedReasonEnum? suppressedReason,
    List<String>? tags,
    AlertRecordSnapshot? recordSnapshot,
    List<AskCitation>? summaryCitations,
    bool? sensitive,
    RegionTags? region,
    AlertContext? context,
    bool? exercise,
    String? exerciseRunId,
    List<AlertSignal>? signals,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlert.copyWith(...)` or call `instanceOfAlert.copyWith.fieldName(value)` for a single field.
class _$AlertCWProxyImpl implements _$AlertCWProxy {
  const _$AlertCWProxyImpl(this._value);

  final Alert _value;

  @override
  Alert id(int id) => call(id: id);

  @override
  Alert ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  Alert ruleName(String? ruleName) => call(ruleName: ruleName);

  @override
  Alert seq(int seq) => call(seq: seq);

  @override
  Alert source_(String source_) => call(source_: source_);

  @override
  Alert externalId(String externalId) => call(externalId: externalId);

  @override
  Alert kind(String? kind) => call(kind: kind);

  @override
  Alert firedAt(DateTime firedAt) => call(firedAt: firedAt);

  @override
  Alert message(String message) => call(message: message);

  @override
  Alert acknowledgedAt(DateTime? acknowledgedAt) =>
      call(acknowledgedAt: acknowledgedAt);

  @override
  Alert severity(AlertSeverityEnum severity) => call(severity: severity);

  @override
  Alert summary(String? summary) => call(summary: summary);

  @override
  Alert enrichedAt(DateTime? enrichedAt) => call(enrichedAt: enrichedAt);

  @override
  Alert record(Record? record) => call(record: record);

  @override
  Alert rev(int rev) => call(rev: rev);

  @override
  Alert state(AlertStateEnum state) => call(state: state);

  @override
  Alert observedAt(DateTime? observedAt) => call(observedAt: observedAt);

  @override
  Alert acknowledgedBy(String? acknowledgedBy) =>
      call(acknowledgedBy: acknowledgedBy);

  @override
  Alert resolvedAt(DateTime? resolvedAt) => call(resolvedAt: resolvedAt);

  @override
  Alert resolvedBy(String? resolvedBy) => call(resolvedBy: resolvedBy);

  @override
  Alert resolvedSeq(int? resolvedSeq) => call(resolvedSeq: resolvedSeq);

  @override
  Alert feedback(AlertFeedbackEnum? feedback) => call(feedback: feedback);

  @override
  Alert feedbackAt(DateTime? feedbackAt) => call(feedbackAt: feedbackAt);

  @override
  Alert note(String? note) => call(note: note);

  @override
  Alert snoozedUntil(DateTime? snoozedUntil) =>
      call(snoozedUntil: snoozedUntil);

  @override
  Alert suppressed(bool? suppressed) => call(suppressed: suppressed);

  @override
  Alert suppressedReason(AlertSuppressedReasonEnum? suppressedReason) =>
      call(suppressedReason: suppressedReason);

  @override
  Alert tags(List<String>? tags) => call(tags: tags);

  @override
  Alert recordSnapshot(AlertRecordSnapshot? recordSnapshot) =>
      call(recordSnapshot: recordSnapshot);

  @override
  Alert summaryCitations(List<AskCitation>? summaryCitations) =>
      call(summaryCitations: summaryCitations);

  @override
  Alert sensitive(bool? sensitive) => call(sensitive: sensitive);

  @override
  Alert region(RegionTags? region) => call(region: region);

  @override
  Alert context(AlertContext? context) => call(context: context);

  @override
  Alert exercise(bool? exercise) => call(exercise: exercise);

  @override
  Alert exerciseRunId(String? exerciseRunId) =>
      call(exerciseRunId: exerciseRunId);

  @override
  Alert signals(List<AlertSignal>? signals) => call(signals: signals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Alert(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Alert(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Alert call({
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
    Object? suppressed = const $CopyWithPlaceholder(),
    Object? suppressedReason = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? recordSnapshot = const $CopyWithPlaceholder(),
    Object? summaryCitations = const $CopyWithPlaceholder(),
    Object? sensitive = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
    Object? context = const $CopyWithPlaceholder(),
    Object? exercise = const $CopyWithPlaceholder(),
    Object? exerciseRunId = const $CopyWithPlaceholder(),
    Object? signals = const $CopyWithPlaceholder(),
  }) {
    return Alert(
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
          : severity as AlertSeverityEnum,
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
          : state as AlertStateEnum,
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
          : feedback as AlertFeedbackEnum?,
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
      suppressed: suppressed == const $CopyWithPlaceholder()
          ? _value.suppressed
          // ignore: cast_nullable_to_non_nullable
          : suppressed as bool?,
      suppressedReason: suppressedReason == const $CopyWithPlaceholder()
          ? _value.suppressedReason
          // ignore: cast_nullable_to_non_nullable
          : suppressedReason as AlertSuppressedReasonEnum?,
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
      exercise: exercise == const $CopyWithPlaceholder()
          ? _value.exercise
          // ignore: cast_nullable_to_non_nullable
          : exercise as bool?,
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

extension $AlertCopyWith on Alert {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlert.copyWith(...)` or `instanceOfAlert.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertCWProxy get copyWith => _$AlertCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Alert _$AlertFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Alert',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const [
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
    final val = Alert(
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
        (v) => $enumDecode(_$AlertSeverityEnumEnumMap, v),
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
        (v) => $enumDecode(_$AlertStateEnumEnumMap, v),
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
        (v) => $enumDecodeNullable(_$AlertFeedbackEnumEnumMap, v),
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
      suppressed: $checkedConvert('suppressed', (v) => v as bool? ?? false),
      suppressedReason: $checkedConvert(
        'suppressedReason',
        (v) => $enumDecodeNullable(_$AlertSuppressedReasonEnumEnumMap, v),
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
        (v) =>
            v == null ? null : RegionTags.fromJson(v as Map<String, dynamic>),
      ),
      context: $checkedConvert(
        'context',
        (v) =>
            v == null ? null : AlertContext.fromJson(v as Map<String, dynamic>),
      ),
      exercise: $checkedConvert('exercise', (v) => v as bool? ?? false),
      exerciseRunId: $checkedConvert('exerciseRunId', (v) => v as String?),
      signals: $checkedConvert(
        'signals',
        (v) => (v as List<dynamic>?)
            ?.map((e) => AlertSignal.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
    );
    return val;
  },
  fieldKeyMap: const {'source_': 'source'},
);

Map<String, dynamic> _$AlertToJson(Alert instance) => <String, dynamic>{
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
  'severity': _$AlertSeverityEnumEnumMap[instance.severity]!,
  'summary': ?instance.summary,
  'enrichedAt': ?instance.enrichedAt?.toIso8601String(),
  'record': ?instance.record?.toJson(),
  'rev': instance.rev,
  'state': _$AlertStateEnumEnumMap[instance.state]!,
  'observedAt': ?instance.observedAt?.toIso8601String(),
  'acknowledgedBy': ?instance.acknowledgedBy,
  'resolvedAt': ?instance.resolvedAt?.toIso8601String(),
  'resolvedBy': ?instance.resolvedBy,
  'resolvedSeq': ?instance.resolvedSeq,
  'feedback': ?_$AlertFeedbackEnumEnumMap[instance.feedback],
  'feedbackAt': ?instance.feedbackAt?.toIso8601String(),
  'note': ?instance.note,
  'snoozedUntil': ?instance.snoozedUntil?.toIso8601String(),
  'suppressed': ?instance.suppressed,
  'suppressedReason':
      ?_$AlertSuppressedReasonEnumEnumMap[instance.suppressedReason],
  'tags': ?instance.tags,
  'recordSnapshot': ?instance.recordSnapshot?.toJson(),
  'summaryCitations': ?instance.summaryCitations
      ?.map((e) => e.toJson())
      .toList(),
  'sensitive': ?instance.sensitive,
  'region': ?instance.region?.toJson(),
  'context': ?instance.context?.toJson(),
  'exercise': ?instance.exercise,
  'exerciseRunId': ?instance.exerciseRunId,
  'signals': ?instance.signals?.map((e) => e.toJson()).toList(),
};

const _$AlertSeverityEnumEnumMap = {
  AlertSeverityEnum.info: 'info',
  AlertSeverityEnum.warning: 'warning',
  AlertSeverityEnum.critical: 'critical',
};

const _$AlertStateEnumEnumMap = {
  AlertStateEnum.firing: 'firing',
  AlertStateEnum.acknowledged: 'acknowledged',
  AlertStateEnum.resolved: 'resolved',
};

const _$AlertFeedbackEnumEnumMap = {
  AlertFeedbackEnum.useful: 'useful',
  AlertFeedbackEnum.noise: 'noise',
  AlertFeedbackEnum.late_: 'late',
  AlertFeedbackEnum.falsePositive: 'false_positive',
};

const _$AlertSuppressedReasonEnumEnumMap = {
  AlertSuppressedReasonEnum.alertSnoozed: 'alert_snoozed',
  AlertSuppressedReasonEnum.ruleSnoozed: 'rule_snoozed',
};
