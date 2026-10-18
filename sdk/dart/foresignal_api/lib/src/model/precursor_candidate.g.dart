// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_candidate.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorCandidateCWProxy {
  PrecursorCandidate name(String? name);

  PrecursorCandidate kind(PrecursorCandidateKindEnum kind);

  PrecursorCandidate antecedent(PrecursorSide antecedent);

  PrecursorCandidate consequent(PrecursorSide consequent);

  PrecursorCandidate spatial(PrecursorSpatial? spatial);

  PrecursorCandidate lagMin(String lagMin);

  PrecursorCandidate lagMax(String lagMax);

  PrecursorCandidate regionCodes(List<String>? regionCodes);

  PrecursorCandidate id(String id);

  PrecursorCandidate createdBy(String createdBy);

  PrecursorCandidate createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorCandidate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorCandidate(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorCandidate call({
    String? name,
    PrecursorCandidateKindEnum kind,
    PrecursorSide antecedent,
    PrecursorSide consequent,
    PrecursorSpatial? spatial,
    String lagMin,
    String lagMax,
    List<String>? regionCodes,
    String id,
    String createdBy,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorCandidate.copyWith(...)` or call `instanceOfPrecursorCandidate.copyWith.fieldName(value)` for a single field.
class _$PrecursorCandidateCWProxyImpl implements _$PrecursorCandidateCWProxy {
  const _$PrecursorCandidateCWProxyImpl(this._value);

  final PrecursorCandidate _value;

  @override
  PrecursorCandidate name(String? name) => call(name: name);

  @override
  PrecursorCandidate kind(PrecursorCandidateKindEnum kind) => call(kind: kind);

  @override
  PrecursorCandidate antecedent(PrecursorSide antecedent) =>
      call(antecedent: antecedent);

  @override
  PrecursorCandidate consequent(PrecursorSide consequent) =>
      call(consequent: consequent);

  @override
  PrecursorCandidate spatial(PrecursorSpatial? spatial) =>
      call(spatial: spatial);

  @override
  PrecursorCandidate lagMin(String lagMin) => call(lagMin: lagMin);

  @override
  PrecursorCandidate lagMax(String lagMax) => call(lagMax: lagMax);

  @override
  PrecursorCandidate regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  PrecursorCandidate id(String id) => call(id: id);

  @override
  PrecursorCandidate createdBy(String createdBy) => call(createdBy: createdBy);

  @override
  PrecursorCandidate createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorCandidate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorCandidate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorCandidate call({
    Object? name = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? antecedent = const $CopyWithPlaceholder(),
    Object? consequent = const $CopyWithPlaceholder(),
    Object? spatial = const $CopyWithPlaceholder(),
    Object? lagMin = const $CopyWithPlaceholder(),
    Object? lagMax = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return PrecursorCandidate(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as PrecursorCandidateKindEnum,
      antecedent:
          antecedent == const $CopyWithPlaceholder() || antecedent == null
          ? _value.antecedent
          // ignore: cast_nullable_to_non_nullable
          : antecedent as PrecursorSide,
      consequent:
          consequent == const $CopyWithPlaceholder() || consequent == null
          ? _value.consequent
          // ignore: cast_nullable_to_non_nullable
          : consequent as PrecursorSide,
      spatial: spatial == const $CopyWithPlaceholder()
          ? _value.spatial
          // ignore: cast_nullable_to_non_nullable
          : spatial as PrecursorSpatial?,
      lagMin: lagMin == const $CopyWithPlaceholder() || lagMin == null
          ? _value.lagMin
          // ignore: cast_nullable_to_non_nullable
          : lagMin as String,
      lagMax: lagMax == const $CopyWithPlaceholder() || lagMax == null
          ? _value.lagMax
          // ignore: cast_nullable_to_non_nullable
          : lagMax as String,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      createdBy: createdBy == const $CopyWithPlaceholder() || createdBy == null
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
    );
  }
}

extension $PrecursorCandidateCopyWith on PrecursorCandidate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorCandidate.copyWith(...)` or `instanceOfPrecursorCandidate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorCandidateCWProxy get copyWith =>
      _$PrecursorCandidateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorCandidate _$PrecursorCandidateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrecursorCandidate', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'kind',
          'antecedent',
          'consequent',
          'lagMin',
          'lagMax',
          'id',
          'createdBy',
          'createdAt',
        ],
      );
      final val = PrecursorCandidate(
        name: $checkedConvert('name', (v) => v as String?),
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$PrecursorCandidateKindEnumEnumMap, v),
        ),
        antecedent: $checkedConvert(
          'antecedent',
          (v) => PrecursorSide.fromJson(v as Map<String, dynamic>),
        ),
        consequent: $checkedConvert(
          'consequent',
          (v) => PrecursorSide.fromJson(v as Map<String, dynamic>),
        ),
        spatial: $checkedConvert(
          'spatial',
          (v) => v == null
              ? null
              : PrecursorSpatial.fromJson(v as Map<String, dynamic>),
        ),
        lagMin: $checkedConvert('lagMin', (v) => v as String),
        lagMax: $checkedConvert('lagMax', (v) => v as String),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        id: $checkedConvert('id', (v) => v as String),
        createdBy: $checkedConvert('createdBy', (v) => v as String),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PrecursorCandidateToJson(PrecursorCandidate instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'kind': _$PrecursorCandidateKindEnumEnumMap[instance.kind]!,
      'antecedent': instance.antecedent.toJson(),
      'consequent': instance.consequent.toJson(),
      'spatial': ?instance.spatial?.toJson(),
      'lagMin': instance.lagMin,
      'lagMax': instance.lagMax,
      'regionCodes': ?instance.regionCodes,
      'id': instance.id,
      'createdBy': instance.createdBy,
      'createdAt': instance.createdAt.toIso8601String(),
    };

const _$PrecursorCandidateKindEnumEnumMap = {
  PrecursorCandidateKindEnum.event: 'event',
  PrecursorCandidateKindEnum.series: 'series',
};
