// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_versus_observed.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialVersusObservedCWProxy {
  SocialVersusObserved narrativesByStatus(Map<String, int> narrativesByStatus);

  SocialVersusObserved narratives(
    List<SocialVersusObservedNarrativesInner> narratives,
  );

  SocialVersusObserved attentionGapFindingIds(
    List<String>? attentionGapFindingIds,
  );

  SocialVersusObserved leadTimes(
    List<SocialVersusObservedLeadTimesInner>? leadTimes,
  );

  SocialVersusObserved mediaReuseFindingIds(List<String>? mediaReuseFindingIds);

  SocialVersusObserved coordinationFindingIds(
    List<String>? coordinationFindingIds,
  );

  SocialVersusObserved caveats(List<Caveat>? caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVersusObserved(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVersusObserved(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialVersusObserved call({
    Map<String, int> narrativesByStatus,
    List<SocialVersusObservedNarrativesInner> narratives,
    List<String>? attentionGapFindingIds,
    List<SocialVersusObservedLeadTimesInner>? leadTimes,
    List<String>? mediaReuseFindingIds,
    List<String>? coordinationFindingIds,
    List<Caveat>? caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialVersusObserved.copyWith(...)` or call `instanceOfSocialVersusObserved.copyWith.fieldName(value)` for a single field.
class _$SocialVersusObservedCWProxyImpl
    implements _$SocialVersusObservedCWProxy {
  const _$SocialVersusObservedCWProxyImpl(this._value);

  final SocialVersusObserved _value;

  @override
  SocialVersusObserved narrativesByStatus(
    Map<String, int> narrativesByStatus,
  ) => call(narrativesByStatus: narrativesByStatus);

  @override
  SocialVersusObserved narratives(
    List<SocialVersusObservedNarrativesInner> narratives,
  ) => call(narratives: narratives);

  @override
  SocialVersusObserved attentionGapFindingIds(
    List<String>? attentionGapFindingIds,
  ) => call(attentionGapFindingIds: attentionGapFindingIds);

  @override
  SocialVersusObserved leadTimes(
    List<SocialVersusObservedLeadTimesInner>? leadTimes,
  ) => call(leadTimes: leadTimes);

  @override
  SocialVersusObserved mediaReuseFindingIds(
    List<String>? mediaReuseFindingIds,
  ) => call(mediaReuseFindingIds: mediaReuseFindingIds);

  @override
  SocialVersusObserved coordinationFindingIds(
    List<String>? coordinationFindingIds,
  ) => call(coordinationFindingIds: coordinationFindingIds);

  @override
  SocialVersusObserved caveats(List<Caveat>? caveats) => call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVersusObserved(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVersusObserved(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialVersusObserved call({
    Object? narrativesByStatus = const $CopyWithPlaceholder(),
    Object? narratives = const $CopyWithPlaceholder(),
    Object? attentionGapFindingIds = const $CopyWithPlaceholder(),
    Object? leadTimes = const $CopyWithPlaceholder(),
    Object? mediaReuseFindingIds = const $CopyWithPlaceholder(),
    Object? coordinationFindingIds = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return SocialVersusObserved(
      narrativesByStatus:
          narrativesByStatus == const $CopyWithPlaceholder() ||
              narrativesByStatus == null
          ? _value.narrativesByStatus
          // ignore: cast_nullable_to_non_nullable
          : narrativesByStatus as Map<String, int>,
      narratives:
          narratives == const $CopyWithPlaceholder() || narratives == null
          ? _value.narratives
          // ignore: cast_nullable_to_non_nullable
          : narratives as List<SocialVersusObservedNarrativesInner>,
      attentionGapFindingIds:
          attentionGapFindingIds == const $CopyWithPlaceholder()
          ? _value.attentionGapFindingIds
          // ignore: cast_nullable_to_non_nullable
          : attentionGapFindingIds as List<String>?,
      leadTimes: leadTimes == const $CopyWithPlaceholder()
          ? _value.leadTimes
          // ignore: cast_nullable_to_non_nullable
          : leadTimes as List<SocialVersusObservedLeadTimesInner>?,
      mediaReuseFindingIds: mediaReuseFindingIds == const $CopyWithPlaceholder()
          ? _value.mediaReuseFindingIds
          // ignore: cast_nullable_to_non_nullable
          : mediaReuseFindingIds as List<String>?,
      coordinationFindingIds:
          coordinationFindingIds == const $CopyWithPlaceholder()
          ? _value.coordinationFindingIds
          // ignore: cast_nullable_to_non_nullable
          : coordinationFindingIds as List<String>?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
    );
  }
}

extension $SocialVersusObservedCopyWith on SocialVersusObserved {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialVersusObserved.copyWith(...)` or `instanceOfSocialVersusObserved.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialVersusObservedCWProxy get copyWith =>
      _$SocialVersusObservedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialVersusObserved _$SocialVersusObservedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialVersusObserved', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['narrativesByStatus', 'narratives']);
  final val = SocialVersusObserved(
    narrativesByStatus: $checkedConvert(
      'narrativesByStatus',
      (v) => Map<String, int>.from(v as Map),
    ),
    narratives: $checkedConvert(
      'narratives',
      (v) => (v as List<dynamic>)
          .map(
            (e) => SocialVersusObservedNarrativesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    attentionGapFindingIds: $checkedConvert(
      'attentionGapFindingIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    leadTimes: $checkedConvert(
      'leadTimes',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => SocialVersusObservedLeadTimesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    mediaReuseFindingIds: $checkedConvert(
      'mediaReuseFindingIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    coordinationFindingIds: $checkedConvert(
      'coordinationFindingIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialVersusObservedToJson(
  SocialVersusObserved instance,
) => <String, dynamic>{
  'narrativesByStatus': instance.narrativesByStatus,
  'narratives': instance.narratives.map((e) => e.toJson()).toList(),
  'attentionGapFindingIds': ?instance.attentionGapFindingIds,
  'leadTimes': ?instance.leadTimes?.map((e) => e.toJson()).toList(),
  'mediaReuseFindingIds': ?instance.mediaReuseFindingIds,
  'coordinationFindingIds': ?instance.coordinationFindingIds,
  'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
};
