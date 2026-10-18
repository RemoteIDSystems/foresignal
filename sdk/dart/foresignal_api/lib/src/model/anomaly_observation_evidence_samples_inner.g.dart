// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'anomaly_observation_evidence_samples_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnomalyObservationEvidenceSamplesInnerCWProxy {
  AnomalyObservationEvidenceSamplesInner source_(String source_);

  AnomalyObservationEvidenceSamplesInner externalId(String externalId);

  AnomalyObservationEvidenceSamplesInner seq(int seq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservationEvidenceSamplesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservationEvidenceSamplesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AnomalyObservationEvidenceSamplesInner call({
    String source_,
    String externalId,
    int seq,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnomalyObservationEvidenceSamplesInner.copyWith(...)` or call `instanceOfAnomalyObservationEvidenceSamplesInner.copyWith.fieldName(value)` for a single field.
class _$AnomalyObservationEvidenceSamplesInnerCWProxyImpl
    implements _$AnomalyObservationEvidenceSamplesInnerCWProxy {
  const _$AnomalyObservationEvidenceSamplesInnerCWProxyImpl(this._value);

  final AnomalyObservationEvidenceSamplesInner _value;

  @override
  AnomalyObservationEvidenceSamplesInner source_(String source_) =>
      call(source_: source_);

  @override
  AnomalyObservationEvidenceSamplesInner externalId(String externalId) =>
      call(externalId: externalId);

  @override
  AnomalyObservationEvidenceSamplesInner seq(int seq) => call(seq: seq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservationEvidenceSamplesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservationEvidenceSamplesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnomalyObservationEvidenceSamplesInner call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
  }) {
    return AnomalyObservationEvidenceSamplesInner(
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
    );
  }
}

extension $AnomalyObservationEvidenceSamplesInnerCopyWith
    on AnomalyObservationEvidenceSamplesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnomalyObservationEvidenceSamplesInner.copyWith(...)` or `instanceOfAnomalyObservationEvidenceSamplesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnomalyObservationEvidenceSamplesInnerCWProxy get copyWith =>
      _$AnomalyObservationEvidenceSamplesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnomalyObservationEvidenceSamplesInner
_$AnomalyObservationEvidenceSamplesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnomalyObservationEvidenceSamplesInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['source', 'externalId', 'seq']);
      final val = AnomalyObservationEvidenceSamplesInner(
        source_: $checkedConvert('source', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        seq: $checkedConvert('seq', (v) => (v as num).toInt()),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$AnomalyObservationEvidenceSamplesInnerToJson(
  AnomalyObservationEvidenceSamplesInner instance,
) => <String, dynamic>{
  'source': instance.source_,
  'externalId': instance.externalId,
  'seq': instance.seq,
};
