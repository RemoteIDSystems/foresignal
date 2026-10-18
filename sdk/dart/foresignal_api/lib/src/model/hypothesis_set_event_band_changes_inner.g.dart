// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_event_band_changes_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetEventBandChangesInnerCWProxy {
  HypothesisSetEventBandChangesInner hypothesisId(String hypothesisId);

  HypothesisSetEventBandChangesInner label(String? label);

  HypothesisSetEventBandChangesInner fromTerm(IcdTerm fromTerm);

  HypothesisSetEventBandChangesInner toTerm(IcdTerm toTerm);

  HypothesisSetEventBandChangesInner probability(num probability);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetEventBandChangesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetEventBandChangesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetEventBandChangesInner call({
    String hypothesisId,
    String? label,
    IcdTerm fromTerm,
    IcdTerm toTerm,
    num probability,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetEventBandChangesInner.copyWith(...)` or call `instanceOfHypothesisSetEventBandChangesInner.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetEventBandChangesInnerCWProxyImpl
    implements _$HypothesisSetEventBandChangesInnerCWProxy {
  const _$HypothesisSetEventBandChangesInnerCWProxyImpl(this._value);

  final HypothesisSetEventBandChangesInner _value;

  @override
  HypothesisSetEventBandChangesInner hypothesisId(String hypothesisId) =>
      call(hypothesisId: hypothesisId);

  @override
  HypothesisSetEventBandChangesInner label(String? label) => call(label: label);

  @override
  HypothesisSetEventBandChangesInner fromTerm(IcdTerm fromTerm) =>
      call(fromTerm: fromTerm);

  @override
  HypothesisSetEventBandChangesInner toTerm(IcdTerm toTerm) =>
      call(toTerm: toTerm);

  @override
  HypothesisSetEventBandChangesInner probability(num probability) =>
      call(probability: probability);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetEventBandChangesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetEventBandChangesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetEventBandChangesInner call({
    Object? hypothesisId = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? fromTerm = const $CopyWithPlaceholder(),
    Object? toTerm = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetEventBandChangesInner(
      hypothesisId:
          hypothesisId == const $CopyWithPlaceholder() || hypothesisId == null
          ? _value.hypothesisId
          // ignore: cast_nullable_to_non_nullable
          : hypothesisId as String,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      fromTerm: fromTerm == const $CopyWithPlaceholder() || fromTerm == null
          ? _value.fromTerm
          // ignore: cast_nullable_to_non_nullable
          : fromTerm as IcdTerm,
      toTerm: toTerm == const $CopyWithPlaceholder() || toTerm == null
          ? _value.toTerm
          // ignore: cast_nullable_to_non_nullable
          : toTerm as IcdTerm,
      probability:
          probability == const $CopyWithPlaceholder() || probability == null
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num,
    );
  }
}

extension $HypothesisSetEventBandChangesInnerCopyWith
    on HypothesisSetEventBandChangesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetEventBandChangesInner.copyWith(...)` or `instanceOfHypothesisSetEventBandChangesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetEventBandChangesInnerCWProxy get copyWith =>
      _$HypothesisSetEventBandChangesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetEventBandChangesInner _$HypothesisSetEventBandChangesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisSetEventBandChangesInner', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const ['hypothesisId', 'fromTerm', 'toTerm', 'probability'],
  );
  final val = HypothesisSetEventBandChangesInner(
    hypothesisId: $checkedConvert('hypothesisId', (v) => v as String),
    label: $checkedConvert('label', (v) => v as String?),
    fromTerm: $checkedConvert(
      'fromTerm',
      (v) => $enumDecode(_$IcdTermEnumMap, v),
    ),
    toTerm: $checkedConvert('toTerm', (v) => $enumDecode(_$IcdTermEnumMap, v)),
    probability: $checkedConvert('probability', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$HypothesisSetEventBandChangesInnerToJson(
  HypothesisSetEventBandChangesInner instance,
) => <String, dynamic>{
  'hypothesisId': instance.hypothesisId,
  'label': ?instance.label,
  'fromTerm': _$IcdTermEnumMap[instance.fromTerm]!,
  'toTerm': _$IcdTermEnumMap[instance.toTerm]!,
  'probability': instance.probability,
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
