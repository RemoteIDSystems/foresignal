// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_link_scores_evidence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLinkScoresEvidenceCWProxy {
  ChronicleLinkScoresEvidence jaccard(num? jaccard);

  ChronicleLinkScoresEvidence containment(num? containment);

  ChronicleLinkScoresEvidence sharedEstimate(num? sharedEstimate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLinkScoresEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLinkScoresEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLinkScoresEvidence call({
    num? jaccard,
    num? containment,
    num? sharedEstimate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLinkScoresEvidence.copyWith(...)` or call `instanceOfChronicleLinkScoresEvidence.copyWith.fieldName(value)` for a single field.
class _$ChronicleLinkScoresEvidenceCWProxyImpl
    implements _$ChronicleLinkScoresEvidenceCWProxy {
  const _$ChronicleLinkScoresEvidenceCWProxyImpl(this._value);

  final ChronicleLinkScoresEvidence _value;

  @override
  ChronicleLinkScoresEvidence jaccard(num? jaccard) => call(jaccard: jaccard);

  @override
  ChronicleLinkScoresEvidence containment(num? containment) =>
      call(containment: containment);

  @override
  ChronicleLinkScoresEvidence sharedEstimate(num? sharedEstimate) =>
      call(sharedEstimate: sharedEstimate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLinkScoresEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLinkScoresEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLinkScoresEvidence call({
    Object? jaccard = const $CopyWithPlaceholder(),
    Object? containment = const $CopyWithPlaceholder(),
    Object? sharedEstimate = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLinkScoresEvidence(
      jaccard: jaccard == const $CopyWithPlaceholder()
          ? _value.jaccard
          // ignore: cast_nullable_to_non_nullable
          : jaccard as num?,
      containment: containment == const $CopyWithPlaceholder()
          ? _value.containment
          // ignore: cast_nullable_to_non_nullable
          : containment as num?,
      sharedEstimate: sharedEstimate == const $CopyWithPlaceholder()
          ? _value.sharedEstimate
          // ignore: cast_nullable_to_non_nullable
          : sharedEstimate as num?,
    );
  }
}

extension $ChronicleLinkScoresEvidenceCopyWith on ChronicleLinkScoresEvidence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLinkScoresEvidence.copyWith(...)` or `instanceOfChronicleLinkScoresEvidence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLinkScoresEvidenceCWProxy get copyWith =>
      _$ChronicleLinkScoresEvidenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLinkScoresEvidence _$ChronicleLinkScoresEvidenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLinkScoresEvidence', json, ($checkedConvert) {
  final val = ChronicleLinkScoresEvidence(
    jaccard: $checkedConvert('jaccard', (v) => v as num?),
    containment: $checkedConvert('containment', (v) => v as num?),
    sharedEstimate: $checkedConvert('sharedEstimate', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLinkScoresEvidenceToJson(
  ChronicleLinkScoresEvidence instance,
) => <String, dynamic>{
  'jaccard': ?instance.jaccard,
  'containment': ?instance.containment,
  'sharedEstimate': ?instance.sharedEstimate,
};
