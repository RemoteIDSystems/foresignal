// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_cue.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCueCWProxy {
  SocialCue id(String id);

  SocialCue trigger(SocialCueTrigger trigger);

  SocialCue backend(SocialCueBackendEnum backend);

  SocialCue queries(List<SocialCueQuery> queries);

  SocialCue platforms(List<SocialPlatformId> platforms);

  SocialCue window(TimeRange window);

  SocialCue status(SocialCueStatusEnum status);

  SocialCue statusReason(String? statusReason);

  SocialCue results(List<SocialCueResultsInner> results);

  SocialCue narrativeIds(List<String> narrativeIds);

  SocialCue costUsd(num costUsd);

  SocialCue maxCostUsd(num? maxCostUsd);

  SocialCue createdBy(String createdBy);

  SocialCue createdAt(DateTime createdAt);

  SocialCue startedAt(DateTime? startedAt);

  SocialCue completedAt(DateTime? completedAt);

  SocialCue manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCue(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCue call({
    String id,
    SocialCueTrigger trigger,
    SocialCueBackendEnum backend,
    List<SocialCueQuery> queries,
    List<SocialPlatformId> platforms,
    TimeRange window,
    SocialCueStatusEnum status,
    String? statusReason,
    List<SocialCueResultsInner> results,
    List<String> narrativeIds,
    num costUsd,
    num? maxCostUsd,
    String createdBy,
    DateTime createdAt,
    DateTime? startedAt,
    DateTime? completedAt,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCue.copyWith(...)` or call `instanceOfSocialCue.copyWith.fieldName(value)` for a single field.
class _$SocialCueCWProxyImpl implements _$SocialCueCWProxy {
  const _$SocialCueCWProxyImpl(this._value);

  final SocialCue _value;

  @override
  SocialCue id(String id) => call(id: id);

  @override
  SocialCue trigger(SocialCueTrigger trigger) => call(trigger: trigger);

  @override
  SocialCue backend(SocialCueBackendEnum backend) => call(backend: backend);

  @override
  SocialCue queries(List<SocialCueQuery> queries) => call(queries: queries);

  @override
  SocialCue platforms(List<SocialPlatformId> platforms) =>
      call(platforms: platforms);

  @override
  SocialCue window(TimeRange window) => call(window: window);

  @override
  SocialCue status(SocialCueStatusEnum status) => call(status: status);

  @override
  SocialCue statusReason(String? statusReason) =>
      call(statusReason: statusReason);

  @override
  SocialCue results(List<SocialCueResultsInner> results) =>
      call(results: results);

  @override
  SocialCue narrativeIds(List<String> narrativeIds) =>
      call(narrativeIds: narrativeIds);

  @override
  SocialCue costUsd(num costUsd) => call(costUsd: costUsd);

  @override
  SocialCue maxCostUsd(num? maxCostUsd) => call(maxCostUsd: maxCostUsd);

  @override
  SocialCue createdBy(String createdBy) => call(createdBy: createdBy);

  @override
  SocialCue createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  SocialCue startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  SocialCue completedAt(DateTime? completedAt) =>
      call(completedAt: completedAt);

  @override
  SocialCue manifestId(String? manifestId) => call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCue(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCue call({
    Object? id = const $CopyWithPlaceholder(),
    Object? trigger = const $CopyWithPlaceholder(),
    Object? backend = const $CopyWithPlaceholder(),
    Object? queries = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? statusReason = const $CopyWithPlaceholder(),
    Object? results = const $CopyWithPlaceholder(),
    Object? narrativeIds = const $CopyWithPlaceholder(),
    Object? costUsd = const $CopyWithPlaceholder(),
    Object? maxCostUsd = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? completedAt = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return SocialCue(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      trigger: trigger == const $CopyWithPlaceholder() || trigger == null
          ? _value.trigger
          // ignore: cast_nullable_to_non_nullable
          : trigger as SocialCueTrigger,
      backend: backend == const $CopyWithPlaceholder() || backend == null
          ? _value.backend
          // ignore: cast_nullable_to_non_nullable
          : backend as SocialCueBackendEnum,
      queries: queries == const $CopyWithPlaceholder() || queries == null
          ? _value.queries
          // ignore: cast_nullable_to_non_nullable
          : queries as List<SocialCueQuery>,
      platforms: platforms == const $CopyWithPlaceholder() || platforms == null
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as List<SocialPlatformId>,
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as TimeRange,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SocialCueStatusEnum,
      statusReason: statusReason == const $CopyWithPlaceholder()
          ? _value.statusReason
          // ignore: cast_nullable_to_non_nullable
          : statusReason as String?,
      results: results == const $CopyWithPlaceholder() || results == null
          ? _value.results
          // ignore: cast_nullable_to_non_nullable
          : results as List<SocialCueResultsInner>,
      narrativeIds:
          narrativeIds == const $CopyWithPlaceholder() || narrativeIds == null
          ? _value.narrativeIds
          // ignore: cast_nullable_to_non_nullable
          : narrativeIds as List<String>,
      costUsd: costUsd == const $CopyWithPlaceholder() || costUsd == null
          ? _value.costUsd
          // ignore: cast_nullable_to_non_nullable
          : costUsd as num,
      maxCostUsd: maxCostUsd == const $CopyWithPlaceholder()
          ? _value.maxCostUsd
          // ignore: cast_nullable_to_non_nullable
          : maxCostUsd as num?,
      createdBy: createdBy == const $CopyWithPlaceholder() || createdBy == null
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      completedAt: completedAt == const $CopyWithPlaceholder()
          ? _value.completedAt
          // ignore: cast_nullable_to_non_nullable
          : completedAt as DateTime?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $SocialCueCopyWith on SocialCue {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCue.copyWith(...)` or `instanceOfSocialCue.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCueCWProxy get copyWith => _$SocialCueCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCue _$SocialCueFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialCue', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'trigger',
      'backend',
      'queries',
      'platforms',
      'window',
      'status',
      'results',
      'narrativeIds',
      'costUsd',
      'createdBy',
      'createdAt',
    ],
  );
  final val = SocialCue(
    id: $checkedConvert('id', (v) => v as String),
    trigger: $checkedConvert(
      'trigger',
      (v) => SocialCueTrigger.fromJson(v as Map<String, dynamic>),
    ),
    backend: $checkedConvert(
      'backend',
      (v) => $enumDecode(_$SocialCueBackendEnumEnumMap, v),
    ),
    queries: $checkedConvert(
      'queries',
      (v) => (v as List<dynamic>)
          .map((e) => SocialCueQuery.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    platforms: $checkedConvert(
      'platforms',
      (v) => (v as List<dynamic>)
          .map((e) => $enumDecode(_$SocialPlatformIdEnumMap, e))
          .toList(),
    ),
    window: $checkedConvert(
      'window',
      (v) => TimeRange.fromJson(v as Map<String, dynamic>),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$SocialCueStatusEnumEnumMap, v),
    ),
    statusReason: $checkedConvert('statusReason', (v) => v as String?),
    results: $checkedConvert(
      'results',
      (v) => (v as List<dynamic>)
          .map((e) => SocialCueResultsInner.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    narrativeIds: $checkedConvert(
      'narrativeIds',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    costUsd: $checkedConvert('costUsd', (v) => v as num),
    maxCostUsd: $checkedConvert('maxCostUsd', (v) => v as num?),
    createdBy: $checkedConvert('createdBy', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    completedAt: $checkedConvert(
      'completedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SocialCueToJson(SocialCue instance) => <String, dynamic>{
  'id': instance.id,
  'trigger': instance.trigger.toJson(),
  'backend': _$SocialCueBackendEnumEnumMap[instance.backend]!,
  'queries': instance.queries.map((e) => e.toJson()).toList(),
  'platforms': instance.platforms
      .map((e) => _$SocialPlatformIdEnumMap[e]!)
      .toList(),
  'window': instance.window.toJson(),
  'status': _$SocialCueStatusEnumEnumMap[instance.status]!,
  'statusReason': ?instance.statusReason,
  'results': instance.results.map((e) => e.toJson()).toList(),
  'narrativeIds': instance.narrativeIds,
  'costUsd': instance.costUsd,
  'maxCostUsd': ?instance.maxCostUsd,
  'createdBy': instance.createdBy,
  'createdAt': instance.createdAt.toIso8601String(),
  'startedAt': ?instance.startedAt?.toIso8601String(),
  'completedAt': ?instance.completedAt?.toIso8601String(),
  'manifestId': ?instance.manifestId,
};

const _$SocialCueBackendEnumEnumMap = {
  SocialCueBackendEnum.platformApis: 'platform-apis',
  SocialCueBackendEnum.xaiXSearch: 'xai-x-search',
};

const _$SocialPlatformIdEnumMap = {
  SocialPlatformId.blueskyJetstream: 'bluesky-jetstream',
  SocialPlatformId.blueskySearch: 'bluesky-search',
  SocialPlatformId.mastodon: 'mastodon',
  SocialPlatformId.reddit: 'reddit',
  SocialPlatformId.youtube: 'youtube',
  SocialPlatformId.x: 'x',
  SocialPlatformId.threads: 'threads',
  SocialPlatformId.instagramHashtags: 'instagram-hashtags',
};

const _$SocialCueStatusEnumEnumMap = {
  SocialCueStatusEnum.queued: 'queued',
  SocialCueStatusEnum.running: 'running',
  SocialCueStatusEnum.done: 'done',
  SocialCueStatusEnum.failed: 'failed',
  SocialCueStatusEnum.budgetRefused: 'budget-refused',
};
