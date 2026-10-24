// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorPatchCWProxy {
  IndicatorPatch description(String? description);

  IndicatorPatch signal(IndicatorSignal? signal);

  IndicatorPatch signalFamily(String? signalFamily);

  IndicatorPatch credibility(IndicatorPatchCredibilityEnum? credibility);

  IndicatorPatch relevance(IndicatorPatchRelevanceEnum? relevance);

  IndicatorPatch expectedBy(DateTime? expectedBy);

  IndicatorPatch lr(Map<String, IndicatorLikelihood>? lr);

  IndicatorPatch absenceLr(Map<String, num>? absenceLr);

  IndicatorPatch state(IndicatorPatchStateEnum? state);

  IndicatorPatch observedEvidence(Map<String, Object>? observedEvidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorPatch call({
    String? description,
    IndicatorSignal? signal,
    String? signalFamily,
    IndicatorPatchCredibilityEnum? credibility,
    IndicatorPatchRelevanceEnum? relevance,
    DateTime? expectedBy,
    Map<String, IndicatorLikelihood>? lr,
    Map<String, num>? absenceLr,
    IndicatorPatchStateEnum? state,
    Map<String, Object>? observedEvidence,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorPatch.copyWith(...)` or call `instanceOfIndicatorPatch.copyWith.fieldName(value)` for a single field.
class _$IndicatorPatchCWProxyImpl implements _$IndicatorPatchCWProxy {
  const _$IndicatorPatchCWProxyImpl(this._value);

  final IndicatorPatch _value;

  @override
  IndicatorPatch description(String? description) =>
      call(description: description);

  @override
  IndicatorPatch signal(IndicatorSignal? signal) => call(signal: signal);

  @override
  IndicatorPatch signalFamily(String? signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  IndicatorPatch credibility(IndicatorPatchCredibilityEnum? credibility) =>
      call(credibility: credibility);

  @override
  IndicatorPatch relevance(IndicatorPatchRelevanceEnum? relevance) =>
      call(relevance: relevance);

  @override
  IndicatorPatch expectedBy(DateTime? expectedBy) =>
      call(expectedBy: expectedBy);

  @override
  IndicatorPatch lr(Map<String, IndicatorLikelihood>? lr) => call(lr: lr);

  @override
  IndicatorPatch absenceLr(Map<String, num>? absenceLr) =>
      call(absenceLr: absenceLr);

  @override
  IndicatorPatch state(IndicatorPatchStateEnum? state) => call(state: state);

  @override
  IndicatorPatch observedEvidence(Map<String, Object>? observedEvidence) =>
      call(observedEvidence: observedEvidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorPatch call({
    Object? description = const $CopyWithPlaceholder(),
    Object? signal = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? credibility = const $CopyWithPlaceholder(),
    Object? relevance = const $CopyWithPlaceholder(),
    Object? expectedBy = const $CopyWithPlaceholder(),
    Object? lr = const $CopyWithPlaceholder(),
    Object? absenceLr = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? observedEvidence = const $CopyWithPlaceholder(),
  }) {
    return IndicatorPatch(
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      signal: signal == const $CopyWithPlaceholder()
          ? _value.signal
          // ignore: cast_nullable_to_non_nullable
          : signal as IndicatorSignal?,
      signalFamily: signalFamily == const $CopyWithPlaceholder()
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String?,
      credibility: credibility == const $CopyWithPlaceholder()
          ? _value.credibility
          // ignore: cast_nullable_to_non_nullable
          : credibility as IndicatorPatchCredibilityEnum?,
      relevance: relevance == const $CopyWithPlaceholder()
          ? _value.relevance
          // ignore: cast_nullable_to_non_nullable
          : relevance as IndicatorPatchRelevanceEnum?,
      expectedBy: expectedBy == const $CopyWithPlaceholder()
          ? _value.expectedBy
          // ignore: cast_nullable_to_non_nullable
          : expectedBy as DateTime?,
      lr: lr == const $CopyWithPlaceholder()
          ? _value.lr
          // ignore: cast_nullable_to_non_nullable
          : lr as Map<String, IndicatorLikelihood>?,
      absenceLr: absenceLr == const $CopyWithPlaceholder()
          ? _value.absenceLr
          // ignore: cast_nullable_to_non_nullable
          : absenceLr as Map<String, num>?,
      state: state == const $CopyWithPlaceholder()
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as IndicatorPatchStateEnum?,
      observedEvidence: observedEvidence == const $CopyWithPlaceholder()
          ? _value.observedEvidence
          // ignore: cast_nullable_to_non_nullable
          : observedEvidence as Map<String, Object>?,
    );
  }
}

extension $IndicatorPatchCopyWith on IndicatorPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorPatch.copyWith(...)` or `instanceOfIndicatorPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorPatchCWProxy get copyWith => _$IndicatorPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorPatch _$IndicatorPatchFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IndicatorPatch', json, ($checkedConvert) {
  final val = IndicatorPatch(
    description: $checkedConvert('description', (v) => v as String?),
    signal: $checkedConvert(
      'signal',
      (v) => v == null
          ? null
          : IndicatorSignal.fromJson(v as Map<String, dynamic>),
    ),
    signalFamily: $checkedConvert('signalFamily', (v) => v as String?),
    credibility: $checkedConvert(
      'credibility',
      (v) => $enumDecodeNullable(_$IndicatorPatchCredibilityEnumEnumMap, v),
    ),
    relevance: $checkedConvert(
      'relevance',
      (v) => $enumDecodeNullable(_$IndicatorPatchRelevanceEnumEnumMap, v),
    ),
    expectedBy: $checkedConvert(
      'expectedBy',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lr: $checkedConvert(
      'lr',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(
          k,
          IndicatorLikelihood.fromJson(e as Map<String, dynamic>),
        ),
      ),
    ),
    absenceLr: $checkedConvert(
      'absenceLr',
      (v) => (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as num)),
    ),
    state: $checkedConvert(
      'state',
      (v) => $enumDecodeNullable(_$IndicatorPatchStateEnumEnumMap, v),
    ),
    observedEvidence: $checkedConvert(
      'observedEvidence',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$IndicatorPatchToJson(
  IndicatorPatch instance,
) => <String, dynamic>{
  'description': ?instance.description,
  'signal': ?instance.signal?.toJson(),
  'signalFamily': ?instance.signalFamily,
  'credibility': ?_$IndicatorPatchCredibilityEnumEnumMap[instance.credibility],
  'relevance': ?_$IndicatorPatchRelevanceEnumEnumMap[instance.relevance],
  'expectedBy': ?instance.expectedBy?.toIso8601String(),
  'lr': ?instance.lr?.map((k, e) => MapEntry(k, e.toJson())),
  'absenceLr': ?instance.absenceLr,
  'state': ?_$IndicatorPatchStateEnumEnumMap[instance.state],
  'observedEvidence': ?instance.observedEvidence,
};

const _$IndicatorPatchCredibilityEnumEnumMap = {
  IndicatorPatchCredibilityEnum.H: 'H',
  IndicatorPatchCredibilityEnum.M: 'M',
  IndicatorPatchCredibilityEnum.L: 'L',
};

const _$IndicatorPatchRelevanceEnumEnumMap = {
  IndicatorPatchRelevanceEnum.H: 'H',
  IndicatorPatchRelevanceEnum.M: 'M',
  IndicatorPatchRelevanceEnum.L: 'L',
};

const _$IndicatorPatchStateEnumEnumMap = {
  IndicatorPatchStateEnum.observed: 'observed',
  IndicatorPatchStateEnum.absent: 'absent',
};
