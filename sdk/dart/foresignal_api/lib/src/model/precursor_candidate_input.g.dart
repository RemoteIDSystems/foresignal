// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_candidate_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorCandidateInputCWProxy {
  PrecursorCandidateInput name(String? name);

  PrecursorCandidateInput kind(PrecursorCandidateInputKindEnum kind);

  PrecursorCandidateInput antecedent(PrecursorSide antecedent);

  PrecursorCandidateInput consequent(PrecursorSide consequent);

  PrecursorCandidateInput spatial(PrecursorSpatial? spatial);

  PrecursorCandidateInput lagMin(String? lagMin);

  PrecursorCandidateInput lagMax(String lagMax);

  PrecursorCandidateInput regionCodes(List<String>? regionCodes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorCandidateInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorCandidateInput(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorCandidateInput call({
    String? name,
    PrecursorCandidateInputKindEnum kind,
    PrecursorSide antecedent,
    PrecursorSide consequent,
    PrecursorSpatial? spatial,
    String? lagMin,
    String lagMax,
    List<String>? regionCodes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorCandidateInput.copyWith(...)` or call `instanceOfPrecursorCandidateInput.copyWith.fieldName(value)` for a single field.
class _$PrecursorCandidateInputCWProxyImpl
    implements _$PrecursorCandidateInputCWProxy {
  const _$PrecursorCandidateInputCWProxyImpl(this._value);

  final PrecursorCandidateInput _value;

  @override
  PrecursorCandidateInput name(String? name) => call(name: name);

  @override
  PrecursorCandidateInput kind(PrecursorCandidateInputKindEnum kind) =>
      call(kind: kind);

  @override
  PrecursorCandidateInput antecedent(PrecursorSide antecedent) =>
      call(antecedent: antecedent);

  @override
  PrecursorCandidateInput consequent(PrecursorSide consequent) =>
      call(consequent: consequent);

  @override
  PrecursorCandidateInput spatial(PrecursorSpatial? spatial) =>
      call(spatial: spatial);

  @override
  PrecursorCandidateInput lagMin(String? lagMin) => call(lagMin: lagMin);

  @override
  PrecursorCandidateInput lagMax(String lagMax) => call(lagMax: lagMax);

  @override
  PrecursorCandidateInput regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorCandidateInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorCandidateInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorCandidateInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? antecedent = const $CopyWithPlaceholder(),
    Object? consequent = const $CopyWithPlaceholder(),
    Object? spatial = const $CopyWithPlaceholder(),
    Object? lagMin = const $CopyWithPlaceholder(),
    Object? lagMax = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
  }) {
    return PrecursorCandidateInput(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as PrecursorCandidateInputKindEnum,
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
      lagMin: lagMin == const $CopyWithPlaceholder()
          ? _value.lagMin
          // ignore: cast_nullable_to_non_nullable
          : lagMin as String?,
      lagMax: lagMax == const $CopyWithPlaceholder() || lagMax == null
          ? _value.lagMax
          // ignore: cast_nullable_to_non_nullable
          : lagMax as String,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
    );
  }
}

extension $PrecursorCandidateInputCopyWith on PrecursorCandidateInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorCandidateInput.copyWith(...)` or `instanceOfPrecursorCandidateInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorCandidateInputCWProxy get copyWith =>
      _$PrecursorCandidateInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorCandidateInput _$PrecursorCandidateInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorCandidateInput', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['kind', 'antecedent', 'consequent', 'lagMax'],
  );
  final val = PrecursorCandidateInput(
    name: $checkedConvert('name', (v) => v as String?),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$PrecursorCandidateInputKindEnumEnumMap, v),
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
    lagMin: $checkedConvert('lagMin', (v) => v as String?),
    lagMax: $checkedConvert('lagMax', (v) => v as String),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$PrecursorCandidateInputToJson(
  PrecursorCandidateInput instance,
) => <String, dynamic>{
  'name': ?instance.name,
  'kind': _$PrecursorCandidateInputKindEnumEnumMap[instance.kind]!,
  'antecedent': instance.antecedent.toJson(),
  'consequent': instance.consequent.toJson(),
  'spatial': ?instance.spatial?.toJson(),
  'lagMin': ?instance.lagMin,
  'lagMax': instance.lagMax,
  'regionCodes': ?instance.regionCodes,
};

const _$PrecursorCandidateInputKindEnumEnumMap = {
  PrecursorCandidateInputKindEnum.event: 'event',
  PrecursorCandidateInputKindEnum.series: 'series',
};
