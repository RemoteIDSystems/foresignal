// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_comparison_revision.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialComparisonRevisionCWProxy {
  SocialComparisonRevision revision(int revision);

  SocialComparisonRevision at(DateTime at);

  SocialComparisonRevision status(SocialComparisonRevisionStatusEnum status);

  SocialComparisonRevision previousStatus(
    SocialComparisonRevisionPreviousStatusEnum? previousStatus,
  );

  SocialComparisonRevision reason(String reason);

  SocialComparisonRevision physicalEvidenceRefs(
    List<String>? physicalEvidenceRefs,
  );

  SocialComparisonRevision manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialComparisonRevision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialComparisonRevision(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialComparisonRevision call({
    int revision,
    DateTime at,
    SocialComparisonRevisionStatusEnum status,
    SocialComparisonRevisionPreviousStatusEnum? previousStatus,
    String reason,
    List<String>? physicalEvidenceRefs,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialComparisonRevision.copyWith(...)` or call `instanceOfSocialComparisonRevision.copyWith.fieldName(value)` for a single field.
class _$SocialComparisonRevisionCWProxyImpl
    implements _$SocialComparisonRevisionCWProxy {
  const _$SocialComparisonRevisionCWProxyImpl(this._value);

  final SocialComparisonRevision _value;

  @override
  SocialComparisonRevision revision(int revision) => call(revision: revision);

  @override
  SocialComparisonRevision at(DateTime at) => call(at: at);

  @override
  SocialComparisonRevision status(SocialComparisonRevisionStatusEnum status) =>
      call(status: status);

  @override
  SocialComparisonRevision previousStatus(
    SocialComparisonRevisionPreviousStatusEnum? previousStatus,
  ) => call(previousStatus: previousStatus);

  @override
  SocialComparisonRevision reason(String reason) => call(reason: reason);

  @override
  SocialComparisonRevision physicalEvidenceRefs(
    List<String>? physicalEvidenceRefs,
  ) => call(physicalEvidenceRefs: physicalEvidenceRefs);

  @override
  SocialComparisonRevision manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialComparisonRevision(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialComparisonRevision(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialComparisonRevision call({
    Object? revision = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? previousStatus = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? physicalEvidenceRefs = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return SocialComparisonRevision(
      revision: revision == const $CopyWithPlaceholder() || revision == null
          ? _value.revision
          // ignore: cast_nullable_to_non_nullable
          : revision as int,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SocialComparisonRevisionStatusEnum,
      previousStatus: previousStatus == const $CopyWithPlaceholder()
          ? _value.previousStatus
          // ignore: cast_nullable_to_non_nullable
          : previousStatus as SocialComparisonRevisionPreviousStatusEnum?,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
      physicalEvidenceRefs: physicalEvidenceRefs == const $CopyWithPlaceholder()
          ? _value.physicalEvidenceRefs
          // ignore: cast_nullable_to_non_nullable
          : physicalEvidenceRefs as List<String>?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $SocialComparisonRevisionCopyWith on SocialComparisonRevision {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialComparisonRevision.copyWith(...)` or `instanceOfSocialComparisonRevision.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialComparisonRevisionCWProxy get copyWith =>
      _$SocialComparisonRevisionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialComparisonRevision _$SocialComparisonRevisionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialComparisonRevision', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['revision', 'at', 'status', 'reason']);
  final val = SocialComparisonRevision(
    revision: $checkedConvert('revision', (v) => (v as num).toInt()),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$SocialComparisonRevisionStatusEnumEnumMap, v),
    ),
    previousStatus: $checkedConvert(
      'previousStatus',
      (v) => $enumDecodeNullable(
        _$SocialComparisonRevisionPreviousStatusEnumEnumMap,
        v,
      ),
    ),
    reason: $checkedConvert('reason', (v) => v as String),
    physicalEvidenceRefs: $checkedConvert(
      'physicalEvidenceRefs',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SocialComparisonRevisionToJson(
  SocialComparisonRevision instance,
) => <String, dynamic>{
  'revision': instance.revision,
  'at': instance.at.toIso8601String(),
  'status': _$SocialComparisonRevisionStatusEnumEnumMap[instance.status]!,
  'previousStatus':
      ?_$SocialComparisonRevisionPreviousStatusEnumEnumMap[instance
          .previousStatus],
  'reason': instance.reason,
  'physicalEvidenceRefs': ?instance.physicalEvidenceRefs,
  'manifestId': ?instance.manifestId,
};

const _$SocialComparisonRevisionStatusEnumEnumMap = {
  SocialComparisonRevisionStatusEnum.pending: 'pending',
  SocialComparisonRevisionStatusEnum.corroborated: 'corroborated',
  SocialComparisonRevisionStatusEnum.unobserved: 'unobserved',
  SocialComparisonRevisionStatusEnum.contradicted: 'contradicted',
  SocialComparisonRevisionStatusEnum.insufficientCoverage:
      'insufficient-coverage',
};

const _$SocialComparisonRevisionPreviousStatusEnumEnumMap = {
  SocialComparisonRevisionPreviousStatusEnum.pending: 'pending',
  SocialComparisonRevisionPreviousStatusEnum.corroborated: 'corroborated',
  SocialComparisonRevisionPreviousStatusEnum.unobserved: 'unobserved',
  SocialComparisonRevisionPreviousStatusEnum.contradicted: 'contradicted',
  SocialComparisonRevisionPreviousStatusEnum.insufficientCoverage:
      'insufficient-coverage',
};
