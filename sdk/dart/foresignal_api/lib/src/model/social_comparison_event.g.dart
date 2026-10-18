// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_comparison_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialComparisonEventCWProxy {
  SocialComparisonEvent event(SocialComparisonEventEventEnum event);

  SocialComparisonEvent comparisonId(String comparisonId);

  SocialComparisonEvent claimId(String claimId);

  SocialComparisonEvent narrativeId(String narrativeId);

  SocialComparisonEvent status(SocialComparisonEventStatusEnum status);

  SocialComparisonEvent previousStatus(
    SocialComparisonEventPreviousStatusEnum? previousStatus,
  );

  SocialComparisonEvent revision(int revision);

  SocialComparisonEvent evaluatedAt(DateTime evaluatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialComparisonEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialComparisonEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialComparisonEvent call({
    SocialComparisonEventEventEnum event,
    String comparisonId,
    String claimId,
    String narrativeId,
    SocialComparisonEventStatusEnum status,
    SocialComparisonEventPreviousStatusEnum? previousStatus,
    int revision,
    DateTime evaluatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialComparisonEvent.copyWith(...)` or call `instanceOfSocialComparisonEvent.copyWith.fieldName(value)` for a single field.
class _$SocialComparisonEventCWProxyImpl
    implements _$SocialComparisonEventCWProxy {
  const _$SocialComparisonEventCWProxyImpl(this._value);

  final SocialComparisonEvent _value;

  @override
  SocialComparisonEvent event(SocialComparisonEventEventEnum event) =>
      call(event: event);

  @override
  SocialComparisonEvent comparisonId(String comparisonId) =>
      call(comparisonId: comparisonId);

  @override
  SocialComparisonEvent claimId(String claimId) => call(claimId: claimId);

  @override
  SocialComparisonEvent narrativeId(String narrativeId) =>
      call(narrativeId: narrativeId);

  @override
  SocialComparisonEvent status(SocialComparisonEventStatusEnum status) =>
      call(status: status);

  @override
  SocialComparisonEvent previousStatus(
    SocialComparisonEventPreviousStatusEnum? previousStatus,
  ) => call(previousStatus: previousStatus);

  @override
  SocialComparisonEvent revision(int revision) => call(revision: revision);

  @override
  SocialComparisonEvent evaluatedAt(DateTime evaluatedAt) =>
      call(evaluatedAt: evaluatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialComparisonEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialComparisonEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialComparisonEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? comparisonId = const $CopyWithPlaceholder(),
    Object? claimId = const $CopyWithPlaceholder(),
    Object? narrativeId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? previousStatus = const $CopyWithPlaceholder(),
    Object? revision = const $CopyWithPlaceholder(),
    Object? evaluatedAt = const $CopyWithPlaceholder(),
  }) {
    return SocialComparisonEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as SocialComparisonEventEventEnum,
      comparisonId:
          comparisonId == const $CopyWithPlaceholder() || comparisonId == null
          ? _value.comparisonId
          // ignore: cast_nullable_to_non_nullable
          : comparisonId as String,
      claimId: claimId == const $CopyWithPlaceholder() || claimId == null
          ? _value.claimId
          // ignore: cast_nullable_to_non_nullable
          : claimId as String,
      narrativeId:
          narrativeId == const $CopyWithPlaceholder() || narrativeId == null
          ? _value.narrativeId
          // ignore: cast_nullable_to_non_nullable
          : narrativeId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SocialComparisonEventStatusEnum,
      previousStatus: previousStatus == const $CopyWithPlaceholder()
          ? _value.previousStatus
          // ignore: cast_nullable_to_non_nullable
          : previousStatus as SocialComparisonEventPreviousStatusEnum?,
      revision: revision == const $CopyWithPlaceholder() || revision == null
          ? _value.revision
          // ignore: cast_nullable_to_non_nullable
          : revision as int,
      evaluatedAt:
          evaluatedAt == const $CopyWithPlaceholder() || evaluatedAt == null
          ? _value.evaluatedAt
          // ignore: cast_nullable_to_non_nullable
          : evaluatedAt as DateTime,
    );
  }
}

extension $SocialComparisonEventCopyWith on SocialComparisonEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialComparisonEvent.copyWith(...)` or `instanceOfSocialComparisonEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialComparisonEventCWProxy get copyWith =>
      _$SocialComparisonEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialComparisonEvent _$SocialComparisonEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialComparisonEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'event',
      'comparisonId',
      'claimId',
      'narrativeId',
      'status',
      'revision',
      'evaluatedAt',
    ],
  );
  final val = SocialComparisonEvent(
    event: $checkedConvert(
      'event',
      (v) => $enumDecode(_$SocialComparisonEventEventEnumEnumMap, v),
    ),
    comparisonId: $checkedConvert('comparisonId', (v) => v as String),
    claimId: $checkedConvert('claimId', (v) => v as String),
    narrativeId: $checkedConvert('narrativeId', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$SocialComparisonEventStatusEnumEnumMap, v),
    ),
    previousStatus: $checkedConvert(
      'previousStatus',
      (v) => $enumDecodeNullable(
        _$SocialComparisonEventPreviousStatusEnumEnumMap,
        v,
      ),
    ),
    revision: $checkedConvert('revision', (v) => (v as num).toInt()),
    evaluatedAt: $checkedConvert(
      'evaluatedAt',
      (v) => DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialComparisonEventToJson(
  SocialComparisonEvent instance,
) => <String, dynamic>{
  'event': _$SocialComparisonEventEventEnumEnumMap[instance.event]!,
  'comparisonId': instance.comparisonId,
  'claimId': instance.claimId,
  'narrativeId': instance.narrativeId,
  'status': _$SocialComparisonEventStatusEnumEnumMap[instance.status]!,
  'previousStatus':
      ?_$SocialComparisonEventPreviousStatusEnumEnumMap[instance
          .previousStatus],
  'revision': instance.revision,
  'evaluatedAt': instance.evaluatedAt.toIso8601String(),
};

const _$SocialComparisonEventEventEnumEnumMap = {
  SocialComparisonEventEventEnum.comparisonPeriodUpdated: 'comparison.updated',
};

const _$SocialComparisonEventStatusEnumEnumMap = {
  SocialComparisonEventStatusEnum.pending: 'pending',
  SocialComparisonEventStatusEnum.corroborated: 'corroborated',
  SocialComparisonEventStatusEnum.unobserved: 'unobserved',
  SocialComparisonEventStatusEnum.contradicted: 'contradicted',
  SocialComparisonEventStatusEnum.insufficientCoverage: 'insufficient-coverage',
};

const _$SocialComparisonEventPreviousStatusEnumEnumMap = {
  SocialComparisonEventPreviousStatusEnum.pending: 'pending',
  SocialComparisonEventPreviousStatusEnum.corroborated: 'corroborated',
  SocialComparisonEventPreviousStatusEnum.unobserved: 'unobserved',
  SocialComparisonEventPreviousStatusEnum.contradicted: 'contradicted',
  SocialComparisonEventPreviousStatusEnum.insufficientCoverage:
      'insufficient-coverage',
};
