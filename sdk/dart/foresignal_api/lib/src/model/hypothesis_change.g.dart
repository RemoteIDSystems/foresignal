// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_change.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisChangeCWProxy {
  HypothesisChange previousProbability(num previousProbability);

  HypothesisChange previousTerm(IcdTerm? previousTerm);

  HypothesisChange delta(num delta);

  HypothesisChange sinceRevisionId(String? sinceRevisionId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisChange(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisChange(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisChange call({
    num previousProbability,
    IcdTerm? previousTerm,
    num delta,
    String? sinceRevisionId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisChange.copyWith(...)` or call `instanceOfHypothesisChange.copyWith.fieldName(value)` for a single field.
class _$HypothesisChangeCWProxyImpl implements _$HypothesisChangeCWProxy {
  const _$HypothesisChangeCWProxyImpl(this._value);

  final HypothesisChange _value;

  @override
  HypothesisChange previousProbability(num previousProbability) =>
      call(previousProbability: previousProbability);

  @override
  HypothesisChange previousTerm(IcdTerm? previousTerm) =>
      call(previousTerm: previousTerm);

  @override
  HypothesisChange delta(num delta) => call(delta: delta);

  @override
  HypothesisChange sinceRevisionId(String? sinceRevisionId) =>
      call(sinceRevisionId: sinceRevisionId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisChange(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisChange(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisChange call({
    Object? previousProbability = const $CopyWithPlaceholder(),
    Object? previousTerm = const $CopyWithPlaceholder(),
    Object? delta = const $CopyWithPlaceholder(),
    Object? sinceRevisionId = const $CopyWithPlaceholder(),
  }) {
    return HypothesisChange(
      previousProbability:
          previousProbability == const $CopyWithPlaceholder() ||
              previousProbability == null
          ? _value.previousProbability
          // ignore: cast_nullable_to_non_nullable
          : previousProbability as num,
      previousTerm: previousTerm == const $CopyWithPlaceholder()
          ? _value.previousTerm
          // ignore: cast_nullable_to_non_nullable
          : previousTerm as IcdTerm?,
      delta: delta == const $CopyWithPlaceholder() || delta == null
          ? _value.delta
          // ignore: cast_nullable_to_non_nullable
          : delta as num,
      sinceRevisionId: sinceRevisionId == const $CopyWithPlaceholder()
          ? _value.sinceRevisionId
          // ignore: cast_nullable_to_non_nullable
          : sinceRevisionId as String?,
    );
  }
}

extension $HypothesisChangeCopyWith on HypothesisChange {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisChange.copyWith(...)` or `instanceOfHypothesisChange.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisChangeCWProxy get copyWith => _$HypothesisChangeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisChange _$HypothesisChangeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HypothesisChange', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['previousProbability', 'delta']);
      final val = HypothesisChange(
        previousProbability: $checkedConvert(
          'previousProbability',
          (v) => v as num,
        ),
        previousTerm: $checkedConvert(
          'previousTerm',
          (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
        ),
        delta: $checkedConvert('delta', (v) => v as num),
        sinceRevisionId: $checkedConvert(
          'sinceRevisionId',
          (v) => v as String?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$HypothesisChangeToJson(HypothesisChange instance) =>
    <String, dynamic>{
      'previousProbability': instance.previousProbability,
      'previousTerm': ?_$IcdTermEnumMap[instance.previousTerm],
      'delta': instance.delta,
      'sinceRevisionId': ?instance.sinceRevisionId,
    };

const _$IcdTermEnumMap = {
  IcdTerm.almostNoChance: 'almost no chance',
  IcdTerm.veryUnlikely: 'very unlikely',
  IcdTerm.unlikely: 'unlikely',
  IcdTerm.roughlyEvenChance: 'roughly even chance',
  IcdTerm.likely: 'likely',
  IcdTerm.veryLikely: 'very likely',
  IcdTerm.almostCertain: 'almost certain',
};
