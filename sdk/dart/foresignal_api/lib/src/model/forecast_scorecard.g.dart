// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_scorecard.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastScorecardCWProxy {
  ForecastScorecard forecasterId(String? forecasterId);

  ForecastScorecard category(String? category);

  ForecastScorecard from(DateTime? from);

  ForecastScorecard to(DateTime? to);

  ForecastScorecard retrospective(bool retrospective);

  ForecastScorecard entries(List<ScorecardEntry> entries);

  ForecastScorecard scorerVersion(String scorerVersion);

  ForecastScorecard computedAt(DateTime computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastScorecard(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastScorecard(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastScorecard call({
    String? forecasterId,
    String? category,
    DateTime? from,
    DateTime? to,
    bool retrospective,
    List<ScorecardEntry> entries,
    String scorerVersion,
    DateTime computedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastScorecard.copyWith(...)` or call `instanceOfForecastScorecard.copyWith.fieldName(value)` for a single field.
class _$ForecastScorecardCWProxyImpl implements _$ForecastScorecardCWProxy {
  const _$ForecastScorecardCWProxyImpl(this._value);

  final ForecastScorecard _value;

  @override
  ForecastScorecard forecasterId(String? forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  ForecastScorecard category(String? category) => call(category: category);

  @override
  ForecastScorecard from(DateTime? from) => call(from: from);

  @override
  ForecastScorecard to(DateTime? to) => call(to: to);

  @override
  ForecastScorecard retrospective(bool retrospective) =>
      call(retrospective: retrospective);

  @override
  ForecastScorecard entries(List<ScorecardEntry> entries) =>
      call(entries: entries);

  @override
  ForecastScorecard scorerVersion(String scorerVersion) =>
      call(scorerVersion: scorerVersion);

  @override
  ForecastScorecard computedAt(DateTime computedAt) =>
      call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastScorecard(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastScorecard(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastScorecard call({
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? retrospective = const $CopyWithPlaceholder(),
    Object? entries = const $CopyWithPlaceholder(),
    Object? scorerVersion = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return ForecastScorecard(
      forecasterId: forecasterId == const $CopyWithPlaceholder()
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String?,
      category: category == const $CopyWithPlaceholder()
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      retrospective:
          retrospective == const $CopyWithPlaceholder() || retrospective == null
          ? _value.retrospective
          // ignore: cast_nullable_to_non_nullable
          : retrospective as bool,
      entries: entries == const $CopyWithPlaceholder() || entries == null
          ? _value.entries
          // ignore: cast_nullable_to_non_nullable
          : entries as List<ScorecardEntry>,
      scorerVersion:
          scorerVersion == const $CopyWithPlaceholder() || scorerVersion == null
          ? _value.scorerVersion
          // ignore: cast_nullable_to_non_nullable
          : scorerVersion as String,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
    );
  }
}

extension $ForecastScorecardCopyWith on ForecastScorecard {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastScorecard.copyWith(...)` or `instanceOfForecastScorecard.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastScorecardCWProxy get copyWith =>
      _$ForecastScorecardCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastScorecard _$ForecastScorecardFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecastScorecard', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'retrospective',
          'entries',
          'scorerVersion',
          'computedAt',
        ],
      );
      final val = ForecastScorecard(
        forecasterId: $checkedConvert('forecasterId', (v) => v as String?),
        category: $checkedConvert('category', (v) => v as String?),
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        retrospective: $checkedConvert('retrospective', (v) => v as bool),
        entries: $checkedConvert(
          'entries',
          (v) => (v as List<dynamic>)
              .map((e) => ScorecardEntry.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        scorerVersion: $checkedConvert('scorerVersion', (v) => v as String),
        computedAt: $checkedConvert(
          'computedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ForecastScorecardToJson(ForecastScorecard instance) =>
    <String, dynamic>{
      'forecasterId': ?instance.forecasterId,
      'category': ?instance.category,
      'from': ?instance.from?.toIso8601String(),
      'to': ?instance.to?.toIso8601String(),
      'retrospective': instance.retrospective,
      'entries': instance.entries.map((e) => e.toJson()).toList(),
      'scorerVersion': instance.scorerVersion,
      'computedAt': instance.computedAt.toIso8601String(),
    };
