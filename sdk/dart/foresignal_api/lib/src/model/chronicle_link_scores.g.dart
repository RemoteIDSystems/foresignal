// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_link_scores.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLinkScoresCWProxy {
  ChronicleLinkScores evidence(ChronicleLinkScoresEvidence? evidence);

  ChronicleLinkScores entities(ChronicleLinkScoresEvidence? entities);

  ChronicleLinkScores areaTime(ChronicleLinkScoresEvidence? areaTime);

  ChronicleLinkScores topics(ChronicleLinkScoresEvidence? topics);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLinkScores(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLinkScores(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLinkScores call({
    ChronicleLinkScoresEvidence? evidence,
    ChronicleLinkScoresEvidence? entities,
    ChronicleLinkScoresEvidence? areaTime,
    ChronicleLinkScoresEvidence? topics,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLinkScores.copyWith(...)` or call `instanceOfChronicleLinkScores.copyWith.fieldName(value)` for a single field.
class _$ChronicleLinkScoresCWProxyImpl implements _$ChronicleLinkScoresCWProxy {
  const _$ChronicleLinkScoresCWProxyImpl(this._value);

  final ChronicleLinkScores _value;

  @override
  ChronicleLinkScores evidence(ChronicleLinkScoresEvidence? evidence) =>
      call(evidence: evidence);

  @override
  ChronicleLinkScores entities(ChronicleLinkScoresEvidence? entities) =>
      call(entities: entities);

  @override
  ChronicleLinkScores areaTime(ChronicleLinkScoresEvidence? areaTime) =>
      call(areaTime: areaTime);

  @override
  ChronicleLinkScores topics(ChronicleLinkScoresEvidence? topics) =>
      call(topics: topics);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLinkScores(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLinkScores(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLinkScores call({
    Object? evidence = const $CopyWithPlaceholder(),
    Object? entities = const $CopyWithPlaceholder(),
    Object? areaTime = const $CopyWithPlaceholder(),
    Object? topics = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLinkScores(
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as ChronicleLinkScoresEvidence?,
      entities: entities == const $CopyWithPlaceholder()
          ? _value.entities
          // ignore: cast_nullable_to_non_nullable
          : entities as ChronicleLinkScoresEvidence?,
      areaTime: areaTime == const $CopyWithPlaceholder()
          ? _value.areaTime
          // ignore: cast_nullable_to_non_nullable
          : areaTime as ChronicleLinkScoresEvidence?,
      topics: topics == const $CopyWithPlaceholder()
          ? _value.topics
          // ignore: cast_nullable_to_non_nullable
          : topics as ChronicleLinkScoresEvidence?,
    );
  }
}

extension $ChronicleLinkScoresCopyWith on ChronicleLinkScores {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLinkScores.copyWith(...)` or `instanceOfChronicleLinkScores.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLinkScoresCWProxy get copyWith =>
      _$ChronicleLinkScoresCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLinkScores _$ChronicleLinkScoresFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleLinkScores', json, ($checkedConvert) {
      final val = ChronicleLinkScores(
        evidence: $checkedConvert(
          'evidence',
          (v) => v == null
              ? null
              : ChronicleLinkScoresEvidence.fromJson(v as Map<String, dynamic>),
        ),
        entities: $checkedConvert(
          'entities',
          (v) => v == null
              ? null
              : ChronicleLinkScoresEvidence.fromJson(v as Map<String, dynamic>),
        ),
        areaTime: $checkedConvert(
          'area-time',
          (v) => v == null
              ? null
              : ChronicleLinkScoresEvidence.fromJson(v as Map<String, dynamic>),
        ),
        topics: $checkedConvert(
          'topics',
          (v) => v == null
              ? null
              : ChronicleLinkScoresEvidence.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    }, fieldKeyMap: const {'areaTime': 'area-time'});

Map<String, dynamic> _$ChronicleLinkScoresToJson(
  ChronicleLinkScores instance,
) => <String, dynamic>{
  'evidence': ?instance.evidence?.toJson(),
  'entities': ?instance.entities?.toJson(),
  'area-time': ?instance.areaTime?.toJson(),
  'topics': ?instance.topics?.toJson(),
};
