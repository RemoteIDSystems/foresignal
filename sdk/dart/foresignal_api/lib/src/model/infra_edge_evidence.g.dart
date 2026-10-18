// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_edge_evidence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraEdgeEvidenceCWProxy {
  InfraEdgeEvidence dataset(String? dataset);

  InfraEdgeEvidence distanceKm(num? distanceKm);

  InfraEdgeEvidence matched(Map<String, Object>? matched);

  InfraEdgeEvidence notes(String? notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraEdgeEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraEdgeEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraEdgeEvidence call({
    String? dataset,
    num? distanceKm,
    Map<String, Object>? matched,
    String? notes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraEdgeEvidence.copyWith(...)` or call `instanceOfInfraEdgeEvidence.copyWith.fieldName(value)` for a single field.
class _$InfraEdgeEvidenceCWProxyImpl implements _$InfraEdgeEvidenceCWProxy {
  const _$InfraEdgeEvidenceCWProxyImpl(this._value);

  final InfraEdgeEvidence _value;

  @override
  InfraEdgeEvidence dataset(String? dataset) => call(dataset: dataset);

  @override
  InfraEdgeEvidence distanceKm(num? distanceKm) => call(distanceKm: distanceKm);

  @override
  InfraEdgeEvidence matched(Map<String, Object>? matched) =>
      call(matched: matched);

  @override
  InfraEdgeEvidence notes(String? notes) => call(notes: notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraEdgeEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraEdgeEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraEdgeEvidence call({
    Object? dataset = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? matched = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
  }) {
    return InfraEdgeEvidence(
      dataset: dataset == const $CopyWithPlaceholder()
          ? _value.dataset
          // ignore: cast_nullable_to_non_nullable
          : dataset as String?,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
      matched: matched == const $CopyWithPlaceholder()
          ? _value.matched
          // ignore: cast_nullable_to_non_nullable
          : matched as Map<String, Object>?,
      notes: notes == const $CopyWithPlaceholder()
          ? _value.notes
          // ignore: cast_nullable_to_non_nullable
          : notes as String?,
    );
  }
}

extension $InfraEdgeEvidenceCopyWith on InfraEdgeEvidence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraEdgeEvidence.copyWith(...)` or `instanceOfInfraEdgeEvidence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraEdgeEvidenceCWProxy get copyWith =>
      _$InfraEdgeEvidenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraEdgeEvidence _$InfraEdgeEvidenceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InfraEdgeEvidence', json, ($checkedConvert) {
      final val = InfraEdgeEvidence(
        dataset: $checkedConvert('dataset', (v) => v as String?),
        distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
        matched: $checkedConvert(
          'matched',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        notes: $checkedConvert('notes', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$InfraEdgeEvidenceToJson(InfraEdgeEvidence instance) =>
    <String, dynamic>{
      'dataset': ?instance.dataset,
      'distanceKm': ?instance.distanceKm,
      'matched': ?instance.matched,
      'notes': ?instance.notes,
    };
