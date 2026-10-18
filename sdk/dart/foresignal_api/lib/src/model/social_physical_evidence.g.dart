// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_physical_evidence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPhysicalEvidenceCWProxy {
  SocialPhysicalEvidence ref(String ref);

  SocialPhysicalEvidence family(String family);

  SocialPhysicalEvidence originator(String originator);

  SocialPhysicalEvidence firstObservedAt(DateTime firstObservedAt);

  SocialPhysicalEvidence distanceKm(num? distanceKm);

  SocialPhysicalEvidence negates(bool? negates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPhysicalEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPhysicalEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPhysicalEvidence call({
    String ref,
    String family,
    String originator,
    DateTime firstObservedAt,
    num? distanceKm,
    bool? negates,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPhysicalEvidence.copyWith(...)` or call `instanceOfSocialPhysicalEvidence.copyWith.fieldName(value)` for a single field.
class _$SocialPhysicalEvidenceCWProxyImpl
    implements _$SocialPhysicalEvidenceCWProxy {
  const _$SocialPhysicalEvidenceCWProxyImpl(this._value);

  final SocialPhysicalEvidence _value;

  @override
  SocialPhysicalEvidence ref(String ref) => call(ref: ref);

  @override
  SocialPhysicalEvidence family(String family) => call(family: family);

  @override
  SocialPhysicalEvidence originator(String originator) =>
      call(originator: originator);

  @override
  SocialPhysicalEvidence firstObservedAt(DateTime firstObservedAt) =>
      call(firstObservedAt: firstObservedAt);

  @override
  SocialPhysicalEvidence distanceKm(num? distanceKm) =>
      call(distanceKm: distanceKm);

  @override
  SocialPhysicalEvidence negates(bool? negates) => call(negates: negates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPhysicalEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPhysicalEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPhysicalEvidence call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? originator = const $CopyWithPlaceholder(),
    Object? firstObservedAt = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? negates = const $CopyWithPlaceholder(),
  }) {
    return SocialPhysicalEvidence(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      originator:
          originator == const $CopyWithPlaceholder() || originator == null
          ? _value.originator
          // ignore: cast_nullable_to_non_nullable
          : originator as String,
      firstObservedAt:
          firstObservedAt == const $CopyWithPlaceholder() ||
              firstObservedAt == null
          ? _value.firstObservedAt
          // ignore: cast_nullable_to_non_nullable
          : firstObservedAt as DateTime,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
      negates: negates == const $CopyWithPlaceholder()
          ? _value.negates
          // ignore: cast_nullable_to_non_nullable
          : negates as bool?,
    );
  }
}

extension $SocialPhysicalEvidenceCopyWith on SocialPhysicalEvidence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPhysicalEvidence.copyWith(...)` or `instanceOfSocialPhysicalEvidence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPhysicalEvidenceCWProxy get copyWith =>
      _$SocialPhysicalEvidenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPhysicalEvidence _$SocialPhysicalEvidenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPhysicalEvidence', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['ref', 'family', 'originator', 'firstObservedAt'],
  );
  final val = SocialPhysicalEvidence(
    ref: $checkedConvert('ref', (v) => v as String),
    family: $checkedConvert('family', (v) => v as String),
    originator: $checkedConvert('originator', (v) => v as String),
    firstObservedAt: $checkedConvert(
      'firstObservedAt',
      (v) => DateTime.parse(v as String),
    ),
    distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
    negates: $checkedConvert('negates', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$SocialPhysicalEvidenceToJson(
  SocialPhysicalEvidence instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'family': instance.family,
  'originator': instance.originator,
  'firstObservedAt': instance.firstObservedAt.toIso8601String(),
  'distanceKm': ?instance.distanceKm,
  'negates': ?instance.negates,
};
