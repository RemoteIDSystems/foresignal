// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_join_reason_components.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationJoinReasonComponentsCWProxy {
  SituationJoinReasonComponents region(num? region);

  SituationJoinReasonComponents distanceKm(num? distanceKm);

  SituationJoinReasonComponents entity(num? entity);

  SituationJoinReasonComponents topic(num? topic);

  SituationJoinReasonComponents textSimilarity(num? textSimilarity);

  SituationJoinReasonComponents theme(num? theme);

  SituationJoinReasonComponents recencyHours(num? recencyHours);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationJoinReasonComponents(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationJoinReasonComponents(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationJoinReasonComponents call({
    num? region,
    num? distanceKm,
    num? entity,
    num? topic,
    num? textSimilarity,
    num? theme,
    num? recencyHours,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationJoinReasonComponents.copyWith(...)` or call `instanceOfSituationJoinReasonComponents.copyWith.fieldName(value)` for a single field.
class _$SituationJoinReasonComponentsCWProxyImpl
    implements _$SituationJoinReasonComponentsCWProxy {
  const _$SituationJoinReasonComponentsCWProxyImpl(this._value);

  final SituationJoinReasonComponents _value;

  @override
  SituationJoinReasonComponents region(num? region) => call(region: region);

  @override
  SituationJoinReasonComponents distanceKm(num? distanceKm) =>
      call(distanceKm: distanceKm);

  @override
  SituationJoinReasonComponents entity(num? entity) => call(entity: entity);

  @override
  SituationJoinReasonComponents topic(num? topic) => call(topic: topic);

  @override
  SituationJoinReasonComponents textSimilarity(num? textSimilarity) =>
      call(textSimilarity: textSimilarity);

  @override
  SituationJoinReasonComponents theme(num? theme) => call(theme: theme);

  @override
  SituationJoinReasonComponents recencyHours(num? recencyHours) =>
      call(recencyHours: recencyHours);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationJoinReasonComponents(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationJoinReasonComponents(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationJoinReasonComponents call({
    Object? region = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? entity = const $CopyWithPlaceholder(),
    Object? topic = const $CopyWithPlaceholder(),
    Object? textSimilarity = const $CopyWithPlaceholder(),
    Object? theme = const $CopyWithPlaceholder(),
    Object? recencyHours = const $CopyWithPlaceholder(),
  }) {
    return SituationJoinReasonComponents(
      region: region == const $CopyWithPlaceholder()
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as num?,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
      entity: entity == const $CopyWithPlaceholder()
          ? _value.entity
          // ignore: cast_nullable_to_non_nullable
          : entity as num?,
      topic: topic == const $CopyWithPlaceholder()
          ? _value.topic
          // ignore: cast_nullable_to_non_nullable
          : topic as num?,
      textSimilarity: textSimilarity == const $CopyWithPlaceholder()
          ? _value.textSimilarity
          // ignore: cast_nullable_to_non_nullable
          : textSimilarity as num?,
      theme: theme == const $CopyWithPlaceholder()
          ? _value.theme
          // ignore: cast_nullable_to_non_nullable
          : theme as num?,
      recencyHours: recencyHours == const $CopyWithPlaceholder()
          ? _value.recencyHours
          // ignore: cast_nullable_to_non_nullable
          : recencyHours as num?,
    );
  }
}

extension $SituationJoinReasonComponentsCopyWith
    on SituationJoinReasonComponents {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationJoinReasonComponents.copyWith(...)` or `instanceOfSituationJoinReasonComponents.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationJoinReasonComponentsCWProxy get copyWith =>
      _$SituationJoinReasonComponentsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationJoinReasonComponents _$SituationJoinReasonComponentsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationJoinReasonComponents', json, ($checkedConvert) {
  final val = SituationJoinReasonComponents(
    region: $checkedConvert('region', (v) => v as num?),
    distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
    entity: $checkedConvert('entity', (v) => v as num?),
    topic: $checkedConvert('topic', (v) => v as num?),
    textSimilarity: $checkedConvert('textSimilarity', (v) => v as num?),
    theme: $checkedConvert('theme', (v) => v as num?),
    recencyHours: $checkedConvert('recencyHours', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$SituationJoinReasonComponentsToJson(
  SituationJoinReasonComponents instance,
) => <String, dynamic>{
  'region': ?instance.region,
  'distanceKm': ?instance.distanceKm,
  'entity': ?instance.entity,
  'topic': ?instance.topic,
  'textSimilarity': ?instance.textSimilarity,
  'theme': ?instance.theme,
  'recencyHours': ?instance.recencyHours,
};
