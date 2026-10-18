// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'anomaly_observation_evidence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnomalyObservationEvidenceCWProxy {
  AnomalyObservationEvidence samples(
    List<AnomalyObservationEvidenceSamplesInner>? samples,
  );

  AnomalyObservationEvidence fetchLogRanges(
    List<AnomalyObservationEvidenceFetchLogRangesInner>? fetchLogRanges,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservationEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservationEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  AnomalyObservationEvidence call({
    List<AnomalyObservationEvidenceSamplesInner>? samples,
    List<AnomalyObservationEvidenceFetchLogRangesInner>? fetchLogRanges,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnomalyObservationEvidence.copyWith(...)` or call `instanceOfAnomalyObservationEvidence.copyWith.fieldName(value)` for a single field.
class _$AnomalyObservationEvidenceCWProxyImpl
    implements _$AnomalyObservationEvidenceCWProxy {
  const _$AnomalyObservationEvidenceCWProxyImpl(this._value);

  final AnomalyObservationEvidence _value;

  @override
  AnomalyObservationEvidence samples(
    List<AnomalyObservationEvidenceSamplesInner>? samples,
  ) => call(samples: samples);

  @override
  AnomalyObservationEvidence fetchLogRanges(
    List<AnomalyObservationEvidenceFetchLogRangesInner>? fetchLogRanges,
  ) => call(fetchLogRanges: fetchLogRanges);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservationEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservationEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnomalyObservationEvidence call({
    Object? samples = const $CopyWithPlaceholder(),
    Object? fetchLogRanges = const $CopyWithPlaceholder(),
  }) {
    return AnomalyObservationEvidence(
      samples: samples == const $CopyWithPlaceholder()
          ? _value.samples
          // ignore: cast_nullable_to_non_nullable
          : samples as List<AnomalyObservationEvidenceSamplesInner>?,
      fetchLogRanges: fetchLogRanges == const $CopyWithPlaceholder()
          ? _value.fetchLogRanges
          // ignore: cast_nullable_to_non_nullable
          : fetchLogRanges
                as List<AnomalyObservationEvidenceFetchLogRangesInner>?,
    );
  }
}

extension $AnomalyObservationEvidenceCopyWith on AnomalyObservationEvidence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnomalyObservationEvidence.copyWith(...)` or `instanceOfAnomalyObservationEvidence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnomalyObservationEvidenceCWProxy get copyWith =>
      _$AnomalyObservationEvidenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnomalyObservationEvidence _$AnomalyObservationEvidenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnomalyObservationEvidence', json, ($checkedConvert) {
  final val = AnomalyObservationEvidence(
    samples: $checkedConvert(
      'samples',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => AnomalyObservationEvidenceSamplesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    fetchLogRanges: $checkedConvert(
      'fetchLogRanges',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => AnomalyObservationEvidenceFetchLogRangesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$AnomalyObservationEvidenceToJson(
  AnomalyObservationEvidence instance,
) => <String, dynamic>{
  'samples': ?instance.samples?.map((e) => e.toJson()).toList(),
  'fetchLogRanges': ?instance.fetchLogRanges?.map((e) => e.toJson()).toList(),
};
