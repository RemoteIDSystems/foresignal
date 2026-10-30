// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_citation_corroboration.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskCitationCorroborationCWProxy {
  AskCitationCorroboration evidenceQuality(
    AskCitationCorroborationEvidenceQualityEnum evidenceQuality,
  );

  AskCitationCorroboration independentSources(int independentSources);

  AskCitationCorroboration credibility(int? credibility);

  AskCitationCorroboration reliability(
    AskCitationCorroborationReliabilityEnum? reliability,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskCitationCorroboration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskCitationCorroboration(...).copyWith(id: 12, name: "My name")
  /// ```
  AskCitationCorroboration call({
    AskCitationCorroborationEvidenceQualityEnum evidenceQuality,
    int independentSources,
    int? credibility,
    AskCitationCorroborationReliabilityEnum? reliability,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskCitationCorroboration.copyWith(...)` or call `instanceOfAskCitationCorroboration.copyWith.fieldName(value)` for a single field.
class _$AskCitationCorroborationCWProxyImpl
    implements _$AskCitationCorroborationCWProxy {
  const _$AskCitationCorroborationCWProxyImpl(this._value);

  final AskCitationCorroboration _value;

  @override
  AskCitationCorroboration evidenceQuality(
    AskCitationCorroborationEvidenceQualityEnum evidenceQuality,
  ) => call(evidenceQuality: evidenceQuality);

  @override
  AskCitationCorroboration independentSources(int independentSources) =>
      call(independentSources: independentSources);

  @override
  AskCitationCorroboration credibility(int? credibility) =>
      call(credibility: credibility);

  @override
  AskCitationCorroboration reliability(
    AskCitationCorroborationReliabilityEnum? reliability,
  ) => call(reliability: reliability);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskCitationCorroboration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskCitationCorroboration(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskCitationCorroboration call({
    Object? evidenceQuality = const $CopyWithPlaceholder(),
    Object? independentSources = const $CopyWithPlaceholder(),
    Object? credibility = const $CopyWithPlaceholder(),
    Object? reliability = const $CopyWithPlaceholder(),
  }) {
    return AskCitationCorroboration(
      evidenceQuality:
          evidenceQuality == const $CopyWithPlaceholder() ||
              evidenceQuality == null
          ? _value.evidenceQuality
          // ignore: cast_nullable_to_non_nullable
          : evidenceQuality as AskCitationCorroborationEvidenceQualityEnum,
      independentSources:
          independentSources == const $CopyWithPlaceholder() ||
              independentSources == null
          ? _value.independentSources
          // ignore: cast_nullable_to_non_nullable
          : independentSources as int,
      credibility: credibility == const $CopyWithPlaceholder()
          ? _value.credibility
          // ignore: cast_nullable_to_non_nullable
          : credibility as int?,
      reliability: reliability == const $CopyWithPlaceholder()
          ? _value.reliability
          // ignore: cast_nullable_to_non_nullable
          : reliability as AskCitationCorroborationReliabilityEnum?,
    );
  }
}

extension $AskCitationCorroborationCopyWith on AskCitationCorroboration {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskCitationCorroboration.copyWith(...)` or `instanceOfAskCitationCorroboration.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskCitationCorroborationCWProxy get copyWith =>
      _$AskCitationCorroborationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskCitationCorroboration _$AskCitationCorroborationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AskCitationCorroboration', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['evidenceQuality', 'independentSources'],
  );
  final val = AskCitationCorroboration(
    evidenceQuality: $checkedConvert(
      'evidenceQuality',
      (v) =>
          $enumDecode(_$AskCitationCorroborationEvidenceQualityEnumEnumMap, v),
    ),
    independentSources: $checkedConvert(
      'independentSources',
      (v) => (v as num).toInt(),
    ),
    credibility: $checkedConvert('credibility', (v) => (v as num?)?.toInt()),
    reliability: $checkedConvert(
      'reliability',
      (v) => $enumDecodeNullable(
        _$AskCitationCorroborationReliabilityEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$AskCitationCorroborationToJson(
  AskCitationCorroboration instance,
) => <String, dynamic>{
  'evidenceQuality':
      _$AskCitationCorroborationEvidenceQualityEnumEnumMap[instance
          .evidenceQuality]!,
  'independentSources': instance.independentSources,
  'credibility': ?instance.credibility,
  'reliability':
      ?_$AskCitationCorroborationReliabilityEnumEnumMap[instance.reliability],
};

const _$AskCitationCorroborationEvidenceQualityEnumEnumMap = {
  AskCitationCorroborationEvidenceQualityEnum.strong: 'strong',
  AskCitationCorroborationEvidenceQualityEnum.moderate: 'moderate',
  AskCitationCorroborationEvidenceQualityEnum.weak: 'weak',
  AskCitationCorroborationEvidenceQualityEnum.insufficient: 'insufficient',
};

const _$AskCitationCorroborationReliabilityEnumEnumMap = {
  AskCitationCorroborationReliabilityEnum.A: 'A',
  AskCitationCorroborationReliabilityEnum.B: 'B',
  AskCitationCorroborationReliabilityEnum.C: 'C',
  AskCitationCorroborationReliabilityEnum.D: 'D',
  AskCitationCorroborationReliabilityEnum.E: 'E',
  AskCitationCorroborationReliabilityEnum.F: 'F',
};
