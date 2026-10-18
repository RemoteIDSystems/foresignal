// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_input_ledger_question.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetInputLedgerQuestionCWProxy {
  HypothesisSetInputLedgerQuestion resolver(
    HypothesisSetInputLedgerQuestionResolver resolver,
  );

  HypothesisSetInputLedgerQuestion resolutionCriteria(
    String? resolutionCriteria,
  );

  HypothesisSetInputLedgerQuestion category(String? category);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetInputLedgerQuestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetInputLedgerQuestion(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetInputLedgerQuestion call({
    HypothesisSetInputLedgerQuestionResolver resolver,
    String? resolutionCriteria,
    String? category,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetInputLedgerQuestion.copyWith(...)` or call `instanceOfHypothesisSetInputLedgerQuestion.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetInputLedgerQuestionCWProxyImpl
    implements _$HypothesisSetInputLedgerQuestionCWProxy {
  const _$HypothesisSetInputLedgerQuestionCWProxyImpl(this._value);

  final HypothesisSetInputLedgerQuestion _value;

  @override
  HypothesisSetInputLedgerQuestion resolver(
    HypothesisSetInputLedgerQuestionResolver resolver,
  ) => call(resolver: resolver);

  @override
  HypothesisSetInputLedgerQuestion resolutionCriteria(
    String? resolutionCriteria,
  ) => call(resolutionCriteria: resolutionCriteria);

  @override
  HypothesisSetInputLedgerQuestion category(String? category) =>
      call(category: category);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetInputLedgerQuestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetInputLedgerQuestion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetInputLedgerQuestion call({
    Object? resolver = const $CopyWithPlaceholder(),
    Object? resolutionCriteria = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetInputLedgerQuestion(
      resolver: resolver == const $CopyWithPlaceholder() || resolver == null
          ? _value.resolver
          // ignore: cast_nullable_to_non_nullable
          : resolver as HypothesisSetInputLedgerQuestionResolver,
      resolutionCriteria: resolutionCriteria == const $CopyWithPlaceholder()
          ? _value.resolutionCriteria
          // ignore: cast_nullable_to_non_nullable
          : resolutionCriteria as String?,
      category: category == const $CopyWithPlaceholder()
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String?,
    );
  }
}

extension $HypothesisSetInputLedgerQuestionCopyWith
    on HypothesisSetInputLedgerQuestion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetInputLedgerQuestion.copyWith(...)` or `instanceOfHypothesisSetInputLedgerQuestion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetInputLedgerQuestionCWProxy get copyWith =>
      _$HypothesisSetInputLedgerQuestionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetInputLedgerQuestion _$HypothesisSetInputLedgerQuestionFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('HypothesisSetInputLedgerQuestion', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['resolver']);
      final val = HypothesisSetInputLedgerQuestion(
        resolver: $checkedConvert(
          'resolver',
          (v) => HypothesisSetInputLedgerQuestionResolver.fromJson(
            v as Map<String, dynamic>,
          ),
        ),
        resolutionCriteria: $checkedConvert(
          'resolutionCriteria',
          (v) => v as String?,
        ),
        category: $checkedConvert('category', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$HypothesisSetInputLedgerQuestionToJson(
  HypothesisSetInputLedgerQuestion instance,
) => <String, dynamic>{
  'resolver': instance.resolver.toJson(),
  'resolutionCriteria': ?instance.resolutionCriteria,
  'category': ?instance.category,
};
