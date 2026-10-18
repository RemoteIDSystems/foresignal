// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analytic_finding_evidence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalyticFindingEvidenceCWProxy {
  AnalyticFindingEvidence seriesRefs(List<String> seriesRefs);

  AnalyticFindingEvidence members(List<String> members);

  AnalyticFindingEvidence valuesWithheld(bool? valuesWithheld);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalyticFindingEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalyticFindingEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalyticFindingEvidence call({
    List<String> seriesRefs,
    List<String> members,
    bool? valuesWithheld,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalyticFindingEvidence.copyWith(...)` or call `instanceOfAnalyticFindingEvidence.copyWith.fieldName(value)` for a single field.
class _$AnalyticFindingEvidenceCWProxyImpl
    implements _$AnalyticFindingEvidenceCWProxy {
  const _$AnalyticFindingEvidenceCWProxyImpl(this._value);

  final AnalyticFindingEvidence _value;

  @override
  AnalyticFindingEvidence seriesRefs(List<String> seriesRefs) =>
      call(seriesRefs: seriesRefs);

  @override
  AnalyticFindingEvidence members(List<String> members) =>
      call(members: members);

  @override
  AnalyticFindingEvidence valuesWithheld(bool? valuesWithheld) =>
      call(valuesWithheld: valuesWithheld);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalyticFindingEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalyticFindingEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalyticFindingEvidence call({
    Object? seriesRefs = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
    Object? valuesWithheld = const $CopyWithPlaceholder(),
  }) {
    return AnalyticFindingEvidence(
      seriesRefs:
          seriesRefs == const $CopyWithPlaceholder() || seriesRefs == null
          ? _value.seriesRefs
          // ignore: cast_nullable_to_non_nullable
          : seriesRefs as List<String>,
      members: members == const $CopyWithPlaceholder() || members == null
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<String>,
      valuesWithheld: valuesWithheld == const $CopyWithPlaceholder()
          ? _value.valuesWithheld
          // ignore: cast_nullable_to_non_nullable
          : valuesWithheld as bool?,
    );
  }
}

extension $AnalyticFindingEvidenceCopyWith on AnalyticFindingEvidence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalyticFindingEvidence.copyWith(...)` or `instanceOfAnalyticFindingEvidence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalyticFindingEvidenceCWProxy get copyWith =>
      _$AnalyticFindingEvidenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalyticFindingEvidence _$AnalyticFindingEvidenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalyticFindingEvidence', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['seriesRefs', 'members']);
  final val = AnalyticFindingEvidence(
    seriesRefs: $checkedConvert(
      'seriesRefs',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    members: $checkedConvert(
      'members',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    valuesWithheld: $checkedConvert('valuesWithheld', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$AnalyticFindingEvidenceToJson(
  AnalyticFindingEvidence instance,
) => <String, dynamic>{
  'seriesRefs': instance.seriesRefs,
  'members': instance.members,
  'valuesWithheld': ?instance.valuesWithheld,
};
