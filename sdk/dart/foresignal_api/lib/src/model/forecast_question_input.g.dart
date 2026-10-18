// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_question_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastQuestionInputCWProxy {
  ForecastQuestionInput title(String title);

  ForecastQuestionInput type(ForecastQuestionInputTypeEnum type);

  ForecastQuestionInput outcomes(QuestionOutcomes? outcomes);

  ForecastQuestionInput resolutionCriteria(String resolutionCriteria);

  ForecastQuestionInput resolver(ForecastResolver resolver);

  ForecastQuestionInput opensAt(DateTime? opensAt);

  ForecastQuestionInput closesAt(DateTime closesAt);

  ForecastQuestionInput resolveBy(DateTime resolveBy);

  ForecastQuestionInput category(String category);

  ForecastQuestionInput tags(List<String>? tags);

  ForecastQuestionInput links(QuestionLinks? links);

  ForecastQuestionInput dedupKey(String? dedupKey);

  ForecastQuestionInput private(bool? private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestionInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestionInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastQuestionInput call({
    String title,
    ForecastQuestionInputTypeEnum type,
    QuestionOutcomes? outcomes,
    String resolutionCriteria,
    ForecastResolver resolver,
    DateTime? opensAt,
    DateTime closesAt,
    DateTime resolveBy,
    String category,
    List<String>? tags,
    QuestionLinks? links,
    String? dedupKey,
    bool? private,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastQuestionInput.copyWith(...)` or call `instanceOfForecastQuestionInput.copyWith.fieldName(value)` for a single field.
class _$ForecastQuestionInputCWProxyImpl
    implements _$ForecastQuestionInputCWProxy {
  const _$ForecastQuestionInputCWProxyImpl(this._value);

  final ForecastQuestionInput _value;

  @override
  ForecastQuestionInput title(String title) => call(title: title);

  @override
  ForecastQuestionInput type(ForecastQuestionInputTypeEnum type) =>
      call(type: type);

  @override
  ForecastQuestionInput outcomes(QuestionOutcomes? outcomes) =>
      call(outcomes: outcomes);

  @override
  ForecastQuestionInput resolutionCriteria(String resolutionCriteria) =>
      call(resolutionCriteria: resolutionCriteria);

  @override
  ForecastQuestionInput resolver(ForecastResolver resolver) =>
      call(resolver: resolver);

  @override
  ForecastQuestionInput opensAt(DateTime? opensAt) => call(opensAt: opensAt);

  @override
  ForecastQuestionInput closesAt(DateTime closesAt) => call(closesAt: closesAt);

  @override
  ForecastQuestionInput resolveBy(DateTime resolveBy) =>
      call(resolveBy: resolveBy);

  @override
  ForecastQuestionInput category(String category) => call(category: category);

  @override
  ForecastQuestionInput tags(List<String>? tags) => call(tags: tags);

  @override
  ForecastQuestionInput links(QuestionLinks? links) => call(links: links);

  @override
  ForecastQuestionInput dedupKey(String? dedupKey) => call(dedupKey: dedupKey);

  @override
  ForecastQuestionInput private(bool? private) => call(private: private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestionInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestionInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastQuestionInput call({
    Object? title = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? outcomes = const $CopyWithPlaceholder(),
    Object? resolutionCriteria = const $CopyWithPlaceholder(),
    Object? resolver = const $CopyWithPlaceholder(),
    Object? opensAt = const $CopyWithPlaceholder(),
    Object? closesAt = const $CopyWithPlaceholder(),
    Object? resolveBy = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? dedupKey = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
  }) {
    return ForecastQuestionInput(
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ForecastQuestionInputTypeEnum,
      outcomes: outcomes == const $CopyWithPlaceholder()
          ? _value.outcomes
          // ignore: cast_nullable_to_non_nullable
          : outcomes as QuestionOutcomes?,
      resolutionCriteria:
          resolutionCriteria == const $CopyWithPlaceholder() ||
              resolutionCriteria == null
          ? _value.resolutionCriteria
          // ignore: cast_nullable_to_non_nullable
          : resolutionCriteria as String,
      resolver: resolver == const $CopyWithPlaceholder() || resolver == null
          ? _value.resolver
          // ignore: cast_nullable_to_non_nullable
          : resolver as ForecastResolver,
      opensAt: opensAt == const $CopyWithPlaceholder()
          ? _value.opensAt
          // ignore: cast_nullable_to_non_nullable
          : opensAt as DateTime?,
      closesAt: closesAt == const $CopyWithPlaceholder() || closesAt == null
          ? _value.closesAt
          // ignore: cast_nullable_to_non_nullable
          : closesAt as DateTime,
      resolveBy: resolveBy == const $CopyWithPlaceholder() || resolveBy == null
          ? _value.resolveBy
          // ignore: cast_nullable_to_non_nullable
          : resolveBy as DateTime,
      category: category == const $CopyWithPlaceholder() || category == null
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as QuestionLinks?,
      dedupKey: dedupKey == const $CopyWithPlaceholder()
          ? _value.dedupKey
          // ignore: cast_nullable_to_non_nullable
          : dedupKey as String?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
    );
  }
}

extension $ForecastQuestionInputCopyWith on ForecastQuestionInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastQuestionInput.copyWith(...)` or `instanceOfForecastQuestionInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastQuestionInputCWProxy get copyWith =>
      _$ForecastQuestionInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastQuestionInput _$ForecastQuestionInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastQuestionInput', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'title',
      'type',
      'resolutionCriteria',
      'resolver',
      'closesAt',
      'resolveBy',
      'category',
    ],
  );
  final val = ForecastQuestionInput(
    title: $checkedConvert('title', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ForecastQuestionInputTypeEnumEnumMap, v),
    ),
    outcomes: $checkedConvert(
      'outcomes',
      (v) => v == null
          ? null
          : QuestionOutcomes.fromJson(v as Map<String, dynamic>),
    ),
    resolutionCriteria: $checkedConvert(
      'resolutionCriteria',
      (v) => v as String,
    ),
    resolver: $checkedConvert(
      'resolver',
      (v) => ForecastResolver.fromJson(v as Map<String, dynamic>),
    ),
    opensAt: $checkedConvert(
      'opensAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    closesAt: $checkedConvert('closesAt', (v) => DateTime.parse(v as String)),
    resolveBy: $checkedConvert('resolveBy', (v) => DateTime.parse(v as String)),
    category: $checkedConvert('category', (v) => v as String),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    links: $checkedConvert(
      'links',
      (v) =>
          v == null ? null : QuestionLinks.fromJson(v as Map<String, dynamic>),
    ),
    dedupKey: $checkedConvert('dedupKey', (v) => v as String?),
    private: $checkedConvert('private', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$ForecastQuestionInputToJson(
  ForecastQuestionInput instance,
) => <String, dynamic>{
  'title': instance.title,
  'type': _$ForecastQuestionInputTypeEnumEnumMap[instance.type]!,
  'outcomes': ?instance.outcomes?.toJson(),
  'resolutionCriteria': instance.resolutionCriteria,
  'resolver': instance.resolver.toJson(),
  'opensAt': ?instance.opensAt?.toIso8601String(),
  'closesAt': instance.closesAt.toIso8601String(),
  'resolveBy': instance.resolveBy.toIso8601String(),
  'category': instance.category,
  'tags': ?instance.tags,
  'links': ?instance.links?.toJson(),
  'dedupKey': ?instance.dedupKey,
  'private': ?instance.private,
};

const _$ForecastQuestionInputTypeEnumEnumMap = {
  ForecastQuestionInputTypeEnum.binary: 'binary',
  ForecastQuestionInputTypeEnum.multiple: 'multiple',
  ForecastQuestionInputTypeEnum.ordered: 'ordered',
  ForecastQuestionInputTypeEnum.numeric: 'numeric',
};
