// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_input_ledger_question_resolver.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetInputLedgerQuestionResolverCWProxy {
  HypothesisSetInputLedgerQuestionResolver type(
    HypothesisSetInputLedgerQuestionResolverTypeEnum type,
  );

  HypothesisSetInputLedgerQuestionResolver params(Map<String, Object>? params);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetInputLedgerQuestionResolver(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetInputLedgerQuestionResolver(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetInputLedgerQuestionResolver call({
    HypothesisSetInputLedgerQuestionResolverTypeEnum type,
    Map<String, Object>? params,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetInputLedgerQuestionResolver.copyWith(...)` or call `instanceOfHypothesisSetInputLedgerQuestionResolver.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetInputLedgerQuestionResolverCWProxyImpl
    implements _$HypothesisSetInputLedgerQuestionResolverCWProxy {
  const _$HypothesisSetInputLedgerQuestionResolverCWProxyImpl(this._value);

  final HypothesisSetInputLedgerQuestionResolver _value;

  @override
  HypothesisSetInputLedgerQuestionResolver type(
    HypothesisSetInputLedgerQuestionResolverTypeEnum type,
  ) => call(type: type);

  @override
  HypothesisSetInputLedgerQuestionResolver params(
    Map<String, Object>? params,
  ) => call(params: params);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetInputLedgerQuestionResolver(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetInputLedgerQuestionResolver(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetInputLedgerQuestionResolver call({
    Object? type = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetInputLedgerQuestionResolver(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as HypothesisSetInputLedgerQuestionResolverTypeEnum,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
    );
  }
}

extension $HypothesisSetInputLedgerQuestionResolverCopyWith
    on HypothesisSetInputLedgerQuestionResolver {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetInputLedgerQuestionResolver.copyWith(...)` or `instanceOfHypothesisSetInputLedgerQuestionResolver.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetInputLedgerQuestionResolverCWProxy get copyWith =>
      _$HypothesisSetInputLedgerQuestionResolverCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetInputLedgerQuestionResolver
_$HypothesisSetInputLedgerQuestionResolverFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HypothesisSetInputLedgerQuestionResolver', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = HypothesisSetInputLedgerQuestionResolver(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(
            _$HypothesisSetInputLedgerQuestionResolverTypeEnumEnumMap,
            v,
          ),
        ),
        params: $checkedConvert(
          'params',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$HypothesisSetInputLedgerQuestionResolverToJson(
  HypothesisSetInputLedgerQuestionResolver instance,
) => <String, dynamic>{
  'type':
      _$HypothesisSetInputLedgerQuestionResolverTypeEnumEnumMap[instance.type]!,
  'params': ?instance.params,
};

const _$HypothesisSetInputLedgerQuestionResolverTypeEnumEnumMap = {
  HypothesisSetInputLedgerQuestionResolverTypeEnum.hypothesisOutcome:
      'hypothesis-outcome',
  HypothesisSetInputLedgerQuestionResolverTypeEnum.eventExists: 'event-exists',
  HypothesisSetInputLedgerQuestionResolverTypeEnum.countThreshold:
      'count-threshold',
  HypothesisSetInputLedgerQuestionResolverTypeEnum.seriesValue: 'series-value',
  HypothesisSetInputLedgerQuestionResolverTypeEnum.alertFired: 'alert-fired',
  HypothesisSetInputLedgerQuestionResolverTypeEnum.situationStatus:
      'situation-status',
  HypothesisSetInputLedgerQuestionResolverTypeEnum.manual: 'manual',
};
