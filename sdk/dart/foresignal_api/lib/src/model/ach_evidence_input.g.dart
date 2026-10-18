// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ach_evidence_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AchEvidenceInputCWProxy {
  AchEvidenceInput source_(AchEvidenceInputSource_Enum source_);

  AchEvidenceInput ref(String? ref);

  AchEvidenceInput description(String description);

  AchEvidenceInput credibility(AchEvidenceInputCredibilityEnum credibility);

  AchEvidenceInput relevance(AchEvidenceInputRelevanceEnum relevance);

  AchEvidenceInput ratings(Map<String, AchEvidenceInputRatingsEnum>? ratings);

  AchEvidenceInput citations(List<String>? citations);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AchEvidenceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AchEvidenceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AchEvidenceInput call({
    AchEvidenceInputSource_Enum source_,
    String? ref,
    String description,
    AchEvidenceInputCredibilityEnum credibility,
    AchEvidenceInputRelevanceEnum relevance,
    Map<String, AchEvidenceInputRatingsEnum>? ratings,
    List<String>? citations,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAchEvidenceInput.copyWith(...)` or call `instanceOfAchEvidenceInput.copyWith.fieldName(value)` for a single field.
class _$AchEvidenceInputCWProxyImpl implements _$AchEvidenceInputCWProxy {
  const _$AchEvidenceInputCWProxyImpl(this._value);

  final AchEvidenceInput _value;

  @override
  AchEvidenceInput source_(AchEvidenceInputSource_Enum source_) =>
      call(source_: source_);

  @override
  AchEvidenceInput ref(String? ref) => call(ref: ref);

  @override
  AchEvidenceInput description(String description) =>
      call(description: description);

  @override
  AchEvidenceInput credibility(AchEvidenceInputCredibilityEnum credibility) =>
      call(credibility: credibility);

  @override
  AchEvidenceInput relevance(AchEvidenceInputRelevanceEnum relevance) =>
      call(relevance: relevance);

  @override
  AchEvidenceInput ratings(Map<String, AchEvidenceInputRatingsEnum>? ratings) =>
      call(ratings: ratings);

  @override
  AchEvidenceInput citations(List<String>? citations) =>
      call(citations: citations);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AchEvidenceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AchEvidenceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AchEvidenceInput call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? credibility = const $CopyWithPlaceholder(),
    Object? relevance = const $CopyWithPlaceholder(),
    Object? ratings = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
  }) {
    return AchEvidenceInput(
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as AchEvidenceInputSource_Enum,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      description:
          description == const $CopyWithPlaceholder() || description == null
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String,
      credibility:
          credibility == const $CopyWithPlaceholder() || credibility == null
          ? _value.credibility
          // ignore: cast_nullable_to_non_nullable
          : credibility as AchEvidenceInputCredibilityEnum,
      relevance: relevance == const $CopyWithPlaceholder() || relevance == null
          ? _value.relevance
          // ignore: cast_nullable_to_non_nullable
          : relevance as AchEvidenceInputRelevanceEnum,
      ratings: ratings == const $CopyWithPlaceholder()
          ? _value.ratings
          // ignore: cast_nullable_to_non_nullable
          : ratings as Map<String, AchEvidenceInputRatingsEnum>?,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<String>?,
    );
  }
}

extension $AchEvidenceInputCopyWith on AchEvidenceInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAchEvidenceInput.copyWith(...)` or `instanceOfAchEvidenceInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AchEvidenceInputCWProxy get copyWith => _$AchEvidenceInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AchEvidenceInput _$AchEvidenceInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AchEvidenceInput', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['source', 'description', 'credibility', 'relevance'],
  );
  final val = AchEvidenceInput(
    source_: $checkedConvert(
      'source',
      (v) => $enumDecode(_$AchEvidenceInputSource_EnumEnumMap, v),
    ),
    ref: $checkedConvert('ref', (v) => v as String?),
    description: $checkedConvert('description', (v) => v as String),
    credibility: $checkedConvert(
      'credibility',
      (v) => $enumDecode(_$AchEvidenceInputCredibilityEnumEnumMap, v),
    ),
    relevance: $checkedConvert(
      'relevance',
      (v) => $enumDecode(_$AchEvidenceInputRelevanceEnumEnumMap, v),
    ),
    ratings: $checkedConvert(
      'ratings',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) =>
            MapEntry(k, $enumDecode(_$AchEvidenceInputRatingsEnumEnumMap, e)),
      ),
    ),
    citations: $checkedConvert(
      'citations',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$AchEvidenceInputToJson(AchEvidenceInput instance) =>
    <String, dynamic>{
      'source': _$AchEvidenceInputSource_EnumEnumMap[instance.source_]!,
      'ref': ?instance.ref,
      'description': instance.description,
      'credibility':
          _$AchEvidenceInputCredibilityEnumEnumMap[instance.credibility]!,
      'relevance': _$AchEvidenceInputRelevanceEnumEnumMap[instance.relevance]!,
      'ratings': ?instance.ratings?.map(
        (k, e) => MapEntry(k, _$AchEvidenceInputRatingsEnumEnumMap[e]!),
      ),
      'citations': ?instance.citations,
    };

const _$AchEvidenceInputSource_EnumEnumMap = {
  AchEvidenceInputSource_Enum.record: 'record',
  AchEvidenceInputSource_Enum.finding: 'finding',
  AchEvidenceInputSource_Enum.intelEvent: 'intel_event',
  AchEvidenceInputSource_Enum.alert: 'alert',
  AchEvidenceInputSource_Enum.manual: 'manual',
  AchEvidenceInputSource_Enum.evidenceItem: 'evidence-item',
  AchEvidenceInputSource_Enum.socialClaim: 'social-claim',
};

const _$AchEvidenceInputCredibilityEnumEnumMap = {
  AchEvidenceInputCredibilityEnum.H: 'H',
  AchEvidenceInputCredibilityEnum.M: 'M',
  AchEvidenceInputCredibilityEnum.L: 'L',
};

const _$AchEvidenceInputRelevanceEnumEnumMap = {
  AchEvidenceInputRelevanceEnum.H: 'H',
  AchEvidenceInputRelevanceEnum.M: 'M',
  AchEvidenceInputRelevanceEnum.L: 'L',
};

const _$AchEvidenceInputRatingsEnumEnumMap = {
  AchEvidenceInputRatingsEnum.CC: 'CC',
  AchEvidenceInputRatingsEnum.C: 'C',
  AchEvidenceInputRatingsEnum.N: 'N',
  AchEvidenceInputRatingsEnum.NA: 'NA',
  AchEvidenceInputRatingsEnum.I: 'I',
  AchEvidenceInputRatingsEnum.II: 'II',
};
