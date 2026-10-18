// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exercise_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExerciseStatsCWProxy {
  ExerciseStats versionsScanned(int versionsScanned);

  ExerciseStats alertsByRule(Map<String, int>? alertsByRule);

  ExerciseStats notificationsByChannel(
    Map<String, int>? notificationsByChannel,
  );

  ExerciseStats suppressedByCooldown(int? suppressedByCooldown);

  ExerciseStats suppressedByRateCap(int? suppressedByRateCap);

  ExerciseStats skippedRules(
    List<ExerciseStatsSkippedRulesInner>? skippedRules,
  );

  ExerciseStats caveats(List<Caveat>? caveats);

  ExerciseStats alertsRaised(int? alertsRaised);

  ExerciseStats error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExerciseStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExerciseStats(...).copyWith(id: 12, name: "My name")
  /// ```
  ExerciseStats call({
    int versionsScanned,
    Map<String, int>? alertsByRule,
    Map<String, int>? notificationsByChannel,
    int? suppressedByCooldown,
    int? suppressedByRateCap,
    List<ExerciseStatsSkippedRulesInner>? skippedRules,
    List<Caveat>? caveats,
    int? alertsRaised,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExerciseStats.copyWith(...)` or call `instanceOfExerciseStats.copyWith.fieldName(value)` for a single field.
class _$ExerciseStatsCWProxyImpl implements _$ExerciseStatsCWProxy {
  const _$ExerciseStatsCWProxyImpl(this._value);

  final ExerciseStats _value;

  @override
  ExerciseStats versionsScanned(int versionsScanned) =>
      call(versionsScanned: versionsScanned);

  @override
  ExerciseStats alertsByRule(Map<String, int>? alertsByRule) =>
      call(alertsByRule: alertsByRule);

  @override
  ExerciseStats notificationsByChannel(
    Map<String, int>? notificationsByChannel,
  ) => call(notificationsByChannel: notificationsByChannel);

  @override
  ExerciseStats suppressedByCooldown(int? suppressedByCooldown) =>
      call(suppressedByCooldown: suppressedByCooldown);

  @override
  ExerciseStats suppressedByRateCap(int? suppressedByRateCap) =>
      call(suppressedByRateCap: suppressedByRateCap);

  @override
  ExerciseStats skippedRules(
    List<ExerciseStatsSkippedRulesInner>? skippedRules,
  ) => call(skippedRules: skippedRules);

  @override
  ExerciseStats caveats(List<Caveat>? caveats) => call(caveats: caveats);

  @override
  ExerciseStats alertsRaised(int? alertsRaised) =>
      call(alertsRaised: alertsRaised);

  @override
  ExerciseStats error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExerciseStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExerciseStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExerciseStats call({
    Object? versionsScanned = const $CopyWithPlaceholder(),
    Object? alertsByRule = const $CopyWithPlaceholder(),
    Object? notificationsByChannel = const $CopyWithPlaceholder(),
    Object? suppressedByCooldown = const $CopyWithPlaceholder(),
    Object? suppressedByRateCap = const $CopyWithPlaceholder(),
    Object? skippedRules = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? alertsRaised = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return ExerciseStats(
      versionsScanned:
          versionsScanned == const $CopyWithPlaceholder() ||
              versionsScanned == null
          ? _value.versionsScanned
          // ignore: cast_nullable_to_non_nullable
          : versionsScanned as int,
      alertsByRule: alertsByRule == const $CopyWithPlaceholder()
          ? _value.alertsByRule
          // ignore: cast_nullable_to_non_nullable
          : alertsByRule as Map<String, int>?,
      notificationsByChannel:
          notificationsByChannel == const $CopyWithPlaceholder()
          ? _value.notificationsByChannel
          // ignore: cast_nullable_to_non_nullable
          : notificationsByChannel as Map<String, int>?,
      suppressedByCooldown: suppressedByCooldown == const $CopyWithPlaceholder()
          ? _value.suppressedByCooldown
          // ignore: cast_nullable_to_non_nullable
          : suppressedByCooldown as int?,
      suppressedByRateCap: suppressedByRateCap == const $CopyWithPlaceholder()
          ? _value.suppressedByRateCap
          // ignore: cast_nullable_to_non_nullable
          : suppressedByRateCap as int?,
      skippedRules: skippedRules == const $CopyWithPlaceholder()
          ? _value.skippedRules
          // ignore: cast_nullable_to_non_nullable
          : skippedRules as List<ExerciseStatsSkippedRulesInner>?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
      alertsRaised: alertsRaised == const $CopyWithPlaceholder()
          ? _value.alertsRaised
          // ignore: cast_nullable_to_non_nullable
          : alertsRaised as int?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $ExerciseStatsCopyWith on ExerciseStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExerciseStats.copyWith(...)` or `instanceOfExerciseStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExerciseStatsCWProxy get copyWith => _$ExerciseStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExerciseStats _$ExerciseStatsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExerciseStats', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['versionsScanned']);
  final val = ExerciseStats(
    versionsScanned: $checkedConvert(
      'versionsScanned',
      (v) => (v as num).toInt(),
    ),
    alertsByRule: $checkedConvert(
      'alertsByRule',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    notificationsByChannel: $checkedConvert(
      'notificationsByChannel',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    suppressedByCooldown: $checkedConvert(
      'suppressedByCooldown',
      (v) => (v as num?)?.toInt(),
    ),
    suppressedByRateCap: $checkedConvert(
      'suppressedByRateCap',
      (v) => (v as num?)?.toInt(),
    ),
    skippedRules: $checkedConvert(
      'skippedRules',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ExerciseStatsSkippedRulesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    alertsRaised: $checkedConvert('alertsRaised', (v) => (v as num?)?.toInt()),
    error: $checkedConvert('error', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ExerciseStatsToJson(ExerciseStats instance) =>
    <String, dynamic>{
      'versionsScanned': instance.versionsScanned,
      'alertsByRule': ?instance.alertsByRule,
      'notificationsByChannel': ?instance.notificationsByChannel,
      'suppressedByCooldown': ?instance.suppressedByCooldown,
      'suppressedByRateCap': ?instance.suppressedByRateCap,
      'skippedRules': ?instance.skippedRules?.map((e) => e.toJson()).toList(),
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
      'alertsRaised': ?instance.alertsRaised,
      'error': ?instance.error,
    };
