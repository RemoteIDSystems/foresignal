// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_question_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastQuestionPatchCWProxy {
  ForecastQuestionPatch title(String? title);

  ForecastQuestionPatch tags(List<String>? tags);

  ForecastQuestionPatch closesAt(DateTime? closesAt);

  ForecastQuestionPatch resolutionCriteria(String? resolutionCriteria);

  ForecastQuestionPatch resolver(ForecastResolver? resolver);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestionPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestionPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastQuestionPatch call({
    String? title,
    List<String>? tags,
    DateTime? closesAt,
    String? resolutionCriteria,
    ForecastResolver? resolver,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastQuestionPatch.copyWith(...)` or call `instanceOfForecastQuestionPatch.copyWith.fieldName(value)` for a single field.
class _$ForecastQuestionPatchCWProxyImpl
    implements _$ForecastQuestionPatchCWProxy {
  const _$ForecastQuestionPatchCWProxyImpl(this._value);

  final ForecastQuestionPatch _value;

  @override
  ForecastQuestionPatch title(String? title) => call(title: title);

  @override
  ForecastQuestionPatch tags(List<String>? tags) => call(tags: tags);

  @override
  ForecastQuestionPatch closesAt(DateTime? closesAt) =>
      call(closesAt: closesAt);

  @override
  ForecastQuestionPatch resolutionCriteria(String? resolutionCriteria) =>
      call(resolutionCriteria: resolutionCriteria);

  @override
  ForecastQuestionPatch resolver(ForecastResolver? resolver) =>
      call(resolver: resolver);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestionPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestionPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastQuestionPatch call({
    Object? title = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? closesAt = const $CopyWithPlaceholder(),
    Object? resolutionCriteria = const $CopyWithPlaceholder(),
    Object? resolver = const $CopyWithPlaceholder(),
  }) {
    return ForecastQuestionPatch(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      closesAt: closesAt == const $CopyWithPlaceholder()
          ? _value.closesAt
          // ignore: cast_nullable_to_non_nullable
          : closesAt as DateTime?,
      resolutionCriteria: resolutionCriteria == const $CopyWithPlaceholder()
          ? _value.resolutionCriteria
          // ignore: cast_nullable_to_non_nullable
          : resolutionCriteria as String?,
      resolver: resolver == const $CopyWithPlaceholder()
          ? _value.resolver
          // ignore: cast_nullable_to_non_nullable
          : resolver as ForecastResolver?,
    );
  }
}

extension $ForecastQuestionPatchCopyWith on ForecastQuestionPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastQuestionPatch.copyWith(...)` or `instanceOfForecastQuestionPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastQuestionPatchCWProxy get copyWith =>
      _$ForecastQuestionPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastQuestionPatch _$ForecastQuestionPatchFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastQuestionPatch', json, ($checkedConvert) {
  final val = ForecastQuestionPatch(
    title: $checkedConvert('title', (v) => v as String?),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    closesAt: $checkedConvert(
      'closesAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    resolutionCriteria: $checkedConvert(
      'resolutionCriteria',
      (v) => v as String?,
    ),
    resolver: $checkedConvert(
      'resolver',
      (v) => v == null
          ? null
          : ForecastResolver.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$ForecastQuestionPatchToJson(
  ForecastQuestionPatch instance,
) => <String, dynamic>{
  'title': ?instance.title,
  'tags': ?instance.tags,
  'closesAt': ?instance.closesAt?.toIso8601String(),
  'resolutionCriteria': ?instance.resolutionCriteria,
  'resolver': ?instance.resolver?.toJson(),
};
