// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_evaluation_profiles_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystEvaluationProfilesInnerCWProxy {
  AnalystEvaluationProfilesInner profileId(String profileId);

  AnalystEvaluationProfilesInner runsByStatus(Map<String, int> runsByStatus);

  AnalystEvaluationProfilesInner noopShare(num? noopShare);

  AnalystEvaluationProfilesInner tokensPerDay(
    List<AnalystEvaluationProfilesInnerTokensPerDayInner>? tokensPerDay,
  );

  AnalystEvaluationProfilesInner proposalAcceptanceRate(
    num? proposalAcceptanceRate,
  );

  AnalystEvaluationProfilesInner notesDismissedShare(num? notesDismissedShare);

  AnalystEvaluationProfilesInner usefulRatio(num? usefulRatio);

  AnalystEvaluationProfilesInner wrongRatio(num? wrongRatio);

  AnalystEvaluationProfilesInner citationsDroppedPerRun(
    num? citationsDroppedPerRun,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystEvaluationProfilesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystEvaluationProfilesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystEvaluationProfilesInner call({
    String profileId,
    Map<String, int> runsByStatus,
    num? noopShare,
    List<AnalystEvaluationProfilesInnerTokensPerDayInner>? tokensPerDay,
    num? proposalAcceptanceRate,
    num? notesDismissedShare,
    num? usefulRatio,
    num? wrongRatio,
    num? citationsDroppedPerRun,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystEvaluationProfilesInner.copyWith(...)` or call `instanceOfAnalystEvaluationProfilesInner.copyWith.fieldName(value)` for a single field.
class _$AnalystEvaluationProfilesInnerCWProxyImpl
    implements _$AnalystEvaluationProfilesInnerCWProxy {
  const _$AnalystEvaluationProfilesInnerCWProxyImpl(this._value);

  final AnalystEvaluationProfilesInner _value;

  @override
  AnalystEvaluationProfilesInner profileId(String profileId) =>
      call(profileId: profileId);

  @override
  AnalystEvaluationProfilesInner runsByStatus(Map<String, int> runsByStatus) =>
      call(runsByStatus: runsByStatus);

  @override
  AnalystEvaluationProfilesInner noopShare(num? noopShare) =>
      call(noopShare: noopShare);

  @override
  AnalystEvaluationProfilesInner tokensPerDay(
    List<AnalystEvaluationProfilesInnerTokensPerDayInner>? tokensPerDay,
  ) => call(tokensPerDay: tokensPerDay);

  @override
  AnalystEvaluationProfilesInner proposalAcceptanceRate(
    num? proposalAcceptanceRate,
  ) => call(proposalAcceptanceRate: proposalAcceptanceRate);

  @override
  AnalystEvaluationProfilesInner notesDismissedShare(
    num? notesDismissedShare,
  ) => call(notesDismissedShare: notesDismissedShare);

  @override
  AnalystEvaluationProfilesInner usefulRatio(num? usefulRatio) =>
      call(usefulRatio: usefulRatio);

  @override
  AnalystEvaluationProfilesInner wrongRatio(num? wrongRatio) =>
      call(wrongRatio: wrongRatio);

  @override
  AnalystEvaluationProfilesInner citationsDroppedPerRun(
    num? citationsDroppedPerRun,
  ) => call(citationsDroppedPerRun: citationsDroppedPerRun);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystEvaluationProfilesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystEvaluationProfilesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystEvaluationProfilesInner call({
    Object? profileId = const $CopyWithPlaceholder(),
    Object? runsByStatus = const $CopyWithPlaceholder(),
    Object? noopShare = const $CopyWithPlaceholder(),
    Object? tokensPerDay = const $CopyWithPlaceholder(),
    Object? proposalAcceptanceRate = const $CopyWithPlaceholder(),
    Object? notesDismissedShare = const $CopyWithPlaceholder(),
    Object? usefulRatio = const $CopyWithPlaceholder(),
    Object? wrongRatio = const $CopyWithPlaceholder(),
    Object? citationsDroppedPerRun = const $CopyWithPlaceholder(),
  }) {
    return AnalystEvaluationProfilesInner(
      profileId: profileId == const $CopyWithPlaceholder() || profileId == null
          ? _value.profileId
          // ignore: cast_nullable_to_non_nullable
          : profileId as String,
      runsByStatus:
          runsByStatus == const $CopyWithPlaceholder() || runsByStatus == null
          ? _value.runsByStatus
          // ignore: cast_nullable_to_non_nullable
          : runsByStatus as Map<String, int>,
      noopShare: noopShare == const $CopyWithPlaceholder()
          ? _value.noopShare
          // ignore: cast_nullable_to_non_nullable
          : noopShare as num?,
      tokensPerDay: tokensPerDay == const $CopyWithPlaceholder()
          ? _value.tokensPerDay
          // ignore: cast_nullable_to_non_nullable
          : tokensPerDay
                as List<AnalystEvaluationProfilesInnerTokensPerDayInner>?,
      proposalAcceptanceRate:
          proposalAcceptanceRate == const $CopyWithPlaceholder()
          ? _value.proposalAcceptanceRate
          // ignore: cast_nullable_to_non_nullable
          : proposalAcceptanceRate as num?,
      notesDismissedShare: notesDismissedShare == const $CopyWithPlaceholder()
          ? _value.notesDismissedShare
          // ignore: cast_nullable_to_non_nullable
          : notesDismissedShare as num?,
      usefulRatio: usefulRatio == const $CopyWithPlaceholder()
          ? _value.usefulRatio
          // ignore: cast_nullable_to_non_nullable
          : usefulRatio as num?,
      wrongRatio: wrongRatio == const $CopyWithPlaceholder()
          ? _value.wrongRatio
          // ignore: cast_nullable_to_non_nullable
          : wrongRatio as num?,
      citationsDroppedPerRun:
          citationsDroppedPerRun == const $CopyWithPlaceholder()
          ? _value.citationsDroppedPerRun
          // ignore: cast_nullable_to_non_nullable
          : citationsDroppedPerRun as num?,
    );
  }
}

extension $AnalystEvaluationProfilesInnerCopyWith
    on AnalystEvaluationProfilesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystEvaluationProfilesInner.copyWith(...)` or `instanceOfAnalystEvaluationProfilesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystEvaluationProfilesInnerCWProxy get copyWith =>
      _$AnalystEvaluationProfilesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystEvaluationProfilesInner _$AnalystEvaluationProfilesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalystEvaluationProfilesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['profileId', 'runsByStatus']);
  final val = AnalystEvaluationProfilesInner(
    profileId: $checkedConvert('profileId', (v) => v as String),
    runsByStatus: $checkedConvert(
      'runsByStatus',
      (v) => Map<String, int>.from(v as Map),
    ),
    noopShare: $checkedConvert('noopShare', (v) => v as num?),
    tokensPerDay: $checkedConvert(
      'tokensPerDay',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => AnalystEvaluationProfilesInnerTokensPerDayInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    proposalAcceptanceRate: $checkedConvert(
      'proposalAcceptanceRate',
      (v) => v as num?,
    ),
    notesDismissedShare: $checkedConvert(
      'notesDismissedShare',
      (v) => v as num?,
    ),
    usefulRatio: $checkedConvert('usefulRatio', (v) => v as num?),
    wrongRatio: $checkedConvert('wrongRatio', (v) => v as num?),
    citationsDroppedPerRun: $checkedConvert(
      'citationsDroppedPerRun',
      (v) => v as num?,
    ),
  );
  return val;
});

Map<String, dynamic> _$AnalystEvaluationProfilesInnerToJson(
  AnalystEvaluationProfilesInner instance,
) => <String, dynamic>{
  'profileId': instance.profileId,
  'runsByStatus': instance.runsByStatus,
  'noopShare': ?instance.noopShare,
  'tokensPerDay': ?instance.tokensPerDay?.map((e) => e.toJson()).toList(),
  'proposalAcceptanceRate': ?instance.proposalAcceptanceRate,
  'notesDismissedShare': ?instance.notesDismissedShare,
  'usefulRatio': ?instance.usefulRatio,
  'wrongRatio': ?instance.wrongRatio,
  'citationsDroppedPerRun': ?instance.citationsDroppedPerRun,
};
