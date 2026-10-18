// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_study_window_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStudyWindowStatsCWProxy {
  EventStudyWindowStats window(List<int> window);

  EventStudyWindowStats caar(num? caar);

  EventStudyWindowStats standardisedZ(num? standardisedZ);

  EventStudyWindowStats standardisedP(num? standardisedP);

  EventStudyWindowStats corradoStat(num? corradoStat);

  EventStudyWindowStats corradoP(num? corradoP);

  EventStudyWindowStats placeboP(num? placeboP);

  EventStudyWindowStats placebo(EventStudyWindowStatsPlacebo? placebo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyWindowStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyWindowStats(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStudyWindowStats call({
    List<int> window,
    num? caar,
    num? standardisedZ,
    num? standardisedP,
    num? corradoStat,
    num? corradoP,
    num? placeboP,
    EventStudyWindowStatsPlacebo? placebo,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStudyWindowStats.copyWith(...)` or call `instanceOfEventStudyWindowStats.copyWith.fieldName(value)` for a single field.
class _$EventStudyWindowStatsCWProxyImpl
    implements _$EventStudyWindowStatsCWProxy {
  const _$EventStudyWindowStatsCWProxyImpl(this._value);

  final EventStudyWindowStats _value;

  @override
  EventStudyWindowStats window(List<int> window) => call(window: window);

  @override
  EventStudyWindowStats caar(num? caar) => call(caar: caar);

  @override
  EventStudyWindowStats standardisedZ(num? standardisedZ) =>
      call(standardisedZ: standardisedZ);

  @override
  EventStudyWindowStats standardisedP(num? standardisedP) =>
      call(standardisedP: standardisedP);

  @override
  EventStudyWindowStats corradoStat(num? corradoStat) =>
      call(corradoStat: corradoStat);

  @override
  EventStudyWindowStats corradoP(num? corradoP) => call(corradoP: corradoP);

  @override
  EventStudyWindowStats placeboP(num? placeboP) => call(placeboP: placeboP);

  @override
  EventStudyWindowStats placebo(EventStudyWindowStatsPlacebo? placebo) =>
      call(placebo: placebo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyWindowStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyWindowStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStudyWindowStats call({
    Object? window = const $CopyWithPlaceholder(),
    Object? caar = const $CopyWithPlaceholder(),
    Object? standardisedZ = const $CopyWithPlaceholder(),
    Object? standardisedP = const $CopyWithPlaceholder(),
    Object? corradoStat = const $CopyWithPlaceholder(),
    Object? corradoP = const $CopyWithPlaceholder(),
    Object? placeboP = const $CopyWithPlaceholder(),
    Object? placebo = const $CopyWithPlaceholder(),
  }) {
    return EventStudyWindowStats(
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as List<int>,
      caar: caar == const $CopyWithPlaceholder()
          ? _value.caar
          // ignore: cast_nullable_to_non_nullable
          : caar as num?,
      standardisedZ: standardisedZ == const $CopyWithPlaceholder()
          ? _value.standardisedZ
          // ignore: cast_nullable_to_non_nullable
          : standardisedZ as num?,
      standardisedP: standardisedP == const $CopyWithPlaceholder()
          ? _value.standardisedP
          // ignore: cast_nullable_to_non_nullable
          : standardisedP as num?,
      corradoStat: corradoStat == const $CopyWithPlaceholder()
          ? _value.corradoStat
          // ignore: cast_nullable_to_non_nullable
          : corradoStat as num?,
      corradoP: corradoP == const $CopyWithPlaceholder()
          ? _value.corradoP
          // ignore: cast_nullable_to_non_nullable
          : corradoP as num?,
      placeboP: placeboP == const $CopyWithPlaceholder()
          ? _value.placeboP
          // ignore: cast_nullable_to_non_nullable
          : placeboP as num?,
      placebo: placebo == const $CopyWithPlaceholder()
          ? _value.placebo
          // ignore: cast_nullable_to_non_nullable
          : placebo as EventStudyWindowStatsPlacebo?,
    );
  }
}

extension $EventStudyWindowStatsCopyWith on EventStudyWindowStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStudyWindowStats.copyWith(...)` or `instanceOfEventStudyWindowStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStudyWindowStatsCWProxy get copyWith =>
      _$EventStudyWindowStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStudyWindowStats _$EventStudyWindowStatsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStudyWindowStats', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['window']);
  final val = EventStudyWindowStats(
    window: $checkedConvert(
      'window',
      (v) => (v as List<dynamic>).map((e) => (e as num).toInt()).toList(),
    ),
    caar: $checkedConvert('caar', (v) => v as num?),
    standardisedZ: $checkedConvert('standardisedZ', (v) => v as num?),
    standardisedP: $checkedConvert('standardisedP', (v) => v as num?),
    corradoStat: $checkedConvert('corradoStat', (v) => v as num?),
    corradoP: $checkedConvert('corradoP', (v) => v as num?),
    placeboP: $checkedConvert('placeboP', (v) => v as num?),
    placebo: $checkedConvert(
      'placebo',
      (v) => v == null
          ? null
          : EventStudyWindowStatsPlacebo.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$EventStudyWindowStatsToJson(
  EventStudyWindowStats instance,
) => <String, dynamic>{
  'window': instance.window,
  'caar': ?instance.caar,
  'standardisedZ': ?instance.standardisedZ,
  'standardisedP': ?instance.standardisedP,
  'corradoStat': ?instance.corradoStat,
  'corradoP': ?instance.corradoP,
  'placeboP': ?instance.placeboP,
  'placebo': ?instance.placebo?.toJson(),
};
