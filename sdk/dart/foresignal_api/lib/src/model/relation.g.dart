// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'relation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RelationCWProxy {
  Relation id(String id);

  Relation fromRef(String fromRef);

  Relation toRef(String toRef);

  Relation type(RelationTypeEnum type);

  Relation validFrom(DateTime validFrom);

  Relation validTo(DateTime? validTo);

  Relation confidence(num confidence);

  Relation label(RelationLabelEnum label);

  Relation provenance(RelationProvenance provenance);

  Relation projected(bool projected);

  Relation private(bool? private);

  Relation createdAt(DateTime createdAt);

  Relation retiredAt(DateTime? retiredAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Relation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Relation(...).copyWith(id: 12, name: "My name")
  /// ```
  Relation call({
    String id,
    String fromRef,
    String toRef,
    RelationTypeEnum type,
    DateTime validFrom,
    DateTime? validTo,
    num confidence,
    RelationLabelEnum label,
    RelationProvenance provenance,
    bool projected,
    bool? private,
    DateTime createdAt,
    DateTime? retiredAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRelation.copyWith(...)` or call `instanceOfRelation.copyWith.fieldName(value)` for a single field.
class _$RelationCWProxyImpl implements _$RelationCWProxy {
  const _$RelationCWProxyImpl(this._value);

  final Relation _value;

  @override
  Relation id(String id) => call(id: id);

  @override
  Relation fromRef(String fromRef) => call(fromRef: fromRef);

  @override
  Relation toRef(String toRef) => call(toRef: toRef);

  @override
  Relation type(RelationTypeEnum type) => call(type: type);

  @override
  Relation validFrom(DateTime validFrom) => call(validFrom: validFrom);

  @override
  Relation validTo(DateTime? validTo) => call(validTo: validTo);

  @override
  Relation confidence(num confidence) => call(confidence: confidence);

  @override
  Relation label(RelationLabelEnum label) => call(label: label);

  @override
  Relation provenance(RelationProvenance provenance) =>
      call(provenance: provenance);

  @override
  Relation projected(bool projected) => call(projected: projected);

  @override
  Relation private(bool? private) => call(private: private);

  @override
  Relation createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Relation retiredAt(DateTime? retiredAt) => call(retiredAt: retiredAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Relation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Relation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Relation call({
    Object? id = const $CopyWithPlaceholder(),
    Object? fromRef = const $CopyWithPlaceholder(),
    Object? toRef = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validTo = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? provenance = const $CopyWithPlaceholder(),
    Object? projected = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? retiredAt = const $CopyWithPlaceholder(),
  }) {
    return Relation(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      fromRef: fromRef == const $CopyWithPlaceholder() || fromRef == null
          ? _value.fromRef
          // ignore: cast_nullable_to_non_nullable
          : fromRef as String,
      toRef: toRef == const $CopyWithPlaceholder() || toRef == null
          ? _value.toRef
          // ignore: cast_nullable_to_non_nullable
          : toRef as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as RelationTypeEnum,
      validFrom: validFrom == const $CopyWithPlaceholder() || validFrom == null
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime,
      validTo: validTo == const $CopyWithPlaceholder()
          ? _value.validTo
          // ignore: cast_nullable_to_non_nullable
          : validTo as DateTime?,
      confidence:
          confidence == const $CopyWithPlaceholder() || confidence == null
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as RelationLabelEnum,
      provenance:
          provenance == const $CopyWithPlaceholder() || provenance == null
          ? _value.provenance
          // ignore: cast_nullable_to_non_nullable
          : provenance as RelationProvenance,
      projected: projected == const $CopyWithPlaceholder() || projected == null
          ? _value.projected
          // ignore: cast_nullable_to_non_nullable
          : projected as bool,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      retiredAt: retiredAt == const $CopyWithPlaceholder()
          ? _value.retiredAt
          // ignore: cast_nullable_to_non_nullable
          : retiredAt as DateTime?,
    );
  }
}

extension $RelationCopyWith on Relation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRelation.copyWith(...)` or `instanceOfRelation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RelationCWProxy get copyWith => _$RelationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Relation _$RelationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Relation', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'fromRef',
      'toRef',
      'type',
      'validFrom',
      'confidence',
      'label',
      'provenance',
      'projected',
      'createdAt',
    ],
  );
  final val = Relation(
    id: $checkedConvert('id', (v) => v as String),
    fromRef: $checkedConvert('fromRef', (v) => v as String),
    toRef: $checkedConvert('toRef', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$RelationTypeEnumEnumMap, v),
    ),
    validFrom: $checkedConvert('validFrom', (v) => DateTime.parse(v as String)),
    validTo: $checkedConvert(
      'validTo',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num),
    label: $checkedConvert(
      'label',
      (v) => $enumDecode(_$RelationLabelEnumEnumMap, v),
    ),
    provenance: $checkedConvert(
      'provenance',
      (v) => RelationProvenance.fromJson(v as Map<String, dynamic>),
    ),
    projected: $checkedConvert('projected', (v) => v as bool),
    private: $checkedConvert('private', (v) => v as bool?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    retiredAt: $checkedConvert(
      'retiredAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$RelationToJson(Relation instance) => <String, dynamic>{
  'id': instance.id,
  'fromRef': instance.fromRef,
  'toRef': instance.toRef,
  'type': _$RelationTypeEnumEnumMap[instance.type]!,
  'validFrom': instance.validFrom.toIso8601String(),
  'validTo': ?instance.validTo?.toIso8601String(),
  'confidence': instance.confidence,
  'label': _$RelationLabelEnumEnumMap[instance.label]!,
  'provenance': instance.provenance.toJson(),
  'projected': instance.projected,
  'private': ?instance.private,
  'createdAt': instance.createdAt.toIso8601String(),
  'retiredAt': ?instance.retiredAt?.toIso8601String(),
};

const _$RelationTypeEnumEnumMap = {
  RelationTypeEnum.memberOf: 'member-of',
  RelationTypeEnum.observedBy: 'observed-by',
  RelationTypeEnum.sameAs: 'same-as',
  RelationTypeEnum.locatedIn: 'located-in',
  RelationTypeEnum.near: 'near',
  RelationTypeEnum.dependsOn: 'depends-on',
  RelationTypeEnum.affects: 'affects',
  RelationTypeEnum.derivedFrom: 'derived-from',
  RelationTypeEnum.evidenceFor: 'evidence-for',
  RelationTypeEnum.evidenceAgainst: 'evidence-against',
  RelationTypeEnum.about: 'about',
  RelationTypeEnum.mentions: 'mentions',
  RelationTypeEnum.precedes: 'precedes',
  RelationTypeEnum.causes: 'causes',
  RelationTypeEnum.relatedTo: 'related-to',
};

const _$RelationLabelEnumEnumMap = {
  RelationLabelEnum.confirmed: 'confirmed',
  RelationLabelEnum.likely: 'likely',
  RelationLabelEnum.possible: 'possible',
  RelationLabelEnum.speculative: 'speculative',
};
