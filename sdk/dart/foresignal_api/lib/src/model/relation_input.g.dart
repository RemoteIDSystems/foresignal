// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'relation_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RelationInputCWProxy {
  RelationInput fromRef(String fromRef);

  RelationInput toRef(String toRef);

  RelationInput type(RelationInputTypeEnum type);

  RelationInput validFrom(DateTime? validFrom);

  RelationInput validTo(DateTime? validTo);

  RelationInput confidence(num? confidence);

  RelationInput label(RelationInputLabelEnum? label);

  RelationInput private(bool? private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RelationInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RelationInput(...).copyWith(id: 12, name: "My name")
  /// ```
  RelationInput call({
    String fromRef,
    String toRef,
    RelationInputTypeEnum type,
    DateTime? validFrom,
    DateTime? validTo,
    num? confidence,
    RelationInputLabelEnum? label,
    bool? private,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRelationInput.copyWith(...)` or call `instanceOfRelationInput.copyWith.fieldName(value)` for a single field.
class _$RelationInputCWProxyImpl implements _$RelationInputCWProxy {
  const _$RelationInputCWProxyImpl(this._value);

  final RelationInput _value;

  @override
  RelationInput fromRef(String fromRef) => call(fromRef: fromRef);

  @override
  RelationInput toRef(String toRef) => call(toRef: toRef);

  @override
  RelationInput type(RelationInputTypeEnum type) => call(type: type);

  @override
  RelationInput validFrom(DateTime? validFrom) => call(validFrom: validFrom);

  @override
  RelationInput validTo(DateTime? validTo) => call(validTo: validTo);

  @override
  RelationInput confidence(num? confidence) => call(confidence: confidence);

  @override
  RelationInput label(RelationInputLabelEnum? label) => call(label: label);

  @override
  RelationInput private(bool? private) => call(private: private);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RelationInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RelationInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RelationInput call({
    Object? fromRef = const $CopyWithPlaceholder(),
    Object? toRef = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validTo = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
  }) {
    return RelationInput(
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
          : type as RelationInputTypeEnum,
      validFrom: validFrom == const $CopyWithPlaceholder()
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime?,
      validTo: validTo == const $CopyWithPlaceholder()
          ? _value.validTo
          // ignore: cast_nullable_to_non_nullable
          : validTo as DateTime?,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as RelationInputLabelEnum?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
    );
  }
}

extension $RelationInputCopyWith on RelationInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRelationInput.copyWith(...)` or `instanceOfRelationInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RelationInputCWProxy get copyWith => _$RelationInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RelationInput _$RelationInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RelationInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['fromRef', 'toRef', 'type']);
      final val = RelationInput(
        fromRef: $checkedConvert('fromRef', (v) => v as String),
        toRef: $checkedConvert('toRef', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$RelationInputTypeEnumEnumMap, v),
        ),
        validFrom: $checkedConvert(
          'validFrom',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        validTo: $checkedConvert(
          'validTo',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        confidence: $checkedConvert('confidence', (v) => v as num? ?? 1),
        label: $checkedConvert(
          'label',
          (v) => $enumDecodeNullable(_$RelationInputLabelEnumEnumMap, v),
        ),
        private: $checkedConvert('private', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$RelationInputToJson(RelationInput instance) =>
    <String, dynamic>{
      'fromRef': instance.fromRef,
      'toRef': instance.toRef,
      'type': _$RelationInputTypeEnumEnumMap[instance.type]!,
      'validFrom': ?instance.validFrom?.toIso8601String(),
      'validTo': ?instance.validTo?.toIso8601String(),
      'confidence': ?instance.confidence,
      'label': ?_$RelationInputLabelEnumEnumMap[instance.label],
      'private': ?instance.private,
    };

const _$RelationInputTypeEnumEnumMap = {
  RelationInputTypeEnum.memberOf: 'member-of',
  RelationInputTypeEnum.observedBy: 'observed-by',
  RelationInputTypeEnum.sameAs: 'same-as',
  RelationInputTypeEnum.locatedIn: 'located-in',
  RelationInputTypeEnum.near: 'near',
  RelationInputTypeEnum.dependsOn: 'depends-on',
  RelationInputTypeEnum.affects: 'affects',
  RelationInputTypeEnum.derivedFrom: 'derived-from',
  RelationInputTypeEnum.evidenceFor: 'evidence-for',
  RelationInputTypeEnum.evidenceAgainst: 'evidence-against',
  RelationInputTypeEnum.about: 'about',
  RelationInputTypeEnum.mentions: 'mentions',
  RelationInputTypeEnum.precedes: 'precedes',
  RelationInputTypeEnum.causes: 'causes',
  RelationInputTypeEnum.relatedTo: 'related-to',
  RelationInputTypeEnum.owns: 'owns',
  RelationInputTypeEnum.controls: 'controls',
  RelationInputTypeEnum.officerOf: 'officer-of',
  RelationInputTypeEnum.beneficialOwner: 'beneficial-owner',
  RelationInputTypeEnum.affiliatedWith: 'affiliated-with',
};

const _$RelationInputLabelEnumEnumMap = {
  RelationInputLabelEnum.confirmed: 'confirmed',
  RelationInputLabelEnum.likely: 'likely',
  RelationInputLabelEnum.possible: 'possible',
  RelationInputLabelEnum.speculative: 'speculative',
};
