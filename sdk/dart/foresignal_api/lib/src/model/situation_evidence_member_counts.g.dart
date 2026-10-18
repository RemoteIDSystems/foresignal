// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_evidence_member_counts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationEvidenceMemberCountsCWProxy {
  SituationEvidenceMemberCounts byType(Map<String, int>? byType);

  SituationEvidenceMemberCounts byFamily(Map<String, int>? byFamily);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationEvidenceMemberCounts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationEvidenceMemberCounts(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationEvidenceMemberCounts call({
    Map<String, int>? byType,
    Map<String, int>? byFamily,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationEvidenceMemberCounts.copyWith(...)` or call `instanceOfSituationEvidenceMemberCounts.copyWith.fieldName(value)` for a single field.
class _$SituationEvidenceMemberCountsCWProxyImpl
    implements _$SituationEvidenceMemberCountsCWProxy {
  const _$SituationEvidenceMemberCountsCWProxyImpl(this._value);

  final SituationEvidenceMemberCounts _value;

  @override
  SituationEvidenceMemberCounts byType(Map<String, int>? byType) =>
      call(byType: byType);

  @override
  SituationEvidenceMemberCounts byFamily(Map<String, int>? byFamily) =>
      call(byFamily: byFamily);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationEvidenceMemberCounts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationEvidenceMemberCounts(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationEvidenceMemberCounts call({
    Object? byType = const $CopyWithPlaceholder(),
    Object? byFamily = const $CopyWithPlaceholder(),
  }) {
    return SituationEvidenceMemberCounts(
      byType: byType == const $CopyWithPlaceholder()
          ? _value.byType
          // ignore: cast_nullable_to_non_nullable
          : byType as Map<String, int>?,
      byFamily: byFamily == const $CopyWithPlaceholder()
          ? _value.byFamily
          // ignore: cast_nullable_to_non_nullable
          : byFamily as Map<String, int>?,
    );
  }
}

extension $SituationEvidenceMemberCountsCopyWith
    on SituationEvidenceMemberCounts {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationEvidenceMemberCounts.copyWith(...)` or `instanceOfSituationEvidenceMemberCounts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationEvidenceMemberCountsCWProxy get copyWith =>
      _$SituationEvidenceMemberCountsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationEvidenceMemberCounts _$SituationEvidenceMemberCountsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationEvidenceMemberCounts', json, ($checkedConvert) {
  final val = SituationEvidenceMemberCounts(
    byType: $checkedConvert(
      'byType',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    byFamily: $checkedConvert(
      'byFamily',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationEvidenceMemberCountsToJson(
  SituationEvidenceMemberCounts instance,
) => <String, dynamic>{
  'byType': ?instance.byType,
  'byFamily': ?instance.byFamily,
};
