// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_board_band_changes_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelBoardBandChangesInnerCWProxy {
  IntelBoardBandChangesInner setId(String setId);

  IntelBoardBandChangesInner hypothesisId(String hypothesisId);

  IntelBoardBandChangesInner question(String? question);

  IntelBoardBandChangesInner label(String? label);

  IntelBoardBandChangesInner fromTerm(IcdTerm? fromTerm);

  IntelBoardBandChangesInner toTerm(IcdTerm toTerm);

  IntelBoardBandChangesInner probability(num? probability);

  IntelBoardBandChangesInner at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardBandChangesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardBandChangesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelBoardBandChangesInner call({
    String setId,
    String hypothesisId,
    String? question,
    String? label,
    IcdTerm? fromTerm,
    IcdTerm toTerm,
    num? probability,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelBoardBandChangesInner.copyWith(...)` or call `instanceOfIntelBoardBandChangesInner.copyWith.fieldName(value)` for a single field.
class _$IntelBoardBandChangesInnerCWProxyImpl
    implements _$IntelBoardBandChangesInnerCWProxy {
  const _$IntelBoardBandChangesInnerCWProxyImpl(this._value);

  final IntelBoardBandChangesInner _value;

  @override
  IntelBoardBandChangesInner setId(String setId) => call(setId: setId);

  @override
  IntelBoardBandChangesInner hypothesisId(String hypothesisId) =>
      call(hypothesisId: hypothesisId);

  @override
  IntelBoardBandChangesInner question(String? question) =>
      call(question: question);

  @override
  IntelBoardBandChangesInner label(String? label) => call(label: label);

  @override
  IntelBoardBandChangesInner fromTerm(IcdTerm? fromTerm) =>
      call(fromTerm: fromTerm);

  @override
  IntelBoardBandChangesInner toTerm(IcdTerm toTerm) => call(toTerm: toTerm);

  @override
  IntelBoardBandChangesInner probability(num? probability) =>
      call(probability: probability);

  @override
  IntelBoardBandChangesInner at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardBandChangesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardBandChangesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelBoardBandChangesInner call({
    Object? setId = const $CopyWithPlaceholder(),
    Object? hypothesisId = const $CopyWithPlaceholder(),
    Object? question = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? fromTerm = const $CopyWithPlaceholder(),
    Object? toTerm = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return IntelBoardBandChangesInner(
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      hypothesisId:
          hypothesisId == const $CopyWithPlaceholder() || hypothesisId == null
          ? _value.hypothesisId
          // ignore: cast_nullable_to_non_nullable
          : hypothesisId as String,
      question: question == const $CopyWithPlaceholder()
          ? _value.question
          // ignore: cast_nullable_to_non_nullable
          : question as String?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      fromTerm: fromTerm == const $CopyWithPlaceholder()
          ? _value.fromTerm
          // ignore: cast_nullable_to_non_nullable
          : fromTerm as IcdTerm?,
      toTerm: toTerm == const $CopyWithPlaceholder() || toTerm == null
          ? _value.toTerm
          // ignore: cast_nullable_to_non_nullable
          : toTerm as IcdTerm,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $IntelBoardBandChangesInnerCopyWith on IntelBoardBandChangesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelBoardBandChangesInner.copyWith(...)` or `instanceOfIntelBoardBandChangesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelBoardBandChangesInnerCWProxy get copyWith =>
      _$IntelBoardBandChangesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelBoardBandChangesInner _$IntelBoardBandChangesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelBoardBandChangesInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['setId', 'hypothesisId', 'toTerm', 'at'],
  );
  final val = IntelBoardBandChangesInner(
    setId: $checkedConvert('setId', (v) => v as String),
    hypothesisId: $checkedConvert('hypothesisId', (v) => v as String),
    question: $checkedConvert('question', (v) => v as String?),
    label: $checkedConvert('label', (v) => v as String?),
    fromTerm: $checkedConvert(
      'fromTerm',
      (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
    ),
    toTerm: $checkedConvert('toTerm', (v) => $enumDecode(_$IcdTermEnumMap, v)),
    probability: $checkedConvert('probability', (v) => v as num?),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$IntelBoardBandChangesInnerToJson(
  IntelBoardBandChangesInner instance,
) => <String, dynamic>{
  'setId': instance.setId,
  'hypothesisId': instance.hypothesisId,
  'question': ?instance.question,
  'label': ?instance.label,
  'fromTerm': ?_$IcdTermEnumMap[instance.fromTerm],
  'toTerm': _$IcdTermEnumMap[instance.toTerm]!,
  'probability': ?instance.probability,
  'at': instance.at.toIso8601String(),
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
