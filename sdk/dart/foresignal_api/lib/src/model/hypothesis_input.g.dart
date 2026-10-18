// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisInputCWProxy {
  HypothesisInput label(String label);

  HypothesisInput statement(String statement);

  HypothesisInput type(HypothesisInputTypeEnum type);

  HypothesisInput prior(num prior);

  HypothesisInput priorBasis(HypothesisInputPriorBasis priorBasis);

  HypothesisInput ledgerOutcome(String? ledgerOutcome);

  HypothesisInput linkedFindingIds(List<String>? linkedFindingIds);

  HypothesisInput coincidenceBaseline(
    HypothesisCoincidenceBaseline? coincidenceBaseline,
  );

  HypothesisInput alternatives(List<String>? alternatives);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisInput(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisInput call({
    String label,
    String statement,
    HypothesisInputTypeEnum type,
    num prior,
    HypothesisInputPriorBasis priorBasis,
    String? ledgerOutcome,
    List<String>? linkedFindingIds,
    HypothesisCoincidenceBaseline? coincidenceBaseline,
    List<String>? alternatives,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisInput.copyWith(...)` or call `instanceOfHypothesisInput.copyWith.fieldName(value)` for a single field.
class _$HypothesisInputCWProxyImpl implements _$HypothesisInputCWProxy {
  const _$HypothesisInputCWProxyImpl(this._value);

  final HypothesisInput _value;

  @override
  HypothesisInput label(String label) => call(label: label);

  @override
  HypothesisInput statement(String statement) => call(statement: statement);

  @override
  HypothesisInput type(HypothesisInputTypeEnum type) => call(type: type);

  @override
  HypothesisInput prior(num prior) => call(prior: prior);

  @override
  HypothesisInput priorBasis(HypothesisInputPriorBasis priorBasis) =>
      call(priorBasis: priorBasis);

  @override
  HypothesisInput ledgerOutcome(String? ledgerOutcome) =>
      call(ledgerOutcome: ledgerOutcome);

  @override
  HypothesisInput linkedFindingIds(List<String>? linkedFindingIds) =>
      call(linkedFindingIds: linkedFindingIds);

  @override
  HypothesisInput coincidenceBaseline(
    HypothesisCoincidenceBaseline? coincidenceBaseline,
  ) => call(coincidenceBaseline: coincidenceBaseline);

  @override
  HypothesisInput alternatives(List<String>? alternatives) =>
      call(alternatives: alternatives);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisInput call({
    Object? label = const $CopyWithPlaceholder(),
    Object? statement = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? prior = const $CopyWithPlaceholder(),
    Object? priorBasis = const $CopyWithPlaceholder(),
    Object? ledgerOutcome = const $CopyWithPlaceholder(),
    Object? linkedFindingIds = const $CopyWithPlaceholder(),
    Object? coincidenceBaseline = const $CopyWithPlaceholder(),
    Object? alternatives = const $CopyWithPlaceholder(),
  }) {
    return HypothesisInput(
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
      statement: statement == const $CopyWithPlaceholder() || statement == null
          ? _value.statement
          // ignore: cast_nullable_to_non_nullable
          : statement as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as HypothesisInputTypeEnum,
      prior: prior == const $CopyWithPlaceholder() || prior == null
          ? _value.prior
          // ignore: cast_nullable_to_non_nullable
          : prior as num,
      priorBasis:
          priorBasis == const $CopyWithPlaceholder() || priorBasis == null
          ? _value.priorBasis
          // ignore: cast_nullable_to_non_nullable
          : priorBasis as HypothesisInputPriorBasis,
      ledgerOutcome: ledgerOutcome == const $CopyWithPlaceholder()
          ? _value.ledgerOutcome
          // ignore: cast_nullable_to_non_nullable
          : ledgerOutcome as String?,
      linkedFindingIds: linkedFindingIds == const $CopyWithPlaceholder()
          ? _value.linkedFindingIds
          // ignore: cast_nullable_to_non_nullable
          : linkedFindingIds as List<String>?,
      coincidenceBaseline: coincidenceBaseline == const $CopyWithPlaceholder()
          ? _value.coincidenceBaseline
          // ignore: cast_nullable_to_non_nullable
          : coincidenceBaseline as HypothesisCoincidenceBaseline?,
      alternatives: alternatives == const $CopyWithPlaceholder()
          ? _value.alternatives
          // ignore: cast_nullable_to_non_nullable
          : alternatives as List<String>?,
    );
  }
}

extension $HypothesisInputCopyWith on HypothesisInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisInput.copyWith(...)` or `instanceOfHypothesisInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisInputCWProxy get copyWith => _$HypothesisInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisInput _$HypothesisInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisInput', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['label', 'statement', 'type', 'prior', 'priorBasis'],
  );
  final val = HypothesisInput(
    label: $checkedConvert('label', (v) => v as String),
    statement: $checkedConvert('statement', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$HypothesisInputTypeEnumEnumMap, v),
    ),
    prior: $checkedConvert('prior', (v) => v as num),
    priorBasis: $checkedConvert(
      'priorBasis',
      (v) => HypothesisInputPriorBasis.fromJson(v as Map<String, dynamic>),
    ),
    ledgerOutcome: $checkedConvert('ledgerOutcome', (v) => v as String?),
    linkedFindingIds: $checkedConvert(
      'linkedFindingIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    coincidenceBaseline: $checkedConvert(
      'coincidenceBaseline',
      (v) => v == null
          ? null
          : HypothesisCoincidenceBaseline.fromJson(v as Map<String, dynamic>),
    ),
    alternatives: $checkedConvert(
      'alternatives',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$HypothesisInputToJson(HypothesisInput instance) =>
    <String, dynamic>{
      'label': instance.label,
      'statement': instance.statement,
      'type': _$HypothesisInputTypeEnumEnumMap[instance.type]!,
      'prior': instance.prior,
      'priorBasis': instance.priorBasis.toJson(),
      'ledgerOutcome': ?instance.ledgerOutcome,
      'linkedFindingIds': ?instance.linkedFindingIds,
      'coincidenceBaseline': ?instance.coincidenceBaseline?.toJson(),
      'alternatives': ?instance.alternatives,
    };

const _$HypothesisInputTypeEnumEnumMap = {
  HypothesisInputTypeEnum.outcome: 'outcome',
  HypothesisInputTypeEnum.explanation: 'explanation',
  HypothesisInputTypeEnum.speculativeLink: 'speculative-link',
  HypothesisInputTypeEnum.other: 'other',
};
