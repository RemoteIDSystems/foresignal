// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analog.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalogCWProxy {
  Analog situationId(String? situationId);

  Analog title(String title);

  Analog theme(SituationTheme? theme);

  Analog regionCodes(List<String> regionCodes);

  Analog period(TimeRange period);

  Analog rank(int rank);

  Analog similarity(num similarity);

  Analog featureSimilarity(num? featureSimilarity);

  Analog embeddingSimilarity(num? embeddingSimilarity);

  Analog outcomes(AnalogOutcomes outcomes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Analog(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Analog(...).copyWith(id: 12, name: "My name")
  /// ```
  Analog call({
    String? situationId,
    String title,
    SituationTheme? theme,
    List<String> regionCodes,
    TimeRange period,
    int rank,
    num similarity,
    num? featureSimilarity,
    num? embeddingSimilarity,
    AnalogOutcomes outcomes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalog.copyWith(...)` or call `instanceOfAnalog.copyWith.fieldName(value)` for a single field.
class _$AnalogCWProxyImpl implements _$AnalogCWProxy {
  const _$AnalogCWProxyImpl(this._value);

  final Analog _value;

  @override
  Analog situationId(String? situationId) => call(situationId: situationId);

  @override
  Analog title(String title) => call(title: title);

  @override
  Analog theme(SituationTheme? theme) => call(theme: theme);

  @override
  Analog regionCodes(List<String> regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  Analog period(TimeRange period) => call(period: period);

  @override
  Analog rank(int rank) => call(rank: rank);

  @override
  Analog similarity(num similarity) => call(similarity: similarity);

  @override
  Analog featureSimilarity(num? featureSimilarity) =>
      call(featureSimilarity: featureSimilarity);

  @override
  Analog embeddingSimilarity(num? embeddingSimilarity) =>
      call(embeddingSimilarity: embeddingSimilarity);

  @override
  Analog outcomes(AnalogOutcomes outcomes) => call(outcomes: outcomes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Analog(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Analog(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Analog call({
    Object? situationId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? theme = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? period = const $CopyWithPlaceholder(),
    Object? rank = const $CopyWithPlaceholder(),
    Object? similarity = const $CopyWithPlaceholder(),
    Object? featureSimilarity = const $CopyWithPlaceholder(),
    Object? embeddingSimilarity = const $CopyWithPlaceholder(),
    Object? outcomes = const $CopyWithPlaceholder(),
  }) {
    return Analog(
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      theme: theme == const $CopyWithPlaceholder()
          ? _value.theme
          // ignore: cast_nullable_to_non_nullable
          : theme as SituationTheme?,
      regionCodes:
          regionCodes == const $CopyWithPlaceholder() || regionCodes == null
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>,
      period: period == const $CopyWithPlaceholder() || period == null
          ? _value.period
          // ignore: cast_nullable_to_non_nullable
          : period as TimeRange,
      rank: rank == const $CopyWithPlaceholder() || rank == null
          ? _value.rank
          // ignore: cast_nullable_to_non_nullable
          : rank as int,
      similarity:
          similarity == const $CopyWithPlaceholder() || similarity == null
          ? _value.similarity
          // ignore: cast_nullable_to_non_nullable
          : similarity as num,
      featureSimilarity: featureSimilarity == const $CopyWithPlaceholder()
          ? _value.featureSimilarity
          // ignore: cast_nullable_to_non_nullable
          : featureSimilarity as num?,
      embeddingSimilarity: embeddingSimilarity == const $CopyWithPlaceholder()
          ? _value.embeddingSimilarity
          // ignore: cast_nullable_to_non_nullable
          : embeddingSimilarity as num?,
      outcomes: outcomes == const $CopyWithPlaceholder() || outcomes == null
          ? _value.outcomes
          // ignore: cast_nullable_to_non_nullable
          : outcomes as AnalogOutcomes,
    );
  }
}

extension $AnalogCopyWith on Analog {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalog.copyWith(...)` or `instanceOfAnalog.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalogCWProxy get copyWith => _$AnalogCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Analog _$AnalogFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Analog',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const [
        'title',
        'regionCodes',
        'period',
        'rank',
        'similarity',
        'outcomes',
      ],
    );
    final val = Analog(
      situationId: $checkedConvert('situationId', (v) => v as String?),
      title: $checkedConvert('title', (v) => v as String),
      theme: $checkedConvert(
        'theme',
        (v) => $enumDecodeNullable(_$SituationThemeEnumMap, v),
      ),
      regionCodes: $checkedConvert(
        'regionCodes',
        (v) => (v as List<dynamic>).map((e) => e as String).toList(),
      ),
      period: $checkedConvert(
        'period',
        (v) => TimeRange.fromJson(v as Map<String, dynamic>),
      ),
      rank: $checkedConvert('rank', (v) => (v as num).toInt()),
      similarity: $checkedConvert('similarity', (v) => v as num),
      featureSimilarity: $checkedConvert('featureSimilarity', (v) => v as num?),
      embeddingSimilarity: $checkedConvert(
        'embeddingSimilarity',
        (v) => v as num?,
      ),
      outcomes: $checkedConvert(
        'outcomes',
        (v) => AnalogOutcomes.fromJson(v as Map<String, dynamic>),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$AnalogToJson(Analog instance) => <String, dynamic>{
  'situationId': ?instance.situationId,
  'title': instance.title,
  'theme': ?_$SituationThemeEnumMap[instance.theme],
  'regionCodes': instance.regionCodes,
  'period': instance.period.toJson(),
  'rank': instance.rank,
  'similarity': instance.similarity,
  'featureSimilarity': ?instance.featureSimilarity,
  'embeddingSimilarity': ?instance.embeddingSimilarity,
  'outcomes': instance.outcomes.toJson(),
};

const _$SituationThemeEnumMap = {
  SituationTheme.maritime: 'maritime',
  SituationTheme.aviation: 'aviation',
  SituationTheme.conflict: 'conflict',
  SituationTheme.infrastructure: 'infrastructure',
  SituationTheme.weather: 'weather',
  SituationTheme.cyber: 'cyber',
  SituationTheme.markets: 'markets',
  SituationTheme.health: 'health',
  SituationTheme.space: 'space',
  SituationTheme.mixed: 'mixed',
};
