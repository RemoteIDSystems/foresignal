// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'briefing_run.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BriefingRunCWProxy {
  BriefingRun id(String? id);

  BriefingRun briefingId(String? briefingId);

  BriefingRun status(BriefingRunStatusEnum status);

  BriefingRun startedAt(DateTime startedAt);

  BriefingRun finishedAt(DateTime? finishedAt);

  BriefingRun periodFrom(DateTime periodFrom);

  BriefingRun periodTo(DateTime periodTo);

  BriefingRun mode(BriefingRunModeEnum? mode);

  BriefingRun contentMarkdown(String contentMarkdown);

  BriefingRun citations(List<AskCitation>? citations);

  BriefingRun stats(DigestStats? stats);

  BriefingRun error(String? error);

  BriefingRun usage(Map<String, Object>? usage);

  BriefingRun deliveries(int? deliveries);

  BriefingRun warnings(List<String>? warnings);

  BriefingRun signature(String? signature);

  BriefingRun audio(BriefingRunAudio? audio);

  BriefingRun deliveryResults(
    List<BriefingRunDeliveryResultsInner>? deliveryResults,
  );

  BriefingRun breaking(BriefingRunBreaking? breaking);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingRun(...).copyWith(id: 12, name: "My name")
  /// ```
  BriefingRun call({
    String? id,
    String? briefingId,
    BriefingRunStatusEnum status,
    DateTime startedAt,
    DateTime? finishedAt,
    DateTime periodFrom,
    DateTime periodTo,
    BriefingRunModeEnum? mode,
    String contentMarkdown,
    List<AskCitation>? citations,
    DigestStats? stats,
    String? error,
    Map<String, Object>? usage,
    int? deliveries,
    List<String>? warnings,
    String? signature,
    BriefingRunAudio? audio,
    List<BriefingRunDeliveryResultsInner>? deliveryResults,
    BriefingRunBreaking? breaking,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBriefingRun.copyWith(...)` or call `instanceOfBriefingRun.copyWith.fieldName(value)` for a single field.
class _$BriefingRunCWProxyImpl implements _$BriefingRunCWProxy {
  const _$BriefingRunCWProxyImpl(this._value);

  final BriefingRun _value;

  @override
  BriefingRun id(String? id) => call(id: id);

  @override
  BriefingRun briefingId(String? briefingId) => call(briefingId: briefingId);

  @override
  BriefingRun status(BriefingRunStatusEnum status) => call(status: status);

  @override
  BriefingRun startedAt(DateTime startedAt) => call(startedAt: startedAt);

  @override
  BriefingRun finishedAt(DateTime? finishedAt) => call(finishedAt: finishedAt);

  @override
  BriefingRun periodFrom(DateTime periodFrom) => call(periodFrom: periodFrom);

  @override
  BriefingRun periodTo(DateTime periodTo) => call(periodTo: periodTo);

  @override
  BriefingRun mode(BriefingRunModeEnum? mode) => call(mode: mode);

  @override
  BriefingRun contentMarkdown(String contentMarkdown) =>
      call(contentMarkdown: contentMarkdown);

  @override
  BriefingRun citations(List<AskCitation>? citations) =>
      call(citations: citations);

  @override
  BriefingRun stats(DigestStats? stats) => call(stats: stats);

  @override
  BriefingRun error(String? error) => call(error: error);

  @override
  BriefingRun usage(Map<String, Object>? usage) => call(usage: usage);

  @override
  BriefingRun deliveries(int? deliveries) => call(deliveries: deliveries);

  @override
  BriefingRun warnings(List<String>? warnings) => call(warnings: warnings);

  @override
  BriefingRun signature(String? signature) => call(signature: signature);

  @override
  BriefingRun audio(BriefingRunAudio? audio) => call(audio: audio);

  @override
  BriefingRun deliveryResults(
    List<BriefingRunDeliveryResultsInner>? deliveryResults,
  ) => call(deliveryResults: deliveryResults);

  @override
  BriefingRun breaking(BriefingRunBreaking? breaking) =>
      call(breaking: breaking);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BriefingRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BriefingRun(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BriefingRun call({
    Object? id = const $CopyWithPlaceholder(),
    Object? briefingId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? periodFrom = const $CopyWithPlaceholder(),
    Object? periodTo = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? contentMarkdown = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? stats = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? usage = const $CopyWithPlaceholder(),
    Object? deliveries = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? signature = const $CopyWithPlaceholder(),
    Object? audio = const $CopyWithPlaceholder(),
    Object? deliveryResults = const $CopyWithPlaceholder(),
    Object? breaking = const $CopyWithPlaceholder(),
  }) {
    return BriefingRun(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      briefingId: briefingId == const $CopyWithPlaceholder()
          ? _value.briefingId
          // ignore: cast_nullable_to_non_nullable
          : briefingId as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BriefingRunStatusEnum,
      startedAt: startedAt == const $CopyWithPlaceholder() || startedAt == null
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      periodFrom:
          periodFrom == const $CopyWithPlaceholder() || periodFrom == null
          ? _value.periodFrom
          // ignore: cast_nullable_to_non_nullable
          : periodFrom as DateTime,
      periodTo: periodTo == const $CopyWithPlaceholder() || periodTo == null
          ? _value.periodTo
          // ignore: cast_nullable_to_non_nullable
          : periodTo as DateTime,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as BriefingRunModeEnum?,
      contentMarkdown:
          contentMarkdown == const $CopyWithPlaceholder() ||
              contentMarkdown == null
          ? _value.contentMarkdown
          // ignore: cast_nullable_to_non_nullable
          : contentMarkdown as String,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<AskCitation>?,
      stats: stats == const $CopyWithPlaceholder()
          ? _value.stats
          // ignore: cast_nullable_to_non_nullable
          : stats as DigestStats?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      usage: usage == const $CopyWithPlaceholder()
          ? _value.usage
          // ignore: cast_nullable_to_non_nullable
          : usage as Map<String, Object>?,
      deliveries: deliveries == const $CopyWithPlaceholder()
          ? _value.deliveries
          // ignore: cast_nullable_to_non_nullable
          : deliveries as int?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<String>?,
      signature: signature == const $CopyWithPlaceholder()
          ? _value.signature
          // ignore: cast_nullable_to_non_nullable
          : signature as String?,
      audio: audio == const $CopyWithPlaceholder()
          ? _value.audio
          // ignore: cast_nullable_to_non_nullable
          : audio as BriefingRunAudio?,
      deliveryResults: deliveryResults == const $CopyWithPlaceholder()
          ? _value.deliveryResults
          // ignore: cast_nullable_to_non_nullable
          : deliveryResults as List<BriefingRunDeliveryResultsInner>?,
      breaking: breaking == const $CopyWithPlaceholder()
          ? _value.breaking
          // ignore: cast_nullable_to_non_nullable
          : breaking as BriefingRunBreaking?,
    );
  }
}

extension $BriefingRunCopyWith on BriefingRun {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBriefingRun.copyWith(...)` or `instanceOfBriefingRun.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BriefingRunCWProxy get copyWith => _$BriefingRunCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BriefingRun _$BriefingRunFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BriefingRun', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'status',
      'startedAt',
      'periodFrom',
      'periodTo',
      'contentMarkdown',
    ],
  );
  final val = BriefingRun(
    id: $checkedConvert('id', (v) => v as String?),
    briefingId: $checkedConvert('briefingId', (v) => v as String?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$BriefingRunStatusEnumEnumMap, v),
    ),
    startedAt: $checkedConvert('startedAt', (v) => DateTime.parse(v as String)),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    periodFrom: $checkedConvert(
      'periodFrom',
      (v) => DateTime.parse(v as String),
    ),
    periodTo: $checkedConvert('periodTo', (v) => DateTime.parse(v as String)),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecodeNullable(_$BriefingRunModeEnumEnumMap, v),
    ),
    contentMarkdown: $checkedConvert('contentMarkdown', (v) => v as String),
    citations: $checkedConvert(
      'citations',
      (v) => (v as List<dynamic>?)
          ?.map((e) => AskCitation.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    stats: $checkedConvert(
      'stats',
      (v) => v == null ? null : DigestStats.fromJson(v as Map<String, dynamic>),
    ),
    error: $checkedConvert('error', (v) => v as String?),
    usage: $checkedConvert(
      'usage',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    deliveries: $checkedConvert('deliveries', (v) => (v as num?)?.toInt()),
    warnings: $checkedConvert(
      'warnings',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    signature: $checkedConvert('signature', (v) => v as String?),
    audio: $checkedConvert(
      'audio',
      (v) => v == null
          ? null
          : BriefingRunAudio.fromJson(v as Map<String, dynamic>),
    ),
    deliveryResults: $checkedConvert(
      'deliveryResults',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => BriefingRunDeliveryResultsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    breaking: $checkedConvert(
      'breaking',
      (v) => v == null
          ? null
          : BriefingRunBreaking.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$BriefingRunToJson(
  BriefingRun instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'briefingId': ?instance.briefingId,
  'status': _$BriefingRunStatusEnumEnumMap[instance.status]!,
  'startedAt': instance.startedAt.toIso8601String(),
  'finishedAt': ?instance.finishedAt?.toIso8601String(),
  'periodFrom': instance.periodFrom.toIso8601String(),
  'periodTo': instance.periodTo.toIso8601String(),
  'mode': ?_$BriefingRunModeEnumEnumMap[instance.mode],
  'contentMarkdown': instance.contentMarkdown,
  'citations': ?instance.citations?.map((e) => e.toJson()).toList(),
  'stats': ?instance.stats?.toJson(),
  'error': ?instance.error,
  'usage': ?instance.usage,
  'deliveries': ?instance.deliveries,
  'warnings': ?instance.warnings,
  'signature': ?instance.signature,
  'audio': ?instance.audio?.toJson(),
  'deliveryResults': ?instance.deliveryResults?.map((e) => e.toJson()).toList(),
  'breaking': ?instance.breaking?.toJson(),
};

const _$BriefingRunStatusEnumEnumMap = {
  BriefingRunStatusEnum.running: 'running',
  BriefingRunStatusEnum.succeeded: 'succeeded',
  BriefingRunStatusEnum.fallback: 'fallback',
  BriefingRunStatusEnum.failed: 'failed',
};

const _$BriefingRunModeEnumEnumMap = {
  BriefingRunModeEnum.digest: 'digest',
  BriefingRunModeEnum.llm: 'llm',
};
