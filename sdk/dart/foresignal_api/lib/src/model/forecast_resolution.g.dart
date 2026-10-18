// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_resolution.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastResolutionCWProxy {
  ForecastResolution outcome(String? outcome);

  ForecastResolution value(num? value);

  ForecastResolution resolvedAt(DateTime resolvedAt);

  ForecastResolution resolvedBy(String resolvedBy);

  ForecastResolution early(bool? early);

  ForecastResolution asOf(DateTime? asOf);

  ForecastResolution evidence(List<String> evidence);

  ForecastResolution note(String? note);

  ForecastResolution caveats(List<Caveat>? caveats);

  ForecastResolution manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastResolution(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastResolution(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastResolution call({
    String? outcome,
    num? value,
    DateTime resolvedAt,
    String resolvedBy,
    bool? early,
    DateTime? asOf,
    List<String> evidence,
    String? note,
    List<Caveat>? caveats,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastResolution.copyWith(...)` or call `instanceOfForecastResolution.copyWith.fieldName(value)` for a single field.
class _$ForecastResolutionCWProxyImpl implements _$ForecastResolutionCWProxy {
  const _$ForecastResolutionCWProxyImpl(this._value);

  final ForecastResolution _value;

  @override
  ForecastResolution outcome(String? outcome) => call(outcome: outcome);

  @override
  ForecastResolution value(num? value) => call(value: value);

  @override
  ForecastResolution resolvedAt(DateTime resolvedAt) =>
      call(resolvedAt: resolvedAt);

  @override
  ForecastResolution resolvedBy(String resolvedBy) =>
      call(resolvedBy: resolvedBy);

  @override
  ForecastResolution early(bool? early) => call(early: early);

  @override
  ForecastResolution asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  ForecastResolution evidence(List<String> evidence) =>
      call(evidence: evidence);

  @override
  ForecastResolution note(String? note) => call(note: note);

  @override
  ForecastResolution caveats(List<Caveat>? caveats) => call(caveats: caveats);

  @override
  ForecastResolution manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastResolution(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastResolution(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastResolution call({
    Object? outcome = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? resolvedAt = const $CopyWithPlaceholder(),
    Object? resolvedBy = const $CopyWithPlaceholder(),
    Object? early = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return ForecastResolution(
      outcome: outcome == const $CopyWithPlaceholder()
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as String?,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      resolvedAt:
          resolvedAt == const $CopyWithPlaceholder() || resolvedAt == null
          ? _value.resolvedAt
          // ignore: cast_nullable_to_non_nullable
          : resolvedAt as DateTime,
      resolvedBy:
          resolvedBy == const $CopyWithPlaceholder() || resolvedBy == null
          ? _value.resolvedBy
          // ignore: cast_nullable_to_non_nullable
          : resolvedBy as String,
      early: early == const $CopyWithPlaceholder()
          ? _value.early
          // ignore: cast_nullable_to_non_nullable
          : early as bool?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      evidence: evidence == const $CopyWithPlaceholder() || evidence == null
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<String>,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $ForecastResolutionCopyWith on ForecastResolution {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastResolution.copyWith(...)` or `instanceOfForecastResolution.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastResolutionCWProxy get copyWith =>
      _$ForecastResolutionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastResolution _$ForecastResolutionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecastResolution', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['resolvedAt', 'resolvedBy', 'evidence'],
      );
      final val = ForecastResolution(
        outcome: $checkedConvert('outcome', (v) => v as String?),
        value: $checkedConvert('value', (v) => v as num?),
        resolvedAt: $checkedConvert(
          'resolvedAt',
          (v) => DateTime.parse(v as String),
        ),
        resolvedBy: $checkedConvert('resolvedBy', (v) => v as String),
        early: $checkedConvert('early', (v) => v as bool?),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        evidence: $checkedConvert(
          'evidence',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        note: $checkedConvert('note', (v) => v as String?),
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ForecastResolutionToJson(ForecastResolution instance) =>
    <String, dynamic>{
      'outcome': ?instance.outcome,
      'value': ?instance.value,
      'resolvedAt': instance.resolvedAt.toIso8601String(),
      'resolvedBy': instance.resolvedBy,
      'early': ?instance.early,
      'asOf': ?instance.asOf?.toIso8601String(),
      'evidence': instance.evidence,
      'note': ?instance.note,
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
      'manifestId': ?instance.manifestId,
    };
